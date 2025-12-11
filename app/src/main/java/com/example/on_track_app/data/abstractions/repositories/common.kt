package com.example.on_track_app.data.abstractions.repositories

import kotlinx.coroutines.flow.Flow

interface Initializable {
    suspend fun init()
}
interface UniqueRepository<T> {
    fun get(): T?
}

interface Config {
    var name: String
}

const val LOCAL_CONFIG_ID = "LOCAL_CONFIG"

interface BasicById<T> {
    fun getAll(): Flow<List<T>>
    fun getById(id: String): T?
    suspend fun markAsDeleted(id: String)
    suspend fun delete(id: String)
}

interface IndexedByProject<T> {
    fun byProject(id:String): Flow<List<T>>
}

interface IndexedByOwner<T> {
    fun ownedBy(id:String): Flow<List<T>>
}


