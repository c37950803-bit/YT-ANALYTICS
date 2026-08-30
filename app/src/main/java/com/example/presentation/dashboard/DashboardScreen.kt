package com.example.presentation.dashboard

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.ChannelDetails
import com.example.domain.model.DashboardAnalysis
import com.example.domain.model.VideoItem
import com.example.presentation.common.QuotaFallbackDialog
import com.example.ui.theme.AccentGreenActive
import com.example.ui.theme.PolishOutlineLight
import com.example.ui.theme.PolishSurfaceVariantLight
import com.example.ui.theme.PurpleOnPrimaryContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurplePrimaryContainer
import com.example.util.NumberFormatUtils

/**
 * =========================================================================================
 * 📊 TABLEAU DE BORD D'ANALYSE : DashboardScreen.kt (Vue MVP - Jetpack Compose)
 * =========================================================================================
 * 
 * 💡 EXPLICATION GRAND DÉBUTANT (Comment fonctionne cet écran ?) :
 * 
 * 1. QUEL EST LE RÔLE DE CET ÉCRAN ?
 *    - Cet écran affiche la "fiche d'identité complète" d'une chaîne YouTube :
 *      son avatar, sa description, sa date de création, ses totaux d'abonnés/vues/vidéos.
 *    - Il met en valeur les 2 grandes vidéos championnes (la plus vue et la plus commentée).
 *    - Il propose un filtre interactif pour classer le Top 5 des vidéos selon 3 critères :
 *      1. 🔥 Plus vues
 *      2. 💬 Plus commentées
 *      3. ⏱️ Plus longues (grâce à l'analyse de la durée ISO-8601 ex: "PT1H20M")
 *    - Cliquer sur une vidéo ouvre directement le lecteur vidéo officiel YouTube intégré !
 * 
 * 2. COMMENT FONCTIONNE LE CACHE HORS-LIGNE (0 QUOTA) ?
 *    - Lorsque l'utilisateur consulte une chaîne déjà dans l'historique, les données sont
 *      instantanément chargées depuis le stockage local SQLite du smartphone.
 *    - Le bouton "Rafraîchir" force un appel réseau uniquement si l'utilisateur le demande.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    channelQuery: String,
    presenter: DashboardPresenter,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (videoId: String, title: String) -> Unit,
    onNavigateToApiKeys: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Variables d'état : contiennent les données reçues depuis l'API YouTube
    var analysisData by remember { mutableStateOf<DashboardAnalysis?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showQuotaFallback by remember { mutableStateOf(false) }
    var quotaErrorMessage by remember { mutableStateOf("") }

    // Implémentation du contrat DashboardContract.View (commandes reçues du Presenter)
    val dashboardView = remember {
        object : DashboardContract.View {
            override fun displayDashboard(analysis: DashboardAnalysis) {
                analysisData = analysis
                errorMessage = null
            }

            override fun showQuotaFallback(errorMsg: String) {
                quotaErrorMessage = errorMsg
                showQuotaFallback = true
            }

            override fun showInvalidApiKeyFallback(errorMsg: String) {
                quotaErrorMessage = errorMsg
                showQuotaFallback = true
            }

            override fun showChannelNotFoundState(errorMsg: String) {
                errorMessage = errorMsg
            }

            override fun openPlayerScreen(videoId: String, title: String) {
                onNavigateToPlayer(videoId, title)
            }

            override fun openExternalYouTubeApp(videoId: String) {
                val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId"))
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId"))
                try {
                    context.startActivity(appIntent)
                } catch (e: Exception) {
                    context.startActivity(webIntent)
                }
            }

            override fun showError(message: String) {
                errorMessage = message
            }

            override fun showLoading(loading: Boolean) {
                isLoading = loading
            }
        }
    }

    DisposableEffect(presenter, channelQuery) {
        presenter.attachView(dashboardView)
        presenter.analyzeChannel(channelQuery, forceNetworkRefresh = false)
        onDispose {
            presenter.detachView()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Analyse : ${analysisData?.channel?.title ?: channelQuery}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_dashboard_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    // Badge API ACTIVE
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AccentGreenActive)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "API: ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    IconButton(
                        onClick = { presenter.analyzeChannel(channelQuery, forceNetworkRefresh = true) },
                        modifier = Modifier.testTag("btn_dashboard_refresh")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser via API", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading && analysisData == null) {
                DashboardLoadingView()
            } else if (errorMessage != null && analysisData == null) {
                DashboardErrorView(
                    message = errorMessage ?: "Erreur",
                    onRetry = { presenter.analyzeChannel(channelQuery, forceNetworkRefresh = true) },
                    onNavigateToApiKeys = onNavigateToApiKeys
                )
            } else if (analysisData != null) {
                val data = analysisData!!
                DashboardContentView(
                    analysis = data,
                    onPlayVideo = { video -> presenter.onVideoPlayClicked(video) },
                    onExternalPlay = { video -> presenter.onVideoExternalPlayClicked(video) }
                )
            }
        }
    }

    // Dialogue Fallback Quota
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

enum class Top5RankingType(
    val title: String,
    val subtitle: String,
    val chipLabel: String
) {
    VIEWS("TOP 5 VIDÉOS", "Classées par nombre total de vues", "🔥 Plus vues"),
    COMMENTS("TOP 5 VIDÉOS", "Classées par nombre de commentaires", "💬 Plus commentées"),
    LONGEST("TOP 5 VIDÉOS", "Classées par durée de la vidéo", "⏱️ Plus longues")
}

@Composable
private fun DashboardContentView(
    analysis: DashboardAnalysis,
    onPlayVideo: (VideoItem) -> Unit,
    onExternalPlay: (VideoItem) -> Unit
) {
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var selectedTop5Type by remember { mutableStateOf(Top5RankingType.VIEWS) }

    val displayedVideos = when (selectedTop5Type) {
        Top5RankingType.VIEWS -> analysis.top5Videos
        Top5RankingType.COMMENTS -> analysis.top5MostCommentedVideos.ifEmpty { analysis.top5Videos }
        Top5RankingType.LONGEST -> analysis.top5LongestVideos.ifEmpty { analysis.top5Videos }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_scroll_content"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // En-tête de la chaîne (Carte principale avec 3 stats)
        item {
            PolishChannelHeaderCard(
                channel = analysis.channel,
                isFromCache = analysis.isFromCache,
                analysisTimestamp = analysis.analysisTimestamp,
                isDescriptionExpanded = isDescriptionExpanded,
                onToggleDescription = { isDescriptionExpanded = !isDescriptionExpanded }
            )
        }

        // ==========================================
        // BLOC 2 : Les Deux Champions
        // ==========================================
        if (analysis.mostViewedVideo != null || analysis.mostCommentedVideo != null) {
            item {
                PolishChampionsSection(
                    mostViewed = analysis.mostViewedVideo,
                    mostCommented = analysis.mostCommentedVideo,
                    onPlay = onPlayVideo,
                    onExternal = onExternalPlay
                )
            }
        }

        // ==========================================
        // BLOC 3 : Sélecteur et Liste Top 5 (Vues / Commentaires / Plus Longues)
        // ==========================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedTop5Type.title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = selectedTop5Type.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sélecteur dynamique de classement
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Top5RankingType.values().forEach { rankingType ->
                        val isSelected = selectedTop5Type == rankingType
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTop5Type = rankingType },
                            label = {
                                Text(
                                    text = rankingType.chipLabel,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        if (displayedVideos.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Aucune vidéo publique trouvée pour cette chaîne.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(
                items = displayedVideos,
                key = { "${selectedTop5Type.name}_${it.videoId}" }
            ) { video ->
                PolishTop5VideoCard(
                    video = video,
                    rankingType = selectedTop5Type,
                    onPlay = { onPlayVideo(video) },
                    onExternal = { onExternalPlay(video) }
                )
            }
        }
    }
}

/**
 * Carte d'en-tête de la chaîne selon le design "Professional Polish"
 * White background, rounded-3xl (24dp), border outline, 3-column stats section with dividers.
 */
