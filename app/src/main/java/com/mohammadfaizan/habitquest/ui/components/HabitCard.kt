package com.mohammadfaizan.habitquest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.local.HabitCompletion
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HabitCard(
    habit: Habit,
    completions: List<HabitCompletion> = emptyList(),
    onHabitClick: () -> Unit = {},
    onCompleteClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val habitColor = Color(habit.color.toColorInt())
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    // Check if habit is completed today
    val isCompletedToday = completions.any { completion ->
        completion.dateKey == today
    }
    
    // Get today's completion count for multi-target habits
    val todayCompletionCount = completions.count { completion ->
        completion.dateKey == today
    }
    
    // Check if habit is fully completed for today
    val isFullyCompleted = todayCompletionCount >= habit.targetCount
    
    // Check if we can still complete more times today
    val canCompleteMore = todayCompletionCount < habit.targetCount
    
    // Debug: Print completion info
    LaunchedEffect(completions) {
        println("Habit ${habit.name}: ${completions.size} completions, today: $todayCompletionCount/${habit.targetCount}, canComplete: $canCompleteMore")
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onHabitClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row with icon, name, and complete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Habit icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(habitColor),
                    contentAlignment = Alignment.Center
                ) {
                                            Icon(
                            imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Habit name and description
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (habit.description?.isNotBlank() == true) {
                        Text(
                            text = habit.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Show completion progress for multi-target habits
                    if (habit.targetCount > 1) {
                        Text(
                            text = "$todayCompletionCount/${habit.targetCount} completed today",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isFullyCompleted) 
                                MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Complete button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isFullyCompleted -> habitColor
                                isCompletedToday -> habitColor.copy(alpha = 0.7f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .clickable { 
                            if (canCompleteMore) {
                                onCompleteClick()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFullyCompleted) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = if (isFullyCompleted) "Completed" else "Mark as complete",
                        tint = if (isFullyCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Contribution graph
            ContributionGraph(
                habit = habit,
                completions = completions,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ContributionGraph(
    habit: Habit,
    completions: List<HabitCompletion>,
    modifier: Modifier = Modifier
) {
    val habitColor = Color(habit.color.toColorInt())
    val today = Calendar.getInstance()
    val graphDays = 182 // GitHub uses 364 days (52 columns × 7 rows)
    
    // Create a map of completion dates for quick lookup
    val completionMap = completions.groupBy { it.dateKey }
    
    // Generate the last 364 days in GitHub's pattern
    // GitHub starts from today and goes backwards
    val days = remember {
        List(graphDays) { dayOffset ->
            val date = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -dayOffset)
            }
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.time)
            val dayCompletions = completionMap[dateKey] ?: emptyList()
            DayData(
                dateKey = dateKey,
                completionCount = dayCompletions.size,
                targetCount = habit.targetCount
            )
        }.reversed() // Reverse to match GitHub's order (newest first)
    }
    
    // Create 52 columns × 7 rows grid (GitHub's structure)
    // Today is at top-right, yesterday is below it in same column
    // Each column represents a week
    // Each row represents a day of the week (Sunday to Saturday)
    val gridData = remember(days) {
        val columns = 26
        val rows = 7
        val grid = Array(rows) { row ->
            Array(columns) { col ->
                // Pattern: top-right = today, fill backwards
                val dayIndex = row + col * rows
                if (dayIndex < days.size) {
                    days[dayIndex]
                } else {
                    DayData("", 0, habit.targetCount) // Empty day
                }
            }
        }
        grid
    }
    
    Column(
        modifier = modifier
    ) {
        // GitHub-style grid: 52 columns × 7 rows
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (row in 0 until 7) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Render each column (week)
                    for (col in 0 until 26) {
                        val day = gridData[row][col]
                        ContributionDay(
                            day = day,
                            habitColor = habitColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Less",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                )

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(habitColor.copy(alpha = 0.3f))
                )

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(habitColor)
                )
            }
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = "More",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ContributionDay(
    day: DayData,
    habitColor: Color,
    modifier: Modifier = Modifier
) {
    val alpha = when {
        day.completionCount == 0 -> 0.1f
        day.completionCount >= day.targetCount -> 1.0f
        else -> (day.completionCount.toFloat() / day.targetCount.toFloat()) * 0.8f + 0.2f
    }
    
    Box(
        modifier = modifier
            .height(10.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(
                if (day.completionCount > 0) {
                    habitColor.copy(alpha = alpha)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                }
            )
    )
}

data class DayData(
    val dateKey: String,
    val completionCount: Int,
    val targetCount: Int
)

@Composable
fun HabitList(
    habits: List<Habit>,
    completions: Map<Long, List<HabitCompletion>> = emptyMap(),
    onHabitClick: (Habit) -> Unit = {},
    onCompleteClick: (Habit) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(habits) { habit ->
            HabitCard(
                habit = habit,
                completions = completions[habit.id] ?: emptyList(),
                onHabitClick = { onHabitClick(habit) },
                onCompleteClick = { onCompleteClick(habit) }
            )
        }
    }
}

@Composable
fun EmptyHabitState(
    onAddHabit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No habits yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Start building your habits by adding your first one",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddHabit,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text("Add Your First Habit")
        }
    }
}
