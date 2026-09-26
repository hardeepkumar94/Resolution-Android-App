package com.example.data.ai

import com.example.BuildConfig
import com.example.data.remote.GeminiClient
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerateRequest
import com.example.data.remote.GeminiGenerationConfig
import com.example.data.remote.GeminiPart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class ProposedMilestone(
    var title: String,
    var timeframe: String,
    var description: String = ""
)

data class ProposedGoalPlan(
    val level: String, // "MONTHLY" or "WEEKLY"
    var title: String,
    var timeframe: String
)

data class ProposedDailyAction(
    var title: String,
    var dayOffset: Int = 0, // 0 = today, 1 = tomorrow, etc.
    var estimatedMinutes: Int = 15
)

data class ProposedPlan(
    val summary: String,
    val milestones: MutableList<ProposedMilestone>,
    val monthlyGoals: MutableList<ProposedGoalPlan>,
    val weeklyGoals: MutableList<ProposedGoalPlan>,
    val dailyActions: MutableList<ProposedDailyAction>
)

object AiPlannerService {

    suspend fun generateBreakdown(
        title: String,
        description: String,
        whyStarted: String,
        goalType: String,
        affirmation: String
    ): ProposedPlan = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    You are an expert behavioural coach and productivity architect for OneStep app.
                    Break down this big resolution into a realistic, progressive pyramid:
                    - Resolution Title: "$title"
                    - Description: "$description"
                    - Why I started: "$whyStarted"
                    - Goal Type: "$goalType"
                    - Personal Affirmation: "$affirmation"
                    
                    The UX philosophy is: "One step today. Another tomorrow. Eventually, you arrive."
                    Keep steps realistic, bite-sized (10-30 min daily actions) and encouraging.
                    
                    Return ONLY valid JSON matching this schema:
                    {
                      "summary": "Short inspiring summary of how this plan leads to success",
                      "milestones": [
                        {"title": "Milestone title", "timeframe": "Q1 or Month 1-2", "description": "Short explanation"}
                      ],
                      "monthlyGoals": [
                        {"title": "Monthly goal title", "timeframe": "Month 1"},
                        {"title": "Monthly goal title", "timeframe": "Month 2"},
                        {"title": "Monthly goal title", "timeframe": "Month 3"}
                      ],
                      "weeklyGoals": [
                        {"title": "Weekly focus title", "timeframe": "Week 1"},
                        {"title": "Weekly focus title", "timeframe": "Week 2"},
                        {"title": "Weekly focus title", "timeframe": "Week 3"},
                        {"title": "Weekly focus title", "timeframe": "Week 4"}
                      ],
                      "dailyActions": [
                        {"title": "Immediate action for Day 1", "dayOffset": 0, "estimatedMinutes": 15},
                        {"title": "Action for Day 2", "dayOffset": 1, "estimatedMinutes": 20},
                        {"title": "Action for Day 3", "dayOffset": 2, "estimatedMinutes": 15},
                        {"title": "Action for Day 4", "dayOffset": 3, "estimatedMinutes": 20},
                        {"title": "Action for Day 5", "dayOffset": 4, "estimatedMinutes": 25},
                        {"title": "Action for Day 6", "dayOffset": 5, "estimatedMinutes": 20},
                        {"title": "Action for Day 7 (review & rest)", "dayOffset": 6, "estimatedMinutes": 15}
                      ]
                    }
                """.trimIndent()

                val request = GeminiGenerateRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                    ),
                    generationConfig = GeminiGenerationConfig(
                        temperature = 0.5f,
                        responseMimeType = "application/json"
                    )
                )

                val response = GeminiClient.apiService.generateContent(apiKey, request)
                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!rawText.isNullOrBlank()) {
                    val parsed = parsePlanJson(rawText)
                    if (parsed != null) {
                        return@withContext parsed
                    }
                }
            } catch (e: Exception) {
                // Fallback to intelligent generator if network/API fails
            }
        }

        // Resilient intelligent fallback generator
        generateFallbackPlan(title, description, whyStarted, goalType, affirmation)
    }

    private fun parsePlanJson(jsonString: String): ProposedPlan? {
        return try {
            val cleanJson = jsonString.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val obj = JSONObject(cleanJson)
            val summary = obj.optString("summary", "A progressive step-by-step roadmap to achieve your resolution.")

            val milestones = mutableListOf<ProposedMilestone>()
            val mArray = obj.optJSONArray("milestones") ?: JSONArray()
            for (i in 0 until mArray.length()) {
                val item = mArray.getJSONObject(i)
                milestones.add(
                    ProposedMilestone(
                        title = item.optString("title", "Milestone ${i + 1}"),
                        timeframe = item.optString("timeframe", "Month ${i + 1}"),
                        description = item.optString("description", "")
                    )
                )
            }

            val monthlyGoals = mutableListOf<ProposedGoalPlan>()
            val moArray = obj.optJSONArray("monthlyGoals") ?: JSONArray()
            for (i in 0 until moArray.length()) {
                val item = moArray.getJSONObject(i)
                monthlyGoals.add(
                    ProposedGoalPlan(
                        level = "MONTHLY",
                        title = item.optString("title", "Month ${i + 1} Goal"),
                        timeframe = item.optString("timeframe", "Month ${i + 1}")
                    )
                )
            }

            val weeklyGoals = mutableListOf<ProposedGoalPlan>()
            val wArray = obj.optJSONArray("weeklyGoals") ?: JSONArray()
            for (i in 0 until wArray.length()) {
                val item = wArray.getJSONObject(i)
                weeklyGoals.add(
                    ProposedGoalPlan(
                        level = "WEEKLY",
                        title = item.optString("title", "Week ${i + 1} Focus"),
                        timeframe = item.optString("timeframe", "Week ${i + 1}")
                    )
                )
            }

            val dailyActions = mutableListOf<ProposedDailyAction>()
            val dArray = obj.optJSONArray("dailyActions") ?: JSONArray()
            for (i in 0 until dArray.length()) {
                val item = dArray.getJSONObject(i)
                dailyActions.add(
                    ProposedDailyAction(
                        title = item.optString("title", "Daily Step ${i + 1}"),
                        dayOffset = item.optInt("dayOffset", i),
                        estimatedMinutes = item.optInt("estimatedMinutes", 15)
                    )
                )
            }

            ProposedPlan(summary, milestones, monthlyGoals, weeklyGoals, dailyActions)
        } catch (e: Exception) {
            null
        }
    }

    private fun generateFallbackPlan(
        title: String,
        description: String,
        whyStarted: String,
        goalType: String,
        affirmation: String
    ): ProposedPlan {
        val summary = "Carefully structured roadmap breaking '$title' into manageable horizons. Remember: $affirmation"

        val milestones = when (goalType) {
            "HEALTH_FITNESS" -> mutableListOf(
                ProposedMilestone("Establish Base Rhythm & Endurance", "Month 1", "Build consistency without injury"),
                ProposedMilestone("Double Volume & Functional Strength", "Month 2-3", "Noticeable transformation"),
                ProposedMilestone("Master Peak Performance & Lifestyle", "Month 4-6", "Sustainable long-term habit")
            )
            "CAREER_SKILLS" -> mutableListOf(
                ProposedMilestone("Fundamentals & Core Curriculum", "Month 1", "Grasp basic architecture and concepts"),
                ProposedMilestone("Build Portfolio Showcase Project", "Month 2", "Hands-on applied practical skills"),
                ProposedMilestone("Industry Ready Mastery & Networking", "Month 3-4", "Presenting results and landing goals")
            )
            "FINANCIAL" -> mutableListOf(
                ProposedMilestone("Financial Audit & Safety Buffer", "Month 1", "Clear debt inventory & emergency fund"),
                ProposedMilestone("Automated Savings & Investment System", "Month 2-3", "Consistent percentage allocation"),
                ProposedMilestone("Compound Wealth Milestone Reached", "Month 4-6", "Substantial financial milestone")
            )
            else -> mutableListOf(
                ProposedMilestone("Build Daily Momentum & Habit Anchor", "Month 1", "Lay solid daily routine foundation"),
                ProposedMilestone("Deepening Practice & Consistency", "Month 2-3", "Overcome plateaus and build skill"),
                ProposedMilestone("Full Habit Integration & Mastery", "Month 4-6", "Effortless, natural achievement")
            )
        }

        val monthlyGoals = mutableListOf(
            ProposedGoalPlan("MONTHLY", "Month 1: Consistency & Baseline Metrics", "Month 1"),
            ProposedGoalPlan("MONTHLY", "Month 2: Progressive Challenge & Expansion", "Month 2"),
            ProposedGoalPlan("MONTHLY", "Month 3: Consolidation & Habit Mastery", "Month 3")
        )

        val weeklyGoals = mutableListOf(
            ProposedGoalPlan("WEEKLY", "Week 1: Setup environment & complete Day 1-7 rituals", "Week 1"),
            ProposedGoalPlan("WEEKLY", "Week 2: Increase focus duration by 10%", "Week 2"),
            ProposedGoalPlan("WEEKLY", "Week 3: Overcome friction points & track insights", "Week 3"),
            ProposedGoalPlan("WEEKLY", "Week 4: Review Month 1 progress and celebrate wins", "Week 4")
        )

        val dailyActions = mutableListOf(
            ProposedDailyAction("Take the very first step: 15-min focused action on $title", 0, 15),
            ProposedDailyAction("Establish designated time slot & clear friction triggers", 1, 15),
            ProposedDailyAction("Complete 20-min session + write reflection in journal", 2, 20),
            ProposedDailyAction("Mid-week momentum check: review '$whyStarted'", 3, 15),
            ProposedDailyAction("Execute progressive drill with full presence", 4, 25),
            ProposedDailyAction("Active practice: apply one new technique or improvement", 5, 20),
            ProposedDailyAction("Weekly reflection & celebrate 7 days of showing up", 6, 15)
        )

        return ProposedPlan(summary, milestones, monthlyGoals, weeklyGoals, dailyActions)
    }

    suspend fun getCoachAdvice(
        userMessage: String,
        currentResolution: String?,
        streak: Int,
        totalCompleted: Int
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    You are OneStep Coach, an empathetic, supportive, and practical habit and resolution mentor.
                    User context:
                    - Active Resolution: ${currentResolution ?: "Building life habits"}
                    - Current Momentum Streak: $streak days
                    - Total Lifetime Actions Completed: $totalCompleted
                    
                    User query/challenge: "$userMessage"
                    
                    Core Coaching Philosophy:
                    - "One step today. Another tomorrow. Eventually, you arrive."
                    - Progress matters infinitely more than streaks.
                    - Missed days are NOT failure. Guilt kills momentum; grace fuels restart.
                    - Always give 1 immediate, bite-sized action the user can do in the next 5-10 minutes.
                    - Keep tone warm, concise, and inspiring (under 150 words).
                """.trimIndent()

