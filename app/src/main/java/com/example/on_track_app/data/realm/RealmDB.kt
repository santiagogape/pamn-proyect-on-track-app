package com.example.on_track_app.data.realm

import com.example.on_track_app.data.realm.entities.*

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

object RealmDatabase {
    private val configuration = RealmConfiguration.create(
        schema = setOf(
            //config
            LocalConfig::class,
            //entities
            ReminderRealmEntity::class,
            GroupRealmEntity::class,
            ProjectRealmEntity::class,
            TaskRealmEntity::class,
            EventRealmEntity::class,
            UserRealmEntity::class,
            RealmMembershipEntity::class
        )
    )

    val realm: Realm by lazy {
        Realm.open(configuration)
    }
}
