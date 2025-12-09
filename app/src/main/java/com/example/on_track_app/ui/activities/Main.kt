package com.example.on_track_app.ui.activities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.on_track_app.ui.navigation.AppNavigation
import com.example.on_track_app.ui.navigation.Destinations
import com.example.on_track_app.ui.navigation.isOnDestination
import com.example.on_track_app.ui.navigation.routes
import com.example.on_track_app.ui.fragments.reusable.header.MainHeader
import com.example.on_track_app.ui.theme.OnTrackAppTheme

@Composable
fun Main(darkTheme: Boolean,
         onToggleTheme: () -> Unit,
         ) {

    OnTrackAppTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()



        // Bottom navigation items
        val items = routes(listOf(
            Destinations.HOME,
            Destinations.TASKS,
            Destinations.PROJECTS,
            Destinations.CALENDAR
        ))

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val label = items.find { navBackStackEntry?.destination.isOnDestination(it.route) }?.label ?: ""

        ActivityScaffold(
            header = {
                MainHeader(label,darkTheme,onToggleTheme,null)
                     },
            footer = { NavBar(navController,items) }
        ){
            AppNavigation(navController = navController)
        }


    }
}
