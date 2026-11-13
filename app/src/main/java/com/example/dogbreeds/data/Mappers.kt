package com.example.dogbreeds.data

import com.example.dogbreeds.data.local.BreedEntity
import com.example.dogbreeds.domain.Breed

fun BreedEntity.toDomain() = Breed(
    id = id,
    name = name,
    temperament = temperament,
    origin = origin,
    lifeSpan = lifeSpan,
    description = description,
    imageUrl = imageUrl,
    isFavorite = isFavorite
)
