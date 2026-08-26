package com.example.presentation.dashboard

import com.example.domain.model.DashboardAnalysis
import com.example.domain.model.VideoItem
import com.example.presentation.base.BaseView

/**
 * Contrat MVP pour l'écran de Résultats et Dashboard d'analyse.
 */
interface DashboardContract {

    interface View : BaseView {
        fun displayDashboard(analysis: DashboardAnalysis)
        fun showQuotaFallback(errorMessage: String)
        fun showInvalidApiKeyFallback(errorMessage: String)
        fun showChannelNotFoundState(errorMessage: String)
        fun openPlayerScreen(videoId: String, title: String)
        fun openExternalYouTubeApp(videoId: String)
    }

    interface Presenter {
        fun analyzeChannel(queryOrChannelId: String, forceNetworkRefresh: Boolean = false)
        fun onVideoPlayClicked(video: VideoItem)
        fun onVideoExternalPlayClicked(video: VideoItem)
        fun retryAnalysisWithApiKey(apiKey: String)
    }
}
