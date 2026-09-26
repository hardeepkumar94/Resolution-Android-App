package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val periodType: String = "WEEKLY", // "WEEKLY" or "MONTHLY"
    val periodKey: String, // e.g. "2026-W39" or "2026-09"
    val resolutionId: Long? = null,
    val wins: String = "",
    val adjustments: String = "",
    val rating: Int = 4, // 1 to 5
    val createdAt: Long = System.currentTimeMillis()
)
