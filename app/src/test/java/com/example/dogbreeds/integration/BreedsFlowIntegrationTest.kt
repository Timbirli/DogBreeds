package com.example.dogbreeds.integration

import com.example.dogbreeds.data.local.BreedEntity
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * Интеграционные тесты взаимодействия слоёв data -> domain -> view.
 * Используются реальные Retrofit/Moshi (MockWebServer), репозиторий, use case'ы и ViewModel;
 * вместо Room — in-memory FakeBreedDao.
 */
class BreedsFlowIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private var env: TestEnvironment? = null

    @After
    fun tearDown() {
        env?.shutdown()
    }

    private fun environment(initial: List<BreedEntity> = emptyList()) =
        TestEnvironment(initial).also { env = it }

    /** №1: ответ API попадает в базу (data) и отображается во ViewModel (view); URL картинки выбирает domain. */
    @Test
    fun refresh_savesApiBreedsToDatabase_andExposesThemThroughViewModel() = runBlocking {
        val env = environment()
        env.enqueueBreeds()

        val viewModel = env.newViewModel()
        val state = withTimeout(5_000) { viewModel.uiState.first { it.totalCount == 2 && !it.isLoading } }

        assertNull(state.error)
        assertEquals(listOf("Beagle", "Labrador Retriever"), state.breeds.map { it.name })
        assertEquals(2, env.dao.snapshot().size)

        val beagle = state.breeds.first { it.id == 1 }
        val labrador = state.breeds.first { it.id == 2 }
        assertEquals("https://img.example/beagle.jpg", beagle.imageUrl)
        assertEquals("https://cdn2.thedogapi.com/images/ref2.jpg", labrador.imageUrl)
    }

    /** №2: поиск (domain) и избранное (data) работают поверх базы, а refresh не сбрасывает избранное. */
    @Test
    fun searchAndFavorites_workOnTopOfDatabase_andRefreshKeepsFavorites() = runBlocking {
        val env = environment(listOf(breedEntity(id = 1, name = "Beagle", isFavorite = true)))
        env.enqueueBreeds()

        val viewModel = env.newViewModel()
        withTimeout(5_000) { viewModel.uiState.first { it.totalCount == 2 && !it.isLoading } }

        // поиск
        viewModel.onQueryChange("  LAB ")
        val filtered = withTimeout(5_000) { viewModel.uiState.first { it.breeds.size == 1 } }
        assertEquals("Labrador Retriever", filtered.breeds.single().name)

        // избранное, выставленное до обновления, сохранилось
        val favorites = withTimeout(5_000) { viewModel.favorites.first { it.isNotEmpty() } }
        assertEquals(listOf("Beagle"), favorites.map { it.name })

        // переключение избранного из ViewModel записывается в базу и приходит обратно
        viewModel.toggleFavorite(2)
        val both = withTimeout(5_000) { viewModel.favorites.first { it.size == 2 } }
        assertEquals(listOf("Beagle", "Labrador Retriever"), both.map { it.name })
    }

    /** №3: ошибка сервера -> ServerException (data) -> сообщение (domain) -> событие и состояние (view); кэш остаётся. */
    @Test
    fun serverError_withCache_keepsCachedBreeds_andEmitsMessage() = runBlocking {
        val env = environment(listOf(breedEntity(id = 1, name = "Beagle")))
        env.enqueueServerError(code = 500)

        val viewModel = env.newViewModel()
        // подписываемся на одноразовые события до того, как сервер ответит (ответ задержан)
        val event = async(start = CoroutineStart.UNDISPATCHED) { viewModel.events.first() }

        val message = withTimeout(5_000) { event.await() }
        assertEquals("Ошибка сервера (500). Показаны сохранённые данные.", message)

        val state = withTimeout(5_000) { viewModel.uiState.first { !it.isLoading } }
        assertEquals("Ошибка сервера (500).", state.error)
        assertEquals(listOf("Beagle"), state.breeds.map { it.name })
    }
}
