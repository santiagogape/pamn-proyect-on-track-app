package com.example.on_track_app.utils

import android.util.Log
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.synchronization.ProjectDTO
import com.example.on_track_app.model.MockProject

object DebugLogcatLogger {

    private const val TAG = "SyncDebug"

    // ---------------------------------------------------------------------
    // MOCK PROJECT CREATED
    // ---------------------------------------------------------------------
    fun logMockProject(project: MockProject) {
        Log.d(
            TAG,
            """
            ---- MOCK PROJECT CREATED ----
            id: ${project.id}
            name: ${project.name}
            members: ${project.membersId}
            ownerType: ${project.ownerType}
            ownerId: ${project.ownerId}
            --------------------------------
            """.trimIndent()
        )
    }

    // ---------------------------------------------------------------------
    // REALM ENTITY SAVED
    // ---------------------------------------------------------------------
    fun logRealmSaved(project: ProjectRealmEntity) {
        Log.d(
            TAG,
            """
            ---- PROJECT SAVED IN REALM ----
            id: ${project.id.toHexString()}
            cloudId: ${project.cloudId}
            name: ${project.name}
            members: ${project.members}
            version: ${project.version}
            syncStatus: ${project.synchronizationStatus}
            --------------------------------
            """.trimIndent()
        )
    }

    // ---------------------------------------------------------------------
    // DTO SENT TO FIRESTORE
    // ---------------------------------------------------------------------
    fun logDTOToRemote(dto: ProjectDTO) {
        Log.d(
            TAG,
            """
            ---- DTO SENT TO FIRESTORE ----
            cloudId: ${dto.cloudId}
            name: ${dto.name}
            members: ${dto.members}
            ownerType: ${dto.ownerType}
            ownerId: ${dto.ownerId}
            version: ${dto.version}
            deleted: ${dto.deleted}
            --------------------------------
            """.trimIndent()
        )
    }

    // ---------------------------------------------------------------------
    // DTO RECEIVED FROM FIRESTORE
    // ---------------------------------------------------------------------

    enum class event {
        insert,update
    }
    fun logDTOFromRemote(event:event, dto: ProjectDTO) {


        Log.d(
            TAG,
            """
            ---- ${when (event){
                DebugLogcatLogger.event.insert -> "DTO INSERTED FROM FIRESTORE"
                DebugLogcatLogger.event.update -> "DTO UPDATED FROM FIRESTORE"
            }} ----
            cloudId: ${dto.cloudId}
            name: ${dto.name}
            members: ${dto.members}
            ownerType: ${dto.ownerType}
            ownerId: ${dto.ownerId}
            version: ${dto.version}
            deleted: ${dto.deleted}
            --------------------------------------
            """.trimIndent()
        )
    }
}
