package com.example.dogbreeds.domain

import kotlinx.coroutines.flow.Flow

/** Контракт репозитория. Реализация живёт в data-слое, domain о ней ничего не знает. */
interface BreedRepository {
    val breeds: Flow<List<Breed>>
    val favorites: Flow<List<Breed>>

    fun observeBreed(id: Int): Flow<Breed?>

    suspend fun cachedCount(): Int
    suspend fun toggleFavorite(id: Int)

    /** Синхронизирует кэш с сервером. */
    suspend fun refresh(): Result<Unit>

    /** Фотографии породы; при ошибке сети возвращает только [fallbackUrl] (если он не пуст). */
    suspend fun getBreedImages(id: Int, fallbackUrl: String): List<String>
}

/** Сервер ответил HTTP-ошибкой. Data-слой превращает retrofit2.HttpException в это исключение. */
class ServerException(val code: Int) : Exception("HTTP $code")
