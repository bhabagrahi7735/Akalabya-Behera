package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.JournalEntry
import com.example.data.model.Mood

@Entity(
    tableName = "journal_entries",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "dateMillis"]),
        Index(value = ["userId", "updatedAt"])
    ]
)
data class JournalEntryEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val mood: String,
    val dateMillis: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val isFavorite: Boolean = false,
    val tags: String = "", // Comma-separated tags
    val isSynced: Boolean = false
) {
    fun toDomain(): JournalEntry {
        return JournalEntry(
            id = id,
            userId = userId,
            title = title,
            content = content,
            mood = Mood.fromString(mood),
            dateMillis = dateMillis,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isFavorite = isFavorite,
            tags = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() },
            isSynced = isSynced
        )
    }

    companion object {
        fun fromDomain(entry: JournalEntry): JournalEntryEntity {
            return JournalEntryEntity(
                id = entry.id,
                userId = entry.userId,
                title = entry.title,
                content = entry.content,
                mood = entry.mood.name,
                dateMillis = entry.dateMillis,
                createdAt = entry.createdAt,
                updatedAt = entry.updatedAt,
                isFavorite = entry.isFavorite,
                tags = entry.tags.joinToString(","),
                isSynced = entry.isSynced
            )
        }
    }
}
