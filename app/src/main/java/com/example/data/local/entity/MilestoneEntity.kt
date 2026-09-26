package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "milestones",
    foreignKeys = [
        ForeignKey(
            entity = ResolutionEntity::class,
            parentColumns = ["id"],
            childColumns = ["resolutionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("resolutionId")]
)
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val resolutionId: Long,
    val title: String,
    val description: String = "",
    val targetTimeframe: String = "", // e.g., "Q1", "Month 1-3", "July 2026"
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0
)
