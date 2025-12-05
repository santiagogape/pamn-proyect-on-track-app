package com.example.on_track_app.data.realm

import com.example.on_track_app.data.realm.entities.*

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

// Cambia 'class' por 'object'
object RealmDatabase {
    private val configuration = RealmConfiguration.create(
        schema = setOf(
            // Common
            CloudIdField::class,
            TemporalDataField::class,
            //entities
            ReminderRealmEntity::class,
            GroupRealmEntity::class,
            ProjectRealmEntity::class,
            TaskRealmEntity::class,
            EventRealmEntity::class,
            UserRealmEntity::class
        )
    )

    // 'lazy' asegura que la base de datos solo se abrirá la primera vez que se acceda a ella.
    // Esto es más eficiente que abrirla en cuanto se inicia la app si no se va a usar inmediatamente.
    val realm: Realm by lazy {
        Realm.open(configuration)
    }
}
