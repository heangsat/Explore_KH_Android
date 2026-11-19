package com.example.android.cambodia25.model

data class Province(
    val id: String,
    val nameEn: String,
    val nameKh: String,
    val region: String,
    val description: String,
    val imageUrl: String
)

data class Attraction(
    val id: String,
    val provinceId: String,
    val titleEn: String,
    val titleKh: String,
    val category: String,
    val descriptionEn: String,
    val descriptionKh: String,
    val latitude: Double,
    val longitude: Double,
    val priceKhr: String,
    val priceUsd: String,
    val openingHours: String,
    val rating: Double = 4.5,
    val imageUrl: String
)
