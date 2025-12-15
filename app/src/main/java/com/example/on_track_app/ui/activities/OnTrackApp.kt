package com.example.on_track_app.ui.activities


import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.on_track_app.data.auth.GoogleAuthClient
import com.example.on_track_app.di.AppViewModelFactory
import com.example.on_track_app.model.Reminder
import com.example.on_track_app.navigation.AppNavigation
import com.example.on_track_app.navigation.Destinations
import com.example.on_track_app.navigation.isOnDestination
import com.example.on_track_app.navigation.routes
import com.example.on_track_app.ui.fragments.reusable.header.MainHeader
import com.example.on_track_app.ui.theme.OnTrackAppTheme
import com.example.on_track_app.domain.viewModels.main.RemindersViewModel
import androidx.lifecycle.lifecycleScope
import com.example.on_track_app.MainActivity
import kotlinx.coroutines.launch

@Composable
fun OnTrackApp(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    factory: AppViewModelFactory,
    userPhotoUrl: String? = null,
    authClient: GoogleAuthClient
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

        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        ActivityScaffold(
            factory = factory,
            header = {
                MainHeader(
                    label = label,
                    darkTheme = darkTheme,
                    onToggleTheme = onToggleTheme,
                    reminders = reminders,
                    pfpUrl = userPhotoUrl,
                    onLogout = {
                        scope.launch {
                            authClient.signOut()

                            // 2. Navigate back to Login Screen
                            val intent = Intent(context, MainActivity::class.java)
                            // Clear back stack so user can't go back to Agenda
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                            (context as? Activity)?.finish()
                        }
                    }
                )
                     },
            footer = { NavBar(navController,items) }
        ){
            AppNavigation(navController = navController, factory = factory)
        }


    }
}
