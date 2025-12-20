package com.example.on_track_app.data.realm.entities
import com.example.on_track_app.model.Described
import com.example.on_track_app.model.Named
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

// ---------- Entities ----------
class UserRealmEntity : RealmObject, RealmOwner, SynchronizableEntity {
    @PrimaryKey
    override var id: ObjectId = ObjectId()
    override var identity: SynchronizationEntity? = null
    override var name: String = ""
    var email: String = ""
}


class GroupRealmEntity : RealmObject, RealmOwner,RealmMembership, Described, RealmOwned, SynchronizableEntity {
    @PrimaryKey
    override var id: ObjectId = ObjectId()
    override var identity: SynchronizationEntity? = null
    override var name: String = ""
    override var description: String = ""
    override var owner: OwnerReference? = null
}


class ProjectRealmEntity : RealmObject,RealmMembership, Described, RealmOwned, SynchronizableEntity {
    @PrimaryKey
    override var id: ObjectId = ObjectId()
    override var identity: SynchronizationEntity? = null
    override var name: String = ""
    override var description: String = ""
    override var owner: OwnerReference? = null
}

class TaskRealmEntity : RealmObject, RealmLinkable, Described, RealmOwned, RealmProjectOwned, SynchronizableEntity {
    @PrimaryKey
    override var id: ObjectId = ObjectId()
    override var identity: SynchronizationEntity? = null
    override var name: String = ""
    override var description: String = ""
    override var owner: OwnerReference? = null
    override var project: ProjectReference? = null
    var due: TimeRealmEmbeddedObject? = null
    @Index
    var status: String = ""
}

class EventRealmEntity : RealmObject, RealmLinkable, Described, RealmOwned,RealmProjectOwned, SynchronizableEntity {
    @PrimaryKey
    override var id: ObjectId = ObjectId()
    override var identity: SynchronizationEntity? = null
    override var name: String = ""
    override var description: String = ""
    override var owner: OwnerReference? = null
    override var project: ProjectReference? = null
    var start: TimeRealmEmbeddedObject? = null
    var end: TimeRealmEmbeddedObject? = null
}

class ReminderRealmEntity : RealmObject, Entity, Named, Described, RealmOwned,RealmLinked, SynchronizableEntity {
    @PrimaryKey
    override var id: ObjectId = ObjectId()
    override var identity: SynchronizationEntity? = null
    override var name: String = ""
    override var description: String = ""
    override var owner: OwnerReference? = null
    var at: TimeRealmEmbeddedObject? = null
    override var linkedTo: LinkReference? = null
}

class MembershipRealmEntity : RealmObject, Entity, SynchronizableEntity {
    @PrimaryKey
    override var id: ObjectId = ObjectId()
    override var identity: SynchronizationEntity? = null
    var membership: MembershipReference? = null
    var member: UserRealmEntity? = null
}

