package com.example.on_track_app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.on_track_app.ui.fragments.navigable.calendar.CalendarScreen
import com.example.on_track_app.ui.fragments.navigable.dashboard.DashboardScreen
import com.example.on_track_app.ui.fragments.navigable.home.HomeScreen
import com.example.on_track_app.ui.fragments.navigable.notifications.NotificationsScreen
import com.example.on_track_app.ui.fragments.navigable.projects.ProjectsScreen

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

object Destinations {
    const val HOME = "home"
    const val TASKS = "dashboard"
    const val NOTIFICATIONS = "notifications"

    const val CALENDAR = "calendar"
    const val PROJECTS = "Projects"
}

val NavItems = mapOf(
    Destinations.HOME to NavItem(
        route = Destinations.HOME,
        label = "Home",
        icon = Icons.Filled.Home
    ),
    Destinations.TASKS to NavItem(
        route = Destinations.TASKS,
        label = "Dashboard",
        icon =  Icons.AutoMirrored.Filled.List
    ),
    Destinations.NOTIFICATIONS to NavItem(
        route = Destinations.NOTIFICATIONS,
        label = "Notifications",
        icon = Icons.Filled.Notifications
    ),
    Destinations.PROJECTS to NavItem(
        route = Destinations.PROJECTS,
        label = "Projects",
        icon = Icons.Filled.Dashboard
    ),
    Destinations.CALENDAR to NavItem(
        route = Destinations.CALENDAR,
        label = "Calendar",
        icon = Icons.Filled.Event
    )
)

fun Routes(names:List<String>):List<NavItem>  = names.mapNotNull { NavItems[it] }.toList()


// --- BottomNavigation principal de la app ---
@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Destinations.HOME
    ) {
        composable(Destinations.HOME) { HomeScreen() }
        composable(Destinations.TASKS) { DashboardScreen() }
        composable(Destinations.PROJECTS) { ProjectsScreen() }
        composable(Destinations.CALENDAR) { CalendarScreen() }
    }
}

@Composable
fun ProjectNavigation(navHostController: NavHostController, projectId: String){
    NavHost(
        navController = navHostController,
        startDestination = Destinations.TASKS
    ) {
        composable(Destinations.TASKS) { DashboardScreen(projectId = projectId) }
        composable(Destinations.NOTIFICATIONS) { NotificationsScreen(projectId = projectId) }
        composable(Destinations.CALENDAR) { CalendarScreen(projectId = projectId) }
    }
}