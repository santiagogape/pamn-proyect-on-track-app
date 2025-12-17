package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.model.Link
import com.example.on_track_app.model.LinkedType
import io.realm.kotlin.types.RealmInstant
import org.mongodb.kbson.ObjectId

//todo refactor ownerId as UserRealmEntity?,GroupRealmEntity?

// local references
interface Entity {
    var id: ObjectId
}

interface OwnedEntity {
    var ownerId: ObjectId
}


interface OwnershipEntity: OwnedEntity {
    var ownerType: String
}

interface ProjectOwnershipEntity {
    var projectId: ObjectId?
}

interface MembershipEntity {

    var entityId: ObjectId
    var membershipType: String

    var memberId: ObjectId
}

interface LinkedEntity {
    var linkedTo: ObjectId?
    var linkType: String?
}

fun LinkedEntity.toLink(): Link? {
    return if (this.linkedTo != null && this.linkType != null) {
        Link(
            to = this.linkedTo!!.toHexString(),
            ofType = LinkedType.valueOf(this.linkType!!)
        )
    } else null
}



// remote references
interface SynchronizableEntity: Entity {
    var cloudId: String?
    var version: RealmInstant
    var synchronizationStatus: String
}

interface SynchronizableOwnedEntity: OwnedEntity {
    var cloudOwnerId: String?
}

interface SynchronizableOwnershipEntity: OwnershipEntity,SynchronizableOwnedEntity

interface SynchronizableProjectOwnershipEntity: ProjectOwnershipEntity {
    var cloudProjectId: String?
}

interface SynchronizableMembershipEntity: MembershipEntity {
    var cloudEntityId: String?
    var cloudMemberId: String?
}

interface SynchronizableLinkedEntity: LinkedEntity {
    var cloudLinkedTo: String?
}

fun SynchronizableEntity.update() {
    this.version = RealmInstant.now()
    this.synchronizationStatus = SynchronizationState.UPDATED.name
}

fun SynchronizableEntity.delete() {
    this.version = RealmInstant.now()
    this.synchronizationStatus = SynchronizationState.DELETED.name
}

fun SynchronizableEntity.upToDate(){
    this.version = RealmInstant.now()
    this.synchronizationStatus = SynchronizationState.CURRENT.name
}











