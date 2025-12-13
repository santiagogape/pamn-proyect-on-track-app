package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.model.Membership
import com.example.on_track_app.model.MembershipType
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class RealmMembershipEntity : RealmObject, SynchronizableMembershipEntity, SynchronizableEntity {
    @PrimaryKey
    override var id: ObjectId = ObjectId()
    override var cloudId: String? = null
    override var synchronizationStatus: String = SynchronizationState.CREATED.name
    override var version: RealmInstant = RealmInstant.now()
    override var membershipType: String = ""

    @Index
    override var entityId: ObjectId = ObjectId()
    @Index
    override var memberId: ObjectId = ObjectId()
    @Index
    override var cloudEntityId: String? = null
    @Index
    override var cloudMemberId: String? = null
}


fun RealmMembershipEntity.toDomain() = Membership(entityId.toHexString(),memberId.toHexString(),
    MembershipType.valueOf(this.membershipType),id.toHexString())