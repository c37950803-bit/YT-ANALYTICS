package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ApiKeyDao
import com.example.data.local.dao.ChannelDao
import com.example.data.local.dao.VideoDao
import com.example.data.local.entity.ApiKeyEntity
import com.example.data.local.entity.ChannelEntity
import com.example.data.local.entity.VideoEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de données locale Room de l'application YouTube Analytics.
 *
 * Explication métier :
 * Sert de source unique de vérité locale (Single Source of Truth) pour :
 * 1. Les clés API YouTube fournies par l'utilisateur (avec gestion des clés multiples et du fallback).
 * 2. L'historique des chaînes consultées (écran d'accueil "LocalStorage").
 * 3. Le cache complet des analyses (statistiques et vidéos) pour un affichage hors-ligne.
 *
 * Explication technique :
 * - Version 1, pas d'export de schéma pour simplifier la compilation.
 * - Pattern Singleton thread-safe pour l'instanciation de la base.
 */
@Database(
    entities = [
        ApiKeyEntity::class,
        ChannelEntity::class,
        VideoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun apiKeyDao(): ApiKeyDao
    abstract fun channelDao(): ChannelDao
    abstract fun videoDao(): VideoDao

    companion object {
        private const val DATABASE_NAME = "yt_analytics_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Récupère l'instance unique de la base de données (Double-checked locking).
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onCreate(db)
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                populateInitialData(getInstance(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            try {
                val beastChannel = ChannelEntity(
                    channelId = "UCX6OQ3DkcsbYNE6H8uQQuVA",
                    title = "MrBeast",
                    customUrl = "@MrBeast",
                    description = "I want to make the world a better place before I die.",
                    thumbnailUrl = "https://yt3.googleusercontent.com/fxGKYucJAVme-Yzgn葷v",
                    bannerUrl = null,
                    subscriberCount = 318000000L,
                    videoCount = 820L,
                    viewCount = 61200000000L,
                    uploadsPlaylistId = "UUX6OQ3DkcsbYNE6H8uQQuVA",
                    lastViewedTimestamp = System.currentTimeMillis()
                )
                database.channelDao().insertChannel(beastChannel)

                val mkbhdChannel = ChannelEntity(
                    channelId = "UCBJycsmduvYEL83R_U4JriQ",
                    title = "Marques Brownlee",
                    customUrl = "@mkbhd",
                    description = "Quality Tech Videos | YouTuber | Geek",
                    thumbnailUrl = "https://yt3.googleusercontent.com/lkH37D712tiyphnu0Id0Q5MwwQ9oiMWgEQgnArdrGlfWgIFOqVzutqwOq_O0GLp2-bpTrSQf72s=s176-c-k-c0x00ffffff-no-rj",
                    bannerUrl = null,
                    subscriberCount = 19100000L,
                    videoCount = 1640L,
                    viewCount = 4300000000L,
                    uploadsPlaylistId = "UUBJycsmduvYEL83R_U4JriQ",
                    lastViewedTimestamp = System.currentTimeMillis() - 3600000
                )
                database.channelDao().insertChannel(mkbhdChannel)

                // Top 5 videos for MrBeast
                val beastVideos = listOf(
                    VideoEntity(
                        videoId = "0e3GPea1Tyg",
                        channelId = "UCX6OQ3DkcsbYNE6H8uQQuVA",
                        title = "$456,000 Squid Game In Real Life!",
                        description = "Recreating Squid Game in real life with 456 players!",
                        thumbnailUrl = "https://i.ytimg.com/vi/0e3GPea1Tyg/hqdefault.jpg",
                        publishedAt = "2021-11-24T21:00:00Z",
                        viewCount = 670000000L,
                        likeCount = 17500000L,
                        commentCount = 620000L,
                        durationIso = "PT25M42S",
                        isTop5 = true,
                        isMostViewed = true,
                        isMostCommented = true
                    ),
                    VideoEntity(
                        videoId = "9bqk6ZUsKyA",
                        channelId = "UCX6OQ3DkcsbYNE6H8uQQuVA",
                        title = "Last To Leave Circle Wins $500,000",
                        description = "100 people in a giant red circle competing for $500k.",
                        thumbnailUrl = "https://i.ytimg.com/vi/9bqk6ZUsKyA/hqdefault.jpg",
                        publishedAt = "2021-08-31T20:00:00Z",
                        viewCount = 410000000L,
                        likeCount = 11000000L,
                        commentCount = 380000L,
                        durationIso = "PT18M21S",
                        isTop5 = true,
                        isMostViewed = false,
                        isMostCommented = false
                    ),
                    VideoEntity(
                        videoId = "GLoeAJUcz38",
                        channelId = "UCX6OQ3DkcsbYNE6H8uQQuVA",
                        title = "I Spent 50 Hours In Solitary Confinement",
                        description = "No light, no clocks, no human contact for 50 straight hours.",
                        thumbnailUrl = "https://i.ytimg.com/vi/GLoeAJUcz38/hqdefault.jpg",
                        publishedAt = "2020-04-18T19:00:00Z",
                        viewCount = 340000000L,
                        likeCount = 8900000L,
                        commentCount = 290000L,
                        durationIso = "PT15M52S",
                        isTop5 = true,
                        isMostViewed = false,
                        isMostCommented = false
                    ),
                    VideoEntity(
                        videoId = "gHzuabZUd6c",
                        channelId = "UCX6OQ3DkcsbYNE6H8uQQuVA",
                        title = "I Cleaned The World's Dirtiest Beach #TeamSeas",
                        description = "Cleaning up oceans and beaches with TeamSeas.",
                        thumbnailUrl = "https://i.ytimg.com/vi/gHzuabZUd6c/hqdefault.jpg",
                        publishedAt = "2021-10-29T20:00:00Z",
                        viewCount = 310000000L,
                        likeCount = 9400000L,
                        commentCount = 310000L,
                        durationIso = "PT21M05S",
                        isTop5 = true,
                        isMostViewed = false,
                        isMostCommented = false
                    ),
                    VideoEntity(
                        videoId = "r7zJ8srWWjk",
                        channelId = "UCX6OQ3DkcsbYNE6H8uQQuVA",
                        title = "I Built Willy Wonka's Chocolate Factory!",
                        description = "Full scale working chocolate river and rooms!",
                        thumbnailUrl = "https://i.ytimg.com/vi/r7zJ8srWWjk/hqdefault.jpg",
                        publishedAt = "2022-06-04T20:00:00Z",
                        viewCount = 290000000L,
                        likeCount = 8200000L,
                        commentCount = 240000L,
                        durationIso = "PT17M14S",
                        isTop5 = true,
                        isMostViewed = false,
                        isMostCommented = false
                    )
                )
                database.videoDao().insertVideos(beastVideos)
            } catch (_: Exception) {
                // Ignore fallback pre-population errors
            }
        }
    }
}
