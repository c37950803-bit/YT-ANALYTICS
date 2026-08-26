package com.example.data.remote.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Fournisseur d'instances réseau (OkHttpClient, Moshi, Retrofit).
 *
 * Explication technique :
 * Configure le client HTTP avec des timeouts raisonnables, la journalisation
 * et le sérialiseur JSON Moshi compatible Kotlin.
 */
object ApiClient {

    private const val BASE_URL = "https://www.googleapis.com/"

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    /**
     * Instance prête à l'emploi du service Retrofit YouTube.
     */
    val youTubeApiService: YouTubeApiService by lazy {
        retrofit.create(YouTubeApiService::class.java)
    }
}
