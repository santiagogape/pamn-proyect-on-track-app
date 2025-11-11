package com.example.on_track_app.ui.fragments.navigable.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {
    private val _text = MutableStateFlow("This is home screen")
    val text: StateFlow<String> = _text
}
