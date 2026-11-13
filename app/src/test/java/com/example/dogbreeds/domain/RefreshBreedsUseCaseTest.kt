package com.example.dogbreeds.domain

import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit-тест №4: модуль обновления данных (учёт офлайн-кэша). */
class RefreshBreedsUseCaseTest {

    private class FakeRepository(
        private val refreshResult: Result<Unit>,
        private val cached: Int
    ) : BreedRepository {
        override val breeds: Flow<List<Breed>> = flowOf(emptyList())
        override val favorites: Flow<List<Breed>> = flowOf(emptyList())
        override fun observeBreed(id: Int): Flow<Breed?> = flowOf(null)
        override suspend fun cachedCount(): Int = cached
        override suspend fun toggleFavorite(id: Int) = Unit
        override suspend fun refresh(): Result<Unit> = refreshResult
        override suspend fun getBreedImages(id: Int, fallbackUrl: String): List<String> = emptyList()
    }

    @Test
    fun successfulRefresh_returnsSuccess() = runBlocking {
        val outcome = RefreshBreedsUseCase(FakeRepository(Result.success(Unit), cached = 0))()

        assertEquals(RefreshOutcome.Success, outcome)
    }

    @Test
    fun failedRefresh_withCachedData_reportsCacheAvailable() = runBlocking {
        val repository = FakeRepository(Result.failure(IOException()), cached = 25)

        val outcome = RefreshBreedsUseCase(repository)()

        assertTrue(outcome is RefreshOutcome.Failure)
        outcome as RefreshOutcome.Failure
        assertEquals("Нет подключения к интернету.", outcome.message)
        assertTrue(outcome.hasCachedData)
    }

    @Test
    fun failedRefresh_withEmptyCache_reportsNoCache() = runBlocking {
        val repository = FakeRepository(Result.failure(ServerException(500)), cached = 0)

        val outcome = RefreshBreedsUseCase(repository)() as RefreshOutcome.Failure

        assertEquals("Ошибка сервера (500).", outcome.message)
        assertEquals(false, outcome.hasCachedData)
    }
}
