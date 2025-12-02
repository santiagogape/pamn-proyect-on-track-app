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
    private val db: FirebaseFirestore,
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

    fun addUser(
        username: String,
        email: String,
        onResult: (Boolean) -> Unit,
        groups: List<Group>? = null
    ) {
        val data = mutableMapOf<String,Any>(
            "username" to username,
            "email" to email
        )

        groups?.let { data["group"] = it }

        db.collection(collectionName)
            .add(data)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding document (user)", e)
                onResult(false)
            }
    }

    fun updateUser(
        userId: String,
        onResult: (Boolean) -> Unit,
        username: String? = null,
        email: String? = null,
        groups: List<Group>? = null
    ) {
        val user = mutableMapOf<String, Any>()

        username?.let { user["name"] = it }
        email?.let { user["email"] = it }
        groups?.let { user["groups"] = it }

        if (user.isEmpty()) {
            onResult(false)
            return
        }

        db.collection(collectionName)
            .document(userId)
            .set(user)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("Firestore", String.format("Error updating user by ID (%s)", userId), e)
            }
    }

    fun addTask(
        name: String,
        description: String,
        date: String,
        onResult: (Boolean) -> Unit
    ) {
        val data = hashMapOf<String, Any>(
            "name" to name,
            "description" to description,
            "date" to date
        )

        db.collection(collectionName)
            .add(data)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding document (task)", e)
                onResult(false)
            }
    }

    fun updateTask(
        taskId: String,
        onResult: (Boolean) -> Unit,
        name: String? = null,
        date: String? = null,
        description: String? = null,
        reminders: List<Reminder>? = null,
        project: Project? = null
    ) {
        val task = mutableMapOf<String, Any>()

        name?.let { task["name"] = it }
        date?.let { task["date"] = it }
        description?.let { task["description"] = it }
        reminders?.let { task["reminders"] = it }
        project?.let { task["project"] = it }

        if (task.isEmpty()) {
            onResult(false)
            return
        }

        db.collection(collectionName)
            .document(taskId)
            .set(task)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("Firestore", String.format("Error updating task by ID (%s)", taskId))
            }
    }

    fun addEvent(
        name: String,
        description: String,
        startDate: String,
        onResult: (Boolean) -> Unit,
        project: Project? = null,
        startTime: String? = null,
        endTime: String? = null,
        endDate: String? = null
    ) {
        val data = mutableMapOf<String, Any>(
            "name" to name,
            "description" to description,
            "startDate" to startDate,
        )

        project?.let { data["project"] = it }
        startTime?.let{ data["startTime"] = it }
        endTime?.let{ data["endTime"] = it }
        endDate?.let{ data["endDate"] = it }

        db.collection(collectionName)
            .add(data)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("Firestore", String.format("Error adding document (%s)", collectionName), e)
                onResult(false)
            }
    }

    fun updateEvents(
        eventId: String,
        name: String? = null,
        description: String? = null,
        startDate: String? = null,
        onResult: (Boolean) -> Unit,
        project: Project? = null,
        startTime: String? = null,
        endTime: String? = null,
        endDate: String? = null
    ) {
        val event = mutableMapOf<String, Any>()

        name?.let { event["name"] = it }
        description?.let { event["description"] = it }
        startDate?.let { event["startDate"] = it }
        project?.let { event["project"] = it }
        startTime?.let { event["startTime"] = it }
        endTime?.let { event["endTime"] = it }
        endDate?.let { event["endDate"] = it }

        if (event.isEmpty()) {
            onResult(true)
            return
        }

        db.collection(collectionName)
            .document(eventId)
            .set(event)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("Firestore", String.format("Error updating event by ID (%s)", eventId), e)
            }
    }

    fun addProject(
        id: String,
        name: String,
        onResult: (Boolean) -> Unit,
        members: List<User>? = null
    ) {
        val data = mutableMapOf<String, Any>(
            "name" to name,
        )

        members?.let { data["members"] }

        db.collection(collectionName)
            .add(data)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding document (project)", e)
                onResult(true)
            }
    }

    fun updateProject(
        projectId: String,
        onResult: (Boolean) -> Unit,
        name: String? = null,
        members: List<User>? = null
    ) {
        val project = mutableMapOf<String, Any>()

        name?.let { project["name"] = it }
        members?.let { project["members"] = it }

        if (project.isEmpty()) {
            onResult(true)
            return
        }

        db.collection(collectionName)
            .document(projectId)
            .set(project)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("Firestore", String.format("Error updating project by ID (%s)", projectId), e)
            }
    }

    fun addReminder(
        date: String,
        time: String,
        onResult: (Boolean) -> Unit,
        tasks: List<Task>? = null
    ) {
        val data = hashMapOf<String, Any>(
            "date" to date,
            "time" to time
        )

        tasks?.let { data["tasks"] = it }

        db.collection(collectionName)
            .add(data)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding document (reminder)", e)
                onResult(false)
            }

    }

    fun updateReminder(
        reminderId: String,
        onResult: (Boolean) -> Unit,
        date: String? = null,
        time: String? = null,
        tasks: List<Task>? = null
    ) {
        val reminder = mutableMapOf<String, Any>()

        date?.let { reminder["date"] = it }
        time?.let { reminder["time"] = it }
        tasks?.let { reminder["tasks"] = it }

        if (reminder.isEmpty()) {
            onResult(false)
            return
        }

        db.collection(collectionName)
            .document(reminderId)
            .set(reminder)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("Firestore", String.format("Error updating reminder by ID (%s)", reminderId), e)
            }
    }

    fun addGroup(
        members: List<User>,
        onResult: (Boolean) -> Unit
    ) {
        val data = mutableMapOf<String, Any>(
            "members" to members
        )

        db.collection(collectionName)
            .add(data)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding document (group)", e)
                onResult(false)
            }
    }

    fun updateGroup(
        groupId: String,
        members: List<User>,
        onResult: (Boolean) -> Unit,
        name: String? = null
    ) {
        val group = mutableMapOf<String, Any>(
            "members" to members
        )

        name?.let { group["name"] = it }

        if (group.isEmpty()) {
            onResult(false)
            return
        }

        db.collection(collectionName)
            .document(groupId)
            .set(group)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("Firestore", String.format("Error updating group by ID (%s)", groupId), e)
            }
    }

}