package com.example.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.ChannelDetails
import com.example.presentation.common.ConfirmDeleteDialog
import com.example.presentation.common.QuotaFallbackDialog
import com.example.ui.theme.AccentGreenActive
import com.example.ui.theme.MinimalistBorderLight
import com.example.ui.theme.YouTubeRed
import com.example.util.NumberFormatUtils

/**
 * =========================================================================================
 * 🏠 ÉCRAN D'ACCUEIL : HomeScreen.kt (Vue MVP - Jetpack Compose)
 * =========================================================================================
 * 
 * 💡 EXPLICATION POUR QUASI-DÉBUTANT (Aucune connaissance en code requise) :
 * 
 * 1. QU'EST-CE QUE CET ÉCRAN FAIT ?
 *    - C'est la porte d'entrée de l'application.
 *    - L'utilisateur peut y taper le nom ou le lien d'une chaîne YouTube (ex: "@MrBeast").
 *    - Il affiche aussi l'historique des chaînes déjà consultées, enregistrées directement
 *      sur le téléphone dans une base de données locale (Room / SQLite) pour économiser
 *      la connexion Internet et les quotas de l'API.
 * 
 * 2. C'EST QUOI LE PATTERN "MVP" (Modèle-Vue-Presenter) ?
 *    - La "VUE" (ce fichier HomeScreen) s'occupe UNIQUEMENT du dessin sur l'écran (boutons, textes, couleurs).
 *    - Le "PRESENTER" (HomePresenter) s'occupe de la réflexion et des calculs (parler à la base de données,
 *      déclencher la recherche, gérer les erreurs).
 *    - Ainsi, le code reste propre, facile à comprendre et sans mélange désordonné !
 * 
 * 3. C'EST QUOI UN "STATE" (ex: `remember { mutableStateOf(...) }`) ?
 *    - En Jetpack Compose, un "State" est une variable magique. Dès que sa valeur change,
 *      l'écran se redessine automatiquement à l'endroit concerné, sans qu'on ait besoin de
 *      recharger toute la page !
 */
@Composable
fun HomeScreen(
    presenter: HomePresenter,
    onNavigateToDashboard: (String) -> Unit,
    onNavigateToApiKeys: () -> Unit,
    modifier: Modifier = Modifier
) {
    // -------------------------------------------------------------------------------------
    // VARIABLES D'ÉTAT (State) : Elles gardent la mémoire des données affichées à l'écran
    // -------------------------------------------------------------------------------------
    // Liste des chaînes enregistrées dans l'historique local du téléphone
    var historyList by remember { mutableStateOf<List<ChannelDetails>>(emptyList()) }
    
    // Indique si l'application est en train de chercher ou charger des données (affiche un sablier/rond de chargement)
    var isLoading by remember { mutableStateOf(false) }
    
    // Message d'erreur éventuel à montrer à l'utilisateur
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Contrôle l'ouverture d'une fenêtre d'alerte si le quota YouTube de la clé est dépassé
    var showQuotaFallback by remember { mutableStateOf(false) }
    var quotaErrorMessage by remember { mutableStateOf("") }
    
    // Texte actuellement saisi par l'utilisateur dans la barre de recherche
    var searchQuery by remember { mutableStateOf("") }
    
    // Identifiant de la chaîne que l'utilisateur souhaite supprimer de son historique
    var channelToDelete by remember { mutableStateOf<String?>(null) }
    
    // Contrôle la popup de confirmation pour tout effacer d'un coup
    var showClearAllConfirmation by remember { mutableStateOf(false) }

    // -------------------------------------------------------------------------------------
    // INTERFACE DE COMMUNICATION : Le Presenter appelle ces méthodes pour donner des ordres à la Vue
    // -------------------------------------------------------------------------------------
    val homeView = remember {
        object : HomeContract.View {
            override fun displayHistory(history: List<ChannelDetails>) {
                // Met à jour la liste des chaînes affichées
                historyList = history
            }

            override fun showEmptyHistory() {
                // Vide la liste si la base locale est vide
                historyList = emptyList()
            }

            override fun navigateToDashboard(channelId: String) {
                // Change d'écran vers le tableau de bord de statistiques
                onNavigateToDashboard(channelId)
            }

            override fun showApiKeyRequiredDialog() {
                // Redirige vers la gestion des clés si aucune clé n'est configurée
                onNavigateToApiKeys()
            }

            override fun showQuotaFallbackDialog(errorMessage: String) {
                quotaErrorMessage = errorMessage
                showQuotaFallback = true
            }

            override fun showError(message: String) {
                errorMessage = message
            }

            override fun showLoading(loading: Boolean) {
                isLoading = loading
            }
        }
    }

    // Lie le Presenter à la Vue quand l'écran s'ouvre, et le déconnecte quand l'écran se ferme
    DisposableEffect(presenter) {
        presenter.attachView(homeView)
        onDispose {
            presenter.detachView()
        }
    }

    // -------------------------------------------------------------------------------------
    // MISE EN PAGE VISUELLE (Design Minimaliste Épuré)
    // -------------------------------------------------------------------------------------
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // En-tête minimaliste avec logo rouge YouTube & titre épuré
        HomeMinimalistHeader()

        // Section de saisie et de recherche
        SearchMinimalistSection(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = {
                if (searchQuery.isNotBlank()) {
                    presenter.onSearchSubmitted(searchQuery)
                }
            },
            onClear = { searchQuery = "" }
        )

        // En-tête de l'historique local avec compteur et boutons d'actions
        HistoryHeader(
            historyCount = historyList.size,
            isLoading = isLoading,
            onRefresh = { presenter.refreshLocalHistory() },
            onClearAll = {
                if (historyList.isNotEmpty()) {
                    showClearAllConfirmation = true
                }
            }
        )

        // Affichage du contenu : Soit une vue vide élégante, soit la liste des cartes
        if (historyList.isEmpty()) {
            EmptyHistoryView()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("history_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = historyList,
                    key = { it.channelId }
                ) { channel ->
                    ChannelHistoryCard(
                        channel = channel,
                        onClick = { presenter.onChannelSelected(channel) },
                        onDelete = { channelToDelete = channel.channelId }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // -------------------------------------------------------------------------------------
    // BOÎTES DE DIALOGUES (Popups de confirmation)
    // -------------------------------------------------------------------------------------
    if (channelToDelete != null) {
        ConfirmDeleteDialog(
            title = "Supprimer de l'historique",
            message = "Voulez-vous retirer cette chaîne de votre historique local ?",
            onDismiss = { channelToDelete = null },
            onConfirm = {
                channelToDelete?.let { presenter.deleteHistoryChannel(it) }
                channelToDelete = null
            }
        )
    }

    if (showClearAllConfirmation) {
        ConfirmDeleteDialog(
            title = "Vider l'historique",
            message = "Voulez-vous effacer toutes les chaînes enregistrées dans la base locale Room ?",
            onDismiss = { showClearAllConfirmation = false },
            onConfirm = {
                presenter.clearHistory()
                showClearAllConfirmation = false
            }
        )
    }

    if (showQuotaFallback) {
        QuotaFallbackDialog(
            errorMessage = quotaErrorMessage,
            onDismiss = { showQuotaFallback = false },
            onNavigateToApiKeyManager = {
                showQuotaFallback = false
                onNavigateToApiKeys()
            }
        )
    }
}

/**
 * 🌟 EN-TÊTE MINIMALISTE (HomeMinimalistHeader)
 * Un bandeau sobre, épuré avec typographie équilibrée et icône YouTube nette.
 */
@Composable
private fun HomeMinimalistHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Petit badge rouge YouTube minimaliste
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = YouTubeRed,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Logo YouTube Analytics",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "YouTube Analytics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Analyse de chaînes & Top 5 Vidéos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 🔍 BARRE DE RECHERCHE MINIMALISTE (SearchMinimalistSection)
 * Champ texte arrondi avec suggestions en bulles compactes et bouton d'action net.
 */
@Composable
private fun SearchMinimalistSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Rechercher une chaîne",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Champ de saisie utilisateur
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        "URL, @Handle (ex: @MrBeast), ou ID",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Rechercher",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = onClear) {
                            Icon(Icons.Default.Clear, contentDescription = "Effacer")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_channel")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Astuce : Compatible avec les liens complets, @handles et ID de chaînes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Bouton principal d'analyse
                Button(
                    onClick = onSearch,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("btn_submit_search")
                ) {
                    Text("Analyser", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * 📋 EN-TÊTE DE LA SECTION HISTORIQUE
 */
@Composable
private fun HistoryHeader(
    historyCount: Int,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onClearAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Historique Local ($historyCount)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            if (isLoading) {
                Spacer(modifier = Modifier.width(8.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row {
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.testTag("btn_refresh_history")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rafraîchir base locale",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (historyCount > 0) {
                IconButton(
                    onClick = onClearAll,
                    modifier = Modifier.testTag("btn_clear_history")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Effacer l'historique",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * 📇 CARTE D'UNE CHAÎNE DANS L'HISTORIQUE (ChannelHistoryCard)
 * Style minimaliste : bordure fine, photo de profil arrondie, compteurs compacts.
 */
@Composable
private fun ChannelHistoryCard(
    channel: ChannelDetails,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("channel_card_${channel.channelId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo de profil de la chaîne YouTube
            AsyncImage(
                model = channel.thumbnailUrl,
                contentDescription = "Avatar ${channel.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!channel.customUrl.isNullOrBlank()) {
                    Text(
                        text = channel.customUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Nombre d'abonnés
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Subscriptions,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${NumberFormatUtils.formatCompactNumber(channel.subscriberCount)} abonnés",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Total des vues
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${NumberFormatUtils.formatCompactNumber(channel.viewCount)} vues",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Consulté le ${NumberFormatUtils.formatDateTime(channel.lastViewedTimestamp)}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Supprimer de l'historique",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * 📭 ÉTAT VIDE (EmptyHistoryView)
 * S'affiche quand l'utilisateur vient d'installer l'application et n'a pas encore de chaînes enregistrées.
 * Offre un design 100% propre, sobre et minimaliste.
 */
@Composable
private fun EmptyHistoryView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Historique vierge",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Entrez l'URL, le @handle ou l'identifiant d'une chaîne ci-dessus pour lancer votre première analyse.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}


