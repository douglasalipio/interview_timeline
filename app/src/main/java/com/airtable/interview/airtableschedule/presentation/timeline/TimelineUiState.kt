package com.airtable.interview.airtableschedule.presentation.timeline

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airtable.interview.airtableschedule.domain.model.Event
import java.util.Date

/**
 * Represents a lane with its events
 */
data class LaneWithEvents(
    val laneIndex: Int,
    val events: List<Event>
)

/**
 * Processed timeline data with lanes and date ranges
 */
data class ProcessedTimelineData(
    val lanes: List<LaneWithEvents>,
    val minDate: Date?,
    val maxDate: Date?,
    val totalWidth: Dp,
    val zoomLevel: Float = 1.0f
)

/**
 * Represents drag preview state
 */
data class DragPreviewState(
    val event: Event,
    val originalPosition: Float,
    val currentOffset: Float,
    val previewDate: Date
)

/**
 * UI state for the timeline screen.
 */
data class TimelineUiState(
    val events: List<Event> = emptyList(),
    val processedData: ProcessedTimelineData = ProcessedTimelineData(
        lanes = emptyList(),
        minDate = null,
        maxDate = null,
        totalWidth = 0.dp,
        zoomLevel = 1.0f
    ),
    val editingEvent: Event? = null,
    val isEditMode: Boolean = false,
    val dragPreview: DragPreviewState? = null
)
