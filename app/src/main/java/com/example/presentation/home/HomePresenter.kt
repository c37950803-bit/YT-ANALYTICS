package com.example.presentation.home

import com.example.data.repository.ApiKeyRepository
import com.example.data.repository.YouTubeRepository
import com.example.domain.model.ChannelDetails
import com.example.presentation.base.BasePresenter
import kotlinx.coroutines.launch

/**
 * =========================================================================================
 * 🧠 LE CERVEAU DE L'ACCUEIL : HomePresenter.kt (Architecture MVP)
 * =========================================================================================
 * 
 * 💡 EXPLICATION GRAND DÉBUTANT (Comment ça fonctionne ?) :
 * 
 * 1. QU'EST-CE QU'UN "PRESENTER" ?
 *    - Imagine un restaurant :
 *      - L'écran (HomeScreen) est le serveur qui sourit et montre la carte au client.
 *      - Le Presenter (ce fichier) est le chef cuisinier !
 *      - Le Repository (base de données et réseau) est le garde-manger.
 *    - Quand l'utilisateur clique sur "Analyser", HomeScreen envoie la commande à HomePresenter.
 *    - HomePresenter va chercher les ingrédients (données YouTube / base locale) et dit à
 *      HomeScreen : "C'est prêt ! Affiche ces informations à l'écran".
 * 
 * 2. C'EST QUOI UNE "COROUTINE" (`presenterScope.launch`) ?
 *    - Sur un smartphone, si tu télécharges des données depuis Internet sur le fil principal
 *      (le "UI Thread"), l'écran se fige et l'application bloque !
 *    - Les "Coroutines" sont des tâches en arrière-plan invisibles. Elles font le travail lourd
 *      silencieusement sans jamais ralentir l'animation de l'écran.
 * 
 * 3. POURQUOI LE RAFRAÎCHISSEMENT NE CONSOMME AUCUN QUOTA ?
 *    - L'API YouTube a une limite quotidienne (quota).
 *    - Pour protéger ce quota, quand l'utilisateur tire vers le bas ou clique sur "Rafraîchir"
 *      dans l'historique, le Presenter interroge UNIQUEMENT la mémoire locale du téléphone
 *      (la base Room / SQLite). Zéro appel réseau = 100% gratuit et instantané !
 */
class HomePresenter(
    private val youTubeRepository: YouTubeRepository,
    private val apiKeyRepository: ApiKeyRepository
) : BasePresenter<HomeContract.View>(), HomeContract.Presenter {

    /**
     * Appelé dès que l'écran s'ouvre : On attache la vue et on lit l'historique local.
     */
    override fun attachView(view: HomeContract.View) {
        super.attachView(view)
        loadHistory()
    }

    /**
     * 📥 Charge les chaînes YouTube mémorisées dans la base de données Room du téléphone.
     */
    override fun loadHistory() {
        presenterScope.launch {
            view?.showLoading(true)
            try {
                // Écoute en continu le flux de données Room (Flow)
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
     * 🔄 Rafraîchit l'affichage en consultant exclusivement la base locale Room.
     * Aucun quota réseau YouTube n'est consommé.
     */
    override fun refreshLocalHistory() {
        presenterScope.launch {
            view?.showLoading(true)
            try {
                // Le flux Room émet automatiquement les données à jour
                view?.showLoading(false)
            } catch (e: Exception) {
                view?.showLoading(false)
                view?.showError("Erreur lors de l'actualisation locale.")
            }
        }
    }

    /**
     * 🔎 Traite la soumission d'une recherche par l'utilisateur.
     * Vérifie d'abord si une clé API est présente avant de changer d'écran.
     */
    override fun onSearchSubmitted(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            view?.showError("Veuillez saisir une URL ou un nom de chaîne YouTube.")
            return
        }

        presenterScope.launch {
            // Vérification de la clé API
            val defaultKey = apiKeyRepository.getDefaultApiKey()
            if (defaultKey == null) {
                view?.showApiKeyRequiredDialog()
                return@launch
            }
            // Navigation vers le tableau de bord avec le mot-clé
            view?.navigateToDashboard(trimmed)
        }
    }

    /**
     * 👆 Clic sur une carte d'historique : ouvre directement le tableau de bord de cette chaîne.
     */
    override fun onChannelSelected(channel: ChannelDetails) {
        view?.navigateToDashboard(channel.channelId)
    }

    /**
     * 🗑️ Supprime une seule chaîne de la base de données locale du téléphone.
     */
    override fun deleteHistoryChannel(channelId: String) {
        presenterScope.launch {
            try {
                youTubeRepository.deleteHistoryItem(channelId)
            } catch (e: Exception) {
                view?.showError("Impossible de supprimer cet élément.")
            }
        }
    }

    /**
     * 🧹 Vide tout l'historique enregistré en local.
     */
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

