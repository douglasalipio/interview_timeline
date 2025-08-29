package com.airtable.interview.airtableschedule.domain.usecase

import com.airtable.interview.airtableschedule.domain.repository.EventRepository

/**
 * Use case for deleting an event
 */
class DeleteEventUseCase(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: Int): Result<Unit> {
        return try {
            if (eventId <= 0) {
                return Result.failure(IllegalArgumentException("Event ID must be positive"))
            }
            
            eventRepository.deleteEvent(eventId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}