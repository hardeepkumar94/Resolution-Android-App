package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ActionDao
import com.example.data.local.dao.JournalDao
import com.example.data.local.dao.ResolutionDao
import com.example.data.local.dao.ReviewDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.DailyActionEntity
import com.example.data.local.entity.DailyJournalEntity
import com.example.data.local.entity.GoalPlanEntity
import com.example.data.local.entity.MilestoneEntity
import com.example.data.local.entity.ResolutionEntity
import com.example.data.local.entity.ReviewEntity
import com.example.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ResolutionEntity::class,
        MilestoneEntity::class,
        GoalPlanEntity::class,
        DailyActionEntity::class,
        DailyJournalEntity::class,
        ReviewEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OneStepDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun resolutionDao(): ResolutionDao
    abstract fun actionDao(): ActionDao
    abstract fun journalDao(): JournalDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile
        private var INSTANCE: OneStepDatabase? = null

        fun getDatabase(context: Context): OneStepDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OneStepDatabase::class.java,
                    "onestep_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
