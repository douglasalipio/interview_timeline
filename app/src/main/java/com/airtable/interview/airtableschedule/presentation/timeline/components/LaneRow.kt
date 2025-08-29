package com.airtable.interview.airtableschedule.presentation.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airtable.interview.airtableschedule.domain.model.Event
import java.util.Date

/**
 * A row representing a single lane in the timeline with its events
 */
@Composable
fun LaneRow(
    events: List<Event>,
    minDate: Date,
    totalWidth: Dp,
    laneIndex: Int,
    zoomLevel: Float = 1.0f,
    modifier: Modifier = Modifier,
    onEventClick: (Event) -> Unit = {},
    onEventMove: (Event, Float) -> Unit = { _, _ -> },
    onDragStart: (Event, Float) -> Unit = { _, _ -> },
    onDragUpdate: (Float) -> Unit = {},
    onDragEnd: () -> Unit = {},
    dragPreview: com.airtable.interview.airtableschedule.presentation.timeline.DragPreviewState? = null
) {
    val laneHeight = 60.dp
    val isEvenLane = laneIndex % 2 == 0
    val backgroundColor = if (isEvenLane) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(laneHeight)
            .background(backgroundColor)
    ) {
        // Container with full timeline width for proper positioning
        Box(
            modifier = Modifier
                .width(totalWidth)
                .height(laneHeight)
        ) {
            // Render each event in this lane
            events.forEach { event ->
                EventCardWithCalculatedWidth(
                    event = event,
                    minDate = minDate,
                    zoomLevel = zoomLevel,
                    modifier = Modifier.padding(vertical = 4.dp),
                    onEventClick = onEventClick,
                    onEventDrag = onEventMove,
                    onDragStart = onDragStart,
                    onDragUpdate = onDragUpdate,
                    onDragEnd = onDragEnd
                )
            }
            
            // Render drag preview if dragging
            dragPreview?.let { preview ->
                val previewWidth = com.airtable.interview.airtableschedule.domain.logic.DateCalculations.calculateEventWidth(
                    preview.event.startDate, 
                    preview.event.endDate, 
                    zoomLevel
                )
                val previewX = com.airtable.interview.airtableschedule.domain.logic.DateCalculations.calculateXPosition(
                    preview.previewDate, 
                    minDate, 
                    zoomLevel
                )
                
                EventCard(
                    event = preview.event,
                    width = previewWidth,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .offset(x = previewX)
                        .alpha(0.7f)
                )
            }
        }
        
        // Lane separator
        HorizontalDivider(
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomStart),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            thickness = 0.5.dp
        )
    }
}

/**
 * Empty lane row for spacing when no events
 */
@Composable
fun EmptyLaneRow(
    laneIndex: Int,
    modifier: Modifier = Modifier
) {
    val laneHeight = 60.dp
    val isEvenLane = laneIndex % 2 == 0
    val backgroundColor = if (isEvenLane) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(laneHeight)
            .background(backgroundColor)
    ) {
        HorizontalDivider(
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomStart),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            thickness = 0.5.dp
        )
    }
}