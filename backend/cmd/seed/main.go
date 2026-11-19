package main

import (
	"context"
	"encoding/json"
	"flag"
	"fmt"
	"log"
	"os"
	"time"

	"github.com/explorekh/backend/internal/config"
	"github.com/explorekh/backend/internal/database"
	"github.com/explorekh/backend/internal/seed"
)

const upsertProvince = `
	INSERT INTO provinces (id, name_en, name_kh, region, description, image_url)
	VALUES (?, ?, ?, ?, ?, ?)
	ON DUPLICATE KEY UPDATE
		name_en = VALUES(name_en),
		name_kh = VALUES(name_kh),
		region = VALUES(region),
		description = VALUES(description),
		image_url = VALUES(image_url)`

const upsertAttraction = `
	INSERT INTO attractions (
		id, province_id, title_en, title_kh, category,
		description_en, description_kh, latitude, longitude,
		price_khr, price_usd, price_khr_value, price_usd_value,
		opening_hours, is_24_hours, rating, image_url
	)
	VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
	ON DUPLICATE KEY UPDATE
		province_id = VALUES(province_id),
		title_en = VALUES(title_en),
		title_kh = VALUES(title_kh),
		category = VALUES(category),
		description_en = VALUES(description_en),
		description_kh = VALUES(description_kh),
		latitude = VALUES(latitude),
		longitude = VALUES(longitude),
		price_khr = VALUES(price_khr),
		price_usd = VALUES(price_usd),
		price_khr_value = VALUES(price_khr_value),
		price_usd_value = VALUES(price_usd_value),
		opening_hours = VALUES(opening_hours),
		is_24_hours = VALUES(is_24_hours),
		rating = VALUES(rating),
		image_url = VALUES(image_url)`

func main() {
	defaultFile := os.Getenv("SEED_FILE")
	if defaultFile == "" {
		defaultFile = "seeds/destinations.json"
	}
	filePath := flag.String("file", defaultFile, "path to destinations.json")
	flag.Parse()

	cfg, err := config.Load()
	if err != nil {
		log.Fatalf("load config: %v", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()

	db, err := database.Open(ctx, cfg)
	if err != nil {
		log.Fatalf("connect database: %v", err)
	}
	defer db.Close()

	raw, err := os.ReadFile(*filePath)
	if err != nil {
		log.Fatalf("read seed file: %v", err)
	}

	var data seed.File
	if err := json.Unmarshal(raw, &data); err != nil {
		log.Fatalf("parse seed file: %v", err)
	}
	if len(data.Provinces) == 0 || len(data.Attractions) == 0 {
		log.Fatal("seed file must contain provinces and attractions")
	}

	provinceIDs := make(map[string]bool, len(data.Provinces))
	for _, province := range data.Provinces {
		provinceIDs[province.ID] = true
	}
	for _, attraction := range data.Attractions {
		if !provinceIDs[attraction.ProvinceID] {
			log.Fatalf("attraction %q references unknown province %q", attraction.ID, attraction.ProvinceID)
		}
	}

	tx, err := db.BeginTxx(ctx, nil)
	if err != nil {
		log.Fatalf("begin transaction: %v", err)
	}
	defer func() {
		_ = tx.Rollback()
	}()

	for _, province := range data.Provinces {
		if _, err := tx.ExecContext(
			ctx,
			upsertProvince,
			province.ID,
			province.NameEn,
			province.NameKh,
			province.Region,
			province.Description,
			province.ImageURL,
		); err != nil {
			log.Fatalf("upsert province %q: %v", province.ID, err)
		}
	}

	hourCount := 0
	for _, attraction := range data.Attractions {
		ranges, is24Hours := seed.ParseHours(attraction.OpeningHours)
		if _, err := tx.ExecContext(
			ctx,
			upsertAttraction,
			attraction.ID,
			attraction.ProvinceID,
			attraction.TitleEn,
			attraction.TitleKh,
			attraction.Category,
			attraction.DescriptionEn,
			attraction.DescriptionKh,
			attraction.Latitude,
			attraction.Longitude,
			attraction.PriceKhr,
			attraction.PriceUsd,
			seed.ParseKHR(attraction.PriceKhr),
			seed.ParseUSD(attraction.PriceUsd),
			attraction.OpeningHours,
			is24Hours,
			attraction.Rating,
			attraction.ImageURL,
		); err != nil {
			log.Fatalf("upsert attraction %q: %v", attraction.ID, err)
		}

		if _, err := tx.ExecContext(ctx, `DELETE FROM attraction_hours WHERE attraction_id = ?`, attraction.ID); err != nil {
			log.Fatalf("clear hours for %q: %v", attraction.ID, err)
		}
		for _, hourRange := range ranges {
			if _, err := tx.ExecContext(
				ctx,
				`INSERT INTO attraction_hours (attraction_id, opens_at, closes_at) VALUES (?, ?, ?)`,
				attraction.ID,
				hourRange.OpensAt,
				hourRange.ClosesAt,
			); err != nil {
				log.Fatalf("insert hours for %q: %v", attraction.ID, err)
			}
			hourCount++
		}
	}

	if _, err := tx.ExecContext(ctx, `
		INSERT INTO catalog_versions (id, version) VALUES (1, 2)
		ON DUPLICATE KEY UPDATE version = version + 1`); err != nil {
		log.Fatalf("bump catalog version: %v", err)
	}

	if err := tx.Commit(); err != nil {
		log.Fatalf("commit seed: %v", err)
	}

	fmt.Printf("Seeded %d provinces, %d attractions, %d opening-hour ranges\n",
		len(data.Provinces), len(data.Attractions), hourCount)
}
