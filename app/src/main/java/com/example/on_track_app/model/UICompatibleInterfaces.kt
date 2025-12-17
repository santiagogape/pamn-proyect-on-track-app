package com.example.on_track_app.model

import java.time.LocalTime


interface Expandable: Identifiable, Named, Described

interface TimedExpandable: Expandable,Timed

fun List<TimedExpandable>.sort(): List<TimedExpandable> =
    this.sortedWith(compareBy({ it.toTime() ?: LocalTime.MIN }))

fun <T> List<T>.sortByTime(): List<T> where T:Timed =
    this.sortedWith(compareBy({ it.toTime() ?: LocalTime.MIN }))


interface Selectable: Identifiable, Named
