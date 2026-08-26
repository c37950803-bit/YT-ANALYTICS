package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entité Room représentant une vidéo analysée rattachée à une chaîne spécifique.
 *
 * Explication métier :
 * Stocke les vidéos mises en cache de la chaîne, incluant le Top 5 et les deux "Champions"
 * (la plus regardée et la plus commentée), pour un affichage immédiat même sans connexion Internet.
 *
 * Explication technique :
 * - `videoId` : ID YouTube unique de la vidéo (ex: "dQw4w9WgXcQ").
 * - `channelId` : Clé étrangère pointant vers la table `channels`.
 * - `viewCount`, `likeCount`, `commentCount` : Métriques clés pour les algorithmes de tri.
 * - `isTop5`, `isMostViewed`, `isMostCommented` : Drapeaux de pré-calcul d'affichage.
 */
@Entity(
    tableName = "videos",
    foreignKeys = [
        ForeignKey(
            entity = ChannelEntity::class,
            parentColumns = ["channelId"],
            childColumns = ["channelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["channelId"])]
)
data class VideoEntity(
    @PrimaryKey
    val videoId: String,
    val channelId: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val publishedAt: String,
    val viewCount: Long = 0L,
    val likeCount: Long = 0L,
    val commentCount: Long = 0L,
    val durationIso: String? = null,
    val isTop5: Boolean = false,
    val isMostViewed: Boolean = false,
    val isMostCommented: Boolean = false
)
