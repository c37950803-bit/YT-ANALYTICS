package com.example.domain.model

/**
 * Modèle de domaine représentant les détails complets et statistiques d'une chaîne YouTube.
 */
data class ChannelDetails(
    val channelId: String,
    val title: String,
    val customUrl: String? = null,
    val description: String? = null,
    val thumbnailUrl: String,
    val bannerUrl: String? = null,
    val subscriberCount: Long = 0L,
    val videoCount: Long = 0L,
    val viewCount: Long = 0L,
    val uploadsPlaylistId: String? = null,
    val lastViewedTimestamp: Long = System.currentTimeMillis(),
    val cachedAtTimestamp: Long = System.currentTimeMillis()
)
