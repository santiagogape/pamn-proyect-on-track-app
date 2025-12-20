package com.example.on_track_app.utils

import android.util.Log
import com.example.on_track_app.data.realm.entities.Entity
import com.example.on_track_app.data.realm.entities.SynchronizableEntity
import com.example.on_track_app.data.synchronization.SynchronizableDTO
import com.example.on_track_app.model.CloudIdentifiable
import com.example.on_track_app.model.Identifiable
import com.example.on_track_app.model.Named
import com.example.on_track_app.model.OwnerType
import com.example.on_track_app.model.User

object DebugLogcatLogger {

    private const val TAG = "SyncDebug"

    fun log(message: String) = Log.d(TAG, """
            ---- LOGGING MESSAGE ----
            ${"\t"} $message ${"\n\n"}
            --------------------------------
            """)

    fun logConfig(conf: User, where: String){
        Log.d(
            TAG,
            """
            ---- LOCAL CONFIG at $where ----
            userID: ${conf.id}
            name: ${conf.name}
            email: ${conf.email}
            cloudId: ${conf.cloudId}
            --------------------------------
            """)
    }

    fun logCreatingProject(name: String, description: String, owner: String, ownerType: OwnerType){
        Log.d(TAG,"""
            ---- CREATING PROJECT ----
            name: $name
            description: $description
            owner: $owner
            ownerType: $ownerType
            --------------------------------
            """.trimIndent())

    }
    fun <T> logMockProject(project: T) where T: CloudIdentifiable, T: Identifiable, T : Named {
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
    fun <T> logRealmSaved(project: T) where T: SynchronizableEntity, T: Entity {
        Log.d(
            TAG,
            """
            ----  SAVED IN REALM ----
            id: ${project.id.toHexString()}
            cloudId: ${project.identity?.cloudId}
            version: ${project.identity?.version}
            syncStatus: ${project.identity?.synchronizationStatus}
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
