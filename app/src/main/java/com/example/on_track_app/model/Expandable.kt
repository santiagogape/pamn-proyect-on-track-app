package com.example.on_track_app.model

class Expandable(
    override val id: String,
    override val name: String,
    override val description: String
): Identifiable, Named, Described

fun <T> T.toExpandable(): Expandable where T:Identifiable, T:Named, T:Described = Expandable(id,name,description)