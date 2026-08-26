package com.example.domain.model

/**
 * Modèle de domaine représentant une clé API configurée dans l'application.
 */
data class ApiKey(
    val id: Long = 0L,
    val name: String,
    val apiKey: String,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
