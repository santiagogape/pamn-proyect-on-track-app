package com.example.on_track_app.data.firebase

import com.example.on_track_app.data.abstractions.repositories.SyncRepository
import com.example.on_track_app.data.synchronization.SynchronizableDTO
import com.example.on_track_app.data.synchronization.UserDTO
import com.example.on_track_app.utils.DebugLogcatLogger
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

@Suppress("UNCHECKED_CAST")
class FirestoreSyncRepository<T: SynchronizableDTO>(
    private val db: FirebaseFirestore,
    private val collectionName: String,
    private val clazz: Class<T>
): SyncRepository<T> {

    override fun generateCloudId(): String {
        return collection.document().id
    }

    private val collection: CollectionReference = this.db.collection(collectionName)
    private var userId = ""
    val filter = when {
        clazz.isAssignableFrom(UserDTO::class.java) ->
            "cloudId"
        else -> "cloudOwnerId"
    }

    //todo -> group -> new observeRemoteChanges call
    override fun observeRemoteChanges(): Flow<T> = callbackFlow {
        val reg = collection.whereEqualTo(filter, userId).addSnapshotListener { snap, err ->
            if (err != null) {
                close(err)
                return@addSnapshotListener
            }

            snap?.documents?.forEach { doc ->
                doc.toObject(clazz)?.let { trySend(it) }
            }
        }

        awaitClose { reg.remove() }
    }


    override suspend fun push(cloudId:String, dto: T) {
        DebugLogcatLogger.log("PUSH ${collection.path}/$cloudId")
        collection.document(cloudId).set(dto, SetOptions.merge()).await()
    }


    override suspend fun delete(dto: T) {
        val id = dto.cloudId ?: return
        collection.document(id).delete().await()
    }

    override suspend fun getUpdatedAfter(version: Long): List<T> {
        return collection.whereEqualTo(filter,userId)
            .whereGreaterThan("version", version)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(this.clazz) }
    }

    override suspend fun getAll(): List<T> {
        return collection
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(this.clazz) }
    }

    override fun setUserId(id: String) {
        userId = id
    }

}

