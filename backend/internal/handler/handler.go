package handler

import (
	"context"
	"net/http"
	"time"

	"github.com/explorekh/backend/internal/repository"
	"github.com/gin-gonic/gin"
	"github.com/jmoiron/sqlx"
)

type Handler struct {
	DB          *sqlx.DB
	Provinces   *repository.ProvinceRepository
	Attractions *repository.AttractionRepository
	Location    *time.Location
}

func New(db *sqlx.DB, provinces *repository.ProvinceRepository, attractions *repository.AttractionRepository, location *time.Location) *Handler {
	return &Handler{
		DB:          db,
		Provinces:   provinces,
		Attractions: attractions,
		Location:    location,
	}
}

func (h *Handler) Health(c *gin.Context) {
	ctx, cancel := context.WithTimeout(c.Request.Context(), 2*time.Second)
	defer cancel()

	if err := h.DB.PingContext(ctx); err != nil {
		c.JSON(http.StatusServiceUnavailable, gin.H{
			"status":   "degraded",
			"database": "unreachable",
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"status":   "ok",
		"database": "up",
		"time":     time.Now().In(h.Location).Format(time.RFC3339),
	})
}

func respondData(c *gin.Context, status int, data any) {
	c.JSON(status, gin.H{"data": data})
}

func respondError(c *gin.Context, status int, code, message string) {
	c.JSON(status, gin.H{
		"error": gin.H{
			"code":    code,
			"message": message,
		},
	})
}
