package com.example.domain.model

/**
 * Modèle de domaine représentant une vidéo avec ses métriques de performance.
 */
data class VideoItem(
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
    val isMostCommented: Boolean = false,
    val rank: Int = 0
)
