package com.example.on_track_app.data.dependencyInjections.builders

import com.example.on_track_app.data.realm.entities.SynchronizableEntity
import com.example.on_track_app.data.realm.repositories.decorated.RealmSynchronizableRepository
import com.example.on_track_app.data.realm.repositories.decorated.SyncMapper
import com.example.on_track_app.data.synchronization.ReferenceIntegrityManager
import com.example.on_track_app.data.synchronization.SynchronizableDTO
import com.example.on_track_app.model.Identifiable
import io.realm.kotlin.Realm
import io.realm.kotlin.types.RealmObject
import kotlin.reflect.KClass

abstract class RealmRepoBuilder<
        RE,
        DTO,
        DOM
        >(
    protected val realm: Realm,
    protected val mapper: SyncMapper<RE, DTO, DOM>,
    protected val maker: () -> RE,
    protected val entityClass: KClass<RE>,
    protected val dtoClass: KClass<DTO>,
    protected val integrityManager: ReferenceIntegrityManager
)
        where RE : RealmObject,
              RE : SynchronizableEntity,
              DOM : Identifiable,
              DTO : SynchronizableDTO {

    protected fun base(): RealmSynchronizableRepository<RE, DTO, DOM> =
        RealmSynchronizableRepository(
            db = realm,
            syncMapper = mapper,
            maker = maker,
            localClass = entityClass,
            transferClass = dtoClass,
            integrityManager = integrityManager
        )
}
