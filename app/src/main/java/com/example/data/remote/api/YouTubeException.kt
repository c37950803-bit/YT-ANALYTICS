package com.example.data.remote.api

/**
 * Hiérarchie scellée d'exceptions pour typer avec précision les erreurs YouTube Data API v3.
 *
 * Explication métier :
 * L'API YouTube impose des contraintes de quotas strictes (10 000 unités/jour).
 * Typer les erreurs permet au Presenter de déclencher le fallback élégant (dialogue de changement de clé)
 * ou d'afficher des explications claires et compréhensibles en français.
 */
sealed class YouTubeApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /**
     * Erreur 403 : Quota journalier dépassé pour la clé API en cours d'utilisation.
     */
    class QuotaExceededException(
        message: String = "Le quota journalier de cette clé API YouTube a été dépassé."
    ) : YouTubeApiException(message)

    /**
     * Erreur 400 ou 403 : Clé API invalide, révoquée ou non configurée pour YouTube Data API v3.
     */
    class InvalidApiKeyException(
        message: String = "La clé API YouTube fournie est invalide ou non autorisée."
    ) : YouTubeApiException(message)

    /**
     * Erreur 404 : La chaîne spécifiée n'a pas été trouvée sur YouTube.
     */
    class ChannelNotFoundException(
        message: String = "Aucune chaîne YouTube trouvée pour cet identifiant ou cette URL."
    ) : YouTubeApiException(message)

    /**
     * Erreur de connectivité réseau (pas d'Internet, timeout, DNS).
     */
    class NetworkException(
        message: String = "Impossible de joindre les serveurs YouTube. Vérifiez votre connexion Internet.",
        cause: Throwable? = null
    ) : YouTubeApiException(message, cause)

    /**
     * Erreur générique inattendue.
     */
    class GenericApiException(
        message: String,
        cause: Throwable? = null
    ) : YouTubeApiException(message, cause)
}
