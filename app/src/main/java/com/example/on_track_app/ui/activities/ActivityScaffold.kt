package com.example.on_track_app.ui.activities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.on_track_app.di.AppViewModelFactory
import com.example.on_track_app.navigation.NavItem
import com.example.on_track_app.navigation.isOnDestination
import com.example.on_track_app.ui.fragments.dialogs.ActiveDialog
import com.example.on_track_app.ui.fragments.dialogs.GlobalDialogCoordinator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScaffold(
    factory: AppViewModelFactory,
    header: @Composable (()-> Unit),
    footer: @Composable (()-> Unit),
    navigationTarget: @Composable (() -> Unit)
) {
    var showMenu by remember { mutableStateOf(false) }
    var activeDialog by remember { mutableStateOf<ActiveDialog>(ActiveDialog.None) }
    val snackbarHostState = remember { SnackbarHostState() }


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { header() },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },

        floatingActionButton = {
            Box(
                modifier = Modifier.offset(y = 45.dp),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = { showMenu = !showMenu },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add New")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,

        bottomBar = { footer() }

    )  { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            navigationTarget()
            if (showMenu) {
                val density = LocalDensity.current
                val offsetPx = remember(density) {
                    with(density) { -45.dp.roundToPx() }
                }
                Popup(
                    alignment = Alignment.BottomCenter,
                    offset = IntOffset(0, offsetPx),
                    onDismissRequest = { showMenu = false },
                    properties = PopupProperties(clippingEnabled = false)
                ) {
                    Box {
                        Surface(
                            modifier = Modifier.width(280.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 6.dp
                        ) {
                            androidx.compose.foundation.layout.Column(
                                modifier = Modifier.padding(top = 20.dp, bottom = 20.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("NEW TASK") },
                                    onClick = {
                                        showMenu = false
                                        activeDialog = ActiveDialog.CreateTask
                                    },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("NEW PROJECT") },
                                    onClick = {
                                        showMenu = false
                                        activeDialog = ActiveDialog.CreateProject
                                    },
                                    // TODO: Change the icon
                                    leadingIcon = { Icon(Icons.Default.AccountTree, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("NEW EVENT") },
                                    onClick = {
                                        showMenu = false
                                        activeDialog = ActiveDialog.CreateEvent
                                    },
                                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("NEW REMINDER") },
                                    onClick = {
                                        showMenu = false
                                        activeDialog = ActiveDialog.CreateReminder
                                    },
                                    leadingIcon = { Icon(Icons.Default.Alarm, contentDescription = null) }
                                )
                            }
                        }
                    }
                }
            }
            GlobalDialogCoordinator(
                activeDialog = activeDialog,
                onDismiss = { activeDialog = ActiveDialog.None },
                snackbarHostState = snackbarHostState,
                viewModelFactory = factory
            )
        }
    }
}


@Composable
fun NavBar(
    controller: NavHostController,
    navigable: List<NavItem>
) {
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


@Composable
fun NextPrev(onPreviousDay: () -> Unit, onNextDay: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousDay) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Día anterior")
        }
        IconButton(onClick = onNextDay) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Día siguiente")
        }
    }
}

