package com.example.on_track_app.data.realm.utils

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList


fun <T> List<T>.toRealmList(): RealmList<T> {
    return realmListOf<T>().apply { addAll(this@toRealmList) }
}
