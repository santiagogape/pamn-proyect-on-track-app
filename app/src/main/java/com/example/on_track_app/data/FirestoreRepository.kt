package com.example.on_track_app.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.example.on_track_app.model.User
import com.example.on_track_app.model.Task
import com.example.on_track_app.model.Group
import com.example.on_track_app.model.Reminder
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Event

class FirestoreRepository<T: Any>(
    private val db: FirebaseFirestore = FirestoreService.firestore,
    private val collectionName: String,
    private val clazz: Class<T>
) {
    fun getElementById(id: String, onResult: (T?) -> Unit) {
        db.collection(collectionName)
            .document(id)
            .get()
            .addOnSuccessListener { doc ->
                val obj = doc.toObject(clazz)
                if (obj != null) {
                    val field = clazz.declaredFields.find { it.name == "id" }
                    field?.isAccessible = true
                    field?.set(obj, doc.id)
                }
                onResult(obj)
            }
            .addOnFailureListener {
                onResult(null)
            }

    }

    fun deleteElement(id: String, onResult: (Boolean) -> Unit) {
        db.collection(collectionName)
            .document(id)
            .delete()
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun getElements(onResult: (List<T>) -> Unit) {
        db.collection(collectionName)
            .get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { doc ->
                    doc.toObject(clazz)?.apply {
                        val field = clazz.declaredFields.find { it.name == "id" }
                        field?.isAccessible = true
                        field?.set(this, doc.id)
                    }
                }
                onResult(list)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun elementToMap(element: T): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        clazz.declaredFields.forEach { field ->
            field.isAccessible = true
            val value = field.get(element)
            if (value != null) {
                map[field.name] = value
            }
        }
        return map
    }

    fun addElement(
        element: T,
        onResult: (Boolean) -> Unit
    ) {
        val data = elementToMap(element)
        db.collection(collectionName)
            .add(data)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding document", e)
                onResult(false)
            }
    }

    fun updateElement(
        id: String,
        element: T,
        onResult: (Boolean) -> Unit
    ) {
        val data = elementToMap(element)
        if (data.isEmpty()) {
            onResult(false)
            return
        }

        db.collection(collectionName)
            .document(id)
            .set(data)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating document ($id)", e)
                onResult(false)
            }
    }

    fun getTasksByProject(
        projectId: String,
        onResult: (List<Task>) -> Unit
    ) {
        db.collection(collectionName)
            .whereEqualTo("projectId", projectId)
            .get()
            .addOnSuccessListener { result ->
                val list = result.toObjects(Task::class.java)
                onResult(list)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", String.format("Error fetching project tasks (ID=%s)", projectId), e)
                onResult(emptyList())
            }
    }

}