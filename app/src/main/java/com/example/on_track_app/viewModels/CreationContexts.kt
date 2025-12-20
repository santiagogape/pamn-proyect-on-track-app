package com.example.on_track_app.viewModels

import com.example.on_track_app.model.User


//todo replace LocalOwnership y LocalReference for the interfaces here

// project
sealed interface OwnerContext { val ownerId: String }
data class UserOwnerContext(private val user: User): OwnerContext{
    override val ownerId: String = user.id
}
data class GroupOwnerContext(override val ownerId:String): OwnerContext

// creation common
sealed interface CreationContext{ val ownerId:String;val projectId: String?}
data class UserCreationContext(private val user: User): CreationContext{
    override val projectId: String? = null
    override val ownerId:String = user.id
}
data class GroupCreationContext(override val ownerId:String): CreationContext{
    override val projectId: String? = null
}
data class ProjectCreationContext(override val ownerId:String, override val projectId: String): CreationContext

//reminder
sealed interface ReminderCreationContext {val ownerId:String;val linkedTo:String?}
data class UserReminderCreationContext(private val user: User): ReminderCreationContext{
    override val linkedTo: String? = null
    override val ownerId:String = user.id
}
data class GroupReminderCreationContext(override val ownerId:String): ReminderCreationContext{
    override val linkedTo: String? = null
}

data class TaskReminderCreationContext(
    override val ownerId: String,
    override val linkedTo: String
): ReminderCreationContext
data class EventReminderCreationContext(
    override val ownerId: String,
    override val linkedTo: String
): ReminderCreationContext