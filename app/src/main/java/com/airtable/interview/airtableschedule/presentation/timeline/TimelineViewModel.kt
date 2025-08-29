package com.airtable.interview.airtableschedule.presentation.timeline

import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airtable.interview.airtableschedule.data.repository.EventRepositoryImpl
import com.airtable.interview.airtableschedule.domain.model.Event
import com.airtable.interview.airtableschedule.domain.repository.EventRepository
import com.airtable.interview.airtableschedule.domain.usecase.AssignLanesUseCase
import com.airtable.interview.airtableschedule.domain.usecase.UpdateEventUseCase
import com.airtable.interview.airtableschedule.domain.usecase.DeleteEventUseCase
import com.airtable.interview.airtableschedule.domain.usecase.MoveEventUseCase
import com.airtable.interview.airtableschedule.domain.logic.DateCalculations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel responsible for managing the state of the timeline screen.
 */
class TimelineViewModel : ViewModel() {
    private val eventRepository: EventRepository = EventRepositoryImpl()
    private val assignLanesUseCase: AssignLanesUseCase = AssignLanesUseCase()
    private val updateEventUseCase: UpdateEventUseCase = UpdateEventUseCase(eventRepository)
    private val deleteEventUseCase: DeleteEventUseCase = DeleteEventUseCase(eventRepository)
    private val moveEventUseCase: MoveEventUseCase = MoveEventUseCase(eventRepository)

    // Zoom level state
    private val _zoomLevel = MutableStateFlow(1.0f)
    val zoomLevel: StateFlow<Float> = _zoomLevel

    // Edit state
    private val _editingEvent = MutableStateFlow<Event?>(null)
    private val _isEditMode = MutableStateFlow(false)
    
    // Drag preview state
    private val _dragPreview = MutableStateFlow<DragPreviewState?>(null)

    val uiState: StateFlow<TimelineUiState> = combine(
        eventRepository.getTimelineItems(),
        _zoomLevel,
        _editingEvent,
        _isEditMode,
        _dragPreview
    ) { events, zoomLevel, editingEvent, isEditMode, dragPreview ->
        println("ViewModel: UI State updating with ${events.size} events, zoom: $zoomLevel")
        val processedData = processEventsIntoLanes(events, zoomLevel)
        TimelineUiState(
            events = events,
            processedData = processedData,
            editingEvent = editingEvent,
            isEditMode = isEditMode,
            dragPreview = dragPreview
        )
    }.stateIn(
        viewModelScope,
        initialValue = TimelineUiState(),
        started = SharingStarted.WhileSubscribed()
    )

    private fun processEventsIntoLanes(
        events: List<com.airtable.interview.airtableschedule.domain.model.Event>,
        zoomLevel: Float
    ): ProcessedTimelineData {
        if (events.isEmpty()) {
            return ProcessedTimelineData(
                lanes = emptyList(),
                minDate = null,
                maxDate = null,
                totalWidth = 0.dp
            )
        }

        // Assign events to lanes using the use case
        val lanesWithEvents = assignLanesUseCase(events)
        val lanes = lanesWithEvents.mapIndexed { index, eventsInLane ->
            LaneWithEvents(
                laneIndex = index,
                events = eventsInLane
            )
        }

        // Calculate date range
        val minDate = DateCalculations.findMinDate(events)
        val maxDate = DateCalculations.findMaxDate(events)

        val totalWidth = if (minDate != null && maxDate != null) {
            DateCalculations.calculateTimelineWidth(minDate, maxDate, zoomLevel)
        } else {
            0.dp
        }

        return ProcessedTimelineData(
            lanes = lanes,
            minDate = minDate,
            maxDate = maxDate,
            totalWidth = totalWidth,
            zoomLevel = zoomLevel
        )
    }

    fun setZoomLevel(zoomLevel: Float) {
        val clampedZoom = zoomLevel.coerceIn(DateCalculations.MIN_ZOOM, DateCalculations.MAX_ZOOM)
        _zoomLevel.value = clampedZoom
    }

    fun zoomIn() {
        val newZoom = (_zoomLevel.value * 1.5f).coerceAtMost(DateCalculations.MAX_ZOOM)
        _zoomLevel.value = newZoom
    }

    fun zoomOut() {
        val newZoom = (_zoomLevel.value / 1.5f).coerceAtLeast(DateCalculations.MIN_ZOOM)
        _zoomLevel.value = newZoom
    }

    fun startEditingEvent(event: Event) {
        _editingEvent.value = event
        _isEditMode.value = true
    }

    fun stopEditingEvent() {
        _editingEvent.value = null
        _isEditMode.value = false
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            println("ViewModel: Updating event ${event.id} - ${event.name}")
            val result = updateEventUseCase(event)
            result.fold(
                onSuccess = { updatedEvent ->
                    println("ViewModel: Event updated successfully: ${updatedEvent.name}")
                    stopEditingEvent()
                },
                onFailure = { error ->
                    println("ViewModel: Failed to update event: ${error.message}")
                }
            )
        }
    }

    fun deleteEvent(eventId: Int) {
        viewModelScope.launch {
            val result = deleteEventUseCase(eventId)
            result.fold(
                onSuccess = {
                    stopEditingEvent()
                },
                onFailure = {
                    // TODO: Handle error (show toast, etc.)
                }
            )
        }
    }
    
    fun startDragPreview(event: Event, originalPosition: Float) {
        val previewState = DragPreviewState(
            event = event,
            originalPosition = originalPosition,
            currentOffset = 0f,
            previewDate = event.startDate // Will be updated during drag
        )
        _dragPreview.value = previewState
    }
    
    fun updateDragPreview(pixelOffset: Float) {
        _dragPreview.value?.let { currentPreview ->
            // Calculate preview date based on offset
            val daysOffset = pixelOffsetToDays(pixelOffset, _zoomLevel.value)
            val newStartDate = addDaysToDate(currentPreview.event.startDate, daysOffset)
            
            _dragPreview.value = currentPreview.copy(
                currentOffset = pixelOffset,
                previewDate = newStartDate
            )
        }
    }
    
    fun endDragPreview() {
        _dragPreview.value = null
    }
    
    fun moveEvent(event: Event, pixelOffset: Float) {
        // Skip if offset is too small to matter
        if (kotlin.math.abs(pixelOffset) < 5.0f) {
            return
        }
        
        viewModelScope.launch {
            println("ViewModel: Moving event ${event.id} by $pixelOffset pixels")
            val result = moveEventUseCase(event, pixelOffset, _zoomLevel.value)
            result.fold(
                onSuccess = { updatedEvent ->
                    // Only log if the dates actually changed
                    if (updatedEvent.startDate != event.startDate || updatedEvent.endDate != event.endDate) {
                        println("ViewModel: Event moved successfully: ${updatedEvent.name}, new dates: ${updatedEvent.startDate} - ${updatedEvent.endDate}")
                    }
                },
                onFailure = { error ->
                    println("ViewModel: Failed to move event: ${error.message}")
                }
            )
        }
    }
    
    private fun pixelOffsetToDays(pixelOffset: Float, zoomLevel: Float): Int {
        val pixelsPerDay = DateCalculations.getPixelsPerDay(zoomLevel)
        val daysFloat = pixelOffset / pixelsPerDay
        return kotlin.math.round(daysFloat).toInt()
    }
    
    private fun addDaysToDate(date: Date, days: Int): Date {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        calendar.add(java.util.Calendar.DAY_OF_MONTH, days)
        return calendar.time
    }
}
