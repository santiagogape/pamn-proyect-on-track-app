package com.example.on_track_app.data.realm.entities

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class UserRealmEntity: RealmObject {
    @PrimaryKey
    val id: String = ""
    var username: String = ""
    var email: String = ""
    var groups: RealmList<String> = realmListOf()
}