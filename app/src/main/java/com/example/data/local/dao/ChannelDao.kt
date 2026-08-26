package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ChannelEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface DAO pour la gestion de l'historique et du cache des chaînes YouTube.
 *
 * Explication métier :
 * Alimente l'écran d'accueil avec les dernières chaînes consultées ("LocalStorage").
 * Permet un rafraîchissement local ultra-rapide (SwipeRefreshLayout) sans consommation de quota réseau.
 */
@Dao
interface ChannelDao {

    /**
     * Récupère l'historique complet des chaînes, de la plus récemment consultée à la plus ancienne.
     */
    @Query("SELECT * FROM channels ORDER BY lastViewedTimestamp DESC")
    fun getAllHistory(): Flow<List<ChannelEntity>>

    /**
     * Récupère une chaîne en cache via son ID YouTube.
     */
    @Query("SELECT * FROM channels WHERE channelId = :channelId LIMIT 1")
    suspend fun getChannelById(channelId: String): ChannelEntity?

    /**
     * Recherche une chaîne en cache local par ID, handle ou titre pour le fallback hors-ligne.
     */
    @Query("SELECT * FROM channels WHERE channelId = :query OR customUrl LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' LIMIT 1")
    suspend fun findChannelByQuery(query: String): ChannelEntity?

    /**
     * Insère ou remplace une chaîne dans l'historique.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity)

    /**
     * Met à jour le timestamp de dernière consultation d'une chaîne.
     */
    @Query("UPDATE channels SET lastViewedTimestamp = :timestamp WHERE channelId = :channelId")
    suspend fun updateLastViewed(channelId: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Supprime une chaîne spécifique de l'historique (cascade vers ses vidéos).
     */
    @Query("DELETE FROM channels WHERE channelId = :channelId")
    suspend fun deleteChannelById(channelId: String)

    /**
     * Vide intégralement l'historique des chaînes.
     */
    @Query("DELETE FROM channels")
    suspend fun clearHistory()
}
