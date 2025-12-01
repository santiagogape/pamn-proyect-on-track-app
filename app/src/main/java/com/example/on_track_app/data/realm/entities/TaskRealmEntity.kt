package com.example.on_track_app.data.realm.entities

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class TaskRealmEntity: RealmObject {
    @PrimaryKey
    val id: String = ""
    var name: String = ""
    var temporalData: TemporizedRealmEntity = TemporizedRealmEntity()
    var description: String = ""
    var status: String = ""
    val reminders: RealmList<String> = realmListOf()
    var proyect: String? = ""
}