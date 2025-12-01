package com.example.on_track_app.data.realm.entities

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class ProyectRealmEntity: RealmObject {
    @PrimaryKey
    val id: String = ""
    var name: String = ""
}