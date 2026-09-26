package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_journals",
    indices = [Index(value = ["date"], unique = true)]
)
data class DailyJournalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // "YYYY-MM-DD"
    val remarks: String = "",
    val mood: String = "GOOD", // "GREAT", "GOOD", "NEUTRAL", "TOUGH", "REST"
    val learning: String = "",
    val difficulties: String = "",
    val timeSpentMinutes: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
