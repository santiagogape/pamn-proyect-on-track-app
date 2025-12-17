package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.model.MockTask
import com.example.on_track_app.model.MockTimeField
import com.example.on_track_app.model.OwnerType

interface TaskRepository: BasicById<MockTask>,
	IndexedByProject<MockTask>,
	IndexedByOwner<MockTask>,
    Update<MockTask>,
    InTimeInterval<MockTask>,
    GroupAndInterval<MockTask>,
        ProjectAndInterval<MockTask>
{

    suspend fun addTask(
        name: String,
        description: String,
        date: MockTimeField,
        projectId: String?,
        cloudId: String?,
        ownerId: String,
        ownerType: OwnerType
    ): String

}