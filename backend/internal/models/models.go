package models

type Province struct {
	ID          string `json:"id" db:"id"`
	NameEn      string `json:"nameEn" db:"name_en"`
	NameKh      string `json:"nameKh" db:"name_kh"`
	Region      string `json:"region" db:"region"`
	Description string `json:"description" db:"description"`
	ImageURL    string `json:"imageUrl" db:"image_url"`
}

type Attraction struct {
	ID            string  `json:"id" db:"id"`
	ProvinceID    string  `json:"provinceId" db:"province_id"`
	TitleEn       string  `json:"titleEn" db:"title_en"`
	TitleKh       string  `json:"titleKh" db:"title_kh"`
	Category      string  `json:"category" db:"category"`
	DescriptionEn string  `json:"descriptionEn" db:"description_en"`
	DescriptionKh string  `json:"descriptionKh" db:"description_kh"`
	Latitude      float64 `json:"latitude" db:"latitude"`
	Longitude     float64 `json:"longitude" db:"longitude"`
	PriceKhr      string  `json:"priceKhr" db:"price_khr"`
	PriceUsd      string  `json:"priceUsd" db:"price_usd"`
	OpeningHours  string  `json:"openingHours" db:"opening_hours"`
	Is24Hours     bool    `json:"is24Hours" db:"is_24_hours"`
	Rating        float64 `json:"rating" db:"rating"`
	ImageURL      string  `json:"imageUrl" db:"image_url"`
}

type PageMeta struct {
	Page       int `json:"page"`
	PageSize   int `json:"pageSize"`
	Total      int `json:"total"`
	TotalPages int `json:"totalPages"`
}