                val request = GeminiGenerateRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
                )

                val response = GeminiClient.apiService.generateContent(apiKey, request)
                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!reply.isNullOrBlank()) {
                    return@withContext reply.trim()
                }
            } catch (e: Exception) {
                // fall through to local coaching wisdom
            }
        }

        // Empathetic offline coach response
        getOfflineCoachAdvice(userMessage, streak)
    }

    private fun getOfflineCoachAdvice(message: String, streak: Int): String {
        val lower = message.lowercase()
        return when {
            lower.contains("miss") || lower.contains("behind") || lower.contains("guilt") || lower.contains("fail") -> {
                "Take a deep breath. Missing a day is not failure—it is simply a pause. In OneStep, progress matters far more than an unbroken line. Your brain learns resilience when you restart gently. Reschedule today's step for tomorrow, or do a micro-version right now for just 3 minutes. You are still moving forward."
            }
            lower.contains("motivation") || lower.contains("lazy") || lower.contains("hard") || lower.contains("tired") -> {
                "Motivation follows action, not the other way around. Don't worry about finishing the whole mountain today. Just open the book, put on your shoes, or write one sentence. Give yourself permission to do the smallest possible piece. That one step is your victory today."
            }
            lower.contains("adjust") || lower.contains("plan") || lower.contains("too much") -> {
                "Honoring your current season of life is wisdom, not quitting. When goals feel heavy, reduce the daily volume, not the habit. Lower your daily target to 10 minutes. Small, consistent raindrops carve canyons. Let's adjust your timeline and keep momentum effortless."
            }
            else -> {
                "Remember your foundation: 'One step today. Another tomorrow. Eventually, you arrive.' You currently have $streak days of momentum! What is one small, joyful action you can do in the next 10 minutes to move your needle forward? Focus only on this single step."
            }
        }
    }
}
