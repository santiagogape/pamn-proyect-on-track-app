package com.example.on_track_app.viewModels

import com.example.on_track_app.model.Link
import com.example.on_track_app.model.LinkedType
import com.example.on_track_app.model.MockEvent
import com.example.on_track_app.model.MockGroup
import com.example.on_track_app.model.MockProject
import com.example.on_track_app.model.MockTask
import com.example.on_track_app.model.OwnerType
import com.example.on_track_app.model.Ownership

//todo replace LocalOwnership y LocalReference for the interfaces here

//group
sealed interface ProjectCreationContext: Ownership
data class UserProjectsContext(val userId: String, val availableGroups: List<MockGroup>): ProjectCreationContext{
    override val ownerType: OwnerType = OwnerType.USER
    override val ownerId: String = userId //current
}

data class GroupProjectsContext(val groupId: String, val userId: String): ProjectCreationContext{
    override val ownerType: OwnerType = OwnerType.GROUP
    override val ownerId: String = groupId //current
}



//task, event
sealed interface CreationContext: Ownership {
    override val ownerType: OwnerType
    override val ownerId: String
    val availableProjects: List<MockProject>
    val currentProject: String
}

data class Context(
    val userId: String,
    override val availableProjects: List<MockProject>,//user -> all
    override val currentProject: String //user -> all
) : CreationContext {
    override val ownerType = OwnerType.USER
    override val ownerId = userId
}

data class GroupContext(
    val groupId: String,
    override val availableProjects: List<MockProject>, //group
    override val currentProject: String //group
) : CreationContext {
    override val ownerType = OwnerType.GROUP
    override val ownerId = groupId
}

//reminder
sealed interface ReminderCreationContext: Ownership {
    val link: Link?
    val availableTasks: List<MockTask>
    val availableEvents: List<MockEvent>
}

data class RemindersContext(
    val userId: String,
    override val availableTasks: List<MockTask>,
    override val availableEvents: List<MockEvent>
): ReminderCreationContext {
    override val link: Link? = null
    override val ownerType: OwnerType = OwnerType.USER
    override val ownerId: String = userId
}

data class GroupRemindersContext(
    val groupId: String,
    override val availableTasks: List<MockTask>,
    override val availableEvents: List<MockEvent>
): ReminderCreationContext {
    override val link: Link? = null
    override val ownerType: OwnerType = OwnerType.GROUP
    override val ownerId: String = groupId
}

data class TaskRemindersContext(
    val taskId: String, //current
    val ownerContext: ReminderCreationContext, //UserReminderContext o GroupRemindersContext
): ReminderCreationContext{
    override val availableTasks: List<MockTask> = ownerContext.availableTasks
    override val availableEvents: List<MockEvent> = ownerContext.availableEvents
    override val link: Link = Link(taskId, LinkedType.TASK) //current
    override val ownerType: OwnerType = ownerContext.ownerType
    override val ownerId: String = ownerContext.ownerId
}

data class EventReminderContext(
    val eventId: String,
    val ownerContext: ReminderCreationContext, //UserReminderContext o GroupRemindersContext
): ReminderCreationContext {
    override val availableTasks: List<MockTask> = ownerContext.availableTasks
    override val availableEvents: List<MockEvent> = ownerContext.availableEvents
    override val link: Link = Link(eventId, LinkedType.EVENT)
    override val ownerType: OwnerType = ownerContext.ownerType
    override val ownerId: String = ownerContext.ownerId
}


