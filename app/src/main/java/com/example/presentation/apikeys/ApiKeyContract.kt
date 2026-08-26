package com.example.presentation.apikeys

import com.example.domain.model.ApiKey
import com.example.presentation.base.BaseView

/**
 * Contrat MVP pour l'écran de Gestion des Clés API.
 */
interface ApiKeyContract {

    interface View : BaseView {
        fun displayApiKeys(keys: List<ApiKey>)
        fun onApiKeySavedSuccess(message: String)
        fun onApiKeyDeletedSuccess()
        fun showKeyValidationError(message: String)
    }

    interface Presenter {
        fun loadApiKeys()
        fun saveApiKey(id: Long = 0L, name: String, key: String, isDefault: Boolean)
        fun deleteApiKey(apiKey: ApiKey)
        fun setAsDefault(apiKeyId: Long)
    }
}
