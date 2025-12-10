package com.example.on_track_app.data.realm.utils

import java.time.ZoneId
import io.realm.kotlin.types.RealmInstant
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
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

fun LocalDate.toRealmInstant(time: LocalTime? = null): RealmInstant {
    val dateTime = this.atTime(time ?: LocalTime.MIN)
    return dateTime.atZone(ZoneId.systemDefault()).toInstant().toRealmInstant()
}

fun LocalDate.toInstant(hour: Int? = null, minute: Int? = null): Instant{
    val zone = ZoneId.systemDefault()

    val time = if (hour == null || minute == null) {
        LocalTime.MIN   // 00:00
    } else {
        LocalTime.of(hour, minute)
    }
    return this.atTime(time).atZone(zone).toInstant()
}

fun LocalDateTime.toInstant(): Instant {
    return this.atZone(ZoneId.systemDefault()).toInstant()
}

fun RealmInstant.toMillis(): Long = this.toInstant().toEpochMilli()
fun Long.toRealmInstant(): RealmInstant =
    Instant.ofEpochMilli(this).toRealmInstant()