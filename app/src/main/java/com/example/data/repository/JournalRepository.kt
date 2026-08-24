package com.example.data.repository

import android.content.Context
import com.example.data.local.JournalDao
import com.example.data.local.JournalEntryEntity
import com.example.data.model.JournalEntry
import com.example.data.model.Mood
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class JournalStats(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalEntries: Int = 0,
    val totalWords: Int = 0,
    val averageWordsPerEntry: Int = 0,
    val moodCounts: Map<Mood, Int> = emptyMap(),
    val mostFrequentMood: Mood? = null
)

class JournalRepository(
    private val context: Context,
    private val journalDao: JournalDao
) {
    private var firestore: FirebaseFirestore? = null

    init {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firestore = FirebaseFirestore.getInstance()
            }
        } catch (_: Exception) {
            firestore = null
        }
    }

    fun getEntriesForUser(userId: String): Flow<List<JournalEntry>> {
        return journalDao.getEntriesForUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getEntryById(userId: String, id: String): Flow<JournalEntry?> {
        return journalDao.getEntryById(userId, id).map { it?.toDomain() }
    }

    suspend fun getEntryByIdDirect(userId: String, id: String): JournalEntry? {
        return withContext(Dispatchers.IO) {
            journalDao.getEntryByIdDirect(userId, id)?.toDomain()
        }
    }

    fun getEntryForDate(userId: String, dateMillis: Long): Flow<JournalEntry?> {
        val (startOfDay, endOfDay) = getDayBounds(dateMillis)
        return journalDao.getEntryForDate(userId, startOfDay, endOfDay).map { it?.toDomain() }
    }

    fun searchEntries(userId: String, query: String): Flow<List<JournalEntry>> {
        return journalDao.searchEntries(userId, query).map { list -> list.map { it.toDomain() } }
    }

    fun getFavoriteEntries(userId: String): Flow<List<JournalEntry>> {
        return journalDao.getFavoriteEntries(userId).map { list -> list.map { it.toDomain() } }
    }

    fun getEntriesByMood(userId: String, mood: Mood): Flow<List<JournalEntry>> {
        return journalDao.getEntriesByMood(userId, mood.name).map { list -> list.map { it.toDomain() } }
    }

    suspend fun saveOrUpdateEntry(entry: JournalEntry): JournalEntry {
        return withContext(Dispatchers.IO) {
            val entity = JournalEntryEntity.fromDomain(entry)
            journalDao.insertEntry(entity)

            // Attempt async cloud sync if Firestore is available
            syncEntryToCloud(entry)

            entry
        }
    }

    suspend fun toggleFavorite(userId: String, entryId: String, currentFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            val entry = journalDao.getEntryByIdDirect(userId, entryId)
            if (entry != null) {
                val updated = entry.copy(isFavorite = !currentFavorite, updatedAt = System.currentTimeMillis())
                journalDao.updateEntry(updated)
                syncEntryToCloud(updated.toDomain())
            }
        }
    }

    suspend fun deleteEntry(userId: String, entryId: String) {
        withContext(Dispatchers.IO) {
            journalDao.deleteEntryById(userId, entryId)
            deleteEntryFromCloud(userId, entryId)
        }
    }

    suspend fun deleteAllEntriesForUser(userId: String) {
        withContext(Dispatchers.IO) {
            journalDao.deleteAllForUser(userId)
            try {
                firestore?.collection("users")
                    ?.document(userId)
                    ?.collection("entries")
                    ?.get()
                    ?.await()
                    ?.documents
                    ?.forEach { doc ->
                        doc.reference.delete()
                    }
            } catch (_: Exception) {}
        }
    }

    private suspend fun syncEntryToCloud(entry: JournalEntry) {
        if (entry.userId.startsWith("local_") || entry.userId.startsWith("guest_")) return
        try {
            val db = firestore ?: return
            val data = hashMapOf(
                "id" to entry.id,
                "userId" to entry.userId,
                "title" to entry.title,
                "content" to entry.content,
                "mood" to entry.mood.name,
                "dateMillis" to entry.dateMillis,
                "createdAt" to entry.createdAt,
                "updatedAt" to entry.updatedAt,
                "isFavorite" to entry.isFavorite,
                "tags" to entry.tags
            )
            db.collection("users")
                .document(entry.userId)
                .collection("entries")
                .document(entry.id)
                .set(data, SetOptions.merge())
                .await()

            // Update synced state locally
            val entity = JournalEntryEntity.fromDomain(entry.copy(isSynced = true))
            journalDao.insertEntry(entity)
        } catch (_: Exception) {
            // Remains unsynced for offline retry
        }
    }

    private suspend fun deleteEntryFromCloud(userId: String, entryId: String) {
        if (userId.startsWith("local_") || userId.startsWith("guest_")) return
        try {
            firestore?.collection("users")
                ?.document(userId)
                ?.collection("entries")
                ?.document(entryId)
                ?.delete()
                ?.await()
        } catch (_: Exception) {}
    }

    suspend fun syncAllWithCloud(userId: String) {
        if (userId.startsWith("local_") || userId.startsWith("guest_")) return
        withContext(Dispatchers.IO) {
            try {
                val db = firestore ?: return@withContext

                // 1. Push local unsynced entries
                val unsynced = journalDao.getUnsyncedEntries(userId)
                for (item in unsynced) {
                    syncEntryToCloud(item.toDomain())
                }

                // 2. Pull remote entries
                val snapshot = db.collection("users")
                    .document(userId)
                    .collection("entries")
                    .get()
                    .await()

                val remoteEntries = snapshot.documents.mapNotNull { doc ->
                    try {
                        val id = doc.getString("id") ?: doc.id
                        val title = doc.getString("title") ?: ""
                        val content = doc.getString("content") ?: ""
                        val mood = Mood.fromString(doc.getString("mood"))
                        val dateMillis = doc.getLong("dateMillis") ?: System.currentTimeMillis()
                        val createdAt = doc.getLong("createdAt") ?: dateMillis
                        val updatedAt = doc.getLong("updatedAt") ?: createdAt
                        val isFavorite = doc.getBoolean("isFavorite") ?: false
                        @Suppress("UNCHECKED_CAST")
                        val tags = (doc.get("tags") as? List<String>) ?: emptyList()

                        JournalEntry(
                            id = id,
                            userId = userId,
                            title = title,
                            content = content,
                            mood = mood,
                            dateMillis = dateMillis,
                            createdAt = createdAt,
                            updatedAt = updatedAt,
                            isFavorite = isFavorite,
                            tags = tags,
                            isSynced = true
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                if (remoteEntries.isNotEmpty()) {
                    journalDao.insertAll(remoteEntries.map { JournalEntryEntity.fromDomain(it) })
                }
            } catch (_: Exception) {}
        }
    }

    fun calculateStats(entries: List<JournalEntry>): JournalStats {
        if (entries.isEmpty()) {
            return JournalStats()
        }

        val totalEntries = entries.size
        val totalWords = entries.sumOf { it.wordCount }
        val averageWords = if (totalEntries > 0) totalWords / totalEntries else 0

        // Mood breakdown
        val moodCounts = mutableMapOf<Mood, Int>()
        Mood.entries.forEach { moodCounts[it] = 0 }
        entries.forEach { entry ->
            moodCounts[entry.mood] = (moodCounts[entry.mood] ?: 0) + 1
        }
        val mostFrequent = moodCounts.maxByOrNull { it.value }?.key

        // Streak Calculation using distinct calendar days
        val calendar = Calendar.getInstance()
        val entryDayKeys = entries.map { entry ->
            calendar.timeInMillis = entry.dateMillis
            getYearDayKey(calendar)
        }.distinct().sortedDescending()

        val todayCalendar = Calendar.getInstance()
        val todayKey = getYearDayKey(todayCalendar)
        todayCalendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayKey = getYearDayKey(todayCalendar)

        // Current streak
        var currentStreak = 0
        var checkCal = Calendar.getInstance()

        // Check if there is an entry today or yesterday
        val hasEntryToday = entryDayKeys.contains(todayKey)
        val hasEntryYesterday = entryDayKeys.contains(yesterdayKey)

        if (hasEntryToday || hasEntryYesterday) {
            if (!hasEntryToday) {
                // start counting from yesterday
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            }
            while (entryDayKeys.contains(getYearDayKey(checkCal))) {
                currentStreak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            }
        }

        // Longest streak
        var longestStreak = 0
        if (entryDayKeys.isNotEmpty()) {
            val sortedAscKeys = entryDayKeys.sorted()
            var tempStreak = 0
            var previousCal: Calendar? = null

            for (key in sortedAscKeys) {
                val currentCal = parseYearDayKey(key)
                if (previousCal == null) {
                    tempStreak = 1
                } else {
                    previousCal.add(Calendar.DAY_OF_YEAR, 1)
                    if (getYearDayKey(previousCal) == key) {
                        tempStreak++
                    } else {
                        longestStreak = maxOf(longestStreak, tempStreak)
                        tempStreak = 1
                    }
                }
                previousCal = currentCal
            }
            longestStreak = maxOf(longestStreak, tempStreak)
        }

        longestStreak = maxOf(longestStreak, currentStreak)

        return JournalStats(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalEntries = totalEntries,
            totalWords = totalWords,
            averageWordsPerEntry = averageWords,
            moodCounts = moodCounts,
            mostFrequentMood = mostFrequent
        )
    }

    fun exportToJson(entries: List<JournalEntry>, userEmail: String): String {
        val root = JSONObject()
        root.put("appName", "Akalabya Journal")
        root.put("exportDate", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))
        root.put("userEmail", userEmail)
        root.put("totalEntries", entries.size)

        val array = JSONArray()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        for (e in entries) {
            val item = JSONObject()
            item.put("id", e.id)
            item.put("title", e.title)
            item.put("content", e.content)
            item.put("mood", e.mood.displayName)
            item.put("date", dateFormat.format(Date(e.dateMillis)))
            item.put("dateMillis", e.dateMillis)
            item.put("createdAt", e.createdAt)
            item.put("updatedAt", e.updatedAt)
            item.put("isFavorite", e.isFavorite)
            item.put("wordCount", e.wordCount)
            array.put(item)
        }
        root.put("entries", array)
        return root.toString(2)
    }

    fun exportToMarkdown(entries: List<JournalEntry>, userEmail: String): String {
        val sb = StringBuilder()
        sb.append("# Akalabya Journal Export\n\n")
        sb.append("**User:** ").append(userEmail).append("\n")
        sb.append("**Export Date:** ").append(SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())).append("\n")
        sb.append("**Total Entries:** ").append(entries.size).append("\n\n")
        sb.append("---\n\n")

        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())

        for (e in entries.sortedByDescending { it.dateMillis }) {
            sb.append("## ").append(if (e.title.isNotBlank()) e.title else "Untitled Entry").append("\n\n")
            sb.append("*Date:* ").append(dateFormat.format(Date(e.dateMillis)))
            sb.append(" | *Mood:* ").append(e.mood.displayName)
            sb.append(" | *Words:* ").append(e.wordCount).append("\n\n")
            sb.append(e.content).append("\n\n")
            sb.append("---\n\n")
        }

        return sb.toString()
    }

    private fun getYearDayKey(cal: Calendar): String {
        return "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.DAY_OF_YEAR)}"
    }

    private fun parseYearDayKey(key: String): Calendar {
        val parts = key.split("_")
        val year = parts[0].toInt()
        val dayOfYear = parts[1].toInt()
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.DAY_OF_YEAR, dayOfYear)
        return cal
    }

    private fun getDayBounds(timestamp: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return Pair(start, end)
    }
}
