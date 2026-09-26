package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DailyActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActions(actions: List<DailyActionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: DailyActionEntity): Long

    @Update
    suspend fun updateAction(action: DailyActionEntity)

    @Delete
    suspend fun deleteAction(action: DailyActionEntity)

    @Query("SELECT * FROM daily_actions WHERE scheduledDate = :date ORDER BY isCompleted ASC, orderIndex ASC")
    fun getActionsForDate(date: String): Flow<List<DailyActionEntity>>

    @Query("SELECT * FROM daily_actions WHERE scheduledDate = :date ORDER BY isCompleted ASC, orderIndex ASC")
    suspend fun getActionsForDateSync(date: String): List<DailyActionEntity>

    @Query("SELECT * FROM daily_actions WHERE resolutionId = :resolutionId ORDER BY scheduledDate ASC, orderIndex ASC")
    fun getActionsForResolution(resolutionId: Long): Flow<List<DailyActionEntity>>

    @Query("SELECT * FROM daily_actions WHERE resolutionId = :resolutionId")
    suspend fun getActionsForResolutionSync(resolutionId: Long): List<DailyActionEntity>

    @Query("SELECT * FROM daily_actions WHERE isCompleted = 0 ORDER BY scheduledDate ASC, orderIndex ASC LIMIT 1")
    fun getNextAction(): Flow<DailyActionEntity?>

    @Query("SELECT * FROM daily_actions WHERE resolutionId = :resolutionId AND isCompleted = 0 ORDER BY scheduledDate ASC, orderIndex ASC LIMIT 1")
    fun getNextActionForResolution(resolutionId: Long): Flow<DailyActionEntity?>

    @Query("UPDATE daily_actions SET isCompleted = :completed, completedAt = :timestamp WHERE id = :actionId")
    suspend fun setActionCompleted(actionId: Long, completed: Boolean, timestamp: Long?)

    @Query("UPDATE daily_actions SET scheduledDate = :newDate, rescheduledCount = rescheduledCount + 1 WHERE id = :actionId")
    suspend fun rescheduleAction(actionId: Long, newDate: String)

    @Query("SELECT COUNT(*) FROM daily_actions WHERE scheduledDate = :date")
    fun getTodayTotalCount(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM daily_actions WHERE scheduledDate = :date AND isCompleted = 1")
    fun getTodayCompletedCount(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM daily_actions WHERE isCompleted = 1")
    fun getTotalCompletedCount(): Flow<Int>

    @Query("SELECT DISTINCT scheduledDate FROM daily_actions WHERE isCompleted = 1")
    fun getDatesWithCompletedActions(): Flow<List<String>>

    @Query("SELECT * FROM daily_actions WHERE id = :id LIMIT 1")
    suspend fun getActionById(id: Long): DailyActionEntity?
}
