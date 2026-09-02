package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.MonadireBottomNavBar
import com.example.ui.components.MonadireTopHeader
import com.example.ui.screens.*
import com.example.ui.theme.ForestBlack
import com.example.ui.theme.MonadireTheme
import com.example.viewmodel.AppDestination
import com.example.viewmodel.MonadireViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonadireTheme {
                val viewModel: MonadireViewModel = viewModel()
                MonadireApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MonadireApp(viewModel: MonadireViewModel) {
    val currentDestination by viewModel.currentDestination.collectAsState()
    val activeHunt by viewModel.activeHunt.collectAsState()
    val notifications by viewModel.allNotifications.collectAsState()
    val unreadNotifCount = remember(notifications) { notifications.count { !it.isRead } }

    // Handle back button for sub-destinations
    BackHandler(enabled = currentDestination != AppDestination.HOME) {
        viewModel.navigateTo(AppDestination.HOME)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ForestBlack),
        topBar = {
            if (currentDestination != AppDestination.GLOBAL_SEARCH) {
                MonadireTopHeader(
                    activeHunt = activeHunt,
                    unreadNotificationCount = unreadNotifCount,
                    onSearchClick = { viewModel.navigateTo(AppDestination.GLOBAL_SEARCH) },
                    onNotificationClick = { viewModel.navigateTo(AppDestination.PROFILE) },
                    onActiveHuntClick = { viewModel.navigateTo(AppDestination.ACTIVE_HUNT) }
                )
            }
        },
        bottomBar = {
            if (currentDestination != AppDestination.GLOBAL_SEARCH) {
                MonadireBottomNavBar(
                    currentDestination = currentDestination,
                    onNavigate = { destination ->
                        viewModel.navigateTo(destination)
                    }
                )
            }
        },
        containerColor = ForestBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { destination ->
                when (destination) {
                    AppDestination.HOME -> HomeScreen(viewModel = viewModel)
                    AppDestination.MAP -> MapScreen(viewModel = viewModel)
                    AppDestination.ACTIVE_HUNT -> ActiveHuntScreen(viewModel = viewModel)
                    AppDestination.JOURNAL -> JournalScreen(viewModel = viewModel)
                    AppDestination.PROFILE -> ProfileScreen(viewModel = viewModel)
                    AppDestination.SPECIES_CATALOG -> SpeciesScreen(viewModel = viewModel)
                    AppDestination.EQUIPMENT_INVENTORY -> EquipmentScreen(viewModel = viewModel)
                    AppDestination.SAFETY_AND_RULES -> SafetyRulesScreen()
                    AppDestination.ADMIN_PANEL -> AdminScreen(viewModel = viewModel)
                    AppDestination.GLOBAL_SEARCH -> GlobalSearchScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo(AppDestination.HOME) }
                    )
                }
            }
        }
    }
}
