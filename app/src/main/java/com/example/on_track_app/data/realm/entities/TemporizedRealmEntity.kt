package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.toSystemLocalDate
import com.example.on_track_app.data.realm.utils.toSystemLocalTime
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.annotations.Ignore
import java.time.LocalDate
import java.time.LocalTime

class TemporizedRealmEntity: EmbeddedRealmObject {

    var date: RealmInstant = RealmInstant.now()

    var withTime: Boolean = false

    @Ignore
    val localDate: LocalDate
        get() = date.toSystemLocalDate()

    @Ignore
    val localTime: LocalTime?
        get() = if (withTime) {
            date.toSystemLocalTime()
        } else {
            null
        }
}


