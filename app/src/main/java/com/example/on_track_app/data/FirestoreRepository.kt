package com.example.on_track_app.data

import android.util.Log
import com.example.on_track_app.model.Expandable
import com.example.on_track_app.model.Reminder
import com.example.on_track_app.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirestoreRepository<T : Any>(
    private val db: FirebaseFirestore,
    private val collectionName: String,
    private val clazz: Class<T>
) {

    suspend fun getElementById(id: String): T? {
        return try {
            val doc = db.collection(collectionName).document(id).get().await()
            val obj = doc.toObject(clazz)
            if (obj != null) {
                injectId(obj, doc.id)
            }
            obj
        } catch (e: Exception) {
            Log.e("Firestore", "Error getting element $id", e)
            null
        }
    }

    suspend fun deleteElement(id: String): Boolean {
        return try {
            db.collection(collectionName).document(id).delete().await()
            true
        } catch (e: Exception) {
            Log.e("Firestore", "Error deleting element $id", e)
            false
        }
    }

    // TODO: Add parameter userId: String and add whereEqualTo() in the query to the DB
    fun getElements(userId: String): Flow<List<T>> = callbackFlow {
        val listenerRegistration = db.collection(collectionName)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // Close the flow on error
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(clazz)?.apply {
                        injectId(this, doc.id)
                    }
                } ?: emptyList()

                trySend(list) // Emit the list to the collector
            }

        // This block runs when the Flow scope is cancelled (e.g., ViewModel cleared)
        awaitClose { listenerRegistration.remove() }
    }

    suspend fun addElement(element: Expandable): Boolean {

        return try {
            db.collection(collectionName).document(element.id).set(element).await()
            true
        } catch (e: Exception) {
            Log.e("Firestore", "Error adding document", e)
            false
        }
    }

    suspend fun updateElement(id: String, element: T): Boolean {
        val data = elementToMap(element)
        if (data.isEmpty()) return false

        return try {
            db.collection(collectionName).document(id).set(data).await()
            true
        } catch (e: Exception) {
            Log.e("Firestore", "Error updating document ($id)", e)
            false
        }
    }

    fun getElementsByProjectId(projectId: String): Flow<List<Expandable>> = callbackFlow {
        val listenerRegistration = db.collection(collectionName)
            .whereEqualTo("projectId", projectId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Error fetching project tasks (ID=$projectId)", error)
                    close(error)
                    return@addSnapshotListener
                }

                val list = snapshot?.toObjects(Task::class.java) ?: emptyList()
                trySend(list)
            }

        awaitClose { listenerRegistration.remove() }
    }

    fun getProjectReminders(userId: String, projectTasks: Flow<List<Task>>): Flow<List<Reminder>> {
        // TODO: Add events to the filtering logic
        val allReminders: Flow<List<Reminder>> = getElements(userId) as Flow<List<Reminder>>
        return combine(projectTasks, allReminders) { tasks, reminders ->
            val projectTasksIds = tasks.map { it.id }.toSet()
            // val projectEventsIds = events.map { it.projectId }.toSet()
            reminders.filter { reminder ->
                (reminder.taskId) != null && projectTasksIds.contains(reminder.taskId)
                // || (reminder.eventId != null && projectEventIds.contains(reminder.eventId))
            } as List<Reminder>
        }
    }

    private fun injectId(obj: T, id: String) {
        try {
            val field = clazz.declaredFields.find { it.name == "id" }
            field?.isAccessible = true
            field?.set(obj, id)
        } catch (e: Exception) {
            Log.e("Firestore", "Reflection error injecting ID", e)
        }
    }

    private fun elementToMap(element: T): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        try {
            clazz.declaredFields.forEach { field ->
                field.isAccessible = true
                val value = field.get(element)
                if (value != null) {
                    map[field.name] = value
                }
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Reflection error mapping object", e)
        }
        return map
    }
}