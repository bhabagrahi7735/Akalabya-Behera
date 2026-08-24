package com.example.ui.screens.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.JournalEntry
import com.example.data.model.Mood
import com.example.data.repository.JournalRepository
import com.example.ui.components.SaveState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class EditorViewModel(
    private val journalRepository: JournalRepository,
    private val userId: String,
    private val initialEntryId: String?,
    private val initialDateMillis: Long?
) : ViewModel() {

    private val entryId: String = initialEntryId ?: UUID.randomUUID().toString()
    val currentEntryId: String get() = entryId

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _mood = MutableStateFlow(Mood.CALM)
    val mood: StateFlow<Mood> = _mood.asStateFlow()

    private val _dateMillis = MutableStateFlow(initialDateMillis ?: System.currentTimeMillis())
    val dateMillis: StateFlow<Long> = _dateMillis.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _saveState = MutableStateFlow(SaveState.IDLE)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    private val _lastSavedTime = MutableStateFlow<Long?>(null)
    val lastSavedTime: StateFlow<Long?> = _lastSavedTime.asStateFlow()

    private var createdAt: Long = System.currentTimeMillis()
    private var autoSaveJob: Job? = null
    private var isLoaded = false

    init {
        loadEntry()
    }

    private fun loadEntry() {
        if (initialEntryId != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val existing = journalRepository.getEntryByIdDirect(userId, initialEntryId)
                if (existing != null) {
                    _title.value = existing.title
                    _content.value = existing.content
                    _mood.value = existing.mood
                    _dateMillis.value = existing.dateMillis
                    _isFavorite.value = existing.isFavorite
                    _lastSavedTime.value = existing.updatedAt
                    _saveState.value = SaveState.SAVED
                    createdAt = existing.createdAt
                }
                isLoaded = true
            }
        } else {
            isLoaded = true
        }
    }

    fun onTitleChange(newTitle: String) {
        if (_title.value == newTitle) return
        _title.value = newTitle
        triggerDebouncedAutoSave()
    }

    fun onContentChange(newContent: String) {
        if (_content.value == newContent) return
        _content.value = newContent
        triggerDebouncedAutoSave()
    }

    fun onMoodChange(newMood: Mood) {
        if (_mood.value == newMood) return
        _mood.value = newMood
        triggerDebouncedAutoSave()
    }

    fun onDateChange(newDateMillis: Long) {
        _dateMillis.value = newDateMillis
        triggerDebouncedAutoSave()
    }

    fun toggleFavorite() {
        _isFavorite.value = !_isFavorite.value
        triggerDebouncedAutoSave()
    }

    fun applyFormatting(prefix: String, suffix: String = "", placeholder: String = "") {
        val current = _content.value
        val newText = if (current.isEmpty()) {
            "$prefix$placeholder$suffix"
        } else if (current.endsWith("\n")) {
            "$current$prefix$placeholder$suffix"
        } else {
            "$current\n$prefix$placeholder$suffix"
        }
        onContentChange(newText)
    }

    private fun triggerDebouncedAutoSave() {
        if (!isLoaded) return
        _saveState.value = SaveState.SAVING
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch(Dispatchers.IO) {
            delay(750L) // Debounce 750ms
            saveEntryInternal()
        }
    }

    fun forceSaveImmediate() {
        autoSaveJob?.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            saveEntryInternal()
        }
    }

    private suspend fun saveEntryInternal() {
        val currentTitle = _title.value
        val currentContent = _content.value

        // If completely empty and brand new, don't persist blank noise
        if (currentTitle.isBlank() && currentContent.isBlank() && initialEntryId == null) {
            _saveState.value = SaveState.IDLE
            return
        }

        try {
            val now = System.currentTimeMillis()
            val entry = JournalEntry(
                id = entryId,
                userId = userId,
                title = currentTitle,
                content = currentContent,
                mood = _mood.value,
                dateMillis = _dateMillis.value,
                createdAt = createdAt,
                updatedAt = now,
                isFavorite = _isFavorite.value,
                isSynced = false
            )
            journalRepository.saveOrUpdateEntry(entry)
            _lastSavedTime.value = now
            _saveState.value = SaveState.SAVED
        } catch (e: Exception) {
            _saveState.value = SaveState.ERROR
        }
    }

    fun deleteEntry(onDeleted: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            journalRepository.deleteEntry(userId, entryId)
            viewModelScope.launch(Dispatchers.Main) {
                onDeleted()
            }
        }
    }

    class Factory(
        private val journalRepository: JournalRepository,
        private val userId: String,
        private val initialEntryId: String?,
        private val initialDateMillis: Long?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EditorViewModel(journalRepository, userId, initialEntryId, initialDateMillis) as T
        }
    }
}
