package repository

import (
	"context"
	"database/sql"
	"errors"
	"fmt"

	"github.com/explorekh/backend/internal/models"
	"github.com/jmoiron/sqlx"
)

type ProvinceRepository struct {
	db *sqlx.DB
}

func NewProvinceRepository(db *sqlx.DB) *ProvinceRepository {
	return &ProvinceRepository{db: db}
}

func (r *ProvinceRepository) List(ctx context.Context) ([]models.Province, error) {
	provinces := make([]models.Province, 0, 25)
	query := `
		SELECT id, name_en, name_kh, region, description, image_url
		FROM provinces
		ORDER BY name_en`

	if err := r.db.SelectContext(ctx, &provinces, query); err != nil {
		return nil, fmt.Errorf("list provinces: %w", err)
	}
	return provinces, nil
}

func (r *ProvinceRepository) GetByID(ctx context.Context, id string) (models.Province, error) {
	var province models.Province
	query := `
		SELECT id, name_en, name_kh, region, description, image_url
		FROM provinces
		WHERE id = ?`

	if err := r.db.GetContext(ctx, &province, query, id); err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return models.Province{}, ErrNotFound
		}
		return models.Province{}, fmt.Errorf("get province %q: %w", id, err)
	}
	return province, nil
}