@Composable
private fun PolishChannelHeaderCard(
    channel: ChannelDetails,
    isFromCache: Boolean,
    analysisTimestamp: Long,
    isDescriptionExpanded: Boolean,
    onToggleDescription: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Ligne du Profil : Avatar + Titre + Handle/ID
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar / Thumbnail
                AsyncImage(
                    model = channel.thumbnailUrl,
                    contentDescription = "Avatar ${channel.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = channel.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val handleText = channel.customUrl.takeIf { !it.isNullOrBlank() } ?: "ID: ${channel.channelId}"
                    Text(
                        text = handleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Badge Cache / Freshness
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (isFromCache) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (isFromCache) Icons.Default.Storage else Icons.Default.Whatshot,
                                contentDescription = null,
                                tint = if (isFromCache) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isFromCache) "Cache (${NumberFormatUtils.formatDateTime(analysisTimestamp)})" else "En direct API",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isFromCache) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Description repliable si présente
            if (!channel.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = channel.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .clickable { onToggleDescription() }
                        .padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Grille 3 colonnes : Abonnés | Vues | Vidéos
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colonne 1 : Abonnés
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ABONNÉS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = NumberFormatUtils.formatCompactNumber(channel.subscriberCount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                // Colonne 2 : Vues
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VUES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = NumberFormatUtils.formatCompactNumber(channel.viewCount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                // Colonne 3 : Vidéos
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VIDÉOS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = NumberFormatUtils.formatCompactNumber(channel.videoCount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * 🏆 SECTION "LES DEUX CHAMPIONS" (PolishChampionsSection)
 * 
 * 💡 EXPLICATION DÉBUTANT :
 * Cette section extrait automatiquement deux records majeurs de la chaîne :
 * 1. Le Record absolu de vues (La vidéo la plus populaire)
 * 2. Le Record de commentaires (La vidéo qui a suscité le plus d'engagement)
 */
@Composable
private fun PolishChampionsSection(
    mostViewed: VideoItem?,
    mostCommented: VideoItem?,
    onPlay: (VideoItem) -> Unit,
    onExternal: (VideoItem) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 🥇 Champion #1 : Record de vues
        if (mostViewed != null) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
                ),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onPlay(mostViewed) }
            ) {
                Column(
                    modifier = Modifier
                        .padding(14.dp)
                        .height(130.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = YouTubeRed.copy(alpha = 0.12f),
                            modifier = Modifier.size(18.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Whatshot,
                                    contentDescription = null,
                                    tint = YouTubeRed,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Text(
                            text = "RECORD VUES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column {
                        Text(
                            text = NumberFormatUtils.formatCompactNumber(mostViewed.viewCount),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = mostViewed.title,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = YouTubeRed,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Lire",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 💬 Champion #2 : Record de commentaires
        if (mostCommented != null) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
                ),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onPlay(mostCommented) }
            ) {
                Column(
                    modifier = Modifier
                        .padding(14.dp)
                        .height(130.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(18.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Comment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                        Text(
                            text = "PLUS COMMENTÉE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column {
                        Text(
                            text = NumberFormatUtils.formatCompactNumber(mostCommented.commentCount),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = mostCommented.title,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Lire",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🎬 CARTE VIDÉO DU TOP 5 (PolishTop5VideoCard)
 * 
 * 💡 EXPLICATION DÉBUTANT :
 * Représente chaque vidéo du Top 5 avec son badge de rang (#1, #2, #3, #4, #5),
 * sa miniature 16:9, sa durée formatée (ex: "12:45"), son titre, et ses statistiques.
 */
@Composable
private fun PolishTop5VideoCard(
    video: VideoItem,
    rankingType: Top5RankingType = Top5RankingType.VIEWS,
    onPlay: () -> Unit,
    onExternal: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
            .testTag("top5_video_card_${video.videoId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge Rang (#1, #2...)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (video.rank) {
                    1 -> YouTubeRed
                    2 -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "#${video.rank}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (video.rank) {
                            1 -> Color.White
                            2 -> MaterialTheme.colorScheme.onPrimary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Miniature avec badge durée incrusté
            Box(
                modifier = Modifier
                    .size(width = 84.dp, height = 52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = "Miniature ${video.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (!video.durationIso.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Black.copy(alpha = 0.80f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(3.dp)
                    ) {
                        Text(
                            text = video.formattedDuration,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(3.dp))

                when (rankingType) {
                    Top5RankingType.VIEWS -> {
                        Text(
                            text = "${NumberFormatUtils.formatCompactNumber(video.viewCount)} vues • ${NumberFormatUtils.formatIsoDate(video.publishedAt)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = YouTubeRed
                        )
                    }
                    Top5RankingType.COMMENTS -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${NumberFormatUtils.formatCompactNumber(video.commentCount)} commentaires",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " • ${NumberFormatUtils.formatCompactNumber(video.viewCount)} vues",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Top5RankingType.LONGEST -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⏱️ ${video.formattedDuration}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " • ${NumberFormatUtils.formatCompactNumber(video.viewCount)} vues",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onPlay,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Lire la vidéo",
                    tint = YouTubeRed,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun DashboardLoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Analyse de la chaîne en cours...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Calcul des métriques, playlist Uploads et Top 5",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun DashboardErrorView(
    message: String,
    onRetry: () -> Unit,
    onNavigateToApiKeys: () -> Unit
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
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Échec de l'analyse",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onNavigateToApiKeys,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Vérifier les Clés API")
                }

                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Réessayer")
                }
            }
        }
    }
}

