package com.example.on_track_app.utils

import android.util.Log
import com.example.on_track_app.data.realm.entities.Entity
import com.example.on_track_app.data.realm.entities.SynchronizableEntity
import com.example.on_track_app.data.synchronization.SynchronizableDTO
import com.example.on_track_app.model.CloudIdentifiable
import com.example.on_track_app.model.Identifiable
import com.example.on_track_app.model.LocalConfigurations

object DebugLogcatLogger {

    private const val TAG = "SyncDebug"

    fun logConfig(conf: LocalConfigurations, where: String){
        Log.d(
            TAG,
            """
            ---- LOCAL CONFIG at $where ----
            userID: ${conf.userID}
            defaultProjectID: ${conf.defaultProjectID}
            --------------------------------
            """)
    }

    // ---------------------------------------------------------------------
    // MOCK PROJECT CREATED
    // ---------------------------------------------------------------------
    fun <T> logMockProject(project: T) where T: CloudIdentifiable, T: Identifiable {
        Log.d(
            TAG,
            """
            ---- MOCK  CREATED ----
            id: ${project.id}
            name: ${project.name}
            --------------------------------
            """.trimIndent()
        )
    }

    // ---------------------------------------------------------------------
    // REALM ENTITY SAVED
    // ---------------------------------------------------------------------
    fun <T> logRealmSaved(project: T) where T: SynchronizableEntity,T: Entity {
        Log.d(
            TAG,
            """
            ----  SAVED IN REALM ----
            id: ${project.id.toHexString()}
            cloudId: ${project.cloudId}
            version: ${project.version}
            syncStatus: ${project.synchronizationStatus}
            --------------------------------
            """.trimIndent()
        )
    }

    // ---------------------------------------------------------------------
    // DTO SENT TO FIRESTORE
    // ---------------------------------------------------------------------
    fun <T> logDTOToRemote(dto: T) where T: SynchronizableDTO {
        Log.d(
            TAG,
            """
            ---- DTO SENT TO FIRESTORE ----
            cloudId: ${dto.cloudId}
            version: ${dto.version}
            deleted: ${dto.deleted}
            --------------------------------
            """.trimIndent()
        )
    }

    // ---------------------------------------------------------------------
    // DTO RECEIVED FROM FIRESTORE
    // ---------------------------------------------------------------------

    enum class EventType {
        INSERT,UPDATE
    }
    fun <T> logDTOFromRemote(event:EventType, dto: T) where T: SynchronizableDTO {


        Log.d(
            TAG,
            """
            ---- ${when (event){
                EventType.INSERT -> "DTO INSERTED FROM FIRESTORE"
                EventType.UPDATE -> "DTO UPDATED FROM FIRESTORE"
            }} ----
            cloudId: ${dto.cloudId}
            version: ${dto.version}
            deleted: ${dto.deleted}
            --------------------------------------
            """.trimIndent()
        )
    }
}
