package handler

import (
	"errors"
	"fmt"
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/explorekh/backend/internal/models"
	"github.com/explorekh/backend/internal/repository"
	"github.com/gin-gonic/gin"
)

var allowedCategories = map[string]bool{
	"Heritage":    true,
	"Eco-Tourism": true,
	"Culture":     true,
	"Leisure":     true,
}

var allowedSorts = map[string]bool{
	"rating_desc": true,
	"rating_asc":  true,
	"price_asc":   true,
	"price_desc":  true,
	"name_asc":    true,
	"name_desc":   true,
}

func (h *Handler) ListAttractions(c *gin.Context) {
	filter, err := attractionFilterFromQuery(c)
	if err != nil {
		respondError(c, http.StatusBadRequest, "invalid_query", err.Error())
		return
	}
	h.respondAttractionList(c, filter)
}

func (h *Handler) GetAttraction(c *gin.Context) {
	attraction, err := h.Attractions.GetByID(c.Request.Context(), c.Param("id"))
	if err != nil {
		if errors.Is(err, repository.ErrNotFound) {
			respondError(c, http.StatusNotFound, "not_found", "Attraction not found.")
			return
		}
		respondError(c, http.StatusInternalServerError, "internal_error", "Unable to load the attraction.")
		return
	}
	respondData(c, http.StatusOK, attraction)
}

func (h *Handler) ListCategories(c *gin.Context) {
	categories, err := h.Attractions.Categories(c.Request.Context())
	if err != nil {
		respondError(c, http.StatusInternalServerError, "internal_error", "Unable to load categories.")
		return
	}
	respondData(c, http.StatusOK, categories)
}

func (h *Handler) respondAttractionList(c *gin.Context, filter repository.AttractionFilter) {
	currentTime := time.Now().In(h.Location).Format("15:04:05")
	attractions, total, err := h.Attractions.List(c.Request.Context(), filter, currentTime)
	if err != nil {
		respondError(c, http.StatusInternalServerError, "internal_error", "Unable to load attractions.")
		return
	}

	page, pageSize := filter.Page, filter.PageSize
	if page < 1 {
		page = 1
	}
	if pageSize < 1 {
		pageSize = 20
	}
	if pageSize > 100 {
		pageSize = 100
	}

	totalPages := 0
	if total > 0 {
		totalPages = (total + pageSize - 1) / pageSize
	}

	c.JSON(http.StatusOK, gin.H{
		"data": attractions,
		"meta": models.PageMeta{
			Page:       page,
			PageSize:   pageSize,
			Total:      total,
			TotalPages: totalPages,
		},
	})
}

func attractionFilterFromQuery(c *gin.Context) (repository.AttractionFilter, error) {
	filter := repository.AttractionFilter{
		Query:      strings.TrimSpace(c.Query("q")),
		ProvinceID: strings.TrimSpace(c.Query("provinceId")),
		Category:   strings.TrimSpace(c.Query("category")),
		Sort:       strings.TrimSpace(c.Query("sort")),
	}

	page, err := parsePositiveInt(c.Query("page"), 1)
	if err != nil {
		return filter, fmt.Errorf("page must be a positive integer")
	}
	pageSize, err := parsePositiveInt(c.Query("pageSize"), 20)
	if err != nil {
		return filter, fmt.Errorf("pageSize must be a positive integer")
	}
	if pageSize > 100 {
		return filter, fmt.Errorf("pageSize cannot exceed 100")
	}
	filter.Page = page
	filter.PageSize = pageSize

	minRating, err := parseOptionalFloat(c.Query("minRating"))
	if err != nil {
		return filter, fmt.Errorf("minRating must be a number")
	}
	if minRating < 0 || minRating > 5 {
		return filter, fmt.Errorf("minRating must be between 0 and 5")
	}
	filter.MinRating = minRating

	maxPrice, err := parseOptionalFloat(c.Query("maxPriceUsd"))
	if err != nil {
		return filter, fmt.Errorf("maxPriceUsd must be a number")
	}
	if maxPrice < 0 {
		return filter, fmt.Errorf("maxPriceUsd cannot be negative")
	}
	filter.MaxPriceUSD = maxPrice

	openNow, err := parseOptionalBool(c.Query("openNow"))
	if err != nil {
		return filter, fmt.Errorf("openNow must be true or false")
	}
	filter.OpenNow = openNow

	if filter.Category != "" && !allowedCategories[filter.Category] {
		return filter, fmt.Errorf("unknown category %q", filter.Category)
	}
	if filter.Sort != "" && !allowedSorts[filter.Sort] {
		return filter, fmt.Errorf("unknown sort %q", filter.Sort)
	}

	return filter, nil
}

func parsePositiveInt(value string, fallback int) (int, error) {
	value = strings.TrimSpace(value)
	if value == "" {
		return fallback, nil
	}
	parsed, err := strconv.Atoi(value)
	if err != nil || parsed < 1 {
		return 0, fmt.Errorf("invalid positive integer")
	}
	return parsed, nil
}

func parseOptionalFloat(value string) (float64, error) {
	value = strings.TrimSpace(value)
	if value == "" {
		return 0, nil
	}
	parsed, err := strconv.ParseFloat(value, 64)
	if err != nil {
		return 0, err
	}
	return parsed, nil
}

func parseOptionalBool(value string) (bool, error) {
	value = strings.TrimSpace(value)
	if value == "" {
		return false, nil
	}
	return strconv.ParseBool(value)
}
