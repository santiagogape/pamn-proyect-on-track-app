package com.example.on_track_app.domain.viewModels.main

import com.example.on_track_app.model.Expandable
import com.example.on_track_app.ui.fragments.dialogs.CreationStatus

sealed interface ItemStatus {
    data object Loading : ItemStatus
    data class Success(val elements: List<Expandable>) : ItemStatus
    data object Error : ItemStatus
}