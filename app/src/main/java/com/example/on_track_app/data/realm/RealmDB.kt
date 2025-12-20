package com.example.on_track_app.data.realm

import com.example.on_track_app.data.realm.entities.EventRealmEntity
import com.example.on_track_app.data.realm.entities.GroupRealmEntity
import com.example.on_track_app.data.realm.entities.LinkReference
import com.example.on_track_app.data.realm.entities.LocalConfig
import com.example.on_track_app.data.realm.entities.MembershipRealmEntity
import com.example.on_track_app.data.realm.entities.MembershipReference
import com.example.on_track_app.data.realm.entities.OwnerReference
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.realm.entities.ProjectReference
import com.example.on_track_app.data.realm.entities.ReminderRealmEntity
import com.example.on_track_app.data.realm.entities.SynchronizationEntity
import com.example.on_track_app.data.realm.entities.TaskRealmEntity
import com.example.on_track_app.data.realm.entities.TimeRealmEmbeddedObject
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

object RealmDatabase {
    private val configuration = RealmConfiguration.create(
        schema = setOf(
            //config
            LocalConfig::class,
            //synchronization
            SynchronizationEntity::class,
            //entities
            UserRealmEntity::class,
            GroupRealmEntity::class,
            ProjectRealmEntity::class,
            MembershipRealmEntity::class,
            TaskRealmEntity::class,
            EventRealmEntity::class,
            ReminderRealmEntity::class,
            //embedded
            TimeRealmEmbeddedObject::class,
            OwnerReference::class,
            ProjectReference::class,
            MembershipReference::class,
            LinkReference::class

        )
    )

    val realm: Realm by lazy {
        Realm.open(configuration)
    }
}
