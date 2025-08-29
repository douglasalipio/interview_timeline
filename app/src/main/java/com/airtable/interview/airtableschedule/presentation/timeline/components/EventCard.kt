package com.airtable.interview.airtableschedule.presentation.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.airtable.interview.airtableschedule.domain.model.Event
import com.airtable.interview.airtableschedule.domain.logic.DateCalculations
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Individual event card component that displays an event in the timeline
 */
@Composable
fun EventCard(
    event: Event,
    width: Dp,
    modifier: Modifier = Modifier,
    onEventClick: (Event) -> Unit = {},
    onEventDrag: (Event, Float) -> Unit = { _, _ -> },
    onDragStart: (Event, Float) -> Unit = { _, _ -> },
    onDragUpdate: (Float) -> Unit = {},
    onDragEnd: () -> Unit = {}
) {
    val backgroundColor = DateCalculations.generateColorFromId(event.id)
    val dateFormatter = SimpleDateFormat("MMM dd", Locale.getDefault())
    var dragOffset by remember { mutableStateOf(0f) }
    var initialPosition by remember { mutableStateOf(0f) }
    
    Card(
        modifier = modifier
            .offset { IntOffset(dragOffset.roundToInt(), 0) }
            .clip(RoundedCornerShape(6.dp))
            .clickable { onEventClick(event) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        initialPosition = offset.x
                        onDragStart(event, offset.x)
                    },
                    onDrag = { _, dragAmount ->
                        dragOffset += dragAmount.x
                        onDragUpdate(dragOffset)
                    },
                    onDragEnd = {
                        if (kotlin.math.abs(dragOffset) >= 5.0f) { // Only trigger if dragged at least 5 pixels
                            onEventDrag(event, dragOffset)
                        }
                        onDragEnd()
                        dragOffset = 0f
                    }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .width(width)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                // Event name
                Text(
                    text = event.name,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Date range (if width allows)
                if (width > 80.dp) {
                    Text(
                        text = if (event.startDate == event.endDate) {
                            dateFormatter.format(event.startDate)
                        } else {
                            "${dateFormatter.format(event.startDate)} - ${dateFormatter.format(event.endDate)}"
                        },
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Variant of EventCard that calculates its own width based on event duration
 */
@Composable
fun EventCardWithCalculatedWidth(
    event: Event,
    minDate: Date,
    zoomLevel: Float = 1.0f,
    modifier: Modifier = Modifier,
    onEventClick: (Event) -> Unit = {},
    onEventDrag: (Event, Float) -> Unit = { _, _ -> },
    onDragStart: (Event, Float) -> Unit = { _, _ -> },
    onDragUpdate: (Float) -> Unit = {},
    onDragEnd: () -> Unit = {}
) {
    val width = DateCalculations.calculateEventWidth(event.startDate, event.endDate, zoomLevel)
    val offsetX = DateCalculations.calculateXPosition(event.startDate, minDate, zoomLevel)
    
    EventCard(
        event = event,
        width = width,
        modifier = modifier.offset(x = offsetX),
        onEventClick = onEventClick,
        onEventDrag = onEventDrag,
        onDragStart = onDragStart,
        onDragUpdate = onDragUpdate,
        onDragEnd = onDragEnd
    )
}