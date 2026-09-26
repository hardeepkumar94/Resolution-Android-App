package com.example.ui.screens.journal

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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.DailyJournalEntity
import com.example.ui.OneStepViewModel
import com.example.ui.theme.MoodGoodColor
import com.example.ui.theme.MoodGreatColor
import com.example.ui.theme.MoodNeutralColor
import com.example.ui.theme.MoodRestColor
import com.example.ui.theme.MoodToughColor

data class MoodOption(
    val key: String,
    val label: String,
    val color: Color
)

@Composable
fun JournalScreen(
    viewModel: OneStepViewModel,
    modifier: Modifier = Modifier
) {
    val todayJournal by viewModel.todayJournal.collectAsStateWithLifecycle()
    val allJournals by viewModel.allJournals.collectAsStateWithLifecycle()

    var remarks by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("GOOD") }
    var learning by remember { mutableStateOf("") }
    var difficulties by remember { mutableStateOf("") }
    var timeSpentMinutes by remember { mutableIntStateOf(30) }
    var savedSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(todayJournal) {
        if (todayJournal != null) {
            remarks = todayJournal?.remarks ?: ""
            selectedMood = todayJournal?.mood ?: "GOOD"
            learning = todayJournal?.learning ?: ""
            difficulties = todayJournal?.difficulties ?: ""
            timeSpentMinutes = todayJournal?.timeSpentMinutes ?: 30
        }
    }

    val moodOptions = listOf(
        MoodOption("GREAT", "Great 😄", MoodGreatColor),
        MoodOption("GOOD", "Good 🙂", MoodGoodColor),
        MoodOption("NEUTRAL", "Neutral 😐", MoodNeutralColor),
        MoodOption("TOUGH", "Tough 😮‍💨", MoodToughColor),
        MoodOption("REST", "Rest 🌿", MoodRestColor)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
    ) {
        item {
            Text(
                text = "Daily Remarks & Journal",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "What did I do today? Track lessons, reflections, and growth.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Active Entry Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("today_journal_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today: ${viewModel.getTodayString()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (savedSuccess) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Saved!",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // "What did I do today?"
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = {
                            remarks = it
                            savedSuccess = false
                        },
                        label = { Text("What did I do today? *") },
                        placeholder = { Text("Reflect on your actions, milestones, or micro-steps taken...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("journal_remarks_input"),
                        minLines = 3,
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mood Selector
                    Text(
                        text = "Mood / Energy",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(moodOptions) { mood ->
                            FilterChip(
                                selected = selectedMood == mood.key,
                                onClick = {
                                    selectedMood = mood.key
                                    savedSuccess = false
                                },
                                label = { Text(mood.label) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Learning
                    OutlinedTextField(
                        value = learning,
                        onValueChange = {
                            learning = it
                            savedSuccess = false
                        },
                        label = { Text("What did I learn today? (Optional)") },
                        placeholder = { Text("New insight, technique, or mindset takeaway...") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Lightbulb, null, tint = MaterialTheme.colorScheme.secondary)
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Difficulties & adaptation
                    OutlinedTextField(
                        value = difficulties,
                        onValueChange = {
                            difficulties = it
                            savedSuccess = false
                        },
                        label = { Text("Difficulties & How I Adapted (Optional)") },
                        placeholder = { Text("Friction faced, resistance overcome, or adjustments made...") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Psychology, null, tint = MaterialTheme.colorScheme.tertiary)
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Time spent chips
                    Text(
                        text = "Time Invested: $timeSpentMinutes minutes",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 30, 45, 60, 90).forEach { mins ->
                            FilterChip(
                                selected = timeSpentMinutes == mins,
                                onClick = {
                                    timeSpentMinutes = mins
                                    savedSuccess = false
                                },
                                label = { Text("${mins}m") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            viewModel.saveJournalEntry(
                                remarks = remarks.trim(),
                                mood = selectedMood,
                                learning = learning.trim(),
                                difficulties = difficulties.trim(),
                                timeSpentMinutes = timeSpentMinutes
                            )
                            savedSuccess = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_journal_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Today's Journal Entry")
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Journal History Section
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Journal History (${allJournals.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (allJournals.isEmpty()) {
            item {
                Text(
                    text = "No previous journal entries yet. Save your first entry above!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(allJournals, key = { it.id }) { journal ->
                JournalHistoryRow(journal = journal)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun JournalHistoryRow(journal: DailyJournalEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = journal.date,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = journal.mood,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (journal.timeSpentMinutes > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${journal.timeSpentMinutes}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (journal.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = journal.remarks,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (journal.learning.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lightbulb, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Learned: ${journal.learning}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (journal.difficulties.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Adapted: ${journal.difficulties}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
