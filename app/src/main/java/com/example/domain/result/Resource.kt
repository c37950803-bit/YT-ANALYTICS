package com.example.domain.result

/**
 * Wrapper générique scellé pour remonter les états de traitement (Succès, Erreur, Chargement)
 * du Model vers le Presenter, puis du Presenter vers la View.
 *
 * Explication technique :
 * Assure la sécurité de typage et force la gestion exhaustive des états d'erreur
 * (notamment le quota dépassé et les erreurs réseau).
 */
sealed class Resource<out T> {

    /**
     * État de chargement en cours avec message indicatif optionnel.
     */
    data class Loading(val message: String? = null) : Resource<Nothing>()

    /**
     * État de succès avec données embarquées.
     */
    data class Success<out T>(val data: T) : Resource<T>()

    /**
     * État d'erreur avec type d'erreur catégorisé.
     */
    data class Error(
        val message: String,
        val errorType: ErrorType = ErrorType.GENERIC,
        val cause: Throwable? = null
    ) : Resource<Nothing>()

    /**
     * Catégorisation métier des erreurs de l'application.
     */
    enum class ErrorType {
        NO_API_KEY,         // Aucune clé configurée
        QUOTA_EXCEEDED,     // Quota journalier YouTube dépassé (Erreur 403)
        INVALID_API_KEY,    // Clé API invalide ou non autorisée
        CHANNEL_NOT_FOUND,  // Chaîne introuvable
        NETWORK_ERROR,      // Problème de connexion réseau
        GENERIC             // Autre erreur inattendue
    }
}
