package com.airtable.interview.airtableschedule.presentation.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airtable.interview.airtableschedule.domain.logic.DateCalculations
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Header component that shows the time axis with date markers
 */
@Composable
fun TimeAxisHeader(
    minDate: Date?,
    maxDate: Date?,
    totalWidth: Dp,
    zoomLevel: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    val headerHeight = 50.dp
    
    if (minDate == null || maxDate == null) {
        // Empty header if no dates
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(headerHeight)
                .background(MaterialTheme.colorScheme.surface)
        )
        return
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(headerHeight)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Container with full timeline width
        Box(
            modifier = Modifier
                .width(totalWidth)
                .height(headerHeight)
        ) {
            TimeAxisMarkers(
                minDate = minDate,
                maxDate = maxDate,
                zoomLevel = zoomLevel
            )
        }
        
        // Bottom border
        HorizontalDivider(
            modifier = Modifier.align(Alignment.BottomStart),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            thickness = 1.dp
        )
    }
}

/**
 * Generate time axis markers (days/months)
 */
@Composable
private fun TimeAxisMarkers(
    minDate: Date,
    maxDate: Date,
    zoomLevel: Float = 1.0f
) {
    val dayFormatter = SimpleDateFormat("dd", Locale.getDefault())
    val monthFormatter = SimpleDateFormat("MMM", Locale.getDefault())
    val fullDateFormatter = SimpleDateFormat("MMM dd", Locale.getDefault())
    
    val calendar = Calendar.getInstance()
    calendar.time = minDate
    
    // Reset to start of day
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    
    val endCalendar = Calendar.getInstance()
    endCalendar.time = maxDate
    
    // Generate markers for each day
    while (calendar.time <= endCalendar.time) {
        val currentDate = calendar.time
        val xPosition = DateCalculations.calculateXPosition(currentDate, minDate, zoomLevel)
        val isFirstDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH) == 1
        
        // Day marker
        Box(
            modifier = Modifier
                .offset(x = xPosition)
                .width(DateCalculations.getPixelsPerDay(zoomLevel).dp)
                .height(50.dp)
        ) {
            // Vertical line for day separation
            if (isFirstDayOfMonth) {
                VerticalDivider(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .height(25.dp),
                    color = MaterialTheme.colorScheme.primary,
                    thickness = 2.dp
                )
            } else {
                VerticalDivider(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .height(15.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    thickness = 0.5.dp
                )
            }
            
            // Date label
            if (isFirstDayOfMonth) {
                // Show month name for first day of month
                Text(
                    text = monthFormatter.format(currentDate),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 2.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            } else {
                // Show day number
                Text(
                    text = dayFormatter.format(currentDate),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 2.dp),
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Move to next day
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }
}

/**
 * Simplified time axis header for smaller spaces
 */
@Composable
fun SimpleTimeAxisHeader(
    minDate: Date?,
    maxDate: Date?,
    modifier: Modifier = Modifier
) {
    val headerHeight = 30.dp
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(headerHeight)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (minDate != null && maxDate != null) {
            Text(
                text = "${dateFormatter.format(minDate)} → ${dateFormatter.format(maxDate)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            Text(
                text = "Timeline",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        HorizontalDivider(
            modifier = Modifier.align(Alignment.BottomStart),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    }
}