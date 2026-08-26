package com.example.presentation.dashboard

import com.example.data.repository.ApiKeyRepository
import com.example.data.repository.YouTubeRepository
import com.example.domain.model.VideoItem
import com.example.domain.result.Resource
import com.example.presentation.base.BasePresenter
import kotlinx.coroutines.launch

/**
 * Presenter pour le Dashboard d'analyse de chaîne YouTube (MVP).
 *
 * Explication métier :
 * - Orchestre les étapes d'analyse :
 *   1. Vérifie si des données récentes sont disponibles en cache local Room pour consultation hors-ligne instantanée.
 *   2. Si non ou si rafraîchissement demandé, effectue les appels réseau via YouTube Data API v3.
 *   3. En cas d'erreur de quota (403), intercepte l'erreur et déclenche le popup de fallback élégant.
 * - Calcule et prépare les structures de données pour les 3 blocs de l'UI :
 *   - Bloc 1 : Statistiques globales (abonnés, vues, vidéos).
 *   - Bloc 2 : Les deux Champions (La plus regardée, La plus commentée).
 *   - Bloc 3 : Le Top 5 des vidéos les plus vues avec miniatures et métriques.
 */
class DashboardPresenter(
    private val youTubeRepository: YouTubeRepository,
    private val apiKeyRepository: ApiKeyRepository
) : BasePresenter<DashboardContract.View>(), DashboardContract.Presenter {

    private var currentQuery: String = ""

    override fun analyzeChannel(queryOrChannelId: String, forceNetworkRefresh: Boolean) {
        currentQuery = queryOrChannelId

        presenterScope.launch {
            // 1. Consultation préalable du cache Room si ce n'est pas un refresh forcé
            if (!forceNetworkRefresh && queryOrChannelId.startsWith("UC") && queryOrChannelId.length == 24) {
                val cached = youTubeRepository.getCachedAnalysis(queryOrChannelId)
                if (cached != null) {
                    view?.displayDashboard(cached)
                    return@launch
                }
            }

            // 2. Récupération de la clé API active
            val defaultKey = apiKeyRepository.getDefaultApiKey()
            if (defaultKey == null) {
                view?.showInvalidApiKeyFallback("Aucune clé API configurée. Veuillez ajouter une clé dans le gestionnaire.")
                return@launch
            }

            performNetworkAnalysis(queryOrChannelId, defaultKey.apiKey)
        }
    }

    override fun retryAnalysisWithApiKey(apiKey: String) {
        if (currentQuery.isNotBlank()) {
            presenterScope.launch {
                performNetworkAnalysis(currentQuery, apiKey)
            }
        }
    }

    private suspend fun performNetworkAnalysis(query: String, apiKey: String) {
        view?.showLoading(true)
        val result = youTubeRepository.analyzeChannel(query = query, apiKey = apiKey)
        view?.showLoading(false)

        when (result) {
            is Resource.Success -> {
                view?.displayDashboard(result.data)
            }
            is Resource.Error -> {
                when (result.errorType) {
                    Resource.ErrorType.QUOTA_EXCEEDED -> {
                        view?.showQuotaFallback(result.message)
                    }
                    Resource.ErrorType.INVALID_API_KEY, Resource.ErrorType.NO_API_KEY -> {
                        view?.showInvalidApiKeyFallback(result.message)
                    }
                    Resource.ErrorType.CHANNEL_NOT_FOUND -> {
                        view?.showChannelNotFoundState(result.message)
                    }
                    Resource.ErrorType.NETWORK_ERROR, Resource.ErrorType.GENERIC -> {
                        view?.showError(result.message)
                    }
                }
            }
            is Resource.Loading -> {
                view?.showLoading(true)
            }
        }
    }

    override fun onVideoPlayClicked(video: VideoItem) {
        view?.openPlayerScreen(video.videoId, video.title)
    }

    override fun onVideoExternalPlayClicked(video: VideoItem) {
        view?.openExternalYouTubeApp(video.videoId)
    }
}
