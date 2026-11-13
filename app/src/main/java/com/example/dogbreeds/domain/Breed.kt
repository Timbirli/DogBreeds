package com.example.dogbreeds.domain

/** Доменная модель породы. Не зависит ни от Room, ни от Retrofit, ни от Android. */
data class Breed(
    val id: Int,
    val name: String,
    val temperament: String,
    val origin: String?,
    val lifeSpan: String,
    val description: String?,
    val imageUrl: String,
    val isFavorite: Boolean
)
