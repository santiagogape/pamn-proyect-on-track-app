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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.on_track_app.ui.navigation.NavItem
import com.example.on_track_app.ui.navigation.isOnDestination
import com.example.on_track_app.ui.fragments.dialogs.EventCreation
import com.example.on_track_app.ui.fragments.dialogs.TaskCreation
import com.example.on_track_app.ui.fragments.dialogs.ProjectCreation
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.viewModels.CreationViewModel

enum class Dialogs {
    TASK, EVENT, PROJECT, NONE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScaffold(
    header: @Composable (()-> Unit),
    footer: @Composable (()-> Unit),
    navigationTarget: @Composable (() -> Unit)
) {
    val viewModelFactory = LocalViewModelFactory.current
    val creator: CreationViewModel = viewModel(factory = viewModelFactory)
    var showMenu by remember { mutableStateOf(false) }

    var dialog by remember {mutableStateOf(Dialogs.NONE)}


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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

            when (dialog) {
                Dialogs.TASK -> {
                    TaskCreation(
                        onDismiss = { dialog = Dialogs.NONE },
                        onSubmit = { name, description, project, date, hour, minute ->
                            dialog = Dialogs.NONE
                            creator.addNewTask(name, description, project ?: "DEFAULT", date, hour, minute)
                        }
                    )
                }
                Dialogs.EVENT -> {
                    EventCreation(
                        onDismiss = { dialog = Dialogs.NONE },
                        onSubmit = { name, description, project, startDateTime, endDateTime ->
                            dialog = Dialogs.NONE
                            creator.addNewEvent(name, description, project ?: "DEFAULT", startDateTime, endDateTime) //todo: correct hardcoded "DEFAULT" projectID
                        }
                    )
                }
                Dialogs.PROJECT -> {
                    ProjectCreation(
                        onDismiss = {dialog = Dialogs.NONE}
                    ) { name ->

                        creator.addNewProject(name)
                        dialog = Dialogs.NONE }
                }
                Dialogs.NONE -> {}
            }

            if (showMenu) {
                Popup(
                    alignment = Alignment.BottomCenter,
                    onDismissRequest = { showMenu = false },
                    properties = PopupProperties(clippingEnabled = false)
                ) {
                    Box(modifier = Modifier.offset(y = (-40).dp)) {
                        Surface(
                            modifier = Modifier.width(280.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 6.dp
                        ) {
                            androidx.compose.foundation.layout.Column(
                                modifier = Modifier.padding(top = 30.dp, bottom = 16.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("NEW TASK") },
                                    onClick = {
                                        showMenu = false
                                        dialog = Dialogs.TASK
                                    },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) }
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
                                    text = { Text("NEW PROJECT") },
                                    onClick = { showMenu = false; dialog = Dialogs.PROJECT },
                                    leadingIcon = { Icon(Icons.Default.Alarm, contentDescription = null) }
                                )
                            }
                        }
                    }
                }
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

