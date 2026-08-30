package com.example.presentation.dashboard

import com.example.data.repository.ApiKeyRepository
import com.example.data.repository.YouTubeRepository
import com.example.domain.model.VideoItem
import com.example.domain.result.Resource
import com.example.presentation.base.BasePresenter
import kotlinx.coroutines.launch

/**
 * =========================================================================================
 * 🧠 LE CERVEAU DE L'ANALYSE : DashboardPresenter.kt (Architecture MVP)
 * =========================================================================================
 * 
 * 💡 EXPLICATION POUR QUASI-DÉBUTANT (Comment se déroule une analyse YouTube ?) :
 * 
 * 1. QUELLE EST LA MISSION DU PRESENTER ICI ?
 *    - Quand l'utilisateur demande d'analyser une chaîne (ex: "@MrBeast") :
 *      1. Étape 1 (Cache Local) : Il regarde d'abord si on a déjà analysé cette chaîne récemment
 *         dans la base de données Room du smartphone. Si oui, l'affichage est instantané sans
 *         toucher à Internet ni au quota !
 *      2. Étape 2 (Clé API) : S'il faut aller sur Internet, il récupère la clé API YouTube active.
 *      3. Étape 3 (Appel Réseau) : Il appelle l'API YouTube v3 pour récupérer les statistiques,
 *         la playlist de vidéos, et calcule automatiquement le Top 5 et les champions.
 *      4. Étape 4 (Gestion des Erreurs) : Si le quota journalier YouTube est épuisé (erreur 403),
 *         il propose gentiment à l'utilisateur de basculer sur une autre clé.
 */
class DashboardPresenter(
    private val youTubeRepository: YouTubeRepository,
    private val apiKeyRepository: ApiKeyRepository
) : BasePresenter<DashboardContract.View>(), DashboardContract.Presenter {

    private var currentQuery: String = ""

    /**
     * 🚀 Lance l'analyse d'une chaîne (par URL, Handle @ ou identifiant UC...)
     */
    override fun analyzeChannel(queryOrChannelId: String, forceNetworkRefresh: Boolean) {
        currentQuery = queryOrChannelId

        presenterScope.launch {
            // 1. Consultation préalable du cache Room local (si ce n'est pas un rafraîchissement forcé)
            if (!forceNetworkRefresh && queryOrChannelId.startsWith("UC") && queryOrChannelId.length == 24) {
                val cached = youTubeRepository.getCachedAnalysis(queryOrChannelId)
                if (cached != null) {
                    view?.displayDashboard(cached)
                    return@launch
                }
            }

            // 2. Récupération de la clé API YouTube active
            val defaultKey = apiKeyRepository.getDefaultApiKey()
            if (defaultKey == null) {
                view?.showInvalidApiKeyFallback("Aucune clé API configurée. Veuillez ajouter une clé dans le gestionnaire.")
                return@launch
            }

            performNetworkAnalysis(queryOrChannelId, defaultKey.apiKey)
        }
    }

    /**
     * 🔄 Permet de retenter l'analyse immédiatement avec une nouvelle clé API sélectionnée
     */
    override fun retryAnalysisWithApiKey(apiKey: String) {
        if (currentQuery.isNotBlank()) {
            presenterScope.launch {
                performNetworkAnalysis(currentQuery, apiKey)
            }
        }
    }

    /**
     * 🌐 Effectue les requêtes vers l'API officielle YouTube en arrière-plan
     */
    private suspend fun performNetworkAnalysis(query: String, apiKey: String) {
        view?.showLoading(true)
        val result = youTubeRepository.analyzeChannel(query = query, apiKey = apiKey)
        view?.showLoading(false)

        when (result) {
            is Resource.Success -> {
                // Tout s'est bien passé : On transmet les résultats complets à la vue
                view?.displayDashboard(result.data)
            }
            is Resource.Error -> {
                // Traitement intelligent des cas d'erreurs
                when (result.errorType) {
                    Resource.ErrorType.QUOTA_EXCEEDED -> {
                        // Quota journalier YouTube atteint
                        view?.showQuotaFallback(result.message)
                    }
                    Resource.ErrorType.INVALID_API_KEY, Resource.ErrorType.NO_API_KEY -> {
                        // Clé API invalide ou manquante
                        view?.showInvalidApiKeyFallback(result.message)
                    }
                    Resource.ErrorType.CHANNEL_NOT_FOUND -> {
                        // Chaîne introuvable
                        view?.showChannelNotFoundState(result.message)
                    }
                    Resource.ErrorType.NETWORK_ERROR, Resource.ErrorType.GENERIC -> {
                        // Erreur réseau générique
                        view?.showError(result.message)
                    }
                }
            }
            is Resource.Loading -> {
                view?.showLoading(true)
            }
        }
    }

    /**
     * ▶️ Clic sur "Lire" : Ouvre le lecteur officiel Mini-YouTube intégré
     */
    override fun onVideoPlayClicked(video: VideoItem) {
        view?.openPlayerScreen(video.videoId, video.title)
    }

    /**
     * 📱 Ouvre l'application officielle YouTube installée sur le téléphone
     */
    override fun onVideoExternalPlayClicked(video: VideoItem) {
        view?.openExternalYouTubeApp(video.videoId)
    }
}

