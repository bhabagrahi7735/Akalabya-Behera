package com.example.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JournalEntry
import com.example.ui.components.CalendarGrid
import com.example.ui.components.EmptyState
import com.example.ui.components.EmptyStateType
import com.example.ui.components.JournalEntryCard
import com.example.ui.components.isSameDay
import com.example.ui.screens.JournalViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    journalViewModel: JournalViewModel,
    onOpenEditor: (entryId: String?, dateMillis: Long?) -> Unit
) {
    val allEntries by journalViewModel.allEntries.collectAsState()

    var currentMonthCalendar by remember {
        mutableStateOf(Calendar.getInstance())
    }

    var selectedDateMillis by remember {
        mutableLongStateOf(System.currentTimeMillis())
    }

    val selectedDayEntries = remember(allEntries, selectedDateMillis) {
        allEntries.filter { isSameDay(it.dateMillis, selectedDateMillis) }
    }

    val selectedDayFormatted = remember(selectedDateMillis) {
        SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(selectedDateMillis))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("calendar_screen_container")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "Calendar",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Browse your mindful journey day by day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Calendar Grid Card
            item {
                CalendarGrid(
                    currentMonth = currentMonthCalendar,
                    selectedDateMillis = selectedDateMillis,
                    entries = allEntries,
                    onMonthChange = { delta ->
                        val newCal = currentMonthCalendar.clone() as Calendar
                        newCal.add(Calendar.MONTH, delta)
                        currentMonthCalendar = newCal
                    },
                    onDateSelected = { date ->
                        selectedDateMillis = date
                    }
                )
            }

            // Selected Day Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SELECTED DATE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = selectedDayFormatted,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = { onOpenEditor(null, selectedDateMillis) },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("calendar_write_for_day_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Create,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Write",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            // Selected Day Entries
            if (selectedDayEntries.isEmpty()) {
                item {
                    EmptyState(
                        type = EmptyStateType.CALENDAR,
                        onActionClick = { onOpenEditor(null, selectedDateMillis) },
                        actionText = "Reflect for this Day"
                    )
                }
            } else {
                items(selectedDayEntries, key = { it.id }) { entry ->
                    JournalEntryCard(
                        entry = entry,
                        onClick = { onOpenEditor(entry.id, entry.dateMillis) },
                        onFavoriteClick = { journalViewModel.toggleFavorite(entry) }
                    )
                }
            }
        }
    }
}
