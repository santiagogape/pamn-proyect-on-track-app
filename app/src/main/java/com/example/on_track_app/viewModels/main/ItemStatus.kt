package com.example.on_track_app.viewModels.main

sealed interface ItemStatus<out T> {
    data object Loading : ItemStatus<Nothing>
    data class Success<T>(val elements: T) : ItemStatus<T>
    data object Error : ItemStatus<Nothing>
}