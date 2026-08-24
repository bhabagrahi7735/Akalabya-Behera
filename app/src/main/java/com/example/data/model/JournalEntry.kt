package com.example.data.model

data class JournalEntry(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val mood: Mood,
    val dateMillis: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList(),
    val isSynced: Boolean = false
) {
    val wordCount: Int
        get() = if (content.isBlank()) 0 else content.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size

    val charCount: Int
        get() = content.length

    val readingTimeMinutes: Int
        get() = maxOf(1, (wordCount / 200) + if (wordCount % 200 > 0) 1 else 0)

    val previewText: String
        get() {
            val trimmed = content.trim()
            return if (trimmed.length > 140) trimmed.take(140) + "..." else trimmed
        }
}
