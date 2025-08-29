package com.airtable.interview.airtableschedule.domain.usecase

import com.airtable.interview.airtableschedule.domain.model.Event

/**
 * Use Case for assigning events to timeline lanes based on their start and end dates.
 * This algorithm optimizes space by placing non-overlapping events in the same lane.
 */
class AssignLanesUseCase {
    
    /**
     * Assigns events to lanes to optimize vertical space usage.
     * Events that don't overlap (end date of one is before start date of another) 
     * can share the same lane.
     * 
     * @param events List of events to organize into lanes
     * @return List of lanes, where each lane contains a list of events
     */
    operator fun invoke(events: List<Event>): List<List<Event>> {
        val lanes = mutableListOf<MutableList<Event>>()

        // Sort events by start date for optimal lane assignment
        events.sortedBy { event -> event.startDate }
            .forEach { event ->
                // Find an existing lane where this event can fit
                val availableLane = lanes.find { lane ->
                    lane.last().endDate < event.startDate
                }

                if (availableLane != null) {
                    // Add to existing lane
                    availableLane.add(event)
                } else {
                    // Create new lane for this event
                    lanes.add(mutableListOf(event))
                }
            }
        return lanes
    }
}

/**
 * Convenience function for backward compatibility
 */
fun assignLanes(events: List<Event>): List<List<Event>> {
    return AssignLanesUseCase().invoke(events)
}
