package com.airtable.interview.airtableschedule.domain.usecase

import com.airtable.interview.airtableschedule.domain.model.Event
import com.airtable.interview.airtableschedule.domain.repository.EventRepository

/**
 * Use case for updating event information
 */
class UpdateEventUseCase(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(event: Event): Result<Event> {
        return try {
            // Basic validation
            if (event.name.isBlank()) {
                return Result.failure(IllegalArgumentException("Event name cannot be empty"))
            }
            
            if (event.endDate.before(event.startDate)) {
                return Result.failure(IllegalArgumentException("End date cannot be before start date"))
            }
            
            // Update the event
            eventRepository.updateEvent(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}