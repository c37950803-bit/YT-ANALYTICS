package com.example.presentation.navigation

/**
 * Destinations de navigation de l'application.
 */
sealed class AppDestination(val route: String) {
    object Home : AppDestination("home")
    object ApiKeys : AppDestination("api_keys")
    object About : AppDestination("about")
    data class Dashboard(val query: String) : AppDestination("dashboard/$query")
    data class Player(val videoId: String, val title: String) : AppDestination("player/$videoId")
}
