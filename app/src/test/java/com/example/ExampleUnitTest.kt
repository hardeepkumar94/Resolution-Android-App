package com.example

import com.example.data.ai.AiPlannerService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testAiPlannerFallbackBreakdownGeneratesValidRoadmap() = runBlocking {
        val plan = AiPlannerService.generateBreakdown(
            title = "Run a Half Marathon",
            description = "Train 3 times a week and run 21k",
            whyStarted = "For cardiovascular health",
            goalType = "HEALTH_FITNESS",
            affirmation = "One step today. Another tomorrow."
        )

        assertNotNull(plan)
        assertTrue(plan.milestones.isNotEmpty())
        assertTrue(plan.monthlyGoals.isNotEmpty())
        assertTrue(plan.weeklyGoals.isNotEmpty())
        assertTrue(plan.dailyActions.isNotEmpty())
        assertEquals(7, plan.dailyActions.size)
    }

    @Test
    fun testAiCoachOfflineAdviceEmphasizesProgressOverStreaks() = runBlocking {
        val advice = AiPlannerService.getCoachAdvice(
            userMessage = "I missed a day and feel guilty",
            currentResolution = "Fitness",
            streak = 5,
            totalCompleted = 10
        )

        assertNotNull(advice)
        assertTrue(advice.contains("failure") || advice.contains("pause") || advice.contains("resilience"))
    }
}
