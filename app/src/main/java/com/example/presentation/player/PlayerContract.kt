package com.example.presentation.player

import com.example.presentation.base.BaseView

/**
 * Contrat MVP pour le Lecteur Vidéo intégré.
 */
interface PlayerContract {

    interface View : BaseView {
        fun initializePlayer(videoId: String, title: String)
        fun launchExternalYouTube(videoId: String)
    }

    interface Presenter {
        fun setupVideo(videoId: String, title: String)
        fun onExternalButtonClicked()
    }
}
