package com.airtable.interview.airtableschedule.domain.repository

import com.airtable.interview.airtableschedule.domain.model.Event
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for event data operations
 */
interface EventRepository {
    fun getTimelineItems(): Flow<List<Event>>
    suspend fun getEvents(): List<Event>
    suspend fun updateEvent(event: Event): Result<Event>
    suspend fun deleteEvent(eventId: Int): Result<Unit>
}