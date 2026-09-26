package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Achiever",
    val email: String = "user@onestep.app",
    val avatarIcon: String = "compass",
    val isLoggedIn: Boolean = true,
    val isGuest: Boolean = false,
    val currentStreak: Int = 1,
    val longestStreak: Int = 1,
    val lastActiveDate: String = "", // "YYYY-MM-DD"
    val totalActionsCompleted: Int = 0,
    val themeMode: String = "system" // "system", "light", "dark"
)
