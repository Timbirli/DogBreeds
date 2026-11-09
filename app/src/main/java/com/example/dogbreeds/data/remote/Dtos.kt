package com.example.dogbreeds.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BreedDto(
    val id: Int,
    val name: String,
    val temperament: String? = null,
    val origin: String? = null,
    @Json(name = "life_span") val lifeSpan: String? = null,
    val description: String? = null,
    @Json(name = "reference_image_id") val referenceImageId: String? = null,
    val image: ImageDto? = null
)

@JsonClass(generateAdapter = true)
data class ImageDto(
    val url: String? = null
)
