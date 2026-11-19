package handler

import (
	"errors"
	"net/http"

	"github.com/explorekh/backend/internal/repository"
	"github.com/gin-gonic/gin"
)

func (h *Handler) ListProvinces(c *gin.Context) {
	provinces, err := h.Provinces.List(c.Request.Context())
	if err != nil {
		respondError(c, http.StatusInternalServerError, "internal_error", "Unable to load provinces.")
		return
	}
	respondData(c, http.StatusOK, provinces)
}

func (h *Handler) GetProvince(c *gin.Context) {
	province, err := h.Provinces.GetByID(c.Request.Context(), c.Param("id"))
	if err != nil {
		if errors.Is(err, repository.ErrNotFound) {
			respondError(c, http.StatusNotFound, "not_found", "Province not found.")
			return
		}
		respondError(c, http.StatusInternalServerError, "internal_error", "Unable to load the province.")
		return
	}
	respondData(c, http.StatusOK, province)
}

func (h *Handler) ListProvinceAttractions(c *gin.Context) {
	provinceID := c.Param("id")
	if _, err := h.Provinces.GetByID(c.Request.Context(), provinceID); err != nil {
		if errors.Is(err, repository.ErrNotFound) {
			respondError(c, http.StatusNotFound, "not_found", "Province not found.")
			return
		}
		respondError(c, http.StatusInternalServerError, "internal_error", "Unable to load the province.")
		return
	}

	filter, err := attractionFilterFromQuery(c)
	if err != nil {
		respondError(c, http.StatusBadRequest, "invalid_query", err.Error())
		return
	}
	filter.ProvinceID = provinceID

	h.respondAttractionList(c, filter)
}
