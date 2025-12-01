package com.example.on_track_app.data.realm

import com.example.on_track_app.data.realm.entities.ReminderRealmEntity
import com.example.on_track_app.data.realm.entities.TemporizedRealmEntity
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

// Cambia 'class' por 'object'
object RealmDatabase {
    private val configuration = RealmConfiguration.create(
        schema = setOf(
            ReminderRealmEntity::class,
            TemporizedRealmEntity::class
            // Añade aquí el resto de tus clases de Realm
        )
    )

    // 'lazy' asegura que la base de datos solo se abrirá la primera vez que se acceda a ella.
    // Esto es más eficiente que abrirla en cuanto se inicia la app si no se va a usar inmediatamente.
    val realm: Realm by lazy {
        Realm.open(configuration)
    }
}
