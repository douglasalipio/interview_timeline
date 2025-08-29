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
}