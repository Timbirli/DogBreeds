package com.example.dogbreeds.domain

/** Фабрика тестовых данных для domain-тестов. */
fun testBreed(
    id: Int = 1,
    name: String = "Beagle",
    temperament: String = "Amiable, Gentle",
    origin: String? = "England",
    lifeSpan: String = "12 - 15 years",
    description: String? = null,
    imageUrl: String = "",
    isFavorite: Boolean = false
) = Breed(id, name, temperament, origin, lifeSpan, description, imageUrl, isFavorite)
