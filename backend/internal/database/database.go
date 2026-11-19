package database

import (
	"context"
	"errors"
	"fmt"
	"time"

	"github.com/explorekh/backend/internal/config"
	_ "github.com/go-sql-driver/mysql"
	"github.com/golang-migrate/migrate/v4"
	_ "github.com/golang-migrate/migrate/v4/database/mysql"
	_ "github.com/golang-migrate/migrate/v4/source/file"
	"github.com/jmoiron/sqlx"
)

func Open(ctx context.Context, cfg config.Config) (*sqlx.DB, error) {
	db, err := sqlx.Open("mysql", cfg.Database.DSN())
	if err != nil {
		return nil, fmt.Errorf("open database: %w", err)
	}

	db.SetMaxOpenConns(cfg.Database.MaxOpenConns)
	db.SetMaxIdleConns(cfg.Database.MaxIdleConns)
	db.SetConnMaxLifetime(cfg.Database.ConnMaxLifetime)

	var pingErr error
	for attempt := 1; attempt <= 15; attempt++ {
		pingErr = db.PingContext(ctx)
		if pingErr == nil {
			return db, nil
		}
		select {
		case <-ctx.Done():
			_ = db.Close()
			return nil, ctx.Err()
		case <-time.After(2 * time.Second):
		}
	}

	_ = db.Close()
	return nil, fmt.Errorf("database not reachable after retries: %w", pingErr)
}

func Migrate(cfg config.Config) error {
	m, err := migrate.New(cfg.MigrationsPath, cfg.Database.MigrationsDSN())
	if err != nil {
		return fmt.Errorf("create migrator: %w", err)
	}
	defer func() {
		_, _ = m.Close()
	}()

	if err := m.Up(); err != nil && !errors.Is(err, migrate.ErrNoChange) {
		return fmt.Errorf("run migrations: %w", err)
	}
	return nil
}
