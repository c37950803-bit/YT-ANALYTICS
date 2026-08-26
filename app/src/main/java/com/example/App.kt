package com.example

import android.app.Application
import com.example.di.AppContainer
import com.example.di.DefaultAppContainer

/**
 * Classe Application principale du projet.
 *
 * Explication technique :
 * Initialise le conteneur d'injection de dépendances au démarrage du processus de l'application.
 */
class App : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
