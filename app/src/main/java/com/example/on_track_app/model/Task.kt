package com.example.on_track_app.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

data class Task (
    @DocumentId
    override val id: String = "",
    val userId: String = "",
    override val name: String = "",
    override val description: String = "",
    @get:PropertyName("date")
    val dateIso: String  = LocalDate.now().toString(),
    val projectId: String? = null
) : Expandable {
    @get:Exclude
    val date: LocalDate
        get() = LocalDate.parse(dateIso)
}
