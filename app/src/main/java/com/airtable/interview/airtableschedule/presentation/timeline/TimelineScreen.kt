package com.airtable.interview.airtableschedule.presentation.timeline

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airtable.interview.airtableschedule.presentation.timeline.components.EventEditDialog
import com.airtable.interview.airtableschedule.presentation.timeline.components.LaneRow
import com.airtable.interview.airtableschedule.presentation.timeline.components.TimeAxisHeader

/**
 * A screen that displays a timeline of events.
 */
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        TimelineView(
            uiState = uiState,
            onZoomIn = viewModel::zoomIn,
            onZoomOut = viewModel::zoomOut,
            onZoomChange = viewModel::setZoomLevel,
            onEventClick = viewModel::startEditingEvent,
            onEditDismiss = viewModel::stopEditingEvent,
            onEventSave = viewModel::updateEvent,
            onEventMove = viewModel::moveEvent,
            onDragStart = viewModel::startDragPreview,
            onDragUpdate = viewModel::updateDragPreview,
            onDragEnd = viewModel::endDragPreview
        )
    }
}

/**
 * Main timeline view with lanes and scrolling
 */
@Composable
private fun TimelineView(
    uiState: TimelineUiState,
    onZoomIn: () -> Unit = {},
    onZoomOut: () -> Unit = {},
    onZoomChange: (Float) -> Unit = {},
    onEventClick: (com.airtable.interview.airtableschedule.domain.model.Event) -> Unit = {},
    onEditDismiss: () -> Unit = {},
    onEventSave: (com.airtable.interview.airtableschedule.domain.model.Event) -> Unit = {},
    onEventMove: (com.airtable.interview.airtableschedule.domain.model.Event, Float) -> Unit = { _, _ -> },
    onDragStart: (com.airtable.interview.airtableschedule.domain.model.Event, Float) -> Unit = { _, _ -> },
    onDragUpdate: (Float) -> Unit = {},
    onDragEnd: () -> Unit = {}
) {
    val processedData = uiState.processedData

    if (processedData.lanes.isEmpty()) {
        // Empty state
        EmptyTimelineView()
        return
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Timeline header with zoom controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Timeline",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Zoom controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = onZoomOut
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Zoom Out"
                    )
                }

                Text(
                    text = "${(processedData.zoomLevel * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                FilledIconButton(
                    onClick = onZoomIn
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In"
                    )
                }
            }
        }

        // Scrollable timeline content with pinch-to-zoom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        val currentZoom = processedData.zoomLevel
                        val newZoom = (currentZoom * zoom)
                        onZoomChange(newZoom)
                    }
                }
        ) {
            Column {
                // Time axis header
                TimeAxisHeader(
                    minDate = processedData.minDate,
                    maxDate = processedData.maxDate,
                    totalWidth = processedData.totalWidth,
                    zoomLevel = processedData.zoomLevel
                )

                // Lanes with events
                processedData.lanes.forEach { lane ->
                    LaneRow(
                        events = lane.events,
                        minDate = processedData.minDate!!,
                        totalWidth = processedData.totalWidth,
                        laneIndex = lane.laneIndex,
                        zoomLevel = processedData.zoomLevel,
                        onEventClick = onEventClick,
                        onEventMove = onEventMove,
                        onDragStart = onDragStart,
                        onDragUpdate = onDragUpdate,
                        onDragEnd = onDragEnd,
                        dragPreview = uiState.dragPreview
                    )
                }
            }
        }

        // Show edit dialog when editing
        uiState.editingEvent?.let { event ->
            EventEditDialog(
                event = event,
                onDismiss = onEditDismiss,
                onSave = onEventSave
            )
        }
    }
}

/**
 * Empty state when no events are available
 */
@Composable
private fun EmptyTimelineView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No Events",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "Timeline will appear here when events are loaded",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}
