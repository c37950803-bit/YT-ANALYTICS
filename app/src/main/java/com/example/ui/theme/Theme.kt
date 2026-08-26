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

private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimaryDark,
    onPrimary = PurpleOnPrimaryDark,
    primaryContainer = PurplePrimaryContainerDark,
    onPrimaryContainer = PurpleOnPrimaryContainerDark,
    secondary = PurpleSecondaryContainer,
    onSecondary = PurpleOnSecondaryContainer,
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = PurpleTertiaryContainer,
    onTertiary = PurpleOnTertiaryContainer,
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    background = PolishBackgroundDark,
    onBackground = PolishTextPrimaryDark,
    surface = PolishSurfaceDark,
    onSurface = PolishTextPrimaryDark,
    surfaceVariant = PolishSurfaceVariantDark,
    onSurfaceVariant = PolishTextSecondaryDark,
    outline = PolishOutlineDark,
    outlineVariant = PolishOutlineVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = PurpleOnPrimary,
    primaryContainer = PurplePrimaryContainer,
    onPrimaryContainer = PurpleOnPrimaryContainer,
    secondary = PurpleSecondary,
    onSecondary = PurpleOnSecondary,
    secondaryContainer = PurpleSecondaryContainer,
    onSecondaryContainer = PurpleOnSecondaryContainer,
    tertiary = PurpleTertiary,
    onTertiary = PurpleOnTertiary,
    tertiaryContainer = PurpleTertiaryContainer,
    onTertiaryContainer = PurpleOnTertiaryContainer,
    background = PolishBackgroundLight,
    onBackground = PolishTextPrimaryLight,
    surface = PolishSurfaceLight,
    onSurface = PolishTextPrimaryLight,
    surfaceVariant = PolishSurfaceVariantLight,
    onSurfaceVariant = PolishTextSecondaryLight,
    outline = PolishOutlineLight,
    outlineVariant = PolishOutlineVariantLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep Professional Polish theme consistent
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

