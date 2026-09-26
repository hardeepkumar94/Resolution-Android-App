package com.example.data.repository

import com.example.data.ai.AiPlannerService
import com.example.data.ai.ProposedPlan
import com.example.data.local.OneStepDatabase
import com.example.data.local.entity.DailyActionEntity
import com.example.data.local.entity.DailyJournalEntity
import com.example.data.local.entity.GoalPlanEntity
import com.example.data.local.entity.MilestoneEntity
import com.example.data.local.entity.ResolutionEntity
import com.example.data.local.entity.ReviewEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MultiHorizonProgress(
    val dailyPercentage: Float = 0f,
    val dailyCompleted: Int = 0,
    val dailyTotal: Int = 0,
    val weeklyPercentage: Float = 0f,
    val weeklyCompleted: Int = 0,
    val weeklyTotal: Int = 0,
    val monthlyPercentage: Float = 0f,
    val monthlyCompleted: Int = 0,
    val monthlyTotal: Int = 0,
    val overallPercentage: Float = 0f,
    val totalActionsCompleted: Int = 0,
    val totalActionsCount: Int = 0
)

class OneStepRepository(private val database: OneStepDatabase) {

    private val userDao = database.userDao()
    private val resolutionDao = database.resolutionDao()
    private val actionDao = database.actionDao()
    private val journalDao = database.journalDao()
    private val reviewDao = database.reviewDao()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val weekFormat = SimpleDateFormat("yyyy-'W'ww", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    fun getTodayString(): String = dateFormat.format(Date())

    fun getDateWithOffset(daysOffset: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, daysOffset)
        return dateFormat.format(calendar.time)
    }

    fun getCurrentWeekKey(): String = weekFormat.format(Date())
    fun getCurrentMonthKey(): String = monthFormat.format(Date())

    // --- User & Auth ---
    val userFlow: Flow<UserEntity?> = userDao.getUser()

    suspend fun ensureDefaultUser() = withContext(Dispatchers.IO) {
        val existing = userDao.getUserSync()
        if (existing == null) {
            val defaultUser = UserEntity(
                id = 1,
                name = "Pathfinder",
                email = "explorer@onestep.app",
                isLoggedIn = true,
                isGuest = true,
                currentStreak = 1,
                longestStreak = 1,
                lastActiveDate = getTodayString()
            )
            userDao.insertOrUpdate(defaultUser)

            // Seed a starter inspiring resolution if empty
            val starterResolutions = resolutionDao.getResolutionByIdSync(1)
            if (starterResolutions == null) {
                seedStarterResolution()
            }
        }
    }

    suspend fun updateProfile(name: String, email: String, avatar: String) = withContext(Dispatchers.IO) {
        val current = userDao.getUserSync() ?: UserEntity()
        userDao.insertOrUpdate(current.copy(name = name, email = email, avatarIcon = avatar, isLoggedIn = true, isGuest = false))
    }

    suspend fun setGuestMode() = withContext(Dispatchers.IO) {
        val current = userDao.getUserSync() ?: UserEntity()
        userDao.insertOrUpdate(current.copy(isLoggedIn = true, isGuest = true))
    }

    suspend fun updateTheme(mode: String) = withContext(Dispatchers.IO) {
        userDao.updateThemeMode(mode)
    }

    // --- Resolutions ---
    val allResolutions: Flow<List<ResolutionEntity>> = resolutionDao.getAllResolutions()
    val activeResolutions: Flow<List<ResolutionEntity>> = resolutionDao.getActiveResolutions()

    fun getResolution(id: Long): Flow<ResolutionEntity?> = resolutionDao.getResolutionById(id)

    suspend fun createResolution(
        resolution: ResolutionEntity,
        proposedPlan: ProposedPlan?
    ): Long = withContext(Dispatchers.IO) {
        val resId = resolutionDao.insertResolution(resolution)

        if (proposedPlan != null) {
            // Insert milestones
            val milestones = proposedPlan.milestones.mapIndexed { index, m ->
                MilestoneEntity(
                    resolutionId = resId,
                    title = m.title,
                    description = m.description,
                    targetTimeframe = m.timeframe,
                    orderIndex = index
                )
            }
            resolutionDao.insertMilestones(milestones)

            // Insert monthly and weekly goals
            val allGoals = mutableListOf<GoalPlanEntity>()
            proposedPlan.monthlyGoals.forEachIndexed { index, mg ->
                allGoals.add(
                    GoalPlanEntity(
                        resolutionId = resId,
                        level = "MONTHLY",
                        title = mg.title,
                        timeframe = mg.timeframe,
                        orderIndex = index
                    )
                )
            }
            proposedPlan.weeklyGoals.forEachIndexed { index, wg ->
                allGoals.add(
                    GoalPlanEntity(
                        resolutionId = resId,
                        level = "WEEKLY",
                        title = wg.title,
                        timeframe = wg.timeframe,
                        orderIndex = index
                    )
                )
            }
            resolutionDao.insertGoalPlans(allGoals)

            // Insert daily actions
            val actions = proposedPlan.dailyActions.mapIndexed { index, da ->
                DailyActionEntity(
                    resolutionId = resId,
                    title = da.title,
                    scheduledDate = getDateWithOffset(da.dayOffset),
                    estimatedMinutes = da.estimatedMinutes,
                    orderIndex = index
                )
            }
            actionDao.insertActions(actions)
        }
        resId
    }

    suspend fun updateResolutionStatus(id: Long, status: String) = withContext(Dispatchers.IO) {
        resolutionDao.updateStatus(id, status)
    }

    suspend fun deleteResolution(resolution: ResolutionEntity) = withContext(Dispatchers.IO) {
        resolutionDao.deleteResolution(resolution)
    }

    // Milestones & Goals
    fun getMilestones(resolutionId: Long): Flow<List<MilestoneEntity>> = resolutionDao.getMilestones(resolutionId)
    fun getGoalPlans(resolutionId: Long): Flow<List<GoalPlanEntity>> = resolutionDao.getAllGoalPlans(resolutionId)

    suspend fun toggleMilestoneCompletion(id: Long, completed: Boolean) = withContext(Dispatchers.IO) {
        resolutionDao.updateMilestoneCompletion(id, completed)
    }

    suspend fun toggleGoalCompletion(id: Long, completed: Boolean) = withContext(Dispatchers.IO) {
        resolutionDao.updateGoalCompletion(id, completed)
    }

    // --- Daily Actions ---
    fun getTodayActions(): Flow<List<DailyActionEntity>> = actionDao.getActionsForDate(getTodayString())
    fun getActionsForDate(date: String): Flow<List<DailyActionEntity>> = actionDao.getActionsForDate(date)
    fun getActionsForResolution(resolutionId: Long): Flow<List<DailyActionEntity>> = actionDao.getActionsForResolution(resolutionId)
    fun getNextAction(): Flow<DailyActionEntity?> = actionDao.getNextAction()
    fun getDatesWithActivity(): Flow<List<String>> = actionDao.getDatesWithCompletedActions()

    suspend fun setActionCompleted(actionId: Long, completed: Boolean) = withContext(Dispatchers.IO) {
        val timestamp = if (completed) System.currentTimeMillis() else null
        actionDao.setActionCompleted(actionId, completed, timestamp)
        if (completed) {
            userDao.incrementActionsCompleted()
            updateStreakAfterAction()
        }
    }

    suspend fun rescheduleAction(actionId: Long, newDate: String) = withContext(Dispatchers.IO) {
        actionDao.rescheduleAction(actionId, newDate)
    }

    suspend fun addCustomDailyAction(resolutionId: Long, title: String, date: String, minutes: Int = 15) = withContext(Dispatchers.IO) {
        actionDao.insertAction(
            DailyActionEntity(
                resolutionId = resolutionId,
                title = title,
                scheduledDate = date,
                estimatedMinutes = minutes
            )
        )
    }

    private suspend fun updateStreakAfterAction() {
        val user = userDao.getUserSync() ?: return
        val today = getTodayString()
        if (user.lastActiveDate == today) return // already counted for today

        val yesterday = getDateWithOffset(-1)
        val newStreak = if (user.lastActiveDate == yesterday) {
            user.currentStreak + 1
        } else {
            1
        }
        val longest = maxOf(newStreak, user.longestStreak)
        userDao.updateStreak(newStreak, longest, today)
    }

    // --- Daily Journal ---
    fun getTodayJournal(): Flow<DailyJournalEntity?> = journalDao.getJournalForDate(getTodayString())
    fun getJournalForDate(date: String): Flow<DailyJournalEntity?> = journalDao.getJournalForDate(date)
    val allJournals: Flow<List<DailyJournalEntity>> = journalDao.getAllJournals()

    suspend fun saveJournal(
        date: String,
        remarks: String,
        mood: String,
        learning: String,
        difficulties: String,
        timeSpentMinutes: Int
    ) = withContext(Dispatchers.IO) {
        val existing = journalDao.getJournalForDateSync(date)
        val journal = (existing ?: DailyJournalEntity(date = date)).copy(
            remarks = remarks,
            mood = mood,
            learning = learning,
            difficulties = difficulties,
            timeSpentMinutes = timeSpentMinutes,
            updatedAt = System.currentTimeMillis()
        )
        journalDao.insertOrUpdate(journal)
    }

    // --- Reviews ---
    fun getReviews(type: String): Flow<List<ReviewEntity>> = reviewDao.getReviewsByType(type)
    fun getReviewByKey(key: String): Flow<ReviewEntity?> = reviewDao.getReviewByKey(key)

    suspend fun saveReview(
        type: String,
        key: String,
        wins: String,
        adjustments: String,
        rating: Int
    ) = withContext(Dispatchers.IO) {
        val existing = reviewDao.getReviewByKeySync(key)
        val entity = (existing ?: ReviewEntity(periodType = type, periodKey = key)).copy(
            wins = wins,
            adjustments = adjustments,
            rating = rating,
            createdAt = System.currentTimeMillis()
        )
        reviewDao.insertOrUpdate(entity)
    }

    // --- Multi-Horizon Progress Calculations ---
    fun getMultiHorizonProgress(): Flow<MultiHorizonProgress> {
        val today = getTodayString()
        return combine(
            actionDao.getActionsForDate(today),
            actionDao.getTotalCompletedCount(),
            database.resolutionDao().getAllResolutions()
        ) { todayActions, totalCompleted, allRes ->
            val todayTotal = todayActions.size
            val todayDone = todayActions.count { it.isCompleted }
            val dailyPct = if (todayTotal > 0) todayDone.toFloat() / todayTotal else 0f

            // Weekly estimate
            val weeklyDone = minOf(todayDone * 4 + 2, 10)
            val weeklyTotal = 10
            val weeklyPct = weeklyDone.toFloat() / weeklyTotal

            // Monthly estimate
            val monthlyDone = minOf(todayDone * 12 + 6, 30)
            val monthlyTotal = 30
            val monthlyPct = monthlyDone.toFloat() / monthlyTotal

            // Overall estimate
            val overallPct = if (allRes.isNotEmpty()) {
                val completedRes = allRes.count { it.status == "COMPLETED" }
                (completedRes.toFloat() + (dailyPct * 0.4f)) / allRes.size.coerceAtLeast(1)
            } else 0f

            MultiHorizonProgress(
                dailyPercentage = dailyPct,
                dailyCompleted = todayDone,
                dailyTotal = todayTotal,
                weeklyPercentage = weeklyPct.coerceIn(0f, 1f),
                weeklyCompleted = weeklyDone,
                weeklyTotal = weeklyTotal,
                monthlyPercentage = monthlyPct.coerceIn(0f, 1f),
                monthlyCompleted = monthlyDone,
                monthlyTotal = monthlyTotal,
                overallPercentage = overallPct.coerceIn(0f, 1f),
                totalActionsCompleted = totalCompleted,
                totalActionsCount = maxOf(totalCompleted + (todayTotal - todayDone), 1)
            )
        }
    }

    // Starter seed resolution for instant delight
    private suspend fun seedStarterResolution() {
        val starter = ResolutionEntity(
            id = 1,
            title = "Morning Mind & Movement Practice",
            description = "Devote 20 minutes every morning to physical movement, mindful breathing, and daily intention setting.",
            whyStarted = "To reclaim my mental clarity, reduce stress, and start each day with calm purpose instead of reactivity.",
            goalType = "HEALTH_FITNESS",
            startDate = System.currentTimeMillis(),
            targetDate = System.currentTimeMillis() + 90L * 24 * 60 * 60 * 1000,
            personalAffirmation = "One step today. Another tomorrow. Eventually, I arrive.",
            status = "ACTIVE",
            colorHex = "#0284C7"
        )
        val defaultPlan = AiPlannerService.generateBreakdown(
            starter.title,
            starter.description,
            starter.whyStarted,
            starter.goalType,
            starter.personalAffirmation
        )
        createResolution(starter, defaultPlan)
    }
}
