package com.airtable.interview.airtableschedule.data.repository

import com.airtable.interview.airtableschedule.data.datasource.SampleTimelineItems
import com.airtable.interview.airtableschedule.domain.model.Event
import com.airtable.interview.airtableschedule.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implementation of EventRepository that provides sample data.
 * In a real app, this would connect to actual data sources (API, database, etc.)
 */
class EventRepositoryImpl : EventRepository {
    private val _events = MutableStateFlow(SampleTimelineItems.timelineItems.toMutableList())
    
    override fun getTimelineItems(): Flow<List<Event>> {
        return _events.asStateFlow()
    }
    
    override suspend fun updateEvent(event: Event): Result<Event> {
        return try {
            val currentEvents = _events.value.toMutableList()
            val index = currentEvents.indexOfFirst { it.id == event.id }
            
            println("Repository: Updating event ${event.id}, found at index: $index")
            if (index >= 0) {
                println("Repository: Old event: ${currentEvents[index]}")
                println("Repository: New event: $event")
                currentEvents[index] = event
                _events.value = currentEvents
                println("Repository: Events list updated, size: ${currentEvents.size}")
                Result.success(event)
            } else {
                println("Repository: Event with id ${event.id} not found")
                Result.failure(Exception("Event not found"))
            }
        } catch (e: Exception) {
            println("Repository: Error updating event: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun deleteEvent(eventId: Int): Result<Unit> {
        return try {
            val currentEvents = _events.value.toMutableList()
            val removed = currentEvents.removeAll { it.id == eventId }
            
            if (removed) {
                _events.value = currentEvents
                Result.success(Unit)
            } else {
                Result.failure(Exception("Event not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
