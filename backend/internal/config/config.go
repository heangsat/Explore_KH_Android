package config

import (
	"fmt"
	"net/url"
	"os"
	"strconv"
	"strings"
	"time"

	"github.com/joho/godotenv"
)

type Config struct {
	AppEnv             string
	HTTPAddr           string
	Timezone           string
	Location           *time.Location
	MigrationsPath     string
	RunMigrations      bool
	CORSAllowedOrigins string
	SeedFile           string
	Database           DatabaseConfig
}

type DatabaseConfig struct {
	Host            string
	Port            int
	User            string
	Password        string
	Name            string
	Timezone        string
	MaxOpenConns    int
	MaxIdleConns    int
	ConnMaxLifetime time.Duration
}

func Load() (Config, error) {
	_ = godotenv.Load()

	timezone := env("APP_TIMEZONE", "Asia/Phnom_Penh")
	location, err := time.LoadLocation(timezone)
	if err != nil {
		return Config{}, fmt.Errorf("load timezone %q: %w", timezone, err)
	}

	cfg := Config{
		AppEnv:             env("APP_ENV", "development"),
		HTTPAddr:           env("HTTP_ADDR", ":8080"),
		Timezone:           timezone,
		Location:           location,
		MigrationsPath:     env("MIGRATIONS_PATH", "file://migrations"),
		RunMigrations:      envBool("RUN_MIGRATIONS", true),
		CORSAllowedOrigins: env("CORS_ALLOWED_ORIGINS", "*"),
		SeedFile:           env("SEED_FILE", "seeds/destinations.json"),
		Database: DatabaseConfig{
			Host:            env("DB_HOST", "127.0.0.1"),
			Port:            envInt("DB_PORT", 3306),
			User:            env("DB_USER", "explore"),
			Password:        env("DB_PASSWORD", "explore_dev"),
			Name:            env("DB_NAME", "explore_kh"),
			Timezone:        timezone,
			MaxOpenConns:    envInt("DB_MAX_OPEN_CONNS", 20),
			MaxIdleConns:    envInt("DB_MAX_IDLE_CONNS", 10),
			ConnMaxLifetime: envDuration("DB_CONN_MAX_LIFETIME", 5*time.Minute),
		},
	}

	return cfg, nil
}

func (c DatabaseConfig) DSN() string {
	return fmt.Sprintf(
		"%s:%s@tcp(%s:%d)/%s?parseTime=true&charset=utf8mb4&collation=utf8mb4_unicode_ci&loc=%s",
		c.User,
		c.Password,
		c.Host,
		c.Port,
		c.Name,
		url.QueryEscape(c.Timezone),
	)
}

func (c DatabaseConfig) MigrationsDSN() string {
	return "mysql://" + c.DSN() + "&multiStatements=true"
}

func env(key, fallback string) string {
	value := strings.TrimSpace(os.Getenv(key))
	if value == "" {
		return fallback
	}
	return value
}

func envInt(key string, fallback int) int {
	value := strings.TrimSpace(os.Getenv(key))
	if value == "" {
		return fallback
	}
	parsed, err := strconv.Atoi(value)
	if err != nil {
		return fallback
	}
	return parsed
}

func envBool(key string, fallback bool) bool {
	value := strings.TrimSpace(os.Getenv(key))
	if value == "" {
		return fallback
	}
	parsed, err := strconv.ParseBool(value)
	if err != nil {
		return fallback
	}
	return parsed
}

func envDuration(key string, fallback time.Duration) time.Duration {
	value := strings.TrimSpace(os.Getenv(key))
	if value == "" {
		return fallback
	}
	parsed, err := time.ParseDuration(value)
	if err != nil {
		return fallback
	}
	return parsed
}
