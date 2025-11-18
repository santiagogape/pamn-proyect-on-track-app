package com.example.on_track_app.ui.activities

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.on_track_app.ui.theme.OnTrackAppTheme
import com.example.on_track_app.navigation.AppNavigation
import com.example.on_track_app.navigation.Destinations
import com.example.on_track_app.navigation.Routes

@Composable
fun OnTrackApp(darkTheme: Boolean,
               onToggleTheme: () -> Unit) {

    OnTrackAppTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()

        // Bottom navigation items
        val items = Routes(listOf(
            Destinations.HOME,
            Destinations.TASKS,
            Destinations.PROJECTS,
            Destinations.CALENDAR
        ))

        ActivityScaffold(
            navigable = items,
            controller = navController,
            darkTheme = darkTheme,
            themeToggle = onToggleTheme
        ){
            AppNavigation(navController = navController)
        }


    }
}
