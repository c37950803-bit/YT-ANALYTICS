package com.example.presentation.apikeys

import com.example.data.repository.ApiKeyRepository
import com.example.domain.model.ApiKey
import com.example.presentation.base.BasePresenter
import kotlinx.coroutines.launch

/**
 * =========================================================================================
 * 🧠 LE CERVEAU DE LA GESTION DES CLÉS : ApiKeyPresenter.kt (Architecture MVP)
 * =========================================================================================
 * 
 * 💡 EXPLICATION POUR GRAND DÉBUTANT :
 * 
 * 1. QUELLE EST SA MISSION ?
 *    - Il sert d'arbitre et de garde du corps entre ce que tape l'utilisateur à l'écran
 *      et la base de données Room du smartphone.
 * 
 * 2. LES RÈGLES DE VALIDATION QU'IL CONTRÔLE :
 *    - Le nom ne doit pas être vide (ex: "Clé Perso").
 *    - La clé ne doit pas être vide.
 *    - La clé doit comporter au moins 20 caractères (une vraie clé YouTube Google fait ~39 caractères).
 * 
 * 3. SAUVEGARDE ET SYNCHRONISATION :
 *    - Il utilise les Coroutines Kotlin (`presenterScope.launch`) pour enregistrer, modifier,
 *      supprimer ou définir la clé active par défaut de façon ultra fluide et non-bloquante.
 */
class ApiKeyPresenter(
    private val apiKeyRepository: ApiKeyRepository
) : BasePresenter<ApiKeyContract.View>(), ApiKeyContract.Presenter {

    /**
     * Quand l'écran s'affiche, on commence directement par charger la liste des clés existantes
     */
    override fun attachView(view: ApiKeyContract.View) {
        super.attachView(view)
        loadApiKeys()
    }

    /**
     * 📥 Charge le flux continu de clés depuis la base de données Room
     */
    override fun loadApiKeys() {
        presenterScope.launch {
            view?.showLoading(true)
            try {
                apiKeyRepository.getAllApiKeys().collect { keys ->
                    view?.showLoading(false)
                    view?.displayApiKeys(keys)
                }
            } catch (e: Exception) {
                view?.showLoading(false)
                view?.showError(e.localizedMessage ?: "Erreur lors du chargement des clés API.")
            }
        }
    }

    /**
     * 💾 Sauvegarde une nouvelle clé ou met à jour une clé existante
     */
    override fun saveApiKey(id: Long, name: String, key: String, isDefault: Boolean) {
        // Validation des règles métier (pour éviter que l'utilisateur n'enregistre n'importe quoi)
        if (name.isBlank()) {
            view?.showKeyValidationError("Le nom de la clé est obligatoire (ex: 'Clé Perso').")
            return
        }
        if (key.isBlank()) {
            view?.showKeyValidationError("Veuillez renseigner votre clé API YouTube.")
            return
        }
        if (key.trim().length < 20) {
            view?.showKeyValidationError("Format de clé API YouTube suspect (trop court).")
            return
        }

        presenterScope.launch {
            view?.showLoading(true)
            try {
                if (id == 0L) {
                    // C'est une nouvelle clé
                    apiKeyRepository.insertApiKey(name = name, key = key, isDefault = isDefault)
                    view?.onApiKeySavedSuccess("Clé API '$name' enregistrée avec succès !")
                } else {
                    // C'est la modification d'une clé existante
                    apiKeyRepository.updateApiKey(id = id, name = name, key = key, isDefault = isDefault)
                    view?.onApiKeySavedSuccess("Clé API '$name' mise à jour avec succès !")
                }
            } catch (e: Exception) {
                view?.showError(e.localizedMessage ?: "Erreur lors de la sauvegarde de la clé API.")
            } finally {
                view?.showLoading(false)
            }
        }
    }

    /**
     * 🗑️ Supprime définitivement une clé de la base de données locale
     */
    override fun deleteApiKey(apiKey: ApiKey) {
        presenterScope.launch {
            try {
                apiKeyRepository.deleteApiKey(apiKey)
                view?.onApiKeyDeletedSuccess()
            } catch (e: Exception) {
                view?.showError("Impossible de supprimer la clé API.")
            }
        }
    }

    /**
     * ⭐ Définit quelle clé sera utilisée par défaut pour les analyses
     */
    override fun setAsDefault(apiKeyId: Long) {
        presenterScope.launch {
            try {
                apiKeyRepository.setDefaultApiKey(apiKeyId)
            } catch (e: Exception) {
                view?.showError("Erreur lors de la définition de la clé par défaut.")
            }
        }
    }
}

