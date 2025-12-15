package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.data.synchronization.UserDTO
import com.example.on_track_app.model.MockUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface Initializable {
    suspend fun init(remote: MockUser): UserDTO
}
interface UniqueRepository<T> {
    val config: StateFlow<T?>
    fun ready(): Boolean
    fun get(): T
}

interface Config {
    var name: String
}

const val LOCAL_CONFIG_ID = "LOCAL_CONFIG"

interface BasicById<T> {
    fun getAll(): Flow<List<T>>
    fun getById(id: String): T?
    fun liveById(id: String): Flow<T?>
    suspend fun markAsDeleted(id: String)
    suspend fun delete(id: String)
}

interface IndexedByProject<T> {
    fun byProject(id:String): Flow<List<T>>
}

interface IndexedByOwner<T> {
    fun ownedBy(id:String): Flow<List<T>>
}


