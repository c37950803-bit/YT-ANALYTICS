package com.example.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.di.AppContainer
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
import com.example.ui.theme.PolishBackgroundLight
import com.example.ui.theme.PolishOutlineLight
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurplePrimaryContainer
import com.example.ui.theme.PurpleOnPrimaryContainer
import kotlinx.coroutines.launch

/**
 * Conteneur principal de l'application avec Navigation Drawer (Tiroir de navigation) - Thème Professional Polish.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentDestination by remember { mutableStateOf<AppDestination>(AppDestination.Home) }
    var lastActiveChannelQuery by remember { mutableStateOf<String?>(null) }

    // Instanciation des Presenters MVP
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                // En-tête du Drawer
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
                        val target = lastActiveChannelQuery ?: "@MrBeast"
                        currentDestination = AppDestination.Dashboard(target)
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

                // 4. À propos
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
            }
        },
        modifier = modifier
    ) {
        when (val dest = currentDestination) {
            is AppDestination.Home -> {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "YT",
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "YT Analytics",
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
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            },
                            actions = {
                                // Badge statut API avec point vert
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
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
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                        val fallback = lastActiveChannelQuery ?: "@MrBeast"
                        currentDestination = AppDestination.Dashboard(fallback)
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

@Composable
private fun DrawerHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "YT",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "YT Analytics",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Professional Polish Edition • MVP",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

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
        shape = RoundedCornerShape(16.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag(testTag)
    )
}

