package com.example.dogbreeds

import android.content.Context
import androidx.room.Room
import com.example.dogbreeds.data.BreedRepository
import com.example.dogbreeds.data.local.AppDatabase
import com.example.dogbreeds.data.local.LocalDataSource
import com.example.dogbreeds.data.remote.DogApi
import com.example.dogbreeds.data.remote.RemoteDataSource
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/** Ручное создание зависимостей (без Hilt/Dagger/Koin). */
object AppDependencies {

    private const val BASE_URL = "https://api.thedogapi.com/v1/"
    private const val API_KEY = "live_sxwjZZSm8EqCK2nWxoYqEbAnWA6CNx0r2NhleqATKfxq1Ccrh79tm3ipzAlPtJaL"

    lateinit var repository: BreedRepository
        private set

    fun init(context: Context) {
        if (::repository.isInitialized) return

        val database = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "dogbreeds.db"
        ).build()

        val okHttp = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder().apply {
                    if (API_KEY.isNotBlank()) header("x-api-key", API_KEY)
                }.build()
                chain.proceed(request)
            }
            .build()

        val moshi = Moshi.Builder().build() // адаптеры генерирует moshi-kotlin-codegen

        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(DogApi::class.java)

        repository = BreedRepository(
            remote = RemoteDataSource(api),
            local = LocalDataSource(database.breedDao())
        )
    }
}
