package com.airtable.interview.airtableschedule.domain.usecase

import com.airtable.interview.airtableschedule.domain.model.Event
import com.airtable.interview.airtableschedule.domain.repository.EventRepository
import com.airtable.interview.airtableschedule.domain.logic.DateCalculations
import java.util.Calendar
import java.util.Date

/**
 * Use case for moving events via drag and drop
 * Converts pixel offset to date changes and updates the event
 */
class MoveEventUseCase(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(
        event: Event,
        pixelOffset: Float,
        zoomLevel: Float
    ): Result<Event> {
        return try {
            // Skip processing if no pixel movement
            if (kotlin.math.abs(pixelOffset) < 1.0f) {
                println("MoveEventUseCase: Skipping move - insufficient pixel offset: $pixelOffset")
                return Result.success(event)
            }
            
            // Convert pixel offset to days
            val daysOffset = pixelOffsetToDays(pixelOffset, zoomLevel)
            
            if (daysOffset == 0) {
                // No significant movement, return original event
                println("MoveEventUseCase: Skipping move - insufficient day offset: $daysOffset")
                return Result.success(event)
            }
            
            println("MoveEventUseCase: Moving event ${event.id} by $daysOffset days (from $pixelOffset pixels)")
            
            // Calculate new dates
            val newStartDate = addDaysToDate(event.startDate, daysOffset)
            val newEndDate = addDaysToDate(event.endDate, daysOffset)
            
            // Create updated event
            val updatedEvent = event.copy(
                startDate = newStartDate,
                endDate = newEndDate
            )
            
            // Validate the move (basic validation)
            if (newStartDate.after(newEndDate)) {
                return Result.failure(IllegalArgumentException("Invalid date range after move"))
            }
            
            // Additional validation - prevent moving to far past or future
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val newStartYear = Calendar.getInstance().apply { time = newStartDate }.get(Calendar.YEAR)
            
            if (newStartYear < currentYear - 10 || newStartYear > currentYear + 10) {
                return Result.failure(IllegalArgumentException("Cannot move event beyond reasonable date range"))
            }
            
            // Check for collision with other events
            val collisionResult = validateCollision(updatedEvent)
            if (collisionResult.isFailure) {
                return collisionResult
            }
            
            // Update the event in repository
            eventRepository.updateEvent(updatedEvent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun pixelOffsetToDays(pixelOffset: Float, zoomLevel: Float): Int {
        val pixelsPerDay = DateCalculations.getPixelsPerDay(zoomLevel)
        val daysFloat = pixelOffset / pixelsPerDay
        
        // Only move if dragged more than half a day
        return if (kotlin.math.abs(daysFloat) >= 0.5f) {
            kotlin.math.round(daysFloat).toInt()
        } else {
            0
        }
    }
    
    private fun addDaysToDate(date: Date, days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_MONTH, days)
        return calendar.time
    }
    
    private suspend fun validateCollision(eventToMove: Event): Result<Event> {
        return try {
            // Get all events except the one being moved
            val allEvents = eventRepository.getEvents()
            val otherEvents = allEvents.filter { it.id != eventToMove.id }
            
            // Check for overlaps with other events
            val conflictingEvents = otherEvents.filter { otherEvent ->
                eventsOverlap(eventToMove, otherEvent)
            }
            
            if (conflictingEvents.isNotEmpty()) {
                val conflictNames = conflictingEvents.joinToString(", ") { it.name }
                println("MoveEventUseCase: Collision detected with: $conflictNames")
                return Result.failure(
                    IllegalArgumentException("Event conflicts with: $conflictNames")
                )
            }
            
            Result.success(eventToMove)
        } catch (e: Exception) {
            println("MoveEventUseCase: Error validating collision: ${e.message}")
            Result.failure(e)
        }
    }
    
    private fun eventsOverlap(event1: Event, event2: Event): Boolean {
        // Two events overlap if:
        // event1.start < event2.end AND event2.start < event1.end
        val event1Start = event1.startDate.time
        val event1End = event1.endDate.time
        val event2Start = event2.startDate.time  
        val event2End = event2.endDate.time
        
        return event1Start < event2End && event2Start < event1End
    }
}