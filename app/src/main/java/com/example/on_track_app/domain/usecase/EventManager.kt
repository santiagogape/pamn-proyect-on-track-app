package com.example.on_track_app.domain.usecase

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.FirestoreRepository
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Expandable
import com.example.on_track_app.model.Task
import com.example.on_track_app.ui.fragments.dialogs.CreationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class EventManager(
    private val eventRepository: FirestoreRepository<Event>
) {
    suspend fun createEvent(
        userId: String,
        name: String,
        description: String,
        projectId: String?,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Boolean {

        val sDate = startDate.toLocalDate()
        val sTime = startDate.toLocalTime()

        val eDate = endDate.toLocalDate()
        val eTime = endDate.toLocalTime()

        val newEvent = Event(
            userId = userId,
            name = name,
            description = description,
            startDate = sDate.toString(),
            startTime = sTime.toString(),
            endDate = eDate.toString(),
            endTime = eTime.toString(),
            projectId = projectId
        )

        // 3. Send to Repository
        return eventRepository.addElement(newEvent)
    }

    suspend fun updateEvent(
        eventId: String,
        name: String,
        description: String,
        projectId: String?,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ) {
        val sDate = startDate.toLocalDate()
        val sTime = startDate.toLocalTime()

        val eDate = endDate.toLocalDate()
        val eTime = endDate.toLocalTime()

        val updates = mutableMapOf<String, Any?>(
            "name" to name,
            "description" to description,
            "projectId" to projectId,
            "startDate" to sDate.toString(),
            "startTime" to sTime.toString(),
            "endDate" to eDate.toString(),
            "endTime" to eTime.toString()
        )

        // 4. Send the complete, updated object to the repository
        eventRepository.updateElement(eventId, updates)
    }

    suspend fun deleteEvent(eventId: String) {
        try {
            eventRepository.deleteEventWithReminders(eventId)
        } catch (e: Exception) {
            Log.e("Events (HomeViewModel/CalendarScreen)", "Failed to delete event with ID=$eventId", e)
        }
    }

    fun getEventsByDate(scope: CoroutineScope, events: StateFlow<List<Expandable>>): StateFlow<Map<LocalDate, List<Event>>> {
        return events
            .map { list ->
                list
                    .filterIsInstance<Event>()
                    .groupBy { it.startDateObj }
                    .toSortedMap()
            }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap()
            )
    }

    fun getEventsForDate(
        sourceMap: StateFlow<Map<LocalDate, List<Event>>>,
        date: LocalDate,
        scope: CoroutineScope
    ): StateFlow<List<Event>> {
        return sourceMap
            .map { map -> map[date].orEmpty() }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
}