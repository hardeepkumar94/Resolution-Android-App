package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.OneStepViewModel
import com.example.ui.components.CelebrationDialog
import com.example.ui.components.RescheduleDialog
import com.example.ui.screens.calendar.CalendarHistoryScreen
import com.example.ui.screens.coach.AiCoachScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.journal.JournalScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.progress.ProgressAnalyticsScreen
import com.example.ui.screens.resolutions.CreateResolutionScreen
import com.example.ui.screens.resolutions.ResolutionDetailScreen
import com.example.ui.screens.resolutions.ResolutionsListScreen
import com.example.ui.screens.resolutions.ReviewPlanScreen
import com.example.ui.screens.reviews.ReviewsScreen
import com.example.ui.theme.OneStepTheme

enum class Screen {
    DASHBOARD,
    RESOLUTIONS,
    JOURNAL,
    CALENDAR,
    PROGRESS,
    REVIEWS,
    COACH,
    PROFILE,
    CREATE_RESOLUTION,
    REVIEW_PLAN,
    RESOLUTION_DETAIL
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {

    private val viewModel: OneStepViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val user by viewModel.user.collectAsStateWithLifecycle()
            val celebrationMessage by viewModel.celebrationEvent.collectAsStateWithLifecycle()
            val rescheduleAction by viewModel.rescheduleTarget.collectAsStateWithLifecycle()

            OneStepTheme(themeMode = user?.themeMode ?: "system") {
                var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }

                // BackHandler for secondary screens
                if (currentScreen != Screen.DASHBOARD) {
                    BackHandler {
                        currentScreen = when (currentScreen) {
                            Screen.CREATE_RESOLUTION, Screen.REVIEW_PLAN, Screen.RESOLUTION_DETAIL -> Screen.RESOLUTIONS
                            else -> Screen.DASHBOARD
                        }
                    }
                }

                val bottomNavItems = listOf(
                    BottomNavItem(Screen.DASHBOARD, "Today", Icons.Default.Home, "nav_today"),
                    BottomNavItem(Screen.RESOLUTIONS, "Goals", Icons.Default.Flag, "nav_goals"),
                    BottomNavItem(Screen.COACH, "Coach", Icons.Default.AutoAwesome, "nav_coach"),
                    BottomNavItem(Screen.JOURNAL, "Journal", Icons.Default.EditNote, "nav_journal"),
                    BottomNavItem(Screen.PROGRESS, "Progress", Icons.Default.TrendingUp, "nav_progress"),
                    BottomNavItem(Screen.PROFILE, "Profile", Icons.Default.Person, "nav_profile")
                )

                val showBottomBar = currentScreen in listOf(
                    Screen.DASHBOARD,
                    Screen.RESOLUTIONS,
                    Screen.COACH,
                    Screen.JOURNAL,
                    Screen.CALENDAR,
                    Screen.PROGRESS,
                    Screen.REVIEWS,
                    Screen.PROFILE
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(modifier = Modifier.testTag("main_bottom_nav")) {
                                bottomNavItems.forEach { item ->
                                    val isSelected = when (item.screen) {
                                        Screen.JOURNAL -> currentScreen == Screen.JOURNAL || currentScreen == Screen.CALENDAR
                                        Screen.PROGRESS -> currentScreen == Screen.PROGRESS || currentScreen == Screen.REVIEWS
                                        else -> currentScreen == item.screen
                                    }

                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentScreen = item.screen },
                                        icon = {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.label
                                            )
                                        },
                                        label = { Text(item.label) },
                                        modifier = Modifier.testTag(item.testTag)
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            Screen.DASHBOARD -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToJournal = { currentScreen = Screen.JOURNAL },
                                onNavigateToResolutions = { currentScreen = Screen.RESOLUTIONS },
                                onNavigateToCoach = { currentScreen = Screen.COACH }
                            )

                            Screen.RESOLUTIONS -> ResolutionsListScreen(
                                viewModel = viewModel,
                                onCreateResolution = { currentScreen = Screen.CREATE_RESOLUTION },
                                onSelectResolution = {
                                    currentScreen = Screen.RESOLUTION_DETAIL
                                }
                            )

                            Screen.CREATE_RESOLUTION -> CreateResolutionScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = Screen.RESOLUTIONS },
                                onPlanGenerated = { currentScreen = Screen.REVIEW_PLAN }
                            )

                            Screen.REVIEW_PLAN -> ReviewPlanScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = Screen.CREATE_RESOLUTION },
                                onPlanActivated = {
                                    currentScreen = Screen.RESOLUTION_DETAIL
                                }
                            )

                            Screen.RESOLUTION_DETAIL -> ResolutionDetailScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = Screen.RESOLUTIONS }
                            )

                            Screen.JOURNAL -> JournalScreen(
                                viewModel = viewModel
                            )

                            Screen.CALENDAR -> CalendarHistoryScreen(
                                viewModel = viewModel
                            )

                            Screen.PROGRESS -> ProgressAnalyticsScreen(
                                viewModel = viewModel
                            )

                            Screen.REVIEWS -> ReviewsScreen(
                                viewModel = viewModel
                            )

                            Screen.COACH -> AiCoachScreen(
                                viewModel = viewModel
                            )

                            Screen.PROFILE -> ProfileScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }

                // Global Celebration Dialog
                if (celebrationMessage != null) {
                    CelebrationDialog(
                        message = celebrationMessage!!,
                        onDismiss = { viewModel.dismissCelebration() }
                    )
                }

                // Global Grace & Reschedule Dialog
                if (rescheduleAction != null) {
                    RescheduleDialog(
                        action = rescheduleAction!!,
                        onReschedule = { actionId, daysOffset ->
                            viewModel.rescheduleAction(actionId, daysOffset)
                        },
                        onDismiss = { viewModel.closeRescheduleDialog() }
                    )
                }
            }
        }
    }
}
