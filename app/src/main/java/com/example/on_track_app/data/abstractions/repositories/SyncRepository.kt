package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.data.synchronization.SynchronizableDTO
import kotlinx.coroutines.flow.Flow

interface SyncRepository<T: SynchronizableDTO>{
    fun observeRemoteChanges(): Flow<T>
    suspend fun push(cloudId:String, dto: T)
    fun generateCloudId(): String
    suspend fun delete(dto: T)
    suspend fun getUpdatedAfter(version: Long): List<T>
    suspend fun  getAll(): List<T>
}