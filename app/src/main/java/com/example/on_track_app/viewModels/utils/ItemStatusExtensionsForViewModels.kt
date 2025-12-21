package com.example.on_track_app.viewModels.utils

import com.example.on_track_app.viewModels.main.ItemStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

fun <T> Flow<T>.asItemStatus(): Flow<ItemStatus<T>> =
    this.map<T,ItemStatus<T>> { list ->
        ItemStatus.Success(list)
    }.onStart {
            emit(ItemStatus.Loading)
        }

fun <T> Flow<T>.asItemStatus(
    scope: CoroutineScope,
    started: SharingStarted = SharingStarted.WhileSubscribed(5_000),
    filter: ((T)-> T)? = null
): StateFlow<ItemStatus<T>> =
    this.map<T,ItemStatus<T>> { list ->
        filter?.let { ItemStatus.Success(filter(list)) }  ?: ItemStatus.Success(list)
    }
        .onStart {
            emit(ItemStatus.Loading)
        }
        .stateIn(
            scope,
            started,
            ItemStatus.Loading
        )

inline fun <T> ItemStatus<T>.filter(
    filter: (T) -> T
): ItemStatus<T> =
    when (this) {
        is ItemStatus.Success -> ItemStatus.Success(filter(elements) )
        else -> this
    }

inline fun <T,R> ItemStatus<T>.map(
    mapper: (ItemStatus.Success<T>) -> ItemStatus<R>
): ItemStatus<R> =
    when (this) {
        is ItemStatus.Success -> mapper(this)
        ItemStatus.Error -> ItemStatus.Error
        ItemStatus.Loading -> ItemStatus.Loading
    }


fun <T,R> Flow<ItemStatus<T>>.mapItemStatus(
    scope: CoroutineScope,
    started: SharingStarted = SharingStarted.WhileSubscribed(5_000),
    mapper: (ItemStatus.Success<T>) -> ItemStatus<R>
): StateFlow<ItemStatus<R>> =
    this.map<ItemStatus<T>,ItemStatus<R>> { list ->
        list.map(mapper)
    }
        .onStart {
            emit(ItemStatus.Loading)
        }
        .stateIn(
            scope,
            started,
            ItemStatus.Loading
        )