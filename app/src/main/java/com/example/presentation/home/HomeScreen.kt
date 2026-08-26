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
import com.example.ui.theme.PurplePrimary
import com.example.util.NumberFormatUtils

/**
 * Écran d'accueil de l'application (Vue MVP).
 * Stylisé selon le thème "Professional Polish".
 */
@Composable
fun HomeScreen(
    presenter: HomePresenter,
    onNavigateToDashboard: (String) -> Unit,
    onNavigateToApiKeys: () -> Unit,
    modifier: Modifier = Modifier
) {
    var historyList by remember { mutableStateOf<List<ChannelDetails>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showQuotaFallback by remember { mutableStateOf(false) }
    var quotaErrorMessage by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var channelToDelete by remember { mutableStateOf<String?>(null) }
    var showClearAllConfirmation by remember { mutableStateOf(false) }

    // Implémentation du contrat HomeContract.View via le cycle de vie Compose
    val homeView = remember {
        object : HomeContract.View {
            override fun displayHistory(history: List<ChannelDetails>) {
                historyList = history
            }

            override fun showEmptyHistory() {
                historyList = emptyList()
            }

            override fun navigateToDashboard(channelId: String) {
                onNavigateToDashboard(channelId)
            }

            override fun showApiKeyRequiredDialog() {
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

    DisposableEffect(presenter) {
        presenter.attachView(homeView)
        onDispose {
            presenter.detachView()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // En-tête Hero YouTube Analytics
        HomeHeroHeader()

        // Section de recherche
        SearchSection(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = {
                if (searchQuery.isNotBlank()) {
                    presenter.onSearchSubmitted(searchQuery)
                }
            },
            onClear = { searchQuery = "" }
        )

        // Section Historique ("LocalStorage") avec barre d'actions
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

        // Contenu principal de l'historique
        if (historyList.isEmpty()) {
            EmptyHistoryView(
                onSelectSuggestion = { suggestion ->
                    searchQuery = suggestion
                    presenter.onSearchSubmitted(suggestion)
                }
            )
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

    // Dialogue de confirmation de suppression d'une seule chaîne
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

    // Dialogue de confirmation pour vider l'historique complet
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

    // Dialogue de fallback Quota
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

@Composable
private fun HomeHeroHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "YouTube Channel Analytics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Analyse de chaînes & Top 5 Vidéos (MVP)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SearchSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
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

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        "URL, @Handle (ex: @MrBeast), ou ID",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Rechercher",
                        tint = MaterialTheme.colorScheme.primary
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
                shape = RoundedCornerShape(16.dp),
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
                // Suggestions d'exemples rapides
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val suggestions = listOf("@MrBeast", "@mkbhd", "@OpenAI", "@Veritasium")
                    items(suggestions) { tag ->
                        SuggestionChip(
                            onClick = {
                                onQueryChange(tag)
                            },
                            label = {
                                Text(tag, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = null,
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onSearch,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("btn_submit_search")
                ) {
                    Text("Analyser", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
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
                    modifier = Modifier.size(16.dp),
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

@Composable
private fun ChannelHistoryCard(
    channel: ChannelDetails,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
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
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Miniature / Avatar de la chaîne
            AsyncImage(
                model = channel.thumbnailUrl,
                contentDescription = "Avatar ${channel.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
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
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Abonnés
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Subscriptions,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${NumberFormatUtils.formatCompactNumber(channel.subscriberCount)} abonnés",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Vues
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryView(
    onSelectSuggestion: (String) -> Unit
) {
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
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Aucune chaîne dans l'historique",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Recherchez une chaîne YouTube ci-dessus ou cliquez sur une suggestion pour lancer votre première analyse.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onSelectSuggestion("@MrBeast") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text("Essayer @MrBeast", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }

                Button(
                    onClick = { onSelectSuggestion("@mkbhd") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text("Essayer @mkbhd", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
    }
}

