package com.example.on_track_app.viewModels.main

import com.example.on_track_app.model.Identifiable
import com.example.on_track_app.model.Named
import com.example.on_track_app.ui.fragments.reusable.Selectable
import com.example.on_track_app.ui.fragments.reusable.toSelectable

sealed interface ItemStatus<out T> {
    data object Loading : ItemStatus<Nothing>
    data class Success<T>(val elements: T) : ItemStatus<T>
    data object Error : ItemStatus<Nothing>
}

fun <K> ItemStatus<List<K>>.asSelectable(): ItemStatus<List<Selectable>> where  K: Identifiable, K: Named{
    return when(this){
        is ItemStatus.Success -> ItemStatus.Success(this.elements.map {it.toSelectable()})
        ItemStatus.Error -> ItemStatus.Error
        ItemStatus.Loading -> ItemStatus.Loading
    }
}