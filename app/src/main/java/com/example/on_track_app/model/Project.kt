package com.example.on_track_app.model

data class Project (
    val id: String = "",
    val name: String = "",
    val members: List<User> = emptyList(),
    override val cloudId: String? = null
): CloudIdentifiable