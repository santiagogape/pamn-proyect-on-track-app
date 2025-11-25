package com.example.on_track_app.ui.activities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.on_track_app.navigation.NavItem
import com.example.on_track_app.navigation.isOnDestination
import com.example.on_track_app.ui.fragments.dialogs.EventCreation
import com.example.on_track_app.ui.fragments.dialogs.TaskCreation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScaffold(
    header: @Composable (()-> Unit),
    footer: @Composable (()-> Unit),
    navigationTarget: @Composable (() -> Unit)
) {
    var showMenu by remember { mutableStateOf(false) }
    var taskDialogVisible by remember { mutableStateOf(false) }
    var eventDialogVisible by remember { mutableStateOf(false) }

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
            Box (
                modifier = Modifier.offset(y = 45.dp),
                contentAlignment = Alignment.Center
            ){
                FloatingActionButton(
                    onClick = { showMenu = !showMenu },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add New")
                }
                if (showMenu){
                    androidx.compose.ui.window.Popup(
                        alignment = Alignment.TopCenter,
                        onDismissRequest = {showMenu = false}
                    ) {
                        Box (
                            modifier = Modifier.offset(y = (-210).dp)
                        ){

                        }
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,

        bottomBar = { footer() }

    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            navigationTarget()
            if (taskDialogVisible) {
                TaskCreation(
                    onDismiss = { taskDialogVisible = false },
                    onSubmit = { name, description, project, date, hour, minute ->
                        taskDialogVisible = false
                    }
                )
            } else if (eventDialogVisible) {
                EventCreation(
                    onDismiss = { eventDialogVisible = false },
                    onSubmit = { name, description, project, startDateTime, endDateTime ->
                        taskDialogVisible = false
                    }
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

