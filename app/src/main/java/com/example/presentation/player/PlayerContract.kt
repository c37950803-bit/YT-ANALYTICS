package com.example.presentation.player

import com.example.presentation.base.BaseView

/**
 * Contrat MVP pour le Lecteur Vidéo intégré (Mini YouTube Officiel).
 */
interface PlayerContract {

    interface View : BaseView {
        fun initializePlayer(videoId: String, title: String)
        fun launchExternalYouTube(videoId: String)
        fun setPlaybackMode(useDirectWeb: Boolean)
    }

    interface Presenter {
        fun setupVideo(videoId: String, title: String)
        fun onExternalButtonClicked()
        fun togglePlaybackMode(useDirectWeb: Boolean)
    }
}

