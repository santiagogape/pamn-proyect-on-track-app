package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.SynchronizationState
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











