package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Group
import com.example.on_track_app.model.Linkable
import com.example.on_track_app.model.Membership
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Reminder
import com.example.on_track_app.model.Task
import com.example.on_track_app.model.TimeField
import com.example.on_track_app.model.User
import com.example.on_track_app.model.UserMembership
import com.example.on_track_app.viewModels.OwnerContext
import kotlinx.coroutines.flow.Flow


interface UserRepository : BasicById<User> {

    suspend fun addUser(
        username: String,
        email: String
    ): String

    suspend fun updateUser(
        id: String,
        newEmail: String
    )
}



interface GroupRepository :
    BasicById<Group>,
    IndexedByOwner<Group>,
    Update<Group> {

    suspend fun addGroup(
        name: String,
        description: String,
        owner: OwnerContext
    ): String
}



interface ProjectRepository :
    BasicById<Project>,
    IndexedByOwner<Project>,
    Update<Project> {

    suspend fun addProject(
        name: String,
        description: String,
        owner: OwnerContext
    ): String
}



interface MembershipRepository :
    BasicById<UserMembership> {

    suspend fun addMembership(
        user: User,
        membership: Membership
    ): String


    fun byMember(user:User): Flow<List<UserMembership>>
    fun byMembership(membership: Membership): Flow<List<UserMembership>>

}



interface TaskRepository :
    BasicById<Task>,
    IndexedByProject<Task>,
    IndexedByOwner<Task>,
    Update<Task>,
    InTimeInterval<Task>,
    GroupAndInterval<Task>,
    ProjectAndInterval<Task> {

    suspend fun addTask(
        name: String,
        description: String,
        date: TimeField,
        owner: OwnerContext,
        project: Project?
    ): String
}



interface EventRepository :
    BasicById<Event>,
    IndexedByProject<Event>,
    IndexedByOwner<Event>,
    Update<Event>,
    InTimeInterval<Event>,
    GroupAndInterval<Event>,
    ProjectAndInterval<Event> {

    suspend fun addEvent(
        name: String,
        description: String,
        start: TimeField,
        end: TimeField,
        owner: OwnerContext,
        project: Project?
    ): String
}



interface ReminderRepository :
    BasicById<Reminder>,
    IndexedByOwner<Reminder>,
    IndexedByLink<Reminder>,
    InTimeInterval<Reminder>,
    LinkAndInterval<Reminder>,
    Update<Reminder> {

    suspend fun addReminder(
        name: String,
        description: String,
        at: TimeField,
        owner: OwnerContext,
        linkedTo: Linkable?
    ): String
}


