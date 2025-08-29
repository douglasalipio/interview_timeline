package com.airtable.interview.airtableschedule.presentation.timeline.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.airtable.interview.airtableschedule.domain.model.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditDialog(
    event: Event,
    onDismiss: () -> Unit,
    onSave: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    var eventName by remember { mutableStateOf(event.name) }
    var startDate by remember { mutableStateOf(event.startDate) }
    var endDate by remember { mutableStateOf(event.endDate) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    // Remember the DatePicker states to persist selections
    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = startDate.time
    )
    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = endDate.time
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "Edit Event",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Event Name Field
                OutlinedTextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = { Text("Event Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Start Date Field
                OutlinedTextField(
                    value = dateFormatter.format(startDate),
                    onValueChange = { },
                    label = { Text("Start Date") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        TextButton(
                            onClick = { showStartDatePicker = true }
                        ) {
                            Text("Select")
                        }
                    }
                )
                
                // End Date Field
                OutlinedTextField(
                    value = dateFormatter.format(endDate),
                    onValueChange = { },
                    label = { Text("End Date") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        TextButton(
                            onClick = { showEndDatePicker = true }
                        ) {
                            Text("Select")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    TextButton(
                        onClick = {
                            val updatedEvent = event.copy(
                                name = eventName.trim(),
                                startDate = startDate,
                                endDate = endDate
                            )
                            println("Saving event: ${updatedEvent.name}, start: ${updatedEvent.startDate}, end: ${updatedEvent.endDate}")
                            onSave(updatedEvent)
                        },
                        enabled = eventName.trim().isNotEmpty()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
    
    // Start Date Picker
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startDatePickerState.selectedDateMillis?.let { millis ->
                            val newStartDate = normalizeDate(Date(millis))
                            println("DatePicker: Selected start date: $newStartDate")
                            startDate = newStartDate
                            // Ensure end date is not before start date
                            if (endDate.before(newStartDate)) {
                                endDate = newStartDate
                                println("DatePicker: Updated end date to match: $endDate")
                            }
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showStartDatePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }
    
    // End Date Picker
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endDatePickerState.selectedDateMillis?.let { millis ->
                            val newEndDate = normalizeDate(Date(millis))
                            println("DatePicker: Selected end date: $newEndDate")
                            // Ensure end date is not before start date
                            if (newEndDate.before(startDate)) {
                                endDate = startDate
                                println("DatePicker: End date was before start, set to start date: $endDate")
                            } else {
                                endDate = newEndDate
                                println("DatePicker: End date updated: $endDate")
                            }
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEndDatePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }
}

private fun normalizeDate(date: Date): Date {
    val calendar = java.util.Calendar.getInstance()
    calendar.time = date
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 12)
    calendar.set(java.util.Calendar.MINUTE, 0)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    return calendar.time
}