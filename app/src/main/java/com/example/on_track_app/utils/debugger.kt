package com.example.on_track_app.utils

import android.util.Log
import com.example.on_track_app.data.synchronization.SynchronizableDTO
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


    // ---------------------------------------------------------------------
    // REALM ENTITY SAVED
    // ---------------------------------------------------------------------

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

}
