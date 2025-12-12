package com.example.on_track_app.ui.fragments.dialogs

sealed interface ActiveDialog {
    object None : ActiveDialog
    object CreateTask : ActiveDialog
    object CreateEvent : ActiveDialog
    object CreateProject : ActiveDialog
    object CreateReminder : ActiveDialog
}