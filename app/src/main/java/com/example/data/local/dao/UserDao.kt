package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    suspend fun getUserSync(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)

    @Query("UPDATE users SET currentStreak = :streak, longestStreak = :longest, lastActiveDate = :today WHERE id = 1")
    suspend fun updateStreak(streak: Int, longest: Int, today: String)

    @Query("UPDATE users SET totalActionsCompleted = totalActionsCompleted + 1 WHERE id = 1")
    suspend fun incrementActionsCompleted()

    @Query("UPDATE users SET themeMode = :themeMode WHERE id = 1")
    suspend fun updateThemeMode(themeMode: String)
}
