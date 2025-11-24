package com.example.on_track_app.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.example.on_track_app.model.User
import com.example.on_track_app.model.Task
import com.example.on_track_app.model.Group

class FirestoreRepository(
    private val db: FirebaseFirestore
) {
    fun addUser(username: String, email: String, onResult: (Boolean) -> Unit, groups: List<Group>? = null) {
        val data = hashMapOf(
            "username" to username,
            "email" to email,
            "group" to groups
        )

        db.collection("users")
            .add(data)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding document (user)", e)
                onResult(false)
            }
    }

    fun getUsers(onResult: (List<User>) -> Unit) {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { doc ->
                    val user = doc.toObject(User::class.java)
                    user?.copy(id = doc.id)
                }
                onResult(list)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching users", e)
                onResult(emptyList())
            }
    }

    fun updateUser(
        userId: String,
        onResult: (Boolean) -> Unit,
        username: String? = null,
        email: String? = null,
        groups: List<Group>? = null
    ) {
        val user = hashMapOf(
            "username" to username,
            "email" to email,
            "groups" to groups
        )

        db.collection("users")
            .document(userId)
            .set(user)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("Firestore", String.format("Error updating user by ID=%d", userId), e)
            }
    }

    fun getUserById(userId: String, onResult: (User?) -> Unit) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java)
                user?.copy(id = doc.id)
                onResult(user)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching user by ID", e)
                onResult(User())
            }
    }

    fun deleteUser(userId: String, onResult: (Boolean) -> Unit) {
        db.collection("users")
            .document(userId)
            .delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error deleting user by ID", e)
                onResult(false)
            }
    }

    fun addTask(name: String, description: String, date: String, onResult: (Boolean) -> Unit) {
        val data = hashMapOf(
            "name" to name,
            "description" to description,
            "date" to date
        )

        db.collection("tasks")
            .add(data)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding document (task)", e)
                onResult(false)
            }
    }

    fun getTasks(onResult: (List<Task>) -> Unit) {
        db.collection("tasks")
            .get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { doc ->
                    val task = doc.toObject(Task::class.java)
                    task?.copy(id = doc.id)
                }
                onResult(list)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching tasks", e)
                onResult(emptyList())
            }
    }
}