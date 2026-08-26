package com.example.presentation.base

/**
 * Interface racine pour toutes les Vues du pattern MVP.
 *
 * Explication métier :
 * La Vue se limite strictement à l'affichage des informations et à la capture
 * des événements utilisateur. Elle ne contient aucune logique de décision.
 */
interface BaseView {
    /**
     * Affiche un message d'erreur générique (ex: Toast ou Snackbar).
     */
    fun showError(message: String)

    /**
     * Affiche l'indicateur de chargement.
     */
    fun showLoading(isLoading: Boolean)
}
