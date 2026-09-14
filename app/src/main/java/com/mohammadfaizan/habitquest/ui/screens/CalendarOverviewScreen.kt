package com.mohammadfaizan.habitquest.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.domain.usecase.CalendarDayData
import com.mohammadfaizan.habitquest.ui.viewmodel.CalendarOverviewViewModel
import com.mohammadfaizan.habitquest.ui.viewmodel.CalendarViewMode
import com.mohammadfaizan.habitquest.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val monthLabelFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
private val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
private val shortMonthNames = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

@Composable
fun CalendarOverviewScreen(
    viewModel: CalendarOverviewViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val todayKey = DateUtils.getCurrentDateKey()

    val accentColor = uiState.selectedHabitId
        ?.let { id -> uiState.habits.find { it.id == id }?.color }
        ?.let { Color(it.toColorInt()) }
        ?: MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Calendar Overview",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ViewModeChip(
                label = "Month",
                isSelected = uiState.viewMode == CalendarViewMode.MONTH,
                onClick = { viewModel.setViewMode(CalendarViewMode.MONTH) }
            )
            ViewModeChip(
                label = "Year",
                isSelected = uiState.viewMode == CalendarViewMode.YEAR,
                onClick = { viewModel.setViewMode(CalendarViewMode.YEAR) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                HabitFilterChip(
                    label = "All Habits",
                    color = MaterialTheme.colorScheme.primary,
                    isSelected = uiState.selectedHabitId == null,
                    onClick = { viewModel.selectHabit(null) }
                )
            }
            items(uiState.habits, key = { it.id }) { habit ->
                HabitFilterChip(
                    label = habit.name,
                    color = Color(habit.color.toColorInt()),
                    isSelected = uiState.selectedHabitId == habit.id,
                    onClick = { viewModel.selectHabit(habit.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = viewModel::goToPreviousPeriod) {
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous")
            }
            Text(
                text = if (uiState.viewMode == CalendarViewMode.MONTH) {
                    monthLabel(uiState.year, uiState.month)
                } else {
                    uiState.year.toString()
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = viewModel::goToNextPeriod) {
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
            }
            TextButton(onClick = viewModel::goToToday) {
                Text("Today")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.viewMode == CalendarViewMode.MONTH) {
            MonthCalendarGrid(
                year = uiState.year,
                month = uiState.month,
                days = uiState.days,
                accentColor = accentColor,
                todayKey = todayKey
            )
        } else {
            YearOverviewGrid(
                year = uiState.year,
                days = uiState.days,
                accentColor = accentColor,
                onMonthClick = { month -> viewModel.jumpToMonth(uiState.year, month) }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun ViewModeChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun HabitFilterChip(label: String, color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(width = 1.dp, color = if (isSelected) color else MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private val weekdayHeaderLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@Composable
private fun MonthCalendarGrid(
    year: Int,
    month: Int,
    days: Map<String, CalendarDayData>,
    accentColor: Color,
    todayKey: String
) {
    val cells = remember(year, month) { buildMonthCells(year, month) }

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdayHeaderLabels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { cell ->
                    MonthDayCell(
                        cell = cell,
                        completionRate = cell?.let { days[it.dateKey]?.completionRate },
                        accentColor = accentColor,
                        isToday = cell?.dateKey == todayKey,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthDayCell(
    cell: MonthCell?,
    completionRate: Float?,
    accentColor: Color,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    val rate = completionRate ?: 0f
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    cell == null -> Color.Transparent
                    rate <= 0f -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    else -> accentColor.copy(alpha = (rate * 0.8f + 0.2f).coerceIn(0.2f, 1f))
                }
            )
            .then(
                if (isToday) Modifier.border(2.dp, accentColor, RoundedCornerShape(8.dp)) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (cell != null) {
            Text(
                text = cell.day.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (rate > 0.5f) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun YearOverviewGrid(
    year: Int,
    days: Map<String, CalendarDayData>,
    accentColor: Color,
    onMonthClick: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for (rowStart in 0 until 12 step 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (month in rowStart until (rowStart + 3).coerceAtMost(12)) {
                    MiniMonthCard(
                        year = year,
                        month = month,
                        days = days,
                        accentColor = accentColor,
                        onClick = { onMonthClick(month) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniMonthCard(
    year: Int,
    month: Int,
    days: Map<String, CalendarDayData>,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cells = remember(year, month) { buildMonthCells(year, month) }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Text(
            text = shortMonthNames[month],
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { cell ->
                    val rate = cell?.let { days[it.dateKey]?.completionRate } ?: 0f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(0.5.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                when {
                                    cell == null -> Color.Transparent
                                    rate <= 0f -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    else -> accentColor.copy(alpha = (rate * 0.8f + 0.2f).coerceIn(0.2f, 1f))
                                }
                            )
                    )
                }
            }
        }
    }
}

private data class MonthCell(val day: Int, val dateKey: String)

private fun buildMonthCells(year: Int, month: Int): List<MonthCell?> {
    val calendar = Calendar.getInstance().apply { set(year, month, 1, 0, 0, 0) }
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // Calendar.SUNDAY=1..SATURDAY=7
    val leadingBlanks = when (firstDayOfWeek) {
        Calendar.SUNDAY -> 6
        Calendar.MONDAY -> 0
        else -> firstDayOfWeek - Calendar.MONDAY
    }
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val cells = mutableListOf<MonthCell?>()
    repeat(leadingBlanks) { cells.add(null) }
    for (day in 1..daysInMonth) {
        calendar.set(Calendar.DAY_OF_MONTH, day)
        cells.add(MonthCell(day, dayKeyFormat.format(calendar.time)))
    }
    while (cells.size % 7 != 0) cells.add(null)
    return cells
}

private fun monthLabel(year: Int, month: Int): String {
    val calendar = Calendar.getInstance().apply { set(year, month, 1) }
    return monthLabelFormat.format(calendar.time)
}
