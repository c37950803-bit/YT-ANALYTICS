package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * =========================================================================================
 * 🎭 FICHIER DU THÈME PRINCIPAL (Theme.kt)
 * =========================================================================================
 * 
 * 💡 POURQUOI CE FICHIER EXISTE ? (Pour débutants complets) :
 * Imagine que ton application Android est une maison. Ce fichier est l'architecte et le
 * décorateur d'intérieur ! C'est lui qui dit :
 * - "Quand le téléphone est en mode sombre, voici les couleurs à appliquer automatiquement".
 * - "Quand le téléphone est en mode clair, voici les couleurs lumineuses".
 * 
 * 🛠️ COMMENT ÇA MARCHE (Jetpack Compose) :
 * `MaterialTheme` est le cœur du design de Google (Material Design 3). En englobant nos
 * écrans dans `MyApplicationTheme`, chaque bouton, texte et carte sait exactement quelle
 * couleur adopter sans qu'on ait besoin de lui répéter à chaque fois !
 */

// 🌙 CONFIGURATION DU MODE SOMBRE (Minimaliste Dark)
private val DarkColorScheme = darkColorScheme(
    primary = MinimalistPrimaryDark,
    onPrimary = MinimalistOnPrimaryDark,
    primaryContainer = MinimalistPrimaryContainerDark,
    onPrimaryContainer = MinimalistOnPrimaryContainerDark,
    secondary = YouTubeRed,
    onSecondary = Color.White,
    secondaryContainer = MinimalistSurfaceVariantDark,
    onSecondaryContainer = MinimalistTextPrimaryDark,
    tertiary = AccentGreen,
    onTertiary = Color.White,
    tertiaryContainer = AccentGreenContainer,
    onTertiaryContainer = AccentGreenOnContainer,
    background = MinimalistBackgroundDark,
    onBackground = MinimalistTextPrimaryDark,
    surface = MinimalistSurfaceDark,
    onSurface = MinimalistTextPrimaryDark,
    surfaceVariant = MinimalistSurfaceVariantDark,
    onSurfaceVariant = MinimalistTextSecondaryDark,
    outline = MinimalistBorderDark,
    outlineVariant = MinimalistBorderSubtleDark
)

// ☀️ CONFIGURATION DU MODE CLAIR (Minimaliste Light)
private val LightColorScheme = lightColorScheme(
    primary = MinimalistPrimaryLight,
    onPrimary = MinimalistOnPrimaryLight,
    primaryContainer = MinimalistPrimaryContainerLight,
    onPrimaryContainer = MinimalistOnPrimaryContainerLight,
    secondary = YouTubeRed,
    onSecondary = Color.White,
    secondaryContainer = YouTubeRedContainer,
    onSecondaryContainer = YouTubeRedOnContainer,
    tertiary = AccentGreen,
    onTertiary = Color.White,
    tertiaryContainer = AccentGreenContainer,
    onTertiaryContainer = AccentGreenOnContainer,
    background = MinimalistBackgroundLight,
    onBackground = MinimalistTextPrimaryLight,
    surface = MinimalistSurfaceLight,
    onSurface = MinimalistTextPrimaryLight,
    surfaceVariant = MinimalistSurfaceVariantLight,
    onSurfaceVariant = MinimalistTextSecondaryLight,
    outline = MinimalistBorderLight,
    outlineVariant = MinimalistBorderSubtleLight
)

/**
 * 🌟 COMPOSABLE DU THÈME GLOBAL :
 * C'est cette fonction qui entoure toute l'application dans `MainActivity.kt`.
 * 
 * @param darkTheme : Détecte automatiquement si le téléphone de l'utilisateur est en mode Nuit.
 * @param dynamicColor : Désactivé pour garantir le design minimaliste soigné et cohérent.
 * @param content : Le code des écrans de l'application qui seront affichés à l'intérieur.
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


