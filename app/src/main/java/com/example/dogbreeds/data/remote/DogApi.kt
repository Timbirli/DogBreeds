package com.example.dogbreeds.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface DogApi {

    /** Список всех пород. */
    @GET("breeds")
    suspend fun getBreeds(): List<BreedDto>

    /** Фотографии конкретной породы. */
    @GET("images/search")
    suspend fun getBreedImages(
        @Query("breed_ids") breedId: Int,
        @Query("limit") limit: Int = 20
    ): List<ImageDto>
}

/** Источник данных из сети. */
class RemoteDataSource(private val api: DogApi) {
    suspend fun getBreeds(): List<BreedDto> = api.getBreeds()
    suspend fun getBreedImages(breedId: Int): List<ImageDto> = api.getBreedImages(breedId)
}
