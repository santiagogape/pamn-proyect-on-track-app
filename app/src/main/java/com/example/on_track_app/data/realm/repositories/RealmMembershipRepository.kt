package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.MembershipRepository
import com.example.on_track_app.data.realm.entities.RealmMembershipEntity
import com.example.on_track_app.data.realm.repositories.decorated.SyncMapper
import com.example.on_track_app.data.synchronization.MembershipDTO
import com.example.on_track_app.data.synchronization.ReferenceIntegrityManager
import com.example.on_track_app.data.synchronization.toObjectId
import com.example.on_track_app.model.Membership
import com.example.on_track_app.model.MembershipType
import io.realm.kotlin.Realm
import kotlin.reflect.KClass

class RealmMembershipRepository(
    db: Realm,
    mapper: SyncMapper<RealmMembershipEntity, MembershipDTO, Membership>,
    maker: () -> RealmMembershipEntity,
    klass: KClass<RealmMembershipEntity> = RealmMembershipEntity::class,
    dtoClass: KClass<MembershipDTO> = MembershipDTO::class,
    integrityManager: ReferenceIntegrityManager
) : MembershipRepository, RealmSynchronizableRepository<RealmMembershipEntity, MembershipDTO, Membership>(db,mapper,maker,klass,dtoClass,integrityManager), SynchronizedReference {
    override suspend fun addMembership(ship: String, member: String, type: MembershipType): String {
        var newId = ""
        db.write {
            val entity = maker().apply {
                this.entityId = ship.toObjectId()
                this.memberId = member.toObjectId()
                this.membershipType = type.name
            }
            val saved = copyToRealm(entity)
            newId = saved.id.toHexString()
            integrityManager
                .resolveReferenceOnCreate(
                    transferClass,
                    saved.entityId.toHexString(),
                    Filter.MEMBERSHIP_ENTITY
                )?.let { saved.cloudEntityId = it}
            integrityManager
                .resolveReferenceOnCreate(transferClass,
                    saved.memberId.toHexString(),
                    Filter.MEMBERSHIP_MEMBER
                )?.let { saved.cloudMemberId = it }
        }
        syncEngine?.onLocalChange(newId,transferClass)
        return newId
    }

    override suspend fun synchronizeReferences(id: String, cloudId: String) {
        db.write {
            filter(this,Filter.MEMBERSHIP_ENTITY, id).map { entity ->
                entity.cloudEntityId = cloudId
            }

            filter(this,Filter.MEMBERSHIP_MEMBER, id).map { entity ->
                entity.cloudMemberId = cloudId
            }
        }
    }
}
