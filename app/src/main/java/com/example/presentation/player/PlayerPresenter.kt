package com.example.presentation.player

import com.example.presentation.base.BasePresenter

/**
 * Presenter pour le Lecteur Vidéo (Mini YouTube Officiel - MVP).
 */
class PlayerPresenter : BasePresenter<PlayerContract.View>(), PlayerContract.Presenter {

    private var currentVideoId: String = ""
    private var currentTitle: String = ""
    private var isDirectWebMode: Boolean = false

    override fun setupVideo(videoId: String, title: String) {
        currentVideoId = videoId
        currentTitle = title
        isDirectWebMode = false
        view?.initializePlayer(videoId, title)
    }

    override fun onExternalButtonClicked() {
        if (currentVideoId.isNotBlank()) {
            view?.launchExternalYouTube(currentVideoId)
        }
    }

    override fun togglePlaybackMode(useDirectWeb: Boolean) {
        isDirectWebMode = useDirectWeb
        view?.setPlaybackMode(useDirectWeb)
    }
}

