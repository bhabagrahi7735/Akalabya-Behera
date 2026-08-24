package com.example.ui.screens.archive

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JournalEntry
import com.example.data.model.Mood
import com.example.ui.components.EmptyState
import com.example.ui.components.EmptyStateType
import com.example.ui.components.JournalEntryCard
import com.example.ui.components.MoodChip
import com.example.ui.screens.JournalViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ArchiveScreen(
    journalViewModel: JournalViewModel,
    onOpenEditor: (entryId: String?, dateMillis: Long?) -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val entries by journalViewModel.filteredEntries.collectAsState()
    val selectedMood by journalViewModel.selectedMoodFilter.collectAsState()
    val onlyFavorites by journalViewModel.onlyFavoritesFilter.collectAsState()

    // Group entries by Month & Year
    val monthGroups = remember(entries) {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        entries.groupBy { monthFormat.format(Date(it.dateMillis)) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("archive_screen_container")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header & Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Archive",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${entries.size} reflections preserved",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onNavigateToSearch,
                    modifier = Modifier.testTag("archive_search_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search archive",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Filter Chips Horizontal Scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // All Chip
                FilterChip(
                    selected = selectedMood == null && !onlyFavorites,
                    onClick = {
                        journalViewModel.setSelectedMoodFilter(null)
                        if (onlyFavorites) journalViewModel.toggleFavoritesFilter()
                    },
                    label = { Text("All") },
                    shape = RoundedCornerShape(18.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("archive_filter_all")
                )

                // Favorites Star Chip
                FilterChip(
                    selected = onlyFavorites,
                    onClick = { journalViewModel.toggleFavoritesFilter() },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (onlyFavorites) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (onlyFavorites) Color(0xFFE0A96D) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Starred")
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("archive_filter_starred")
                )

                // Mood Filter Chips
                Mood.entries.forEach { mood ->
                    MoodChip(
                        mood = mood,
                        selected = selectedMood == mood,
                        onClick = { journalViewModel.setSelectedMoodFilter(mood) },
                        compact = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Entries List
            if (entries.isEmpty()) {
                EmptyState(
                    type = if (onlyFavorites) EmptyStateType.FAVORITES else EmptyStateType.ARCHIVE,
                    onActionClick = { onOpenEditor(null, System.currentTimeMillis()) },
                    actionText = "Write New Page",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    monthGroups.forEach { (monthName, monthEntries) ->
                        item(key = "header_$monthName") {
                            Text(
                                text = monthName.uppercase(),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }

                        items(monthEntries, key = { it.id }) { entry ->
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
    }
}
