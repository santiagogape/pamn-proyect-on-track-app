package com.example.on_track_app.data.dependencyInjections.builders

import com.example.on_track_app.data.realm.entities.EventRealmEntity
import com.example.on_track_app.data.realm.entities.GroupRealmEntity
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.realm.entities.ReminderRealmEntity
import com.example.on_track_app.data.realm.entities.TaskRealmEntity
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import com.example.on_track_app.data.realm.repositories.decorated.SyncMapper
import com.example.on_track_app.data.synchronization.EventDTO
import com.example.on_track_app.data.synchronization.GroupDTO
import com.example.on_track_app.data.synchronization.ProjectDTO
import com.example.on_track_app.data.synchronization.ReferenceIntegrityManager
import com.example.on_track_app.data.synchronization.ReminderDTO
import com.example.on_track_app.data.synchronization.TaskDTO
import com.example.on_track_app.data.synchronization.UserDTO
import com.example.on_track_app.model.MockEvent
import com.example.on_track_app.model.MockGroup
import com.example.on_track_app.model.MockProject
import com.example.on_track_app.model.MockReminder
import com.example.on_track_app.model.MockTask
import com.example.on_track_app.model.MockUser
import io.realm.kotlin.Realm

class RepositoryBuilderFactory(
    private val realm: Realm,
    private val integrityManager: ReferenceIntegrityManager
) {

    fun user(mapper: SyncMapper<UserRealmEntity, UserDTO, MockUser>) =
        UserRepositoryBuilder(realm, mapper, integrityManager)

    fun group(mapper: SyncMapper<GroupRealmEntity, GroupDTO, MockGroup>) =
        GroupRepositoryBuilder(realm, mapper, integrityManager)

    fun project(mapper: SyncMapper<ProjectRealmEntity, ProjectDTO, MockProject>) =
        ProjectRepositoryBuilder(realm, mapper, integrityManager)

    fun task(mapper: SyncMapper<TaskRealmEntity, TaskDTO, MockTask>) =
        TaskRepositoryBuilder(realm, mapper, integrityManager)

    fun reminder(mapper: SyncMapper<ReminderRealmEntity, ReminderDTO, MockReminder>) =
        ReminderRepositoryBuilder(realm, mapper, integrityManager)

    fun event(mapper: SyncMapper<EventRealmEntity, EventDTO, MockEvent>) =
        EventRepositoryBuilder(realm, mapper, integrityManager)
}
