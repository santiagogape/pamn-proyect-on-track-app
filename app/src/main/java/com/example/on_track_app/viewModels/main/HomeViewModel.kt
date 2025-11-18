package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {
    private val _text = MutableStateFlow("This is home screen")
    val text: StateFlow<String> = _text

    private val _items = MutableStateFlow(listOf("hi", "bye", "welcome"))
    val items: StateFlow<List<String>> = _items
}