package router

import (
	"net/http"

	"github.com/explorekh/backend/internal/config"
	"github.com/explorekh/backend/internal/handler"
	"github.com/explorekh/backend/internal/middleware"
	"github.com/gin-gonic/gin"
)

func New(h *handler.Handler, cfg config.Config) *gin.Engine {
	r := gin.New()
	r.Use(gin.Logger(), gin.Recovery(), middleware.CORS(cfg.CORSAllowedOrigins))

	r.GET("/health", h.Health)

	v1 := r.Group("/api/v1")
	{
		v1.GET("/health", h.Health)

		v1.GET("/provinces", h.ListProvinces)
		v1.GET("/provinces/:id", h.GetProvince)
		v1.GET("/provinces/:id/attractions", h.ListProvinceAttractions)

		v1.GET("/attractions", h.ListAttractions)
		v1.GET("/attractions/:id", h.GetAttraction)
		v1.GET("/categories", h.ListCategories)
	}

	r.NoRoute(func(c *gin.Context) {
		c.JSON(http.StatusNotFound, gin.H{
			"error": gin.H{
				"code":    "not_found",
				"message": "Route not found.",
			},
		})
	})

	return r
}
