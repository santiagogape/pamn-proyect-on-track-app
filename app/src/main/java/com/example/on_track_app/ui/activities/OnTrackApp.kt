package com.example.on_track_app.ui.activities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.on_track_app.di.AppViewModelFactory
import com.example.on_track_app.navigation.AppNavigation
import com.example.on_track_app.navigation.Destinations
import com.example.on_track_app.navigation.isOnDestination
import com.example.on_track_app.navigation.routes
import com.example.on_track_app.ui.fragments.reusable.header.MainHeader
import com.example.on_track_app.ui.fragments.reusable.header.OpenRemindersIconButton
import com.example.on_track_app.ui.theme.OnTrackAppTheme

@Composable
fun OnTrackApp(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    factory: AppViewModelFactory
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
            AppNavigation(navController = navController, factory = factory)
        }


    }
}
