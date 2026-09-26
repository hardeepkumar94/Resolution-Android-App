package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.GoalPlanEntity
import com.example.data.local.entity.MilestoneEntity
import com.example.data.local.entity.ResolutionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResolutionDao {
    @Query("SELECT * FROM resolutions ORDER BY createdAt DESC")
    fun getAllResolutions(): Flow<List<ResolutionEntity>>

    @Query("SELECT * FROM resolutions WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getActiveResolutions(): Flow<List<ResolutionEntity>>

    @Query("SELECT * FROM resolutions WHERE id = :id LIMIT 1")
    fun getResolutionById(id: Long): Flow<ResolutionEntity?>

    @Query("SELECT * FROM resolutions WHERE id = :id LIMIT 1")
    suspend fun getResolutionByIdSync(id: Long): ResolutionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResolution(resolution: ResolutionEntity): Long

    @Update
    suspend fun updateResolution(resolution: ResolutionEntity)

    @Delete
    suspend fun deleteResolution(resolution: ResolutionEntity)

    @Query("UPDATE resolutions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    // Milestones
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<MilestoneEntity>)

    @Query("SELECT * FROM milestones WHERE resolutionId = :resolutionId ORDER BY orderIndex ASC")
    fun getMilestones(resolutionId: Long): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM milestones WHERE resolutionId = :resolutionId ORDER BY orderIndex ASC")
    suspend fun getMilestonesSync(resolutionId: Long): List<MilestoneEntity>

    @Query("UPDATE milestones SET isCompleted = :completed WHERE id = :id")
    suspend fun updateMilestoneCompletion(id: Long, completed: Boolean)

    // Goal plans (Monthly and Weekly)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoalPlans(goals: List<GoalPlanEntity>)

    @Query("SELECT * FROM goal_plans WHERE resolutionId = :resolutionId AND level = :level ORDER BY orderIndex ASC")
    fun getGoalPlans(resolutionId: Long, level: String): Flow<List<GoalPlanEntity>>

    @Query("SELECT * FROM goal_plans WHERE resolutionId = :resolutionId ORDER BY orderIndex ASC")
    fun getAllGoalPlans(resolutionId: Long): Flow<List<GoalPlanEntity>>

    @Query("SELECT * FROM goal_plans WHERE resolutionId = :resolutionId")
    suspend fun getAllGoalPlansSync(resolutionId: Long): List<GoalPlanEntity>

    @Query("UPDATE goal_plans SET isCompleted = :completed WHERE id = :id")
    suspend fun updateGoalCompletion(id: Long, completed: Boolean)
}
