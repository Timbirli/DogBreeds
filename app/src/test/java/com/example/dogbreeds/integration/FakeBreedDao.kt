package com.example.dogbreeds.integration

import com.example.dogbreeds.data.local.BreedDao
import com.example.dogbreeds.data.local.BreedEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory реализация DAO: ведёт себя как Room (реактивные Flow), но работает на чистой JVM. */
class FakeBreedDao(initial: List<BreedEntity> = emptyList()) : BreedDao {

    private val state = MutableStateFlow(initial.associateBy { it.id })

    fun snapshot(): List<BreedEntity> = state.value.values.sortedBy { it.name }

    override fun observeAll(): Flow<List<BreedEntity>> =
        state.map { m -> m.values.sortedBy { it.name } }

    override fun observeFavorites(): Flow<List<BreedEntity>> =
        state.map { m -> m.values.filter { it.isFavorite }.sortedBy { it.name } }

    override fun observeById(id: Int): Flow<BreedEntity?> = state.map { it[id] }

    override suspend fun getFavoriteIds(): List<Int> =
        state.value.values.filter { it.isFavorite }.map { it.id }

    override suspend fun count(): Int = state.value.size

    override suspend fun upsertAll(breeds: List<BreedEntity>) {
        state.update { it + breeds.associateBy { b -> b.id } }
    }

    override suspend fun toggleFavorite(id: Int) {
        state.update { m ->
            val current = m[id] ?: return@update m
            m + (id to current.copy(isFavorite = !current.isFavorite))
        }
    }
}

fun breedEntity(
    id: Int,
    name: String,
    isFavorite: Boolean = false,
    imageUrl: String = ""
) = BreedEntity(
    id = id,
    name = name,
    temperament = "Friendly, Loyal",
    origin = "England",
    lifeSpan = "12 - 15 years",
    description = null,
    imageUrl = imageUrl,
    isFavorite = isFavorite
)
