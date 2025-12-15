package com.example.on_track_app.ui.activities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.on_track_app.di.AppViewModelFactory
import com.example.on_track_app.model.Reminder
import com.example.on_track_app.navigation.AppNavigation
import com.example.on_track_app.navigation.Destinations
import com.example.on_track_app.navigation.isOnDestination
import com.example.on_track_app.navigation.routes
import com.example.on_track_app.ui.fragments.reusable.header.MainHeader
import com.example.on_track_app.ui.theme.OnTrackAppTheme
import com.example.on_track_app.domain.viewModels.main.RemindersViewModel

@Composable
fun OnTrackApp(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    factory: AppViewModelFactory,
    userPhotoUrl: String? = null
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

        val remindersViewModel: RemindersViewModel = viewModel(factory = factory)
        val reminders: List<Reminder> by remindersViewModel.reminders.collectAsStateWithLifecycle()

        ActivityScaffold(
            factory = factory,
            header = {
                MainHeader(label,darkTheme,onToggleTheme, reminders,userPhotoUrl)
                     },
            footer = { NavBar(navController,items) }
        ){
            AppNavigation(navController = navController, factory = factory)
        }


    }
}
