package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room représentant une chaîne YouTube consultée et mise en cache localement.
 *
 * Explication métier :
 * Constitue l'historique des chaînes analysées sur l'écran d'accueil ("LocalStorage").
 * Permet également la consultation hors-ligne avec un horodatage pour informer l'utilisateur
 * de la fraîcheur des données sans consommer inutilement de précieux quotas API YouTube.
 *
 * Explication technique :
 * - `channelId` : L'identifiant immuable YouTube (ex: UCX6OQ3DkcsbYNE6H8uQQuVA) servant de clé primaire.
 * - `title` : Nom de la chaîne.
 * - `customUrl` : Handle ou URL personnalisée (ex: @MrBeast).
 * - `description` : Courte biographie de la chaîne.
 * - `thumbnailUrl` : URL de l'avatar haute résolution.
 * - `bannerUrl` : URL de la bannière si disponible.
 * - `subscriberCount` : Nombre total d'abonnés formaté en nombre entier.
 * - `videoCount` : Nombre total de vidéos publiées.
 * - `viewCount` : Nombre total de vues cumulées.
 * - `uploadsPlaylistId` : Identifiant de la playlist des vidéos mises en ligne (essentiel pour récupérer les vidéos).
 * - `lastViewedTimestamp` : Date de dernière consultation pour l'ordre dans l'historique.
 * - `cachedAtTimestamp` : Date d'enregistrement pour le calcul de l'ancienneté du cache.
 */
@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey
    val channelId: String,
    val title: String,
    val customUrl: String? = null,
    val description: String? = null,
    val thumbnailUrl: String,
    val bannerUrl: String? = null,
    val subscriberCount: Long = 0L,
    val videoCount: Long = 0L,
    val viewCount: Long = 0L,
    val uploadsPlaylistId: String? = null,
    val lastViewedTimestamp: Long = System.currentTimeMillis(),
    val cachedAtTimestamp: Long = System.currentTimeMillis()
)
