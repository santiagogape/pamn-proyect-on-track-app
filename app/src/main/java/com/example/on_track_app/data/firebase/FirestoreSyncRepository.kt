package com.example.on_track_app.data.firebase

import com.example.on_track_app.data.abstractions.repositories.SyncRepository
import com.example.on_track_app.data.synchronization.SynchronizableDTO
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

@Suppress("UNCHECKED_CAST")
class FirestoreSyncRepository<T: SynchronizableDTO>(
    db: FirebaseFirestore,
    collectionName: String,
    private val clazz: Class<T>
): SyncRepository<T> {
    override fun generateCloudId(): String {
        return collection.document().id
    }

    private val collection: CollectionReference = db.collection(collectionName)

    override fun observeRemoteChanges(): Flow<T> = callbackFlow {
        val reg = collection.addSnapshotListener { snap, err ->
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
        collection.document(cloudId).set(dto, SetOptions.merge()).await()
    }


    override suspend fun delete(dto: T) {
        val id = dto.cloudId ?: return
        collection.document(id).delete().await()
    }

    override suspend fun getUpdatedAfter(version: Long): List<T> {
        return collection
            .whereGreaterThan("version", version)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(clazz) }
    }

    override suspend fun getAll(): List<T> {
        return collection
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(clazz) }
    }

}

