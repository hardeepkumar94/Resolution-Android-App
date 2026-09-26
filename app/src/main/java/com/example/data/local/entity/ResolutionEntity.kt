package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resolutions")
data class ResolutionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val whyStarted: String,
    val goalType: String, // "HEALTH_FITNESS", "CAREER_SKILLS", "FINANCIAL", "MINDFULNESS", "CREATIVE", "PERSONAL_GROWTH"
    val startDate: Long,
    val targetDate: Long,
    val personalAffirmation: String,
    val status: String = "ACTIVE", // "ACTIVE", "PAUSED", "COMPLETED"
    val colorHex: String = "#0284C7",
    val createdAt: Long = System.currentTimeMillis()
)
