package com.example.on_track_app.ui.activities

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.on_track_app.R
import com.example.on_track_app.ui.fragments.dialogs.GlobalDialogCoordinator
import com.example.on_track_app.ui.navigation.Destinations
import com.example.on_track_app.ui.navigation.NavItem
import com.example.on_track_app.ui.navigation.isOnDestination
import com.example.on_track_app.utils.LocalCreationContext
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.viewModels.CreationSourcesViewModel
import com.example.on_track_app.viewModels.ProjectCreationContext
import java.time.LocalDate

enum class Dialogs {
    TASK, EVENT, PROJECT, NONE, REMINDER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScaffold(
    header: @Composable (()-> Unit),
    footer: @Composable (()-> Unit),
    sources: CreationSourcesViewModel =  viewModel(factory = LocalViewModelFactory.current),
    currentDate: LocalDate? = null,
    navigationTarget: @Composable (() -> Unit)
) {
    var showMenu by remember { mutableStateOf(false) }
    var dialog by remember {mutableStateOf(Dialogs.NONE)}
    val snackBarHostState = remember { SnackbarHostState() }
    val creationContext = LocalCreationContext.current
    val inProject = remember(creationContext) {
        creationContext is ProjectCreationContext
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
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
                    with(density) { -20.dp.roundToPx() }
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
                            Column(
                                modifier = Modifier.padding(top = 20.dp, bottom = 20.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("NEW TASK") },
                                    onClick = {
                                        showMenu = false
                                        dialog = Dialogs.TASK
                                    },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) }
                                )
                                if (!inProject)
                                    DropdownMenuItem(
                                        text = { Text("NEW PROJECT") },
                                        onClick = {
                                            showMenu = false
                                            dialog = Dialogs.PROJECT
                                        },
                                        leadingIcon = { Icon(Icons.Default.AccountTree, contentDescription = null) }
                                    )
                                DropdownMenuItem(
                                    text = { Text("NEW EVENT") },
                                    onClick = {
                                        showMenu = false
                                        dialog = Dialogs.EVENT
                                    },
                                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("NEW REMINDER") },
                                    onClick = {
                                        showMenu = false
                                        dialog = Dialogs.REMINDER
                                    },
                                    leadingIcon = { Icon(Icons.Default.Alarm, contentDescription = null) }
                                )
                            }
                        }
                    }
                }
            }
            if (dialog != Dialogs.NONE) {
                GlobalDialogCoordinator(
                    activeDialog = dialog,
                    onDismiss = { dialog = Dialogs.NONE },
                    snackBarHostState = snackBarHostState,
                    sources = sources,
                    currentDate = currentDate
                )
            }
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
                label = { Text(when (item.label){
                    Destinations.HOME -> stringResource(R.string.nav_home)
                    Destinations.TASKS -> stringResource(R.string.nav_tasks)
                    Destinations.PROJECTS -> stringResource(R.string.nav_projects)
                    Destinations.CALENDAR -> stringResource(R.string.nav_calendar)
                    Destinations.NEXT -> stringResource(R.string.nav_next)
                    Destinations.PREV -> stringResource(R.string.nav_prev)
                    else -> ""
                }) },
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



