package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JournalEntry
import com.example.data.model.Mood
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarDay(
    val dayNumber: Int,
    val dateMillis: Long,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val entries: List<JournalEntry>
)

@Composable
fun CalendarGrid(
    currentMonth: Calendar,
    selectedDateMillis: Long,
    entries: List<JournalEntry>,
    onMonthChange: (Int) -> Unit, // -1 or +1
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val monthTitle = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonth.time)

    // Calculate days grid
    val daysInGrid = rememberDaysForMonth(currentMonth, entries)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("calendar_grid_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Month Header with arrows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row {
                    IconButton(
                        onClick = { onMonthChange(-1) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("prev_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "Previous month",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { onMonthChange(1) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("next_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Next month",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Days of week header
            val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6 rows of 7 days
            daysInGrid.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    week.forEach { day ->
                        val isSelected = isSameDay(day.dateMillis, selectedDateMillis)
                        val hasEntries = day.entries.isNotEmpty()

                        val selectedBg by animateColorAsState(
                            targetValue = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                day.isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else -> Color.Transparent
                            },
                            label = "calDayBg"
                        )

                        val textColor = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                            day.isToday -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(selectedBg)
                                .clickable { onDateSelected(day.dateMillis) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = day.dayNumber.toString(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected || day.isToday) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = textColor
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                // Mood Dots
                                if (hasEntries) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        day.entries.take(3).forEach { entry ->
                                            val moodColor = if (isDark) entry.mood.darkColor else entry.mood.lightColor
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(
                                                        color = if (isSelected) Color.White else moodColor,
                                                        shape = CircleShape
                                                    )
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun rememberDaysForMonth(currentMonth: Calendar, entries: List<JournalEntry>): List<CalendarDay> {
    val cal = currentMonth.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
    val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val todayCal = Calendar.getInstance()

    val daysList = mutableListOf<CalendarDay>()

    // Previous month filler days
    val prevMonthCal = currentMonth.clone() as Calendar
    prevMonthCal.add(Calendar.MONTH, -1)
    val maxDaysPrevMonth = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    for (i in (maxDaysPrevMonth - firstDayOfWeek + 1)..maxDaysPrevMonth) {
        prevMonthCal.set(Calendar.DAY_OF_MONTH, i)
        val timeMillis = prevMonthCal.timeInMillis
        val dayEntries = entries.filter { isSameDay(it.dateMillis, timeMillis) }
        daysList.add(
            CalendarDay(
                dayNumber = i,
                dateMillis = timeMillis,
                isCurrentMonth = false,
                isToday = isSameDay(timeMillis, todayCal.timeInMillis),
                entries = dayEntries
            )
        )
    }

    // Current month days
    val currentMonthCal = currentMonth.clone() as Calendar
    for (i in 1..maxDaysInMonth) {
        currentMonthCal.set(Calendar.DAY_OF_MONTH, i)
        val timeMillis = currentMonthCal.timeInMillis
        val dayEntries = entries.filter { isSameDay(it.dateMillis, timeMillis) }
        daysList.add(
            CalendarDay(
                dayNumber = i,
                dateMillis = timeMillis,
                isCurrentMonth = true,
                isToday = isSameDay(timeMillis, todayCal.timeInMillis),
                entries = dayEntries
            )
        )
    }

    // Next month filler days to complete 42 cells (6 rows * 7 columns) or 35 cells
    val totalCells = if (daysList.size > 35) 42 else 35
    val nextMonthCal = currentMonth.clone() as Calendar
    nextMonthCal.add(Calendar.MONTH, 1)
    var nextMonthDay = 1

    while (daysList.size < totalCells) {
        nextMonthCal.set(Calendar.DAY_OF_MONTH, nextMonthDay)
        val timeMillis = nextMonthCal.timeInMillis
        val dayEntries = entries.filter { isSameDay(it.dateMillis, timeMillis) }
        daysList.add(
            CalendarDay(
                dayNumber = nextMonthDay,
                dateMillis = timeMillis,
                isCurrentMonth = false,
                isToday = isSameDay(timeMillis, todayCal.timeInMillis),
                entries = dayEntries
            )
        )
        nextMonthDay++
    }

    return daysList
}

fun isSameDay(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
