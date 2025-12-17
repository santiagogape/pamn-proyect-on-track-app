package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.data.synchronization.UserDTO
import com.example.on_track_app.model.Identifiable
import com.example.on_track_app.model.MockUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant

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

interface BasicById<T: Identifiable> {
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
    fun of(id:String): Flow<List<T>>
}

interface IndexedByLink<T> {
    fun linkedTo(id:String): Flow<List<T>>
    fun linkedTo(ids:List<String>): Flow<List<T>>
}


interface Update<T: Identifiable> {
    suspend fun update(updated: T)
}

interface InTimeInterval<T: Identifiable> {
    fun between(start: Instant, end: Instant): Flow<List<T>>
}

interface GroupAndInterval<T: Identifiable> {
    fun byGroupAndInterval(group:String,start: Instant, end: Instant): Flow<List<T>>
}

interface ProjectAndInterval<T: Identifiable> {
    fun byProjectAndInterval(project:String,start: Instant, end: Instant): Flow<List<T>>
}

interface LinkAndInterval<T: Identifiable>{
    fun byLinkAndInterval(links:List<String>, start: Instant, end: Instant): Flow<List<T>>
}

