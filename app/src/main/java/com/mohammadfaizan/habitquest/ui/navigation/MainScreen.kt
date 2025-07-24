package com.mohammadfaizan.habitquest.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.mohammadfaizan.habitquest.ui.components.TopAppBarComponent

data class TopBarConfig(
    val title: String,
    val onSettingsClick: () -> Unit,
    val onNotificationClick: () -> Unit,
    val onCheckClick: () -> Unit
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Stats,
        BottomNavItem.Profile
    )
    val context = LocalContext.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topBarConfig = when (currentRoute) {
        BottomNavItem.Home.route -> TopBarConfig("Habit Quest", { Toast.makeText(context, "Settings Clicked",
            Toast.LENGTH_SHORT).show() }, { Toast.makeText(context, "Notification Clicked",
            Toast.LENGTH_SHORT).show() }, {Toast.makeText(context, "Notifications Clicked",
            Toast.LENGTH_SHORT).show()})
        BottomNavItem.Stats.route -> TopBarConfig("Habit Quest", {}, {}, {})
        BottomNavItem.Profile.route -> TopBarConfig("Habit Quest", {}, {}, {})
        else -> TopBarConfig("Habit Quest", {}, {}, {})
    }

    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = topBarConfig.title,
                onSettingsClick = topBarConfig.onSettingsClick,
                onNotificationClick = topBarConfig.onNotificationClick,
                onStatsClick = topBarConfig.onCheckClick
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label)},
                        label = { Text(item.label)},
                        selected = currentRoute === item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) { HomeScreen() }
            composable(BottomNavItem.Stats.route) { StatsScreen() }
            composable(BottomNavItem.Profile.route) { ProfileScreen() }
        }
    }
}