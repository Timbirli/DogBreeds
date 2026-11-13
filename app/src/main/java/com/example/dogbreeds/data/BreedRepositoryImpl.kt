package com.example.dogbreeds.data

import com.example.dogbreeds.data.local.BreedEntity
import com.example.dogbreeds.data.local.LocalDataSource
import com.example.dogbreeds.data.remote.BreedDto
import com.example.dogbreeds.data.remote.RemoteDataSource
import com.example.dogbreeds.domain.Breed
import com.example.dogbreeds.domain.BreedRepository
import com.example.dogbreeds.domain.ResolveImageUrlUseCase
import com.example.dogbreeds.domain.ServerException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

/**
 * Реализация [BreedRepository] из domain-слоя.
 * Источник правды для UI — Room; сеть используется только для синхронизации кэша.
 */
class BreedRepositoryImpl(
    private val remote: RemoteDataSource,
    private val local: LocalDataSource,
    private val resolveImageUrl: ResolveImageUrlUseCase = ResolveImageUrlUseCase()
) : BreedRepository {

    override val breeds: Flow<List<Breed>> = local.breeds.map { list -> list.map { it.toDomain() } }
    override val favorites: Flow<List<Breed>> = local.favorites.map { list -> list.map { it.toDomain() } }

    override fun observeBreed(id: Int): Flow<Breed?> = local.observeBreed(id).map { it?.toDomain() }

    override suspend fun cachedCount(): Int = local.count()

    override suspend fun toggleFavorite(id: Int) = local.toggleFavorite(id)

    /** Загружает породы из сети и сохраняет в Room, сохраняя флаги «избранное». */
    override suspend fun refresh(): Result<Unit> = try {
        val dtos = remote.getBreeds()
        val favoriteIds = local.getFavoriteIds().toSet()
        local.upsertAll(dtos.map { it.toEntity(isFavorite = it.id in favoriteIds) })
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        Result.failure(ServerException(e.code()))
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Фото породы для галереи; при ошибке сети — только главное фото из кэша. */
    override suspend fun getBreedImages(id: Int, fallbackUrl: String): List<String> {
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
        imageUrl = resolveImageUrl(image?.url, referenceImageId),
        isFavorite = isFavorite
    )
}
