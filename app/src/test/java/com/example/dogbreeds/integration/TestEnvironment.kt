package com.example.dogbreeds.integration

import com.example.dogbreeds.data.BreedRepositoryImpl
import com.example.dogbreeds.data.local.BreedEntity
import com.example.dogbreeds.data.local.LocalDataSource
import com.example.dogbreeds.data.remote.DogApi
import com.example.dogbreeds.data.remote.RemoteDataSource
import com.example.dogbreeds.domain.BreedRepository
import com.example.dogbreeds.ui.BreedsViewModel
import com.squareup.moshi.Moshi
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Собирает настоящую цепочку data -> domain -> view:
 * MockWebServer + Retrofit/Moshi -> RemoteDataSource -> BreedRepositoryImpl (+ fake DAO) -> use case'ы -> ViewModel.
 */
class TestEnvironment(initialDatabase: List<BreedEntity> = emptyList()) {

    val server = MockWebServer().apply { start() }
    val dao = FakeBreedDao(initialDatabase)

    private val api: DogApi = Retrofit.Builder()
        .baseUrl(server.url("/"))
        .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
        .build()
        .create(DogApi::class.java)

    val repository: BreedRepository = BreedRepositoryImpl(
        remote = RemoteDataSource(api),
        local = LocalDataSource(dao)
    )

    /** ViewModel сразу запускает refresh(), поэтому ответ сервера нужно поставить в очередь ДО вызова. */
    fun newViewModel() = BreedsViewModel(repository, ioDispatcher = Dispatchers.IO)

    fun enqueueBreeds(json: String = BREEDS_JSON) {
        server.enqueue(MockResponse().setResponseCode(200).setBody(json))
    }

    fun enqueueServerError(code: Int = 500, delayMs: Long = 300) {
        server.enqueue(
            MockResponse().setResponseCode(code).setHeadersDelay(delayMs, TimeUnit.MILLISECONDS)
        )
    }

    fun shutdown() = server.shutdown()

    companion object {
        /** Beagle — с прямым url картинки; Labrador — только reference_image_id. */
        const val BREEDS_JSON = """
            [
              {"id": 1, "name": "Beagle", "temperament": "Amiable, Gentle", "origin": "England",
               "life_span": "12 - 15 years", "description": "Friendly hound.",
               "reference_image_id": "ref1", "image": {"url": "https://img.example/beagle.jpg"}},
              {"id": 2, "name": "Labrador Retriever", "temperament": "Kind, Outgoing",
               "life_span": "10 - 12 years", "reference_image_id": "ref2"}
            ]
        """
    }
}
