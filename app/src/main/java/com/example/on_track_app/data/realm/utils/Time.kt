package com.example.on_track_app.data.realm.utils

import java.time.ZoneId
import io.realm.kotlin.types.RealmInstant
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

fun RealmInstant.toInstant(): Instant {
    return Instant.ofEpochSecond(this.epochSeconds, this.nanosecondsOfSecond.toLong())
}

fun Instant.toRealmInstant(): RealmInstant {
    return RealmInstant.from(this.epochSecond, this.nano)
}


fun Instant.toLocalDate(): LocalDate {
    return this.atZone(ZoneId.systemDefault()).toLocalDate()
}

fun Instant.toLocalTime(): LocalTime {
    return this.atZone(ZoneId.systemDefault()).toLocalTime()
}


fun RealmInstant.toSystemLocalDate(): LocalDate {
    return this.toInstant().toLocalDate()
}

fun RealmInstant.toSystemLocalTime(): LocalTime {
    return this.toInstant().toLocalTime()
}

fun RealmInstant.toSyncString(): String {
    // ISO 8601 (UTC)
    return this.toInstant().toString()
}

fun String.toRealmInstant(): RealmInstant {
    return Instant.parse(this).toRealmInstant()
}

fun LocalDate.toRealmInstant(time: LocalTime? = null): RealmInstant {
    val dateTime = this.atTime(time ?: LocalTime.MIN)
    return dateTime.atZone(ZoneId.systemDefault()).toInstant().toRealmInstant()
}