package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE periodType = :type ORDER BY createdAt DESC")
    fun getReviewsByType(type: String): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE periodKey = :key LIMIT 1")
    fun getReviewByKey(key: String): Flow<ReviewEntity?>

    @Query("SELECT * FROM reviews WHERE periodKey = :key LIMIT 1")
    suspend fun getReviewByKeySync(key: String): ReviewEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(review: ReviewEntity)

    @Update
    suspend fun update(review: ReviewEntity)
}
