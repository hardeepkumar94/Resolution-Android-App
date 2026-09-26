package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DailyJournalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM daily_journals WHERE date = :date LIMIT 1")
    fun getJournalForDate(date: String): Flow<DailyJournalEntity?>

    @Query("SELECT * FROM daily_journals WHERE date = :date LIMIT 1")
    suspend fun getJournalForDateSync(date: String): DailyJournalEntity?

    @Query("SELECT * FROM daily_journals ORDER BY date DESC")
    fun getAllJournals(): Flow<List<DailyJournalEntity>>

    @Query("SELECT * FROM daily_journals ORDER BY date DESC LIMIT :limit")
    fun getRecentJournals(limit: Int): Flow<List<DailyJournalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(journal: DailyJournalEntity)

    @Update
    suspend fun update(journal: DailyJournalEntity)

    @Query("DELETE FROM daily_journals WHERE date = :date")
    suspend fun deleteJournal(date: String)
}
