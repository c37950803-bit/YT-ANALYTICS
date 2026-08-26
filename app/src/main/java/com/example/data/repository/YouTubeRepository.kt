package com.example.data.repository

import com.example.data.local.dao.ApiKeyDao
import com.example.data.local.dao.ChannelDao
import com.example.data.local.dao.VideoDao
import com.example.data.local.entity.ChannelEntity
import com.example.data.local.entity.VideoEntity
import com.example.data.remote.api.YouTubeApiService
import com.example.data.remote.model.YouTubeChannelItem
import com.example.data.remote.model.YouTubeChannelListResponse
import com.example.data.remote.model.YouTubeVideoItemDto
import com.example.domain.model.ChannelDetails
import com.example.domain.model.ChannelQueryParser
import com.example.domain.model.ChannelQueryType
import com.example.domain.model.DashboardAnalysis
import com.example.domain.model.VideoItem
import com.example.domain.result.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
 * Implémentation concrète du Repository YouTube avec système de Fallback robuste à plusieurs niveaux :
 * 1. Rotation automatique de clés API (Clé demandée -> Clé active Room -> Autres clés enregistrées -> Clé built-in de secours).
 * 2. Résolution résiliente d'identifiants de chaînes (Channel ID -> Handle avec '@' -> Handle sans '@' -> Nom d'utilisateur legacy).
 * 3. Reprise sur erreur réseau temporaire (Retry avec backoff).
 * 4. Fallback vers le cache local Room complet en cas d'épuisement total des quotas ou mode hors-ligne.
 */
