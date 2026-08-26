package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room représentant une clé API YouTube Data v3 stockée en base locale.
 *
 * Explication métier :
 * L'application nécessite que l'utilisateur fournisse sa propre clé API YouTube.
 * Pour permettre la flexibilité (ex: basculer entre plusieurs clés si le quota est dépassé),
 * nous stockons une collection de clés avec un nom mémorisable et un indicateur de clé par défaut.
 *
 * Explication technique :
 * - `id` : Identifiant unique auto-généré.
 * - `name` : Nom lisible donné par l'utilisateur (ex: "Clé Dev", "Clé Perso").
 * - `apiKey` : La chaîne de caractères de la clé API YouTube.
 * - `isDefault` : Indique si cette clé est la clé active utilisée par défaut pour les requêtes réseau.
 * - `createdAt` : Timestamp de création pour l'historique et le tri.
 */
@Entity(tableName = "api_keys")
data class ApiKeyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val apiKey: String,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
