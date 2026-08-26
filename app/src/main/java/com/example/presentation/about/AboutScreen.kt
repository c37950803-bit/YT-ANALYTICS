package com.example.presentation.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGreen

/**
 * Écran "À propos" de l'application.
 * Stylisé avec le thème "Professional Polish".
 */
@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("about_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // En-tête de l'application
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "Logo",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "YouTube Channel Analytics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Version 1.0 (MVP) • Kotlin Native",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Application Android native pour l'analyse approfondie de chaînes YouTube, le calcul du Top 5 des vidéos et l'identification des vidéos championnes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // Architecture & Stack Technique
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Architecture & Stack Technique",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TechItem(
                        title = "Architecture MVP Stricte",
                        desc = "Séparation nette : View (affichage passif), Presenter (logique métier & coroutines), Model (Repository)",
                        icon = Icons.Default.Layers,
                        iconColor = AccentBlue
                    )

                    TechItem(
                        title = "Base de Données Locale Room",
                        desc = "Single Source of Truth pour l'historique et le cache hors-ligne avec Flow réactif",
                        icon = Icons.Default.Storage,
                        iconColor = AccentGreen
                    )

                    TechItem(
                        title = "Réseau : Retrofit + Moshi",
                        desc = "Clients HTTP asynchrones avec typage strict et interception des quotas",
                        icon = Icons.Default.Code,
                        iconColor = AccentAmber
                    )

                    TechItem(
                        title = "Asynchrone : Coroutines & Flow",
                        desc = "Gestion du cycle de vie sans fuite mémoire (PresenterScope)",
                        icon = Icons.Default.Speed,
                        iconColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Gestion et Économie des Quotas API
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Optimisation des Quotas API YouTube",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "L'API YouTube Data v3 applique un quota quotidien de 10 000 unités. L'application a été conçue pour minimiser la consommation :",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    QuotaItem(
                        endpoint = "1. Récupération chaîne (channels.list)",
                        cost = "1 unité",
                        desc = "Lecture des métriques globales et de l'ID playlist Uploads"
                    )

                    QuotaItem(
                        endpoint = "2. Playlist Uploads (playlistItems.list)",
                        cost = "1 unité",
                        desc = "Évite l'endpoint search.list qui coûte 100 unités par requête !"
                    )

                    QuotaItem(
                        endpoint = "3. Détails Vidéos groupés (videos.list)",
                        cost = "1 unité",
                        desc = "Jusqu'à 50 vidéos analysées en un seul appel réseau"
                    )

                    QuotaItem(
                        endpoint = "4. Historique local Room",
                        cost = "0 unité",
                        desc = "Le rafraîchissement d'accueil recharge la base locale sans appel réseau"
                    )
                }
            }
        }

        // Guide d'obtention de clé
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = AccentAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Comment obtenir une Clé API ?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "1. Rendez-vous sur https://console.cloud.google.com\n2. Créez un nouveau projet ou sélectionnez un projet existant.\n3. Allez dans 'API et services' > 'Bibliothèque' et activez 'YouTube Data API v3'.\n4. Allez dans 'Identifiants' > 'Créer des identifiants' > 'Clé API'.\n5. Copiez votre clé et enregistrez-la dans le menu 'Gestion des clés API' de cette application.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TechItem(
    title: String,
    desc: String,
    icon: ImageVector,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = iconColor.copy(alpha = 0.15f),
            modifier = Modifier.size(30.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QuotaItem(
    endpoint: String,
    cost: String,
    desc: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = endpoint,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = cost,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