class YouTubeRepositoryImpl(
    private val apiService: YouTubeApiService,
    private val channelDao: ChannelDao,
    private val videoDao: VideoDao,
    private val apiKeyDao: ApiKeyDao
) : YouTubeRepository {

    override fun getHistory(): Flow<List<ChannelDetails>> {
        return channelDao.getAllHistory().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getCachedAnalysis(channelId: String): DashboardAnalysis? = withContext(Dispatchers.IO) {
        val channelEntity = channelDao.getChannelById(channelId) ?: return@withContext null
        buildDashboardFromCachedEntity(channelEntity)
    }

    override suspend fun analyzeChannel(query: String, apiKey: String): Resource<DashboardAnalysis> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            return@withContext Resource.Error(
                message = "Veuillez saisir une URL ou un nom de chaîne YouTube.",
                errorType = Resource.ErrorType.GENERIC
            )
        }

        // 1. Construction de la chaîne de clés candidates (Robuste multi-key fallback)
        val candidateKeys = buildCandidateKeysList(apiKey)

        var lastError: Resource.Error? = null

        // 2. Itération sur chaque clé candidate jusqu'à succès
        for (currentKey in candidateKeys) {
            val result = executeAnalysisWithKey(cleanQuery, currentKey)
            when (result) {
                is Resource.Success -> {
                    return@withContext result
                }
                is Resource.Error -> {
                    lastError = result
                    // Si l'erreur est un dépassement de quota ou une clé invalide, on bascule silencieusement sur la clé suivante
                    if (result.errorType == Resource.ErrorType.QUOTA_EXCEEDED ||
                        result.errorType == Resource.ErrorType.INVALID_API_KEY
                    ) {
                        continue
                    }
                    // Si la chaîne est explicitement introuvable ou erreur réseau non récupérable, on sort
                    if (result.errorType == Resource.ErrorType.CHANNEL_NOT_FOUND) {
                        break
                    }
                }
                is Resource.Loading -> Unit
            }
        }

        // 3. Fallback ultime : si toutes les clés ont échoué (quota/réseau), vérifier si la chaîne existe dans le cache local
        val localCached = findInLocalCache(cleanQuery)
        if (localCached != null) {
            return@withContext Resource.Success(localCached)
        }

        // 4. Si aucun cache disponible, retourner la dernière erreur explicite
        lastError ?: Resource.Error(
            message = "Impossible de récupérer les données YouTube pour cette chaîne.",
            errorType = Resource.ErrorType.GENERIC
        )
    }

    /**
     * Exécute l'analyse d'une chaîne pour une clé API donnée, avec résolution multi-stratégie.
     */
    private suspend fun executeAnalysisWithKey(query: String, key: String): Resource<DashboardAnalysis> {
        return try {
            val queryType = ChannelQueryParser.parse(query)

            // Appel Réseau 1 : Résolution de la chaîne avec stratégie de secours intelligente
            val channelResponse = fetchChannelInfoWithFallback(queryType, key)

            if (!channelResponse.isSuccessful) {
                return handleHttpError(channelResponse.code(), channelResponse.errorBody()?.string())
            }

            val channelItems = channelResponse.body()?.items
            if (channelItems.isNullOrEmpty()) {
                return Resource.Error(
                    message = "Chaîne introuvable. Vérifiez l'URL ou le Handle saisi.",
                    errorType = Resource.ErrorType.CHANNEL_NOT_FOUND
                )
            }

            val channelDto = channelItems.first()
            val channelId = channelDto.id
            val snippet = channelDto.snippet
            val stats = channelDto.statistics
            val branding = channelDto.brandingSettings

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

            // Appel Réseau 2 : Récupération des vidéos récentes
            var processedVideos: List<VideoItem> = emptyList()

            if (!uploadsPlaylistId.isNullOrBlank()) {
                val playlistResponse = tryWithRetry {
                    apiService.getPlaylistItems(
                        playlistId = uploadsPlaylistId,
                        maxResults = 50,
                        apiKey = key
                    )
                }

                if (playlistResponse != null && playlistResponse.isSuccessful) {
                    val playlistItems = playlistResponse.body()?.items ?: emptyList()
                    val videoIds = playlistItems.mapNotNull {
                        it.contentDetails?.videoId ?: it.snippet?.resourceId?.videoId
                    }.filter { it.isNotBlank() }

                    if (videoIds.isNotEmpty()) {
                        // Appel Réseau 3 : Statistiques détaillées des vidéos
                        val idsParam = videoIds.joinToString(",")
                        val videosDetailsResponse = tryWithRetry {
                            apiService.getVideosDetails(
                                videoIds = idsParam,
                                apiKey = key
                            )
                        }

                        if (videosDetailsResponse != null && videosDetailsResponse.isSuccessful) {
                            val videoDetailsList = videosDetailsResponse.body()?.items ?: emptyList()
                            processedVideos = mapToVideoItems(videoDetailsList, channelId)
                        }
                    }
                }
            }

            // Calcul des champions et du Top 5
            val mostViewedVideo = processedVideos.maxByOrNull { it.viewCount }
            val mostCommentedVideo = processedVideos.maxByOrNull { it.commentCount }

            val top5Videos = processedVideos
                .sortedByDescending { it.viewCount }
                .take(5)
                .mapIndexed { index, video -> video.copy(rank = index + 1, isTop5 = true) }

            val top5Ids = top5Videos.map { it.videoId }.toSet()
            val finalVideos = processedVideos.map { video ->
                video.copy(
                    isTop5 = top5Ids.contains(video.videoId),
                    isMostViewed = video.videoId == mostViewedVideo?.videoId,
                    isMostCommented = video.videoId == mostCommentedVideo?.videoId
                )
            }

            // Sauvegarde dans la base locale Room
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
                message = "Erreur de connexion Internet : ${e.localizedMessage ?: "Vérifiez votre réseau."}",
                errorType = Resource.ErrorType.NETWORK_ERROR,
                cause = e
            )
        } catch (e: Exception) {
            Resource.Error(
                message = e.localizedMessage ?: "Une erreur inattendue est survenue.",
                errorType = Resource.ErrorType.GENERIC,
                cause = e
            )
        }
    }

    /**
     * Résolution robuste de la chaîne avec stratégie multi-essais :
     * 1. Requête selon le type déduit.
     * 2. Si handle échoue ou vide, essai avec/sans '@', puis par username.
     */
    private suspend fun fetchChannelInfoWithFallback(
        queryType: ChannelQueryType,
        apiKey: String
    ): Response<YouTubeChannelListResponse> {
        when (queryType) {
            is ChannelQueryType.ByChannelId -> {
                return apiService.getChannelById(channelId = queryType.channelId, apiKey = apiKey)
            }
            is ChannelQueryType.ByHandle -> {
                val cleanHandle = queryType.handle.removePrefix("@")
                // Essai 1 : handle propre
                var resp = apiService.getChannelByHandle(handle = cleanHandle, apiKey = apiKey)
                if (resp.isSuccessful && !resp.body()?.items.isNullOrEmpty()) {
                    return resp
                }
                // Essai 2 : handle avec @
                resp = apiService.getChannelByHandle(handle = "@$cleanHandle", apiKey = apiKey)
                if (resp.isSuccessful && !resp.body()?.items.isNullOrEmpty()) {
                    return resp
                }
                // Essai 3 : username legacy
                resp = apiService.getChannelByUsername(username = cleanHandle, apiKey = apiKey)
                if (resp.isSuccessful && !resp.body()?.items.isNullOrEmpty()) {
                    return resp
                }
                // Essai 4 : si 24 caractères commençant par UC
                if (cleanHandle.startsWith("UC") && cleanHandle.length == 24) {
                    resp = apiService.getChannelById(channelId = cleanHandle, apiKey = apiKey)
                    if (resp.isSuccessful && !resp.body()?.items.isNullOrEmpty()) {
                        return resp
                    }
                }
                return resp
            }
            is ChannelQueryType.ByUsername -> {
                var resp = apiService.getChannelByUsername(username = queryType.username, apiKey = apiKey)
                if (resp.isSuccessful && !resp.body()?.items.isNullOrEmpty()) {
                    return resp
                }
                // Fallback handle
                resp = apiService.getChannelByHandle(handle = queryType.username, apiKey = apiKey)
                return resp
            }
        }
    }

    /**
     * Construit la liste ordonnée et dédupliquée des clés API à tenter.
     */
    private suspend fun buildCandidateKeysList(preferredKey: String): List<String> {
        val candidates = mutableListOf<String>()

        if (preferredKey.isNotBlank()) {
            candidates.add(preferredKey.trim())
        }

        try {
            val defaultKey = apiKeyDao.getDefaultApiKey()
            if (defaultKey != null && defaultKey.apiKey.isNotBlank()) {
                candidates.add(defaultKey.apiKey.trim())
            }

            val allKeys = apiKeyDao.getAllApiKeysList()
            for (keyEntity in allKeys) {
                if (keyEntity.apiKey.isNotBlank()) {
                    candidates.add(keyEntity.apiKey.trim())
                }
            }
        } catch (_: Exception) {
            // Room access fail-safe
        }

        // Clé officielle built-in par défaut
        candidates.add(ApiKeyRepository.DEFAULT_BUILTIN_API_KEY)

        return candidates.distinct().filter { it.isNotBlank() }
    }

    /**
     * Recherche dans le cache Room local pour un fallback hors-ligne ou quota épuisé.
     */
    private suspend fun findInLocalCache(query: String): DashboardAnalysis? {
        val cleanQuery = query.trim()
        val queryType = ChannelQueryParser.parse(cleanQuery)

        val channelEntity = when (queryType) {
            is ChannelQueryType.ByChannelId -> channelDao.getChannelById(queryType.channelId)
            is ChannelQueryType.ByHandle -> channelDao.findChannelByQuery(queryType.handle.removePrefix("@"))
            is ChannelQueryType.ByUsername -> channelDao.findChannelByQuery(queryType.username)
        } ?: channelDao.findChannelByQuery(cleanQuery)

        return channelEntity?.let { buildDashboardFromCachedEntity(it) }
    }

    private suspend fun buildDashboardFromCachedEntity(channelEntity: ChannelEntity): DashboardAnalysis {
        val channelId = channelEntity.channelId
        val videoEntities = videoDao.getVideosListForChannel(channelId)

        val channel = channelEntity.toDomain()
        val videos = videoEntities.map { it.toDomain() }

        val top5 = videos.filter { it.isTop5 }.sortedByDescending { it.viewCount }.ifEmpty {
            videos.sortedByDescending { it.viewCount }.take(5)
        }.mapIndexed { index, item -> item.copy(rank = index + 1) }

        val mostViewed = videos.firstOrNull { it.isMostViewed }
            ?: videos.maxByOrNull { it.viewCount }

        val mostCommented = videos.firstOrNull { it.isMostCommented }
            ?: videos.maxByOrNull { it.commentCount }

        return DashboardAnalysis(
            channel = channel,
            top5Videos = top5,
            mostViewedVideo = mostViewed,
            mostCommentedVideo = mostCommented,
            isFromCache = true,
            analysisTimestamp = channelEntity.cachedAtTimestamp
        )
    }

    private suspend fun <T> tryWithRetry(maxAttempts: Int = 2, block: suspend () -> Response<T>): Response<T>? {
        var attempts = 0
        while (attempts < maxAttempts) {
            attempts++
            try {
                return block()
            } catch (e: IOException) {
                if (attempts >= maxAttempts) return null
                delay(300)
            } catch (_: Exception) {
                return null
            }
        }
        return null
    }

    override suspend fun deleteHistoryItem(channelId: String) = withContext(Dispatchers.IO) {
        channelDao.deleteChannelById(channelId)
    }

    override suspend fun clearHistory() = withContext(Dispatchers.IO) {
        channelDao.clearHistory()
    }

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

    private fun <T> handleHttpError(code: Int, errorBody: String?): Resource<T> {
        return when (code) {
            403 -> {
                if (errorBody?.contains("quotaExceeded", ignoreCase = true) == true ||
                    errorBody?.contains("dailyLimitExceeded", ignoreCase = true) == true ||
                    errorBody?.contains("rateLimitExceeded", ignoreCase = true) == true
                ) {
                    Resource.Error(
                        message = "Quota journalier YouTube dépassé pour cette clé API.",
                        errorType = Resource.ErrorType.QUOTA_EXCEEDED
                    )
                } else {
                    Resource.Error(
                        message = "Accès refusé par l'API YouTube (Vérifiez la clé ou les restrictions).",
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
