package com.example.presentation.navigation

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.di.AppContainer
import java.net.URLEncoder
import com.example.presentation.about.AboutCreatorDialog
import com.example.presentation.about.AboutScreen
import com.example.presentation.apikeys.ApiKeyPresenter
import com.example.presentation.apikeys.ApiKeyScreen
import com.example.presentation.dashboard.DashboardPresenter
import com.example.presentation.dashboard.DashboardScreen
import com.example.presentation.home.HomePresenter
import com.example.presentation.home.HomeScreen
import com.example.presentation.player.PlayerPresenter
import com.example.presentation.player.PlayerScreen
import com.example.ui.theme.AccentGreenActive
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.launch

/**
 * =========================================================================================
 * 🗺️ CONTENEUR PRINCIPAL DE NAVIGATION : MainAppScaffold.kt
 * =========================================================================================
 * 
 * 💡 EXPLICATION GRAND DÉBUTANT (Comment se déplace-t-on dans l'application ?) :
 * 
 * 1. QU'EST-CE QU'UN "SCAFFOLD" ?
 *    - En anglais, Scaffold veut dire "Échafaudage".
 *    - C'est le cadre général qui tient tout l'immeuble de l'application :
 *      la barre du haut (TopAppBar), le tiroir latéral glissant (Navigation Drawer),
 *      et la zone centrale où s'affichent les différentes pages.
 * 
 * 2. COMMENT FONCTIONNE LE TIROIR DE NAVIGATION (ModalNavigationDrawer) ?
 *    - Quand l'utilisateur clique sur le bouton Menu (les 3 petites lignes en haut à gauche),
 *      un panneau glisse élégamment depuis la gauche.
 *    - Il permet de naviguer entre :
 *      1. L'Accueil (Historique des chaînes)
 *      2. Le Tableau de bord (Analyses et Top 5)
 *      3. Le Gestionnaire de clés API YouTube
 *      4. La page À propos & Architecture
 *      5. Le raccourci vers le Créateur "SAMUEL DRIVER" avec contact WhatsApp direct !
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    // État du tiroir latéral (ouvert ou fermé)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Page actuellement affichée à l'écran
    var currentDestination by remember { mutableStateOf<AppDestination>(AppDestination.Home) }
    var lastActiveChannelQuery by remember { mutableStateOf<String?>(null) }

    // -------------------------------------------------------------------------------------
    // INSTANCIATION DES PRESENTERS (Architecture MVP)
    // -------------------------------------------------------------------------------------
    val homePresenter = remember {
        HomePresenter(
            youTubeRepository = container.youTubeRepository,
            apiKeyRepository = container.apiKeyRepository
        )
    }

    val apiKeyPresenter = remember {
        ApiKeyPresenter(
            apiKeyRepository = container.apiKeyRepository
        )
    }

    val dashboardPresenter = remember {
        DashboardPresenter(
            youTubeRepository = container.youTubeRepository,
            apiKeyRepository = container.apiKeyRepository
        )
    }

    val playerPresenter = remember {
        PlayerPresenter()
    }

    // -------------------------------------------------------------------------------------
    // TIROIR LATÉRAL GLISSANT (Drawer)
    // -------------------------------------------------------------------------------------
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                // En-tête du menu tiroir
                DrawerHeader()

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // 1. Accueil (Historique)
                DrawerNavItem(
                    label = "Accueil (Historique)",
                    selected = currentDestination is AppDestination.Home,
                    selectedIcon = Icons.Filled.History,
                    unselectedIcon = Icons.Outlined.History,
                    testTag = "drawer_item_home",
                    onClick = {
                        currentDestination = AppDestination.Home
                        scope.launch { drawerState.close() }
                    }
                )

                // 2. Tableau de bord
                DrawerNavItem(
                    label = "Tableau de bord",
                    selected = currentDestination is AppDestination.Dashboard,
                    selectedIcon = Icons.Filled.Dashboard,
                    unselectedIcon = Icons.Outlined.Dashboard,
                    testTag = "drawer_item_dashboard",
                    onClick = {
                        val target = lastActiveChannelQuery
                        if (target != null) {
                            currentDestination = AppDestination.Dashboard(target)
                        } else {
                            currentDestination = AppDestination.Home
                        }
                        scope.launch { drawerState.close() }
                    }
                )

                // 3. Gestion des clés API
                DrawerNavItem(
                    label = "Gestion des clés API",
                    selected = currentDestination is AppDestination.ApiKeys,
                    selectedIcon = Icons.Filled.Key,
                    unselectedIcon = Icons.Outlined.Key,
                    testTag = "drawer_item_api_keys",
                    onClick = {
                        currentDestination = AppDestination.ApiKeys
                        scope.launch { drawerState.close() }
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // 4. À propos & Quotas
                DrawerNavItem(
                    label = "À propos & Quotas",
                    selected = currentDestination is AppDestination.About,
                    selectedIcon = Icons.Filled.Info,
                    unselectedIcon = Icons.Outlined.Info,
                    testTag = "drawer_item_about",
                    onClick = {
                        currentDestination = AppDestination.About
                        scope.launch { drawerState.close() }
                    }
                )

                Spacer(modifier = Modifier.weight(1f, fill = true))

                // Footer Créateur / Contact WhatsApp SAMUEL DRIVER
                DrawerCreatorFooter()
            }
        },
        modifier = modifier
    ) {
        // ---------------------------------------------------------------------------------
        // ROUTAGE VERS LES DIFFÉRENTS ÉCRANS DE L'APPLICATION
        // ---------------------------------------------------------------------------------
        when (val dest = currentDestination) {
            is AppDestination.Home -> {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = YouTubeRed,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "YT",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "YouTube Analytics",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier.testTag("btn_open_drawer")
                                ) {
                                    Icon(Icons.Default.Menu, contentDescription = "Ouvrir le menu")
                                }
                            },
                            actions = {
                                // Petit badge vert indiquant que l'API est prête
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(AccentGreenActive)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "API ACTIVE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    }
                ) { padding ->
                    HomeScreen(
                        presenter = homePresenter,
                        onNavigateToDashboard = { query ->
                            lastActiveChannelQuery = query
                            currentDestination = AppDestination.Dashboard(query)
                        },
                        onNavigateToApiKeys = {
                            currentDestination = AppDestination.ApiKeys
                        },
                        modifier = Modifier.padding(padding)
                    )
                }
            }

            is AppDestination.Dashboard -> {
                DashboardScreen(
                    channelQuery = dest.query,
                    presenter = dashboardPresenter,
                    onNavigateBack = { currentDestination = AppDestination.Home },
                    onNavigateToPlayer = { vId, title ->
                        currentDestination = AppDestination.Player(vId, title)
                    },
                    onNavigateToApiKeys = { currentDestination = AppDestination.ApiKeys }
                )
            }

            is AppDestination.ApiKeys -> {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Gestion des clés API", fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier.testTag("btn_open_drawer_keys")
                                ) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    }
                ) { padding ->
                    ApiKeyScreen(
                        presenter = apiKeyPresenter,
                        modifier = Modifier.padding(padding)
                    )
                }
            }

            is AppDestination.Player -> {
                PlayerScreen(
                    videoId = dest.videoId,
                    videoTitle = dest.title,
                    presenter = playerPresenter,
                    onNavigateBack = {
                        val active = lastActiveChannelQuery
                        if (active != null) {
                            currentDestination = AppDestination.Dashboard(active)
                        } else {
                            currentDestination = AppDestination.Home
                        }
                    }
                )
            }

            is AppDestination.About -> {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("À propos & Architecture", fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier.testTag("btn_open_drawer_about")
                                ) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    }
                ) { padding ->
                    AboutScreen(modifier = Modifier.padding(padding))
                }
            }
        }
    }
}

/**
 * 🏷️ EN-TÊTE DU MENU TIROIR (DrawerHeader)
 */
@Composable
private fun DrawerHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = YouTubeRed,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "YT",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "YouTube Analytics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Édition Minimaliste Épurée • MVP",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 📌 ÉLÉMENT INDIVIDUEL DU TIROIR DE NAVIGATION (DrawerNavItem)
 */
@Composable
private fun DrawerNavItem(
    label: String,
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        shape = RoundedCornerShape(14.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .testTag(testTag)
    )
}

/**
 * 👤 PIED DE PAGE CRÉATEUR & WHATSAPP (DrawerCreatorFooter)
 * Met en valeur SAMUEL DRIVER (+237 659 39 34 46)
 */
@Composable
private fun DrawerCreatorFooter() {
    val context = LocalContext.current
    val rawPhoneNumber = "237659393446"
    val formattedPhoneNumber = "+237 659 39 34 46"
    val developerName = "SAMUEL DRIVER"
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AboutCreatorDialog(onDismiss = { showDialog = false })
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 16.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(bottom = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showDialog = true
                }
                .testTag("drawer_creator_card")
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF25D366),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "WhatsApp",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vibecodé par $developerName",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "LE CRÉATEUR ($formattedPhoneNumber)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


