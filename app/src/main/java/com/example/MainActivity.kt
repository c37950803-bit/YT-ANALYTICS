package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.presentation.navigation.MainAppScaffold
import com.example.ui.theme.MyApplicationTheme

/**
 * Activité principale de l'application YouTube Analytics.
 *
 * Explication technique :
 * - Point d'entrée de l'application Android.
 * - Récupère le conteneur de dépendances depuis la classe Application (`App`).
 * - Active le mode Edge-to-Edge pour une interface immersive moderne.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as App).container

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainAppScaffold(container = appContainer)
                }
            }
        }
    }
}
