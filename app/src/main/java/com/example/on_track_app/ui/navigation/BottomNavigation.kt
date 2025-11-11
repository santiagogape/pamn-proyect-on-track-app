package com.example.on_track_app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.on_track_app.ui.fragments.navigable.dashboard.DashboardScreen
import com.example.on_track_app.ui.fragments.navigable.home.HomeScreen
import com.example.on_track_app.ui.fragments.navigable.notifications.NotificationsScreen

// --- ROUTES ---
object Destinations {
    const val HOME = "home"
    const val DASHBOARD = "dashboard"
    const val NOTIFICATIONS = "notifications"
}

// --- BottomNavigation principal de la app ---
@Composable
fun BottomNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Destinations.HOME
    ) {
        composable(Destinations.HOME) { HomeScreen() }
        composable(Destinations.DASHBOARD) { DashboardScreen() }
        composable(Destinations.NOTIFICATIONS) { NotificationsScreen() }
    }
}
