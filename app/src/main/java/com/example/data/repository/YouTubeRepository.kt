package com.example.data.repository

import com.example.data.local.dao.ChannelDao
import com.example.data.local.dao.VideoDao
import com.example.data.local.entity.ChannelEntity
import com.example.data.local.entity.VideoEntity
import com.example.data.remote.api.YouTubeApiService
import com.example.data.remote.model.YouTubeChannelItem
import com.example.data.remote.model.YouTubeVideoItemDto
import com.example.domain.model.ChannelDetails
import com.example.domain.model.ChannelQueryParser
import com.example.domain.model.ChannelQueryType
import com.example.domain.model.DashboardAnalysis
import com.example.domain.model.VideoItem
import com.example.domain.result.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException

/**
 * Interface définissant les opérations du Repository YouTube.
 *
 * Explication métier :
 * Constitue la source unique de vérité pour toutes les données de chaînes et de vidéos.
 * Orchestre la combinaison entre le stockage local Room (historique, cache hors-ligne)
 * et les appels distants Retrofit (YouTube Data API v3).
 */
interface YouTubeRepository {

    /**
     * Observe le flux réactif de l'historique des chaînes consultées.
     */
    fun getHistory(): Flow<List<ChannelDetails>>

    /**
     * Récupère une analyse complète depuis le cache local Room si disponible.
     */
    suspend fun getCachedAnalysis(channelId: String): DashboardAnalysis?

    /**
     * Lance l'analyse complète d'une chaîne YouTube (requête réseau + calculs + persistance).
     *
     * @param query Saisie utilisateur (URL complète, @handle ou Channel ID)
     * @param apiKey Clé API YouTube à utiliser (si null, utilise la clé par défaut en base)
     */
    suspend fun analyzeChannel(query: String, apiKey: String): Resource<DashboardAnalysis>

    /**
     * Supprime une chaîne spécifique de l'historique local.
     */
    suspend fun deleteHistoryItem(channelId: String)

    /**
     * Efface l'intégralité de l'historique local.
     */
    suspend fun clearHistory()
}

/**
 * Implémentation concrète du Repository YouTube.
 */
class YouTubeRepositoryImpl(
    private val apiService: YouTubeApiService,
    private val channelDao: ChannelDao,
    private val videoDao: VideoDao
) : YouTubeRepository {

    override fun getHistory(): Flow<List<ChannelDetails>> {
        return channelDao.getAllHistory().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getCachedAnalysis(channelId: String): DashboardAnalysis? = withContext(Dispatchers.IO) {
        val channelEntity = channelDao.getChannelById(channelId) ?: return@withContext null
        val videoEntities = videoDao.getVideosListForChannel(channelId)

        val channel = channelEntity.toDomain()
        val videos = videoEntities.map { it.toDomain() }

        // Extraction des statistiques clés depuis le cache
        val top5 = videos.filter { it.isTop5 }.sortedByDescending { it.viewCount }.ifEmpty {
            videos.sortedByDescending { it.viewCount }.take(5)
        }.mapIndexed { index, item -> item.copy(rank = index + 1) }

        val mostViewed = videos.firstOrNull { it.isMostViewed }
            ?: videos.maxByOrNull { it.viewCount }

        val mostCommented = videos.firstOrNull { it.isMostCommented }
            ?: videos.maxByOrNull { it.commentCount }

        DashboardAnalysis(
            channel = channel,
            top5Videos = top5,
            mostViewedVideo = mostViewed,
            mostCommentedVideo = mostCommented,
            isFromCache = true,
            analysisTimestamp = channelEntity.cachedAtTimestamp
        )
    }

    override suspend fun analyzeChannel(query: String, apiKey: String): Resource<DashboardAnalysis> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext Resource.Error(
                    message = "Aucune clé API configurée. Veuillez ajouter une clé API YouTube valide.",
                    errorType = Resource.ErrorType.NO_API_KEY
                )
            }

            // 1. Analyse de la saisie utilisateur (URL / Handle / ID)
            val queryType = ChannelQueryParser.parse(query)

            // 2. Appel Réseau 1 : Récupération des informations de la chaîne
            val channelResponse = fetchChannelInfo(queryType, apiKey)

            // Gestion des erreurs HTTP (Quotas, clé invalide, etc.)
            if (!channelResponse.isSuccessful) {
                return@withContext handleHttpError(channelResponse.code(), channelResponse.errorBody()?.string())
            }

            val channelItems = channelResponse.body()?.items
            if (channelItems.isNullOrEmpty()) {
                return@withContext Resource.Error(
                    message = "Chaîne introuvable. Vérifiez l'URL ou le Handle saisi.",
                    errorType = Resource.ErrorType.CHANNEL_NOT_FOUND
                )
            }

            val channelDto = channelItems.first()
            val channelId = channelDto.id
            val snippet = channelDto.snippet
            val stats = channelDto.statistics
            val branding = channelDto.brandingSettings

            // Détermination de l'ID de la playlist "Uploads"
            // Règle métier YouTube : l'ID uploads commence par "UU" au lieu de "UC"
            val uploadsPlaylistId = channelDto.contentDetails?.relatedPlaylists?.uploads
                ?: if (channelId.startsWith("UC")) "UU" + channelId.removePrefix("UC") else null

            val channelDetails = ChannelDetails(
                channelId = channelId,
                title = snippet?.title ?: "Chaîne YouTube",
                customUrl = snippet?.customUrl,
                description = snippet?.description,
                thumbnailUrl = snippet?.thumbnails?.getBestUrl() ?: "",
                bannerUrl = branding?.image?.bannerExternalUrl,
                subscriberCount = stats?.subscriberCount?.toLongOrNull() ?: 0L,
                videoCount = stats?.videoCount?.toLongOrNull() ?: 0L,
                viewCount = stats?.viewCount?.toLongOrNull() ?: 0L,
                uploadsPlaylistId = uploadsPlaylistId,
                lastViewedTimestamp = System.currentTimeMillis(),
                cachedAtTimestamp = System.currentTimeMillis()
            )

            // 3. Appel Réseau 2 : Récupération des vidéos récentes de la chaîne
            var processedVideos: List<VideoItem> = emptyList()

            if (!uploadsPlaylistId.isNullOrBlank()) {
                val playlistResponse = apiService.getPlaylistItems(
                    playlistId = uploadsPlaylistId,
                    maxResults = 50,
                    apiKey = apiKey
                )

                if (playlistResponse.isSuccessful) {
                    val playlistItems = playlistResponse.body()?.items ?: emptyList()
                    val videoIds = playlistItems.mapNotNull {
                        it.contentDetails?.videoId ?: it.snippet?.resourceId?.videoId
                    }.filter { it.isNotBlank() }

                    if (videoIds.isNotEmpty()) {
                        // 4. Appel Réseau 3 : Récupération des statistiques détaillées des vidéos (Vues, Likes, Commentaires)
                        // L'endpoint videos accepte jusqu'à 50 IDs séparés par des virgules en un seul appel
                        val idsParam = videoIds.joinToString(",")
                        val videosDetailsResponse = apiService.getVideosDetails(
                            videoIds = idsParam,
                            apiKey = apiKey
                        )

                        if (videosDetailsResponse.isSuccessful) {
                            val videoDetailsList = videosDetailsResponse.body()?.items ?: emptyList()
                            processedVideos = mapToVideoItems(videoDetailsList, channelId)
                        }
                    }
                }
            }

            // 5. Traitement métier et Algorithmes de tri
            // a. Détermination des deux champions
            val mostViewedVideo = processedVideos.maxByOrNull { it.viewCount }
            val mostCommentedVideo = processedVideos.maxByOrNull { it.commentCount }

            // b. Calcul du Top 5 des vidéos les plus vues
            val top5Videos = processedVideos
                .sortedByDescending { it.viewCount }
                .take(5)
                .mapIndexed { index, video -> video.copy(rank = index + 1, isTop5 = true) }

            // c. Mise à jour des drapeaux dans la liste complète
            val top5Ids = top5Videos.map { it.videoId }.toSet()
            val finalVideos = processedVideos.map { video ->
                video.copy(
                    isTop5 = top5Ids.contains(video.videoId),
                    isMostViewed = video.videoId == mostViewedVideo?.videoId,
                    isMostCommented = video.videoId == mostCommentedVideo?.videoId
                )
            }

            // 6. Sauvegarde en cache local Room (Source unique de vérité)
            saveToLocalDatabase(channelDetails, finalVideos)

            val analysis = DashboardAnalysis(
                channel = channelDetails,
                top5Videos = top5Videos,
                mostViewedVideo = mostViewedVideo?.copy(isMostViewed = true),
                mostCommentedVideo = mostCommentedVideo?.copy(isMostCommented = true),
                isFromCache = false,
                analysisTimestamp = channelDetails.cachedAtTimestamp
            )

            Resource.Success(analysis)

        } catch (e: IOException) {
            Resource.Error(
                message = "Erreur de connexion : vérifiez votre accès Internet.",
                errorType = Resource.ErrorType.NETWORK_ERROR,
                cause = e
            )
        } catch (e: Exception) {
            Resource.Error(
                message = e.localizedMessage ?: "Une erreur inattendue est survenue lors de l'analyse.",
                errorType = Resource.ErrorType.GENERIC,
                cause = e
            )
        }
    }

    override suspend fun deleteHistoryItem(channelId: String) = withContext(Dispatchers.IO) {
        channelDao.deleteChannelById(channelId)
    }

    override suspend fun clearHistory() = withContext(Dispatchers.IO) {
        channelDao.clearHistory()
    }

    /**
     * Effectue la requête Retrofit appropriée selon le type de paramètre identifié.
     */
    private suspend fun fetchChannelInfo(
        queryType: ChannelQueryType,
        apiKey: String
    ): Response<com.example.data.remote.model.YouTubeChannelListResponse> {
        return when (queryType) {
            is ChannelQueryType.ByChannelId -> apiService.getChannelById(channelId = queryType.channelId, apiKey = apiKey)
            is ChannelQueryType.ByHandle -> apiService.getChannelByHandle(handle = queryType.handle, apiKey = apiKey)
            is ChannelQueryType.ByUsername -> apiService.getChannelByUsername(username = queryType.username, apiKey = apiKey)
        }
    }

    /**
     * Convertit la liste des DTOs vidéo en modèles de domaine prêts pour le calcul.
     */
    private fun mapToVideoItems(dtos: List<YouTubeVideoItemDto>, channelId: String): List<VideoItem> {
        return dtos.map { dto ->
            val snippet = dto.snippet
            val stats = dto.statistics
            val details = dto.contentDetails

            VideoItem(
                videoId = dto.id,
                channelId = channelId,
                title = snippet?.title ?: "Vidéo sans titre",
                description = snippet?.description ?: "",
                thumbnailUrl = snippet?.thumbnails?.getBestUrl() ?: "",
                publishedAt = snippet?.publishedAt ?: "",
                viewCount = stats?.viewCount?.toLongOrNull() ?: 0L,
                likeCount = stats?.likeCount?.toLongOrNull() ?: 0L,
                commentCount = stats?.commentCount?.toLongOrNull() ?: 0L,
                durationIso = details?.duration
            )
        }
    }

    /**
     * Sauvegarde la chaîne et l'ensemble de ses vidéos associées dans Room.
     */
    private suspend fun saveToLocalDatabase(channel: ChannelDetails, videos: List<VideoItem>) {
        val channelEntity = ChannelEntity(
            channelId = channel.channelId,
            title = channel.title,
            customUrl = channel.customUrl,
            description = channel.description,
            thumbnailUrl = channel.thumbnailUrl,
            bannerUrl = channel.bannerUrl,
            subscriberCount = channel.subscriberCount,
            videoCount = channel.videoCount,
            viewCount = channel.viewCount,
            uploadsPlaylistId = channel.uploadsPlaylistId,
            lastViewedTimestamp = System.currentTimeMillis(),
            cachedAtTimestamp = System.currentTimeMillis()
        )
        channelDao.insertChannel(channelEntity)

        val videoEntities = videos.map { video ->
            VideoEntity(
                videoId = video.videoId,
                channelId = video.channelId,
                title = video.title,
                description = video.description,
                thumbnailUrl = video.thumbnailUrl,
                publishedAt = video.publishedAt,
                viewCount = video.viewCount,
                likeCount = video.likeCount,
                commentCount = video.commentCount,
                durationIso = video.durationIso,
                isTop5 = video.isTop5,
                isMostViewed = video.isMostViewed,
                isMostCommented = video.isMostCommented
            )
        }
        videoDao.replaceVideosForChannel(channel.channelId, videoEntities)
    }

    /**
     * Interprète les codes d'erreur HTTP retournés par l'API YouTube Data v3.
     */
    private fun <T> handleHttpError(code: Int, errorBody: String?): Resource<T> {
        return when (code) {
            403 -> {
                if (errorBody?.contains("quotaExceeded", ignoreCase = true) == true ||
                    errorBody?.contains("dailyLimitExceeded", ignoreCase = true) == true
                ) {
                    Resource.Error(
                        message = "Quota journalier YouTube dépassé pour cette clé API.",
                        errorType = Resource.ErrorType.QUOTA_EXCEEDED
                    )
                } else {
                    Resource.Error(
                        message = "Accès refusé par l'API YouTube. Vérifiez que l'API YouTube Data v3 est bien activée pour cette clé.",
                        errorType = Resource.ErrorType.INVALID_API_KEY
                    )
                }
            }
            400 -> Resource.Error(
                message = "Clé API invalide ou requête malformée.",
                errorType = Resource.ErrorType.INVALID_API_KEY
            )
            404 -> Resource.Error(
                message = "Ressource introuvable sur YouTube.",
                errorType = Resource.ErrorType.CHANNEL_NOT_FOUND
            )
            else -> Resource.Error(
                message = "Erreur de l'API YouTube (Code HTTP $code).",
                errorType = Resource.ErrorType.GENERIC
            )
        }
    }

    private fun ChannelEntity.toDomain() = ChannelDetails(
        channelId = channelId,
        title = title,
        customUrl = customUrl,
        description = description,
        thumbnailUrl = thumbnailUrl,
        bannerUrl = bannerUrl,
        subscriberCount = subscriberCount,
        videoCount = videoCount,
        viewCount = viewCount,
        uploadsPlaylistId = uploadsPlaylistId,
        lastViewedTimestamp = lastViewedTimestamp,
        cachedAtTimestamp = cachedAtTimestamp
    )

    private fun VideoEntity.toDomain() = VideoItem(
        videoId = videoId,
        channelId = channelId,
        title = title,
        description = description,
        thumbnailUrl = thumbnailUrl,
        publishedAt = publishedAt,
        viewCount = viewCount,
        likeCount = likeCount,
        commentCount = commentCount,
        durationIso = durationIso,
        isTop5 = isTop5,
        isMostViewed = isMostViewed,
        isMostCommented = isMostCommented
    )
}
