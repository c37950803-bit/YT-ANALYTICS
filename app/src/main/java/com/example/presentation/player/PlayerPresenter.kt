package com.example.presentation.player

import com.example.presentation.base.BasePresenter

/**
 * Presenter pour le Lecteur Vidéo (MVP).
 */
class PlayerPresenter : BasePresenter<PlayerContract.View>(), PlayerContract.Presenter {

    private var currentVideoId: String = ""
    private var currentTitle: String = ""

    override fun setupVideo(videoId: String, title: String) {
        currentVideoId = videoId
        currentTitle = title
        view?.initializePlayer(videoId, title)
    }

    override fun onExternalButtonClicked() {
        if (currentVideoId.isNotBlank()) {
            view?.launchExternalYouTube(currentVideoId)
        }
    }
}
