package com.example.android.cambodia25.data

import android.content.Context
import com.example.android.cambodia25.model.Attraction
import com.example.android.cambodia25.model.Province
import org.json.JSONObject

data class Catalog(
    val provinces: List<Province>,
    val attractions: List<Attraction>
) {
    fun attractionsFor(provinceId: String): List<Attraction> =
        attractions.filter { it.provinceId == provinceId }

    fun findProvince(provinceId: String): Province? =
        provinces.firstOrNull { it.id == provinceId }

    fun provinceName(provinceId: String): String =
        findProvince(provinceId)?.nameEn ?: "Cambodia"
}

object ExploreCatalog {

    @Volatile
    private var cachedCatalog: Catalog? = null

    fun load(context: Context): Catalog {
        cachedCatalog?.let { return it }

        val json = context.assets.open("destinations.json")
            .bufferedReader()
            .use { reader -> reader.readText() }
        val root = JSONObject(json)

        val provincesJson = root.getJSONArray("provinces")
        val provinces = buildList {
            for (index in 0 until provincesJson.length()) {
                val item = provincesJson.getJSONObject(index)
                add(
                    Province(
                        id = item.getString("id"),
                        nameEn = item.getString("nameEn"),
                        nameKh = item.getString("nameKh"),
                        region = item.getString("region"),
                        description = item.getString("description"),
                        imageUrl = item.getString("imageUrl")
                    )
                )
            }
        }

        val attractionsJson = root.getJSONArray("attractions")
        val attractions = buildList {
            for (index in 0 until attractionsJson.length()) {
                val item = attractionsJson.getJSONObject(index)
                add(
                    Attraction(
                        id = item.getString("id"),
                        provinceId = item.getString("provinceId"),
                        titleEn = item.getString("titleEn"),
                        titleKh = item.getString("titleKh"),
                        category = item.getString("category"),
                        descriptionEn = item.getString("descriptionEn"),
                        descriptionKh = item.getString("descriptionKh"),
                        latitude = item.getDouble("latitude"),
                        longitude = item.getDouble("longitude"),
                        priceKhr = item.getString("priceKhr"),
                        priceUsd = item.getString("priceUsd"),
                        openingHours = item.getString("openingHours"),
                        rating = item.getDouble("rating"),
                        imageUrl = item.getString("imageUrl")
                    )
                )
            }
        }

        return Catalog(provinces, attractions).also { cachedCatalog = it }
    }
}
