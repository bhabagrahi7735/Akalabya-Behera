package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.JournalEntry
import com.example.data.model.Mood
import com.example.data.model.User
import com.example.data.repository.JournalRepository
import com.example.data.repository.JournalStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class JournalViewModel(
    private val journalRepository: JournalRepository,
    private val currentUserFlow: StateFlow<User?>
) : ViewModel() {

    val currentUserId: String
        get() = currentUserFlow.value?.uid ?: "local_guest_user"

    // All entries flow for active user
    val allEntries: StateFlow<List<JournalEntry>> = currentUserFlow.flatMapLatest { user ->
        val uid = user?.uid ?: "local_guest_user"
        journalRepository.getEntriesForUser(uid)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Today's entry flow
    val todayEntry: StateFlow<JournalEntry?> = allEntries.flatMapLatest { entries ->
        val todayMillis = System.currentTimeMillis()
        val found = entries.find { isSameCalendarDay(it.dateMillis, todayMillis) }
        flowOf(found)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Calculated Statistics
    val journalStats: StateFlow<JournalStats> = allEntries.flatMapLatest { list ->
        flowOf(journalRepository.calculateStats(list))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = JournalStats()
    )

    // Search query & filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMoodFilter = MutableStateFlow<Mood?>(null)
    val selectedMoodFilter: StateFlow<Mood?> = _selectedMoodFilter.asStateFlow()

    private val _onlyFavoritesFilter = MutableStateFlow(false)
    val onlyFavoritesFilter: StateFlow<Boolean> = _onlyFavoritesFilter.asStateFlow()

    val filteredEntries: StateFlow<List<JournalEntry>> = combine(
        allEntries,
        _searchQuery,
        _selectedMoodFilter,
        _onlyFavoritesFilter
    ) { entries, query, moodFilter, onlyFavs ->
        entries.filter { entry ->
            val matchesQuery = query.isBlank() ||
                    entry.title.contains(query, ignoreCase = true) ||
                    entry.content.contains(query, ignoreCase = true)

            val matchesMood = moodFilter == null || entry.mood == moodFilter
            val matchesFav = !onlyFavs || entry.isFavorite

            matchesQuery && matchesMood && matchesFav
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Cloud Sync State
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedMoodFilter(mood: Mood?) {
        _selectedMoodFilter.value = if (_selectedMoodFilter.value == mood) null else mood
    }

    fun toggleFavoritesFilter() {
        _onlyFavoritesFilter.value = !_onlyFavoritesFilter.value
    }

    fun toggleFavorite(entry: JournalEntry) {
        viewModelScope.launch {
            journalRepository.toggleFavorite(entry.userId, entry.id, entry.isFavorite)
        }
    }

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            journalRepository.deleteEntry(entry.userId, entry.id)
        }
    }

    fun deleteAllEntries() {
        viewModelScope.launch {
            journalRepository.deleteAllEntriesForUser(currentUserId)
        }
    }

    fun syncWithCloud() {
        val uid = currentUserFlow.value?.uid ?: return
        viewModelScope.launch {
            _isSyncing.value = true
            journalRepository.syncAllWithCloud(uid)
            _lastSyncTime.value = System.currentTimeMillis()
            _isSyncing.value = false
        }
    }

    fun exportAndShare(context: Context, asMarkdown: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            val entries = allEntries.value
            val userEmail = currentUserFlow.value?.email ?: "Akalabya User"
            val text = if (asMarkdown) {
                journalRepository.exportToMarkdown(entries, userEmail)
            } else {
                journalRepository.exportToJson(entries, userEmail)
            }

            withContext(Dispatchers.Main) {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, text)
                    putExtra(Intent.EXTRA_TITLE, "Akalabya Journal Export")
                    type = if (asMarkdown) "text/plain" else "application/json"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Export Journal Data")
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(shareIntent)
            }
        }
    }

    private fun isSameCalendarDay(time1: Long, time2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    class Factory(
        private val journalRepository: JournalRepository,
        private val currentUserFlow: StateFlow<User?>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return JournalViewModel(journalRepository, currentUserFlow) as T
        }
    }
}
