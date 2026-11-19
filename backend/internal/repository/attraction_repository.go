package repository

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
	"strings"

	"github.com/explorekh/backend/internal/models"
	"github.com/jmoiron/sqlx"
)

type AttractionFilter struct {
	Query       string
	ProvinceID  string
	Category    string
	MinRating   float64
	MaxPriceUSD float64
	OpenNow     bool
	Page        int
	PageSize    int
	Sort        string
}

type AttractionRepository struct {
	db *sqlx.DB
}

func NewAttractionRepository(db *sqlx.DB) *AttractionRepository {
	return &AttractionRepository{db: db}
}

const attractionColumns = `
	a.id, a.province_id, a.title_en, a.title_kh, a.category,
	a.description_en, a.description_kh, a.latitude, a.longitude,
	a.price_khr, a.price_usd, a.opening_hours, a.is_24_hours,
	a.rating, a.image_url`

func (r *AttractionRepository) List(
	ctx context.Context,
	filter AttractionFilter,
	currentTime string,
) ([]models.Attraction, int, error) {
	page, pageSize := normalizePagination(filter.Page, filter.PageSize)
	where, args := buildAttractionWhere(filter, currentTime)

	baseQuery := `
		FROM attractions a
		JOIN provinces p ON p.id = a.province_id
		WHERE ` + where

	var total int
	countQuery := `SELECT COUNT(*)` + baseQuery
	if err := r.db.GetContext(ctx, &total, countQuery, args...); err != nil {
		return nil, 0, fmt.Errorf("count attractions: %w", err)
	}

	if total == 0 {
		return []models.Attraction{}, 0, nil
	}

	offset := (page - 1) * pageSize
	listQuery := `SELECT ` + attractionColumns + baseQuery +
		` ORDER BY ` + attractionOrder(filter.Sort) +
		` LIMIT ? OFFSET ?`

	listArgs := make([]any, 0, len(args)+2)
	listArgs = append(listArgs, args...)
	listArgs = append(listArgs, pageSize, offset)

	attractions := make([]models.Attraction, 0, pageSize)
	if err := r.db.SelectContext(ctx, &attractions, listQuery, listArgs...); err != nil {
		return nil, 0, fmt.Errorf("list attractions: %w", err)
	}

	return attractions, total, nil
}

func (r *AttractionRepository) GetByID(ctx context.Context, id string) (models.Attraction, error) {
	var attraction models.Attraction
	query := `SELECT ` + attractionColumns + `
		FROM attractions a
		WHERE a.id = ?`

	if err := r.db.GetContext(ctx, &attraction, query, id); err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return models.Attraction{}, ErrNotFound
		}
		return models.Attraction{}, fmt.Errorf("get attraction %q: %w", id, err)
	}
	return attraction, nil
}

func (r *AttractionRepository) Categories(ctx context.Context) ([]string, error) {
	categories := make([]string, 0, 4)
	query := `SELECT DISTINCT category FROM attractions ORDER BY category`

	if err := r.db.SelectContext(ctx, &categories, query); err != nil {
		return nil, fmt.Errorf("list categories: %w", err)
	}
	return categories, nil
}

func normalizePagination(page, pageSize int) (int, int) {
	if page < 1 {
		page = 1
	}
	if pageSize < 1 {
		pageSize = 20
	}
	if pageSize > 100 {
		pageSize = 100
	}
	return page, pageSize
}

func buildAttractionWhere(filter AttractionFilter, currentTime string) (string, []any) {
	conditions := []string{"1 = 1"}
	args := make([]any, 0, 12)

	if filter.Query != "" {
		like := "%" + filter.Query + "%"
		conditions = append(conditions, `(
			a.title_en LIKE ? OR a.title_kh LIKE ? OR
			a.description_en LIKE ? OR a.description_kh LIKE ? OR
			p.name_en LIKE ? OR p.name_kh LIKE ?
		)`)
		args = append(args, like, like, like, like, like, like)
	}

	if filter.ProvinceID != "" {
		conditions = append(conditions, "a.province_id = ?")
		args = append(args, filter.ProvinceID)
	}

	if filter.Category != "" {
		conditions = append(conditions, "a.category = ?")
		args = append(args, filter.Category)
	}

	if filter.MinRating > 0 {
		conditions = append(conditions, "a.rating >= ?")
		args = append(args, filter.MinRating)
	}

	if filter.MaxPriceUSD > 0 {
		conditions = append(conditions, "a.price_usd_value <= ?")
		args = append(args, filter.MaxPriceUSD)
	}

	if filter.OpenNow {
		conditions = append(conditions, `(
			a.is_24_hours = 1 OR EXISTS (
				SELECT 1
				FROM attraction_hours h
				WHERE h.attraction_id = a.id
				  AND (
					(h.closes_at > h.opens_at AND ? >= h.opens_at AND ? < h.closes_at)
					OR
					(h.closes_at <= h.opens_at AND (? >= h.opens_at OR ? < h.closes_at))
				  )
			)
		)`)
		args = append(args, currentTime, currentTime, currentTime, currentTime)
	}

	return strings.Join(conditions, " AND "), args
}

func attractionOrder(sort string) string {
	switch sort {
	case "rating_asc":
		return "a.rating ASC, a.title_en ASC"
	case "price_asc":
		return "a.price_usd_value ASC, a.title_en ASC"
	case "price_desc":
		return "a.price_usd_value DESC, a.title_en ASC"
	case "name_asc":
		return "a.title_en ASC"
	case "name_desc":
		return "a.title_en DESC"
	default:
		return "a.rating DESC, a.title_en ASC"
	}
}
