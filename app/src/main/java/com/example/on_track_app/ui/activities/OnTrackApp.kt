package com.example.on_track_app.ui.activities

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.on_track_app.ui.theme.OnTrackAppTheme

// Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.on_track_app.navigation.Navigation
import com.example.on_track_app.navigation.Destinations

@Composable
fun OnTrackApp() {
    OnTrackAppTheme {
        val navController = rememberNavController()

        // Bottom navigation items
        val items = listOf(
            NavItem(
                route = Destinations.HOME,
                label = "Home",
                icon = Icons.Filled.Home
            ),
            NavItem(
                route = Destinations.DASHBOARD,
                label = "Dashboard",
                icon = Icons.Filled.Dashboard
            ),
            NavItem(
                route = Destinations.NOTIFICATIONS,
                label = "Notifications",
                icon = Icons.Filled.Notifications
            ),
            NavItem(
                route = Destinations.CALENDAR,
                label = "Calendar",
                icon = Icons.Filled.Event
            )
        )

        Scaffold(
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach { item ->
                        val selected = currentDestination.isOnDestination(item.route)
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                Navigation(navController = navController).let {}
            }
        }
    }
}

@Composable
private fun NavDestination?.isOnDestination(route: String): Boolean {
    return this?.hierarchy?.any { it.route == route } == true
}

private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)
