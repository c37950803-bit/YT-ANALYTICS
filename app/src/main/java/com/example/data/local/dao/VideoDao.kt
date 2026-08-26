package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface DAO pour les vidéos analysées et associées aux chaînes YouTube.
 *
 * Explication métier :
 * Assure la persistance locale des vidéos clés (Top 5 et Champions) pour chaque chaîne analysée.
 */
@Dao
interface VideoDao {

    /**
     * Récupère toutes les vidéos enregistrées pour une chaîne donnée.
     */
    @Query("SELECT * FROM videos WHERE channelId = :channelId ORDER BY viewCount DESC")
    fun getVideosForChannel(channelId: String): Flow<List<VideoEntity>>

    /**
     * Récupère de manière directe la liste des vidéos pour une chaîne donnée.
     */
    @Query("SELECT * FROM videos WHERE channelId = :channelId ORDER BY viewCount DESC")
    suspend fun getVideosListForChannel(channelId: String): List<VideoEntity>

    /**
     * Récupère le Top 5 des vidéos d'une chaîne spécifique.
     */
    @Query("SELECT * FROM videos WHERE channelId = :channelId AND isTop5 = 1 ORDER BY viewCount DESC LIMIT 5")
    suspend fun getTop5Videos(channelId: String): List<VideoEntity>

    /**
     * Récupère la vidéo championne en nombre de vues (MAX views).
     */
    @Query("SELECT * FROM videos WHERE channelId = :channelId AND isMostViewed = 1 LIMIT 1")
    suspend fun getMostViewedVideo(channelId: String): VideoEntity?

    /**
     * Récupère la vidéo championne en nombre de commentaires (MAX comments).
     */
    @Query("SELECT * FROM videos WHERE channelId = :channelId AND isMostCommented = 1 LIMIT 1")
    suspend fun getMostCommentedVideo(channelId: String): VideoEntity?

    /**
     * Insère une collection de vidéos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    /**
     * Supprime toutes les vidéos rattachées à une chaîne.
     */
    @Query("DELETE FROM videos WHERE channelId = :channelId")
    suspend fun deleteVideosForChannel(channelId: String)

    /**
     * Transaction atomique pour remplacer l'ensemble des vidéos en cache d'une chaîne.
     */
    @Transaction
    suspend fun replaceVideosForChannel(channelId: String, videos: List<VideoEntity>) {
        deleteVideosForChannel(channelId)
        insertVideos(videos)
    }
}
