package com.example.on_track_app.model

data class User (
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val groups: List<Group> = listOf(),
    override val cloudId: String? = null
): CloudIdentifiable
