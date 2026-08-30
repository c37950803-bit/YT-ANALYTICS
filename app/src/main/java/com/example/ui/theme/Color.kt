package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * =========================================================================================
 * 🎨 FICHIER DE DÉFINITION DES COULEURS (Color.kt)
 * =========================================================================================
 * 
 * 💡 POURQUOI CE FICHIER EXISTE ? (Explication grand débutant) :
 * En informatique et en développement d'applications Android, on ne tape pas des codes
 * de couleurs au hasard partout dans nos écrans. On rassemble toutes nos couleurs au même
 * endroit ! C'est comme la palette de peinture d'un artiste.
 * 
 * 🎨 COMMENT ÇA MARCHE ? :
 * - `Color(0xFF...)` : Le "0x" signifie qu'on utilise le système hexadécimal (la langue des ordinateurs pour les couleurs).
 * - "FF" au début : L'opacité (100% visible, pas transparent).
 * - Les 6 lettres/chiffres suivants : Le mélange de Rouge, Vert et Bleu (RGB).
 * 
 * 🌿 STYLE CHOISI : "Minimaliste Moderne & Épuré"
 * Une esthétique raffinée, sobre, aérée, avec des tons neutres élégants (noir profond,
 * blanc pur, gris doux) et une touche signature de Rouge YouTube (#FF0000).
 */

// -------------------------------------------------------------------------
// 1. COULEURS DU THÈME CLAIR (Light Mode - Minimaliste & Lumineux)
// -------------------------------------------------------------------------
// Fond de l'application : Un blanc cassé très doux et reposant pour les yeux
val MinimalistBackgroundLight = Color(0xFFF8F9FA)

// Cartes et surfaces : Blanc pur pour créer un relief subtil sur le fond
val MinimalistSurfaceLight = Color(0xFFFFFFFF)
val MinimalistSurfaceVariantLight = Color(0xFFF1F3F5)

// Textes : Des contrastes précis pour une lisibilité parfaite
val MinimalistTextPrimaryLight = Color(0xFF111827)    // Noir doux (titres et textes importants)
val MinimalistTextSecondaryLight = Color(0xFF4B5563)  // Gris moyen (descriptions, sous-titres)
val MinimalistTextTertiaryLight = Color(0xFF9CA3AF)   // Gris clair (petites dates, tags discrets)

// Bordures ultra-fines (le secret du style minimaliste moderne)
val MinimalistBorderLight = Color(0xFFE5E7EB)
val MinimalistBorderSubtleLight = Color(0xFFF3F4F6)

// Couleur primaire : Noir profond intemporel
val MinimalistPrimaryLight = Color(0xFF0F172A)
val MinimalistOnPrimaryLight = Color(0xFFFFFFFF)
val MinimalistPrimaryContainerLight = Color(0xFFF1F5F9)
val MinimalistOnPrimaryContainerLight = Color(0xFF0F172A)

// -------------------------------------------------------------------------
// 2. COULEURS DU THÈME SOMBRE (Dark Mode - Minimaliste & Sombre OLED)
// -------------------------------------------------------------------------
// Fond de l'application en mode nuit : Noir profond
val MinimalistBackgroundDark = Color(0xFF0D0F12)
val MinimalistSurfaceDark = Color(0xFF16191E)
val MinimalistSurfaceVariantDark = Color(0xFF1E2229)

val MinimalistTextPrimaryDark = Color(0xFFF9FAFB)
val MinimalistTextSecondaryDark = Color(0xFF9CA3AF)
val MinimalistTextTertiaryDark = Color(0xFF6B7280)

val MinimalistBorderDark = Color(0xFF262B33)
val MinimalistBorderSubtleDark = Color(0xFF1A1D23)

val MinimalistPrimaryDark = Color(0xFFF8FAFC)
val MinimalistOnPrimaryDark = Color(0xFF0F172A)
val MinimalistPrimaryContainerDark = Color(0xFF1E293B)
val MinimalistOnPrimaryContainerDark = Color(0xFFF8FAFC)

// -------------------------------------------------------------------------
// 3. TOUCHES D'ACCENT & COULEURS FONCTIONNELLES
// -------------------------------------------------------------------------
// Rouge signature YouTube officiel
val YouTubeRed = Color(0xFFFF0000)
val YouTubeRedContainer = Color(0xFFFFEBEB)
val YouTubeRedOnContainer = Color(0xFF7A0000)

// Statut "En ligne" / Succès (Vert émeraude)
val AccentGreen = Color(0xFF10B981)
val AccentGreenActive = Color(0xFF10B981)
val AccentGreenContainer = Color(0xFFD1FAE5)
val AccentGreenOnContainer = Color(0xFF065F46)

// Statut Avertissement / Info (Ambre chaud)
val AccentAmber = Color(0xFFF59E0B)
val AccentAmberContainer = Color(0xFFFEF3C7)
val AccentAmberOnContainer = Color(0xFF92400E)

// Bleu subtil
val AccentBlue = Color(0xFF2563EB)
val AccentBlueContainer = Color(0xFFDBEAFE)

// Rétrocompatibilité avec les anciens composants de l'application
val PurplePrimary = MinimalistPrimaryLight
val PurpleOnPrimary = MinimalistOnPrimaryLight
val PurplePrimaryContainer = MinimalistPrimaryContainerLight
val PurpleOnPrimaryContainer = MinimalistOnPrimaryContainerLight
val PurpleSecondary = Color(0xFF475569)
val PurpleOnSecondary = Color(0xFFFFFFFF)
val PurpleSecondaryContainer = Color(0xFFE2E8F0)
val PurpleOnSecondaryContainer = Color(0xFF1E293B)
val PurpleTertiary = YouTubeRed
val PurpleOnTertiary = Color(0xFFFFFFFF)
val PurpleTertiaryContainer = YouTubeRedContainer
val PurpleOnTertiaryContainer = YouTubeRedOnContainer
val PolishBackgroundLight = MinimalistBackgroundLight
val PolishSurfaceLight = MinimalistSurfaceLight
val PolishSurfaceVariantLight = MinimalistSurfaceVariantLight
val PolishOutlineLight = MinimalistBorderLight
val PolishOutlineVariantLight = MinimalistBorderSubtleLight
val PolishTextPrimaryLight = MinimalistTextPrimaryLight
val PolishTextSecondaryLight = MinimalistTextSecondaryLight
val PolishTextTertiaryLight = MinimalistTextTertiaryLight
val PolishBackgroundDark = MinimalistBackgroundDark
val PolishSurfaceDark = MinimalistSurfaceDark
val PolishSurfaceVariantDark = MinimalistSurfaceVariantDark
val PolishOutlineDark = MinimalistBorderDark
val PolishOutlineVariantDark = MinimalistBorderSubtleDark
val PolishTextPrimaryDark = MinimalistTextPrimaryDark
val PolishTextSecondaryDark = MinimalistTextSecondaryDark
val PolishTextTertiaryDark = MinimalistTextTertiaryDark
val PurplePrimaryDark = MinimalistPrimaryDark
val PurpleOnPrimaryDark = MinimalistOnPrimaryDark
val PurplePrimaryContainerDark = MinimalistPrimaryContainerDark
val PurpleOnPrimaryContainerDark = MinimalistOnPrimaryContainerDark


