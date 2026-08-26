package com.example.presentation.home

import com.example.data.repository.ApiKeyRepository
import com.example.data.repository.YouTubeRepository
import com.example.domain.model.ChannelDetails
import com.example.presentation.base.BasePresenter
import kotlinx.coroutines.launch

/**
 * Presenter de l'écran d'accueil (MVP).
 *
 * Explication métier :
 * - Charge l'historique des chaînes depuis Room.
 * - Le rafraîchissement (SwipeRefreshLayout) ne recharge que la base locale sans appel réseau,
 *   respectant ainsi la contrainte d'économie de quota imposée par le cahier des charges.
 * - Vérifie la présence d'une clé API valide avant de lancer une nouvelle recherche.
 */
class HomePresenter(
    private val youTubeRepository: YouTubeRepository,
    private val apiKeyRepository: ApiKeyRepository
) : BasePresenter<HomeContract.View>(), HomeContract.Presenter {

    override fun attachView(view: HomeContract.View) {
        super.attachView(view)
        loadHistory()
    }

    override fun loadHistory() {
        presenterScope.launch {
            view?.showLoading(true)
            try {
                youTubeRepository.getHistory().collect { channels ->
                    view?.showLoading(false)
                    if (channels.isEmpty()) {
                        view?.showEmptyHistory()
                    } else {
                        view?.displayHistory(channels)
                    }
                }
            } catch (e: Exception) {
                view?.showLoading(false)
                view?.showError(e.localizedMessage ?: "Erreur lors du chargement de l'historique.")
            }
        }
    }

    /**
     * Rafraîchit les données en consultant exclusivement la base locale Room.
     * Aucun quota réseau n'est consommé lors de cette action.
     */
    override fun refreshLocalHistory() {
        presenterScope.launch {
            view?.showLoading(true)
            try {
                // Le flux Room émet automatiquement les nouvelles données
                view?.showLoading(false)
            } catch (e: Exception) {
                view?.showLoading(false)
                view?.showError("Erreur lors de l'actualisation locale.")
            }
        }
    }

    override fun onSearchSubmitted(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            view?.showError("Veuillez saisir une URL ou un nom de chaîne YouTube.")
            return
        }

        presenterScope.launch {
            val defaultKey = apiKeyRepository.getDefaultApiKey()
            if (defaultKey == null) {
                view?.showApiKeyRequiredDialog()
                return@launch
            }
            view?.navigateToDashboard(trimmed)
        }
    }

    override fun onChannelSelected(channel: ChannelDetails) {
        view?.navigateToDashboard(channel.channelId)
    }

    override fun deleteHistoryChannel(channelId: String) {
        presenterScope.launch {
            try {
                youTubeRepository.deleteHistoryItem(channelId)
            } catch (e: Exception) {
                view?.showError("Impossible de supprimer cet élément.")
            }
        }
    }

    override fun clearHistory() {
        presenterScope.launch {
            try {
                youTubeRepository.clearHistory()
            } catch (e: Exception) {
                view?.showError("Erreur lors de la suppression de l'historique.")
            }
        }
    }
}
