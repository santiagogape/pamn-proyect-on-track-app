package com.example.on_track_app.viewModels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.reflect.KClass


class ViewModelsFactoryMock(
    private val factoryMap: Map<KClass<out ViewModel>,FactoryEntry<out ViewModel>>,
): ViewModelProvider.Factory {
    data class FactoryEntry<V>(
        val vmClass: Class<out V>,
        val creator: () -> V
    ) where V: ViewModel

    //makers
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val entry = factoryMap[modelClass.kotlin]
            ?: throw IllegalArgumentException()
        return entry.creator() as T
    }

}