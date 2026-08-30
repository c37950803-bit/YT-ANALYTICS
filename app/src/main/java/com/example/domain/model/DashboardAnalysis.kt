package com.example.domain.model

/**
 * Modèle de domaine agrégeant l'ensemble des résultats de l'analyse d'une chaîne pour le Dashboard.
 *
 * Explication métier :
 * Regroupe :
 * 1. Les statistiques globales de la chaîne (abonnés, vues, vidéos, bannière).
 * 2. La vidéo championne "La plus regardée" (MAX views).
 * 3. La vidéo championne "La plus commentée" (MAX comments).
 * 4. Le Top 5 des vidéos les plus vues.
 * 5. Des métadonnées sur la source (est-ce issu du cache local ? date de fraîcheur).
 */
data class DashboardAnalysis(
    val channel: ChannelDetails,
    val top5Videos: List<VideoItem>,
    val top5MostCommentedVideos: List<VideoItem> = emptyList(),
    val top5LongestVideos: List<VideoItem> = emptyList(),
    val mostViewedVideo: VideoItem?,
    val mostCommentedVideo: VideoItem?,
    val isFromCache: Boolean = false,
    val analysisTimestamp: Long = System.currentTimeMillis()
)
