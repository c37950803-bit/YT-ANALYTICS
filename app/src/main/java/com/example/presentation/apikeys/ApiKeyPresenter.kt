package com.example.presentation.apikeys

import com.example.data.repository.ApiKeyRepository
import com.example.domain.model.ApiKey
import com.example.presentation.base.BasePresenter
import kotlinx.coroutines.launch

/**
 * Presenter pour la gestion des clés API YouTube (MVP).
 *
 * Explication métier :
 * Valide les entrées utilisateur (nom obligatoire, clé non vide),
 * interagit avec le Repository pour la persistance locale Room et
 * notifie la Vue des changements d'état.
 */
class ApiKeyPresenter(
    private val apiKeyRepository: ApiKeyRepository
) : BasePresenter<ApiKeyContract.View>(), ApiKeyContract.Presenter {

    override fun attachView(view: ApiKeyContract.View) {
        super.attachView(view)
        loadApiKeys()
    }

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

    override fun saveApiKey(id: Long, name: String, key: String, isDefault: Boolean) {
        // Validation des règles métier
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
                    apiKeyRepository.insertApiKey(name = name, key = key, isDefault = isDefault)
                    view?.onApiKeySavedSuccess("Clé API '$name' enregistrée avec succès !")
                } else {
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
