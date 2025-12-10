package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.data.synchronization.SynchronizableDTO
import com.example.on_track_app.model.Identifiable
import io.realm.kotlin.types.RealmInstant
import org.mongodb.kbson.ObjectId


interface Entity {
    val id: ObjectId
}

interface Owned {
    val ownerId: ObjectId
    val ownerType: String
}

interface ProjectOwnership {
    val projectId: ObjectId
}

interface SynchronizableEntity {
    var cloudId: String?
    var version: RealmInstant
    var synchronizationStatus: String
}

interface SyncMapperGeneric<T, DTO,DOM>
        where DOM : Identifiable,
              DTO : SynchronizableDTO {
    fun toLocal(dto:DTO,entity: T)
    fun toDTO(entity:T): DTO
    fun toDomain(entity:T): DOM
}

interface SyncMapper<RE, DTO,DOM>: SyncMapperGeneric<RE, DTO,DOM>
        where RE : SynchronizableEntity, RE: Entity, DOM : Identifiable,
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
    this.synchronizationStatus = SynchronizationState.DELETED.name
}

fun SynchronizableEntity.upToDate(){
    this.version = RealmInstant.now()
    this.synchronizationStatus = SynchronizationState.CURRENT.name
}











