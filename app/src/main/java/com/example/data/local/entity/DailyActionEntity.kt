package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_actions",
    foreignKeys = [
        ForeignKey(
            entity = ResolutionEntity::class,
            parentColumns = ["id"],
            childColumns = ["resolutionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("resolutionId"), Index("scheduledDate")]
)
data class DailyActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val resolutionId: Long,
    val weeklyGoalId: Long? = null,
    val title: String,
    val scheduledDate: String, // "YYYY-MM-DD"
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val rescheduledCount: Int = 0,
    val estimatedMinutes: Int = 15,
    val notes: String = "",
    val orderIndex: Int = 0
)
