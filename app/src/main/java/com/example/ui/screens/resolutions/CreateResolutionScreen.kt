package com.example.ui.screens.resolutions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.OneStepViewModel
import java.util.Calendar

data class GoalTypeOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateResolutionScreen(
    viewModel: OneStepViewModel,
    onBack: () -> Unit,
    onPlanGenerated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGenerating by viewModel.isGeneratingPlan.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var whyStarted by remember { mutableStateOf("") }
    var selectedGoalType by remember { mutableStateOf("HEALTH_FITNESS") }
    var affirmation by remember { mutableStateOf("One step today. Another tomorrow. Eventually, I arrive.") }
    var selectedMonthsHorizon by remember { mutableStateOf(3) } // 3 months default
    var errorMessage by remember { mutableStateOf("") }

    val goalTypes = listOf(
        GoalTypeOption("HEALTH_FITNESS", "Health & Fitness", Icons.Default.FitnessCenter),
        GoalTypeOption("CAREER_SKILLS", "Career & Skills", Icons.Default.Work),
        GoalTypeOption("FINANCIAL", "Financial", Icons.Default.MonetizationOn),
        GoalTypeOption("MINDFULNESS", "Mindfulness", Icons.Default.SelfImprovement),
        GoalTypeOption("LEARNING", "Learning / Reading", Icons.Default.MenuBook),
        GoalTypeOption("CREATIVE", "Creative Arts", Icons.Default.Palette)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Resolution") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "State your big resolution. Our AI coach will break it down into Milestones → Monthly Goals → Weekly Goals → Daily Actions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Title
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (errorMessage.isNotBlank()) errorMessage = ""
                    },
                    label = { Text("Resolution Title *") },
                    placeholder = { Text("e.g., Run a Half Marathon, Master Kotlin, Save \$10k") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("resolution_title_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Description
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Briefly describe what achieving this looks like...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("resolution_description_input"),
                    minLines = 2,
                    maxLines = 4
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Why I Started (Core Anchor)
            item {
                OutlinedTextField(
                    value = whyStarted,
                    onValueChange = { whyStarted = it },
                    label = { Text("\"Why I started\" (Your core anchor) *") },
                    placeholder = { Text("e.g. To have the energy to play with my kids and live vibrantly.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("resolution_why_started_input"),
                    minLines = 2,
                    maxLines = 3,
                    leadingIcon = {
                        Icon(Icons.Default.FormatQuote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Goal Category
            item {
                Text(
                    text = "Goal Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(goalTypes) { type ->
                        FilterChip(
                            selected = selectedGoalType == type.key,
                            onClick = { selectedGoalType = type.key },
                            label = { Text(type.label) },
                            leadingIcon = {
                                Icon(type.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Target Horizon
            item {
                Text(
                    text = "Target Timeline",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val horizons = listOf(1 to "1 Month", 3 to "3 Months", 6 to "6 Months", 12 to "1 Year")
                    horizons.forEach { (months, label) ->
                        FilterChip(
                            selected = selectedMonthsHorizon == months,
                            onClick = { selectedMonthsHorizon = months },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Personal Affirmation
            item {
                OutlinedTextField(
                    value = affirmation,
                    onValueChange = { affirmation = it },
                    label = { Text("Personal Affirmation") },
                    placeholder = { Text("e.g. I show up every single day with grace.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("resolution_affirmation_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Error display
            if (errorMessage.isNotBlank()) {
                item {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }

            // Generate AI Breakdown Button
            item {
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            errorMessage = "Please enter a resolution title."
                            return@Button
                        }
                        if (whyStarted.isBlank()) {
                            whyStarted = "To become my best self, one step at a time."
                        }

                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.MONTH, selectedMonthsHorizon)
                        val targetDate = calendar.timeInMillis

                        viewModel.generateAiPlanForResolution(
                            title = title.trim(),
                            description = description.trim(),
                            whyStarted = whyStarted.trim(),
                            goalType = selectedGoalType,
                            targetDate = targetDate,
                            affirmation = affirmation.trim(),
                            colorHex = "#0284C7",
                            onReady = onPlanGenerated
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("generate_breakdown_button"),
                    enabled = !isGenerating,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("AI Coach is Building Your Roadmap...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate AI Breakdown & Review")
                    }
                }
            }
        }
    }
}
