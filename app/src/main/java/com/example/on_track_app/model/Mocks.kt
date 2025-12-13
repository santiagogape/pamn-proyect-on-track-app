package com.example.on_track_app.model

import com.example.on_track_app.data.realm.utils.toLocalDate
import com.example.on_track_app.data.realm.utils.toLocalTime
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class MockTimeField(
    val instant: Instant,
    val timed: Boolean = false
)


fun MockTimeField.toDate(): LocalDate {
    return this.instant.toLocalDate()
}

fun MockTimeField.toTime(): LocalTime? {
    return if (this.timed) {
        instant.toLocalTime()
    } else {
        null
    }
}


interface Identifiable {
    val id: String
}


interface UserOwned {
    val ownerId: String
}

interface Ownership: UserOwned {
    val ownerType: OwnerType
}

interface ProjectOwned {
    val projectId: String?
}

interface Named {
    val name: String
}

interface Described {
    val description: String
}

interface Labeled {
    val label: String
}

data class Link(
    val to: String ,//id
    val ofType: LinkedType
)

data class Membership (
    val entityId: String = "",
    val memberId: String = "",
    val membershipType: MembershipType,
    override val id: String
): Identifiable

data class MockReminder(
    override val id: String,
    override val ownerId: String,
    override val ownerType: OwnerType,
    override val label: String,
    val at: MockTimeField,
    val linked: Link?,
    override val cloudId: String?
): Identifiable,
    Ownership,
    Labeled,
    CloudIdentifiable

data class MockEvent(
    override val id: String,
    override val ownerId: String,
    override val ownerType: OwnerType,
    override val projectId: String?,
    override val name: String,
    override val description: String,
    val start: MockTimeField,
    val end: MockTimeField,
    override val cloudId: String?,
): Identifiable,
    Ownership,
    ProjectOwned,
    Named,
    Described,
    CloudIdentifiable

data class MockTask (
    override val id: String,
    override val ownerId: String,
    override val ownerType: OwnerType,
    override val projectId: String?,
    override val name: String,
    override val description: String,
    val date: MockTimeField,
    override val cloudId: String? = null,
): Identifiable,
    Ownership,
    ProjectOwned,
    Named,
    Described,
    CloudIdentifiable

data class MockGroup (
    override val id: String,
    override val name: String,
    override val description: String,
    override val ownerId: String,
    override val cloudId: String?
): Identifiable,
    Named,
    Described,
    UserOwned,
    CloudIdentifiable

data class MockProject (
    override val id: String,
    override val ownerId: String,
    override val ownerType: OwnerType,
    override val name: String,
    override val description: String,
    override val cloudId: String?
): Identifiable,
    Ownership,
    Named,
    Described,
    CloudIdentifiable

data class MockUser (
    override val id: String = "",
    override val name: String = "",
    val email: String = "",
    override val cloudId: String? = null
):  Identifiable,
    Named,
    CloudIdentifiable