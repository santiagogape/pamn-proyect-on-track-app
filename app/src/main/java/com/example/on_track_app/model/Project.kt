package com.example.on_track_app.model

data class Project (
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val description: String = "",
    val members: List<String> = emptyList(),
    override val cloudId: String? = null
): CloudIdentifiable