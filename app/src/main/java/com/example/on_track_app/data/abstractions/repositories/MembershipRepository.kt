package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.model.Membership
import com.example.on_track_app.model.MembershipType

interface MembershipRepository: BasicById<Membership> {
    suspend fun addMembership(ship: String, member: String, type: MembershipType): String
}