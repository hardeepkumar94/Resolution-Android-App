package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "goal_plans",
    foreignKeys = [
        ForeignKey(
            entity = ResolutionEntity::class,
            parentColumns = ["id"],
            childColumns = ["resolutionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("resolutionId"), Index("milestoneId")]
)
data class GoalPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val resolutionId: Long,
    val milestoneId: Long? = null,
    val level: String, // "MONTHLY" or "WEEKLY"
    val title: String,
    val timeframe: String, // e.g. "Month 1", "Week 1"
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0
)
