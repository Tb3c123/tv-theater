package com.tvtheater.app.di

import android.content.Context
import com.tvtheater.app.data.api.NguonCApiService
import com.tvtheater.app.data.local.TVTheaterDatabase
import com.tvtheater.app.data.repository.HistoryRepositoryImpl
import com.tvtheater.app.data.repository.MovieRepositoryImpl
import com.tvtheater.app.domain.repository.HistoryRepository
import com.tvtheater.app.domain.repository.MovieRepository
import com.tvtheater.app.domain.usecase.ExtractStreamUrlUseCase
import com.tvtheater.app.domain.usecase.GetRandomCatalogUseCase
import com.tvtheater.app.domain.usecase.ManageHistoryUseCase
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

interface AppContainer {
    val movieRepository: MovieRepository
    val historyRepository: HistoryRepository
    val getRandomCatalogUseCase: GetRandomCatalogUseCase
    val manageHistoryUseCase: ManageHistoryUseCase
    val extractStreamUrlUseCase: ExtractStreamUrlUseCase
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://phim.nguonc.com/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val nguonCApiService: NguonCApiService by lazy {
        retrofit.create(NguonCApiService::class.java)
    }

    private val database: TVTheaterDatabase by lazy {
        TVTheaterDatabase.getInstance(context)
    }

    override val movieRepository: MovieRepository by lazy {
        MovieRepositoryImpl(nguonCApiService)
    }

    override val historyRepository: HistoryRepository by lazy {
        HistoryRepositoryImpl(
            historyDao = database.watchHistoryDao(),
            watchlistDao = database.watchlistDao()
        )
    }

    override val getRandomCatalogUseCase: GetRandomCatalogUseCase by lazy {
        GetRandomCatalogUseCase(movieRepository)
    }

    override val manageHistoryUseCase: ManageHistoryUseCase by lazy {
        ManageHistoryUseCase(historyRepository)
    }

    override val extractStreamUrlUseCase: ExtractStreamUrlUseCase by lazy {
        ExtractStreamUrlUseCase(okHttpClient)
    }
}
