package com.example.on_track_app.data.realm.entities

import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmInstant

class TemporalDataField(var date: RealmInstant = RealmInstant.now(),
                        var withTime: Boolean = false
): EmbeddedRealmObject


