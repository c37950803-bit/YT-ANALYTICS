package com.example.di

import android.content.Context
import com.example.data.local.database.AppDatabase
import com.example.data.remote.api.ApiClient
import com.example.data.repository.ApiKeyRepository
import com.example.data.repository.ApiKeyRepositoryImpl
import com.example.data.repository.YouTubeRepository
import com.example.data.repository.YouTubeRepositoryImpl

/**
 * Conteneur d'injection de dépendances manuelle de l'application.
 *
 * Explication technique :
 * Fournit les instances uniques des repositories, de la base Room et du client Retrofit
 * sans alourdir le projet avec des frameworks superflus.
 */
interface AppContainer {
    val apiKeyRepository: ApiKeyRepository
    val youTubeRepository: YouTubeRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    override val apiKeyRepository: ApiKeyRepository by lazy {
        ApiKeyRepositoryImpl(database.apiKeyDao())
    }

    override val youTubeRepository: YouTubeRepository by lazy {
        YouTubeRepositoryImpl(
            apiService = ApiClient.youTubeApiService,
            channelDao = database.channelDao(),
            videoDao = database.videoDao()
        )
    }
}
