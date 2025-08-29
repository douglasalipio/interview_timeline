package com.airtable.interview.airtableschedule.domain.logic

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airtable.interview.airtableschedule.domain.model.Event
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Domain logic for date calculations and timeline positioning.
 * Contains business rules for date operations and timeline layout calculations.
 */
object DateCalculations {
    
    /**
     * Base pixels per day for positioning calculations - this is a business rule
     * that determines the visual density of the timeline
     */
    const val BASE_PIXELS_PER_DAY = 40f
    
    /**
     * Minimum zoom level (more zoomed out)
     */
    const val MIN_ZOOM = 0.25f
    
    /**
     * Maximum zoom level (more zoomed in)
     */
    const val MAX_ZOOM = 4.0f
    
    /**
     * Calculate pixels per day based on zoom level
     */
    fun getPixelsPerDay(zoomLevel: Float): Float {
        return BASE_PIXELS_PER_DAY * zoomLevel.coerceIn(MIN_ZOOM, MAX_ZOOM)
    }
    
    /**
     * Calculate the number of days between two dates (inclusive)
     */
    fun getDaysBetween(startDate: Date, endDate: Date): Long {
        val diffInMillis = endDate.time - startDate.time
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)
    }
    
    /**
     * Calculate the duration of an event in days (minimum 1 day)
     */
    fun getEventDurationInDays(startDate: Date, endDate: Date): Long {
        return maxOf(1, getDaysBetween(startDate, endDate) + 1)
    }
    
    /**
     * Calculate X position based on date relative to minimum date
     */
    fun calculateXPosition(date: Date, minDate: Date, zoomLevel: Float = 1.0f): Dp {
        val daysBetween = getDaysBetween(minDate, date)
        val pixelsPerDay = getPixelsPerDay(zoomLevel)
        return (daysBetween * pixelsPerDay).dp
    }
    
    /**
     * Calculate width based on event duration
     */
    fun calculateEventWidth(startDate: Date, endDate: Date, zoomLevel: Float = 1.0f): Dp {
        val duration = getEventDurationInDays(startDate, endDate)
        val pixelsPerDay = getPixelsPerDay(zoomLevel)
        return (duration * pixelsPerDay).dp
    }
    
    /**
     * Find minimum date from a list of events
     */
    fun findMinDate(events: List<Event>): Date? {
        return events.minByOrNull { it.startDate }?.startDate
    }
    
    /**
     * Find maximum date from a list of events
     */
    fun findMaxDate(events: List<Event>): Date? {
        return events.maxByOrNull { it.endDate }?.endDate
    }
    
    /**
     * Calculate total timeline width based on date range
     */
    fun calculateTimelineWidth(minDate: Date, maxDate: Date, zoomLevel: Float = 1.0f): Dp {
        val totalDays = getDaysBetween(minDate, maxDate) + 1
        val pixelsPerDay = getPixelsPerDay(zoomLevel)
        return (totalDays * pixelsPerDay).dp
    }
    
    /**
     * Generate a color hash based on event ID for consistent coloring
     */
    fun generateColorFromId(id: Int): androidx.compose.ui.graphics.Color {
        val colors = listOf(
            androidx.compose.ui.graphics.Color(0xFF6200EE),
            androidx.compose.ui.graphics.Color(0xFF3700B3),
            androidx.compose.ui.graphics.Color(0xFF03DAC6),
            androidx.compose.ui.graphics.Color(0xFFFF5722),
            androidx.compose.ui.graphics.Color(0xFF4CAF50),
            androidx.compose.ui.graphics.Color(0xFFFF9800),
            androidx.compose.ui.graphics.Color(0xFF9C27B0),
            androidx.compose.ui.graphics.Color(0xFF2196F3)
        )
        return colors[id % colors.size]
    }
}