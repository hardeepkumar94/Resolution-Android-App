package com.example.ui.screens.reviews

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ReviewEntity
import com.example.ui.OneStepViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReviewsScreen(
    viewModel: OneStepViewModel,
    modifier: Modifier = Modifier
) {
    val weeklyReviews by viewModel.weeklyReviews.collectAsStateWithLifecycle()
    val monthlyReviews by viewModel.monthlyReviews.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0 = Weekly, 1 = Monthly
    val isWeekly = selectedTabIndex == 0

    var winsText by remember { mutableStateOf("") }
    var adjustmentsText by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(4) }
    var savedSuccess by remember { mutableStateOf(false) }

    val activeList = if (isWeekly) weeklyReviews else monthlyReviews

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
    ) {
        item {
            Text(
                text = "Periodic Reviews",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Reflect on weekly & monthly horizon wins and course-correct gently.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Tabs: Weekly vs Monthly
        item {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = {
                        selectedTabIndex = 0
                        savedSuccess = false
                    },
                    text = { Text("Weekly Review") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = {
                        selectedTabIndex = 1
                        savedSuccess = false
                    },
                    text = { Text("Monthly Review") }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Log Review Form
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("review_form_card"),
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
                            text = if (isWeekly) "This Week's Reflection" else "This Month's Reflection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (savedSuccess) {
                            Text(
                                text = "Saved!",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Wins
                    OutlinedTextField(
                        value = winsText,
                        onValueChange = {
                            winsText = it
                            savedSuccess = false
                        },
                        label = { Text("What were my biggest wins & steps forward?") },
                        placeholder = { Text("Celebrate what went well, habits kept, resistance conquered...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_wins_input"),
                        minLines = 2,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Adjustments
                    OutlinedTextField(
                        value = adjustmentsText,
                        onValueChange = {
                            adjustmentsText = it
                            savedSuccess = false
                        },
                        label = { Text("What needs gentle adjustment for next period?") },
                        placeholder = { Text("Reduce friction, adjust schedule, lighter targets...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_adjustments_input"),
                        minLines = 2,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Star Rating
                    Text(
                        text = "Momentum Rating: $rating / 5",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$i stars",
                                tint = if (i <= rating) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable {
                                        rating = i
                                        savedSuccess = false
                                    }
                                    .padding(2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.saveReview(
                                type = if (isWeekly) "WEEKLY" else "MONTHLY",
                                wins = winsText.trim(),
                                adjustments = adjustmentsText.trim(),
                                rating = rating
                            )
                            savedSuccess = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_review_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isWeekly) "Save Weekly Review" else "Save Monthly Review")
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Review History
        item {
            Text(
                text = "Past ${if (isWeekly) "Weekly" else "Monthly"} Reviews",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (activeList.isEmpty()) {
            item {
                Text(
                    text = "No past reviews logged yet. Complete your first review above!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(activeList, key = { it.id }) { review ->
                ReviewHistoryCard(review = review)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun ReviewHistoryCard(review: ReviewEntity) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

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
                    text = "${review.periodType}: ${review.periodKey}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (i <= review.rating) Color(0xFFF59E0B) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (review.wins.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Wins: ${review.wins}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (review.adjustments.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Adjustments: ${review.adjustments}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Recorded: ${dateFormat.format(Date(review.createdAt))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
