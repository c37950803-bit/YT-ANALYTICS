package com.example.domain.model

/**
 * Type de requête déduit de la saisie utilisateur.
 */
sealed class ChannelQueryType {
    data class ByChannelId(val channelId: String) : ChannelQueryType()
    data class ByHandle(val handle: String) : ChannelQueryType()
    data class ByUsername(val username: String) : ChannelQueryType()
}

/**
 * Utilitaire d'analyse de la saisie utilisateur (URL ou identifiant).
 *
 * Explication métier :
 * Les utilisateurs copient souvent directement l'URL d'une chaîne depuis leur navigateur ou
 * l'application YouTube (ex: "https://www.youtube.com/@mkbhd" ou "https://youtube.com/channel/UC...").
 * Cet utilitaire extrait automatiquement le bon identifiant et choisit l'endpoint API YouTube approprié.
 */
object ChannelQueryParser {

    /**
     * Analyse une chaîne d'entrée et retourne le type de requête correspondant.
     */
    fun parse(input: String): ChannelQueryType {
        val trimmed = input.trim()

        // 1. Détection d'URL YouTube
        if (trimmed.contains("youtube.com") || trimmed.contains("youtu.be")) {
            val cleanUrl = trimmed.replace("http://", "https://")

            // Format /channel/UC...
            val channelMatch = Regex(".*/channel/(UC[a-zA-Z0-9_-]{22})(/.*)?").find(cleanUrl)
            if (channelMatch != null) {
                return ChannelQueryType.ByChannelId(channelMatch.groupValues[1])
            }

            // Format /@handle
            val handleMatch = Regex(".*/@([a-zA-Z0-9_.-]+)(/.*)?").find(cleanUrl)
            if (handleMatch != null) {
                return ChannelQueryType.ByHandle(handleMatch.groupValues[1])
            }

            // Format /c/customName ou /user/username
            val cMatch = Regex(".*/c/([a-zA-Z0-9_.-]+)(/.*)?").find(cleanUrl)
            if (cMatch != null) {
                return ChannelQueryType.ByUsername(cMatch.groupValues[1])
            }

            val userMatch = Regex(".*/user/([a-zA-Z0-9_.-]+)(/.*)?").find(cleanUrl)
            if (userMatch != null) {
                return ChannelQueryType.ByUsername(userMatch.groupValues[1])
            }
        }

        // 2. Détection directe d'ID de chaîne commençant par UC (24 caractères)
        if (trimmed.startsWith("UC") && trimmed.length == 24) {
            return ChannelQueryType.ByChannelId(trimmed)
        }

        // 3. Détection de Handle avec @
        if (trimmed.startsWith("@")) {
            return ChannelQueryType.ByHandle(trimmed.removePrefix("@"))
        }

        // 4. Par défaut, si c'est un mot simple, on teste d'abord comme un Handle
        return ChannelQueryType.ByHandle(trimmed)
    }
}
