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
 * =========================================================================================
 * 💾 BASE DE DONNÉES LOCALE : AppDatabase.kt (Room SQLite)
 * =========================================================================================
 * 
 * 💡 EXPLICATION POUR GRAND DÉBUTANT :
 * 
 * 1. QUEL EST SON RÔLE ?
 *    - Elle stocke les données directement sur le téléphone (hors-ligne).
 *    - Elle gère 3 tables :
 *      1. `ApiKeyEntity` : Les clés API YouTube pour faire fonctionner les requêtes.
 *      2. `ChannelEntity` : Les chaînes recherchées par l'utilisateur (historique local).
 *      3. `VideoEntity` : Les vidéos analysées mises en cache.
 * 
 * 2. ÉTAT INITIAL 100% PROPRE ET VIDE :
 *    - Au premier lancement, AUCUNE chaîne n'est pré-insérée.
 *    - L'historique démarre totalement propre, vide et prêt à accueillir les recherches de l'utilisateur.
 */
@Database(
    entities = [
        ApiKeyEntity::class,
        ChannelEntity::class,
        VideoEntity::class
    ],
    version = 2,
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
         * Récupère l'instance unique de la base de données (Pattern Singleton).
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

        /**
         * Initialisation initiale : configure uniquement la clé API par défaut.
         * Aucune chaîne ni vidéo par défaut n'est injectée : tout est propre et vide !
         */
        private suspend fun populateInitialData(database: AppDatabase) {
            try {
                // Clé API par défaut (Samuel Driver / Clé Principale YouTube Data v3)
                val defaultKey = ApiKeyEntity(
                    name = "Clé YouTube Data v3 (Défaut)",
                    apiKey = "AIzaSyAz35xRYYG9VTKnWT0-cFExPdJaVr2v4EM",
                    isDefault = true,
                    createdAt = System.currentTimeMillis()
                )
                database.apiKeyDao().insertApiKey(defaultKey)
                // Remarque : Aucun canal ou vidéo n'est inséré ici pour garantir un démarrage 100% propre et vide.
            } catch (_: Exception) {
                // Ignore fallback pre-population errors
            }
        }
    }
}
