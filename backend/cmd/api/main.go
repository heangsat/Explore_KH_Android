package main

import (
	"context"
	"errors"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/explorekh/backend/internal/config"
	"github.com/explorekh/backend/internal/database"
	"github.com/explorekh/backend/internal/handler"
	"github.com/explorekh/backend/internal/repository"
	"github.com/explorekh/backend/internal/router"
)

func main() {
	cfg, err := config.Load()
	if err != nil {
		log.Fatalf("load config: %v", err)
	}

	startupCtx, cancelStartup := context.WithTimeout(context.Background(), 45*time.Second)
	defer cancelStartup()

	db, err := database.Open(startupCtx, cfg)
	if err != nil {
		log.Fatalf("connect database: %v", err)
	}
	defer db.Close()

	if cfg.RunMigrations {
		if err := database.Migrate(cfg); err != nil {
			log.Fatalf("run migrations: %v", err)
		}
	}

	provinceRepo := repository.NewProvinceRepository(db)
	attractionRepo := repository.NewAttractionRepository(db)
	h := handler.New(db, provinceRepo, attractionRepo, cfg.Location)
	engine := router.New(h, cfg)

	server := &http.Server{
		Addr:              cfg.HTTPAddr,
		Handler:           engine,
		ReadHeaderTimeout: 5 * time.Second,
		ReadTimeout:       15 * time.Second,
		WriteTimeout:      30 * time.Second,
		IdleTimeout:       60 * time.Second,
	}

	go func() {
		log.Printf("Explore KH API listening on %s (%s)", cfg.HTTPAddr, cfg.AppEnv)
		if err := server.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			log.Fatalf("serve: %v", err)
		}
	}()

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	log.Println("Shutting down API...")

	shutdownCtx, cancelShutdown := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancelShutdown()

	if err := server.Shutdown(shutdownCtx); err != nil {
		log.Printf("forced shutdown: %v", err)
	}
}
