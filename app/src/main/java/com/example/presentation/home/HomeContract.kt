package com.example.presentation.home

import com.example.domain.model.ChannelDetails
import com.example.presentation.base.BaseView

/**
 * Contrat MVP pour l'Écran d'Accueil (Historique local "LocalStorage" + Barre de recherche).
 */
interface HomeContract {

    interface View : BaseView {
        fun displayHistory(history: List<ChannelDetails>)
        fun showEmptyHistory()
        fun navigateToDashboard(channelId: String)
        fun showApiKeyRequiredDialog()
        fun showQuotaFallbackDialog(errorMessage: String)
    }

    interface Presenter {
        fun loadHistory()
        fun refreshLocalHistory()
        fun onSearchSubmitted(query: String)
        fun onChannelSelected(channel: ChannelDetails)
        fun deleteHistoryChannel(channelId: String)
        fun clearHistory()
    }
}
