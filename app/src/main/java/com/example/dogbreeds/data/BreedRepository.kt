package com.example.dogbreeds.data

import com.example.dogbreeds.data.local.BreedEntity
import com.example.dogbreeds.data.local.LocalDataSource
import com.example.dogbreeds.data.remote.BreedDto
import com.example.dogbreeds.data.remote.RemoteDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

/**
 * Единая точка доступа к данным.
 * Источник правды для UI — Room; сеть используется только для синхронизации кэша.
 */
class BreedRepository(
    private val remote: RemoteDataSource,
    private val local: LocalDataSource
) {
    val breeds: Flow<List<BreedEntity>> = local.breeds
    val favorites: Flow<List<BreedEntity>> = local.favorites

    fun observeBreed(id: Int): Flow<BreedEntity?> = local.observeBreed(id)

    suspend fun cachedCount(): Int = local.count()

    suspend fun toggleFavorite(id: Int) = local.toggleFavorite(id)

    /** Загружает породы из сети и сохраняет в Room, сохраняя флаги «избранное». */
    suspend fun refresh(): Result<Unit> = try {
        val dtos = remote.getBreeds()
        val favoriteIds = local.getFavoriteIds().toSet()
        local.upsertAll(dtos.map { it.toEntity(isFavorite = it.id in favoriteIds) })
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Фото породы для галереи; при ошибке сети — только главное фото из кэша. */
    suspend fun getBreedImages(id: Int, fallbackUrl: String): List<String> {
        val fallback = if (fallbackUrl.isBlank()) emptyList() else listOf(fallbackUrl)
        return try {
            remote.getBreedImages(id).mapNotNull { it.url }.distinct().ifEmpty { fallback }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            fallback
        }
    }

    private fun BreedDto.toEntity(isFavorite: Boolean) = BreedEntity(
        id = id,
        name = name,
        temperament = temperament.orEmpty(),
        origin = origin?.takeIf { it.isNotBlank() },
        lifeSpan = lifeSpan.orEmpty(),
        description = description?.takeIf { it.isNotBlank() },
        imageUrl = image?.url
            ?: referenceImageId?.let { "https://cdn2.thedogapi.com/images/$it.jpg" }
            ?: "",
        isFavorite = isFavorite
    )
}
