package com.example.on_track_app.ui.activities

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.on_track_app.R
import com.example.on_track_app.ui.theme.OnTrackAppTheme
import com.example.on_track_app.navigation.AppNavigation
import com.example.on_track_app.navigation.Destinations
import com.example.on_track_app.navigation.NavItem
import com.example.on_track_app.navigation.isOnDestination
import com.example.on_track_app.navigation.routes

@Composable
fun OnTrackApp(darkTheme: Boolean,
               onToggleTheme: () -> Unit) {

    OnTrackAppTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()

        // Bottom navigation items
        val items = routes(listOf(
            Destinations.HOME,
            Destinations.TASKS,
            Destinations.PROJECTS,
            Destinations.CALENDAR
        ))

        ActivityScaffold(
            header = {Header(navController, navigable = items,onToggleTheme,darkTheme)},
            footer = { NavBar(navController,items) }
        ){
            AppNavigation(navController = navController)
        }


    }
}



@Composable
private fun Header(
    controller: NavHostController,
    navigable: List<NavItem>,
    themeToggle: () -> Unit,
    darkTheme: Boolean
) {
    val navBackStackEntry by controller.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentItem = navigable.find { currentDestination.isOnDestination(it.route) }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {

        Text(
            text = currentItem?.label ?: "",
            color = MaterialTheme.colorScheme.onBackground
        )
        IconButton(
            onClick = themeToggle,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = if (darkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = stringResource(R.string.theme_toggle)
            )
        }
    }
}
