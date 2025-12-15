package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.abstractions.repositories.Config
import com.example.on_track_app.data.abstractions.repositories.LOCAL_CONFIG_ID
import com.example.on_track_app.model.LocalConfigurations
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId


class LocalConfig: RealmObject, Config, Entity {
    @PrimaryKey
    override var name: String = LOCAL_CONFIG_ID
    override var id: ObjectId = ObjectId()
    var user: UserRealmEntity? = null
}

fun LocalConfig.toDomain(): LocalConfigurations {
    return LocalConfigurations(
        userID = user?.id?.toHexString() ?: "",
    )
}
