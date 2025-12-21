package com.example.on_track_app.data.di

import com.example.on_track_app.data.GarbageCollector
import com.example.on_track_app.data.abstractions.repositories.BasicById
import com.example.on_track_app.data.realm.entities.EventRealmEntity
import com.example.on_track_app.data.realm.entities.GroupRealmEntity
import com.example.on_track_app.data.realm.entities.MembershipRealmEntity
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.realm.entities.ReminderRealmEntity
import com.example.on_track_app.data.realm.entities.SynchronizableEntity
import com.example.on_track_app.data.realm.entities.TaskRealmEntity
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import com.example.on_track_app.data.realm.repositories.RealmSynchronizableRepository
import com.example.on_track_app.data.realm.repositories.SyncMapper
import com.example.on_track_app.data.realm.repositories.SynchronizableRepository
import com.example.on_track_app.data.synchronization.EventDTO
import com.example.on_track_app.data.synchronization.GroupDTO
import com.example.on_track_app.data.synchronization.MembershipDTO
import com.example.on_track_app.data.synchronization.ProjectDTO
import com.example.on_track_app.data.synchronization.ReminderDTO
import com.example.on_track_app.data.synchronization.SynchronizableDTO
import com.example.on_track_app.data.synchronization.TaskDTO
import com.example.on_track_app.data.synchronization.UserDTO
import com.example.on_track_app.model.CloudIdentifiable
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Group
import com.example.on_track_app.model.Identifiable
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Reminder
import com.example.on_track_app.model.Task
import com.example.on_track_app.model.User
import com.example.on_track_app.model.UserMembership
import io.realm.kotlin.Realm
import io.realm.kotlin.types.RealmObject
import kotlin.reflect.KClass

data class Catalog<DOM, LOCAL, DTO>(
    val domain: KClass<DOM>,
    val local: KClass<LOCAL>,
    val dto: KClass<DTO>,
) where
DOM : Identifiable, DOM: CloudIdentifiable,
LOCAL : RealmObject, LOCAL : SynchronizableEntity,
DTO : SynchronizableDTO


val USER_CATALOG =
    Catalog(User::class, UserRealmEntity::class, UserDTO::class)

val GROUP_CATALOG =
    Catalog(Group::class, GroupRealmEntity::class, GroupDTO::class)

val PROJECT_CATALOG =
    Catalog(Project::class, ProjectRealmEntity::class, ProjectDTO::class)

val MEMBERSHIP_CATALOG =
    Catalog(UserMembership::class, MembershipRealmEntity::class, MembershipDTO::class)

val TASK_CATALOG =
    Catalog(Task::class, TaskRealmEntity::class, TaskDTO::class)

val EVENT_CATALOG =
    Catalog(Event::class, EventRealmEntity::class, EventDTO::class)

val REMINDER_CATALOG =
    Catalog(Reminder::class, ReminderRealmEntity::class, ReminderDTO::class)

class GenericRepositoryBuilder<DOM, LOCAL, DTO, R>(
    private val realm: Realm,
    private val catalog: Catalog<DOM, LOCAL, DTO>,
    private val mapper: SyncMapper<LOCAL, DTO, DOM>,
    private val maker: () -> LOCAL,
    private val gc: GarbageCollector,
    private val clazz: KClass<DOM> = catalog.domain,
    private val buildRepo: (
        core: RealmSynchronizableRepository<LOCAL, DTO, DOM>
    ) -> R
) where
DOM : Identifiable,
DOM : CloudIdentifiable,
LOCAL : RealmObject,
LOCAL : SynchronizableEntity,
DTO : SynchronizableDTO,
R: BasicById<DOM>, R: SynchronizableRepository<DTO>{
    fun build(): RepositoryHandle<R, DOM, DTO> {
        val core = RealmSynchronizableRepository(
            db = realm,
            maker = maker,
            localClass = catalog.local,
            transferClass = catalog.dto,
            mapper = mapper,
            gc = gc,
            domClass = clazz

        )
        val repo = buildRepo(core)

        return RepositoryHandle(
            repo = repo,
            sync = repo
        )
    }
}

