package com.example.on_track_app.ui.activities

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.on_track_app.R
import com.example.on_track_app.navigation.NavItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScaffold(label: String? = null, navigable:List<NavItem>, controller: NavHostController, darkTheme: Boolean, themeToggle: ()-> Unit, navigationTarget: @Composable (()-> Unit)) {
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    val navBackStackEntry by controller.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    val currentItem = navigable.find { currentDestination.isOnDestination(it.route) }
                    val activity = LocalActivity.current

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        if (label != null) {
                            IconButton(
                                onClick = { activity?.finish() },
                                modifier = Modifier.align(Alignment.CenterStart)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.get_back)
                                )
                            }
                        }
                        Text(
                            text = label ?: currentItem?.label ?: "",
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },

        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { showMenu = true },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add New")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("NEW TASK") },
                        onClick = { showMenu = false },
                        leadingIcon = { Icon(Icons.Default.List, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("NEW EVENT") },
                        onClick = { showMenu = false },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("NEW REMINDER") },
                        onClick = { showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Alarm, contentDescription = null) }
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,

        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                val navBackStackEntry by controller.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                navigable.forEach { item ->
                    val selected = currentDestination.isOnDestination(item.route)
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                controller.navigate(item.route) {
                                    popUpTo(controller.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                            unselectedTextColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            navigationTarget()
        }
    }
}

@Composable
private fun NavDestination?.isOnDestination(route: String): Boolean {
    return this?.hierarchy?.any { it.route == route } == true
}