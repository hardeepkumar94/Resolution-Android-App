package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.data.repository.MultiHorizonProgress
import com.example.data.repository.OneStepRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val sender: String, // "USER" or "COACH"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class OneStepViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: OneStepRepository

    init {
        val db = OneStepDatabase.getDatabase(application)
        repository = OneStepRepository(db)
        viewModelScope.launch {
            repository.ensureDefaultUser()
        }
    }

    val user: StateFlow<UserEntity?> = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allResolutions: StateFlow<List<ResolutionEntity>> = repository.allResolutions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeResolutions: StateFlow<List<ResolutionEntity>> = repository.activeResolutions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayActions: StateFlow<List<DailyActionEntity>> = repository.getTodayActions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nextAction: StateFlow<DailyActionEntity?> = repository.getNextAction()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val multiProgress: StateFlow<MultiHorizonProgress> = repository.getMultiHorizonProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MultiHorizonProgress())

    val todayJournal: StateFlow<DailyJournalEntity?> = repository.getTodayJournal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allJournals: StateFlow<List<DailyJournalEntity>> = repository.allJournals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val datesWithActivity: StateFlow<List<String>> = repository.getDatesWithActivity()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyReviews: StateFlow<List<ReviewEntity>> = repository.getReviews("WEEKLY")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyReviews: StateFlow<List<ReviewEntity>> = repository.getReviews("MONTHLY")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Resolution state
    private val _selectedResolutionId = MutableStateFlow<Long?>(null)
    val selectedResolutionId: StateFlow<Long?> = _selectedResolutionId.asStateFlow()

    val selectedResolution: StateFlow<ResolutionEntity?> = _selectedResolutionId.flatMapLatest { id ->
        if (id != null) repository.getResolution(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedResolutionMilestones: StateFlow<List<MilestoneEntity>> = _selectedResolutionId.flatMapLatest { id ->
        if (id != null) repository.getMilestones(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedResolutionGoals: StateFlow<List<GoalPlanEntity>> = _selectedResolutionId.flatMapLatest { id ->
        if (id != null) repository.getGoalPlans(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedResolutionActions: StateFlow<List<DailyActionEntity>> = _selectedResolutionId.flatMapLatest { id ->
        if (id != null) repository.getActionsForResolution(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectResolution(id: Long) {
        _selectedResolutionId.value = id
    }

    // AI Generation Draft State
    private val _draftPlan = MutableStateFlow<ProposedPlan?>(null)
    val draftPlan: StateFlow<ProposedPlan?> = _draftPlan.asStateFlow()

    private val _isGeneratingPlan = MutableStateFlow(false)
    val isGeneratingPlan: StateFlow<Boolean> = _isGeneratingPlan.asStateFlow()

    private val _draftResolution = MutableStateFlow<ResolutionEntity?>(null)
    val draftResolution: StateFlow<ResolutionEntity?> = _draftResolution.asStateFlow()

    fun generateAiPlanForResolution(
        title: String,
        description: String,
        whyStarted: String,
        goalType: String,
        targetDate: Long,
        affirmation: String,
        colorHex: String,
        onReady: () -> Unit
    ) {
        viewModelScope.launch {
            _isGeneratingPlan.value = true
            val pendingRes = ResolutionEntity(
                title = title,
                description = description,
                whyStarted = whyStarted,
                goalType = goalType,
                startDate = System.currentTimeMillis(),
                targetDate = targetDate,
                personalAffirmation = affirmation,
                colorHex = colorHex
            )
            _draftResolution.value = pendingRes

            val plan = AiPlannerService.generateBreakdown(
                title = title,
                description = description,
                whyStarted = whyStarted,
                goalType = goalType,
                affirmation = affirmation
            )
            _draftPlan.value = plan
            _isGeneratingPlan.value = false
            onReady()
        }
    }

    fun updateDraftMilestoneTitle(index: Int, newTitle: String) {
        val current = _draftPlan.value ?: return
        if (index in current.milestones.indices) {
            current.milestones[index].title = newTitle
            _draftPlan.value = current.copy()
        }
    }

    fun updateDraftDailyActionTitle(index: Int, newTitle: String) {
        val current = _draftPlan.value ?: return
        if (index in current.dailyActions.indices) {
            current.dailyActions[index].title = newTitle
            _draftPlan.value = current.copy()
        }
    }

    fun removeDraftDailyAction(index: Int) {
        val current = _draftPlan.value ?: return
        if (index in current.dailyActions.indices) {
            current.dailyActions.removeAt(index)
            _draftPlan.value = current.copy()
        }
    }

    fun addDraftDailyAction(title: String) {
        val current = _draftPlan.value ?: return
        val offset = current.dailyActions.size
        current.dailyActions.add(com.example.data.ai.ProposedDailyAction(title = title, dayOffset = offset))
        _draftPlan.value = current.copy()
    }

    fun activateDraftPlan(onSuccess: (Long) -> Unit) {
        val res = _draftResolution.value ?: return
        val plan = _draftPlan.value ?: return
        viewModelScope.launch {
            val resId = repository.createResolution(res, plan)
            _draftPlan.value = null
            _draftResolution.value = null
            _celebrationEvent.value = "Plan Activated! Your journey of 1,000 miles starts with today's first step."
            onSuccess(resId)
        }
    }

    // Actions & Rescheduling
    private val _rescheduleTarget = MutableStateFlow<DailyActionEntity?>(null)
    val rescheduleTarget: StateFlow<DailyActionEntity?> = _rescheduleTarget.asStateFlow()

    fun openRescheduleDialog(action: DailyActionEntity) {
        _rescheduleTarget.value = action
    }

    fun closeRescheduleDialog() {
        _rescheduleTarget.value = null
    }

    fun rescheduleAction(actionId: Long, daysOffset: Int) {
        viewModelScope.launch {
            val targetDate = repository.getDateWithOffset(daysOffset)
            repository.rescheduleAction(actionId, targetDate)
            _rescheduleTarget.value = null
        }
    }

    private val _celebrationEvent = MutableStateFlow<String?>(null)
    val celebrationEvent: StateFlow<String?> = _celebrationEvent.asStateFlow()

    fun dismissCelebration() {
        _celebrationEvent.value = null
    }

    fun completeAction(action: DailyActionEntity) {
        viewModelScope.launch {
            val newStatus = !action.isCompleted
            repository.setActionCompleted(action.id, newStatus)
            if (newStatus) {
                _celebrationEvent.value = "Step Completed! Every step brings you closer to your arrival."
            }
        }
    }

    fun toggleMilestone(milestone: MilestoneEntity) {
        viewModelScope.launch {
            val newCompleted = !milestone.isCompleted
            repository.toggleMilestoneCompletion(milestone.id, newCompleted)
            if (newCompleted) {
                _celebrationEvent.value = "Milestone Reached! Outstanding milestone achievement."
            }
        }
    }

    fun toggleGoal(goal: GoalPlanEntity) {
        viewModelScope.launch {
            repository.toggleGoalCompletion(goal.id, !goal.isCompleted)
        }
    }

    fun updateResolutionStatus(id: Long, status: String) {
        viewModelScope.launch {
            repository.updateResolutionStatus(id, status)
            if (status == "COMPLETED") {
                _celebrationEvent.value = "Resolution Achieved! You arrived! Take immense pride in your journey."
            }
        }
    }

    // Journal
    fun saveJournalEntry(
        date: String = repository.getTodayString(),
        remarks: String,
        mood: String,
        learning: String,
        difficulties: String,
        timeSpentMinutes: Int
    ) {
        viewModelScope.launch {
            repository.saveJournal(date, remarks, mood, learning, difficulties, timeSpentMinutes)
        }
    }

    // Reviews
    fun saveReview(
        type: String, // "WEEKLY" or "MONTHLY"
        wins: String,
        adjustments: String,
        rating: Int
    ) {
        viewModelScope.launch {
            val key = if (type == "WEEKLY") repository.getCurrentWeekKey() else repository.getCurrentMonthKey()
            repository.saveReview(type, key, wins, adjustments, rating)
        }
    }

    // Profile & Auth
    fun updateProfile(name: String, email: String, avatar: String) {
        viewModelScope.launch {
            repository.updateProfile(name, email, avatar)
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            repository.setGuestMode()
        }
    }

    fun updateTheme(mode: String) {
        viewModelScope.launch {
            repository.updateTheme(mode)
        }
    }

    // OneStep AI Coach
    private val _coachMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "COACH",
                text = "Hello! I am your OneStep Coach. 'One step today. Another tomorrow. Eventually, you arrive.' How can I help you take your next step today?"
            )
        )
    )
    val coachMessages: StateFlow<List<ChatMessage>> = _coachMessages.asStateFlow()

    private val _isCoachThinking = MutableStateFlow(false)
    val isCoachThinking: StateFlow<Boolean> = _isCoachThinking.asStateFlow()

    fun sendCoachMessage(message: String) {
        val userMsg = ChatMessage(sender = "USER", text = message)
        _coachMessages.value = _coachMessages.value + userMsg
        _isCoachThinking.value = true

        viewModelScope.launch {
            val activeTitle = activeResolutions.value.firstOrNull()?.title
            val streak = user.value?.currentStreak ?: 1
            val total = user.value?.totalActionsCompleted ?: 0

            val reply = AiPlannerService.getCoachAdvice(message, activeTitle, streak, total)
            _coachMessages.value = _coachMessages.value + ChatMessage(sender = "COACH", text = reply)
            _isCoachThinking.value = false
        }
    }

    fun getTodayString(): String = repository.getTodayString()

    fun addCustomDailyAction(resolutionId: Long, title: String, date: String, minutes: Int = 15) {
        viewModelScope.launch {
            repository.addCustomDailyAction(resolutionId, title, date, minutes)
        }
    }
}

