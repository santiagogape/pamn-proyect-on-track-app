package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.GCSynchronizableEntity
import com.example.on_track_app.data.GarbageCollectorPhase
import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.model.Named
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

// ---------- realm common ----------
interface Entity { var id: ObjectId }
sealed interface RealmOwner: Entity, Named
sealed interface RealmLinkable: Entity,Named
sealed interface RealmMembership: Entity, Named
interface RealmOwned { var owner: OwnerReference? }
interface RealmProjectOwned { var project: ProjectReference? }
interface RealmLinked { var linkedTo: LinkReference? }

interface Synchronizable : Entity, GCSynchronizableEntity {
    var cloudId: String
    var version: RealmInstant
    var synchronizationStatus: String
}

class SynchronizationEntity : RealmObject, Entity, Synchronizable{
    @PrimaryKey
    override var id: ObjectId = ObjectId()
    @Index
    override var cloudId: String = ""
    @Index
    override var version: RealmInstant = RealmInstant.now()
    @Index
    override var synchronizationStatus: String = SynchronizationState.CREATED.name
    @Index
    override var phase = GarbageCollectorPhase.NONE.name
}

interface SynchronizableEntity: Entity{
    var identity: SynchronizationEntity?
}

fun SynchronizableEntity.update() {
    identity?.version = RealmInstant.now()
    identity?.synchronizationStatus = SynchronizationState.UPDATED.name
}
fun SynchronizableEntity.delete() {
    identity?.version = RealmInstant.now()
    identity?.synchronizationStatus = SynchronizationState.DELETED.name
}
fun SynchronizableEntity.upToDate() {
    identity?.version = RealmInstant.now()
    identity?.synchronizationStatus = SynchronizationState.CURRENT.name
}

sealed interface Reference: SynchronizableEntity

// ---------- Embedded fields ----------
class TimeRealmEmbeddedObject: EmbeddedRealmObject {
    @Index
    var instant: RealmInstant = RealmInstant.now()
    @Index
    var timed: Boolean = false
}

// ---------- Embedded references ----------

class OwnerReference : EmbeddedRealmObject, Reference  {
    @Index
    override var id: ObjectId = ObjectId()
    override var identity: SynchronizationEntity? = null
    var user: UserRealmEntity? = null
    var group: GroupRealmEntity? = null
}

class ProjectReference : EmbeddedRealmObject, Reference {
    @Index
    override var id: ObjectId = ObjectId()
    override var identity: SynchronizationEntity? = null
    var project: ProjectRealmEntity? = null
}


class LinkReference : EmbeddedRealmObject, Reference {
    @Index
    override var id: ObjectId = ObjectId()
    override var identity: SynchronizationEntity? = null
    var task: TaskRealmEntity? = null
    var event: EventRealmEntity? = null
}


class MembershipReference  : EmbeddedRealmObject, Reference {
    @Index
    override var id: ObjectId = ObjectId()
    override var identity: SynchronizationEntity? = null
    var group: GroupRealmEntity? = null
    var project: ProjectRealmEntity? = null
}
