package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY dateMillis DESC, updatedAt DESC")
    fun getEntriesForUser(userId: String): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE userId = :userId AND id = :id LIMIT 1")
    fun getEntryById(userId: String, id: String): Flow<JournalEntryEntity?>

    @Query("SELECT * FROM journal_entries WHERE userId = :userId AND id = :id LIMIT 1")
    suspend fun getEntryByIdDirect(userId: String, id: String): JournalEntryEntity?

    @Query("SELECT * FROM journal_entries WHERE userId = :userId AND dateMillis >= :startOfDay AND dateMillis <= :endOfDay ORDER BY updatedAt DESC LIMIT 1")
    fun getEntryForDate(userId: String, startOfDay: Long, endOfDay: Long): Flow<JournalEntryEntity?>

    @Query("SELECT * FROM journal_entries WHERE userId = :userId AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY dateMillis DESC")
    fun searchEntries(userId: String, query: String): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE userId = :userId AND isFavorite = 1 ORDER BY dateMillis DESC")
    fun getFavoriteEntries(userId: String): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE userId = :userId AND mood = :mood ORDER BY dateMillis DESC")
    fun getEntriesByMood(userId: String, mood: String): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedEntries(userId: String): List<JournalEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<JournalEntryEntity>)

    @Update
    suspend fun updateEntry(entry: JournalEntryEntity)

    @Query("DELETE FROM journal_entries WHERE userId = :userId AND id = :id")
    suspend fun deleteEntryById(userId: String, id: String)

    @Query("DELETE FROM journal_entries WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Query("SELECT COUNT(*) FROM journal_entries WHERE userId = :userId")
    fun getEntryCount(userId: String): Flow<Int>
}
