package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.data.synchronization.SynchronizableDTO
import com.example.on_track_app.model.Identifiable
import com.example.on_track_app.model.Link
import com.example.on_track_app.model.LinkedType
import io.realm.kotlin.types.RealmInstant
import org.mongodb.kbson.ObjectId


// local references
interface Entity {
    var id: ObjectId
}

interface UserOwnedEntity {
    var ownerId: ObjectId
}


interface OwnershipEntity: UserOwnedEntity {
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

interface LinkEntity {
    var linkedTo: ObjectId?
    var linkType: String?
}

fun LinkEntity.toLink(): Link? {
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

interface SynchronizableUserOwnershipEntity: UserOwnedEntity {
    var cloudOwnerId: String?
}

interface SynchronizableOwnershipEntity: OwnershipEntity,SynchronizableUserOwnershipEntity

interface SynchronizableProjectOwnershipEntity: ProjectOwnershipEntity {
    var cloudProjectId: String?
}

interface SynchronizableMembershipEntity: MembershipEntity {
    var cloudEntityId: String?
    var cloudMemberId: String?
}

interface SynchronizableLinkEntity: LinkEntity {
    var cloudLinkedTo: String?
}

interface SyncMapperGeneric<T, DTO,DOM>
        where DOM : Identifiable,
              DTO : SynchronizableDTO {
    fun toLocal(dto:DTO,entity: T)
    fun toDTO(entity:T): DTO
    fun toDomain(entity:T): DOM
}

interface SyncMapper<RE, DTO,DOM>: SyncMapperGeneric<RE, DTO,DOM>
        where RE : SynchronizableEntity, DOM : Identifiable,
              DTO : SynchronizableDTO {
    override fun toLocal(dto:DTO,entity: RE)
    override fun toDTO(entity: RE): DTO
    override fun toDomain(entity: RE): DOM
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











