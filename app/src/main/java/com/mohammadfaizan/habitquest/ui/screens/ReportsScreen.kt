package com.mohammadfaizan.habitquest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.mohammadfaizan.habitquest.domain.usecase.HabitPeriodStat
import com.mohammadfaizan.habitquest.domain.usecase.PeriodReport
import com.mohammadfaizan.habitquest.ui.viewmodel.ReportPeriod
import com.mohammadfaizan.habitquest.ui.viewmodel.ReportsViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

private val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
private val weekLabelFormat = SimpleDateFormat("MMM d", Locale.getDefault())
private val monthLabelFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val report = uiState.report

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
                text = "Reports",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PeriodChip(
                label = "Week",
                isSelected = uiState.period == ReportPeriod.WEEK,
                onClick = { viewModel.setPeriod(ReportPeriod.WEEK) }
            )
            PeriodChip(
                label = "Month",
                isSelected = uiState.period == ReportPeriod.MONTH,
                onClick = { viewModel.setPeriod(ReportPeriod.MONTH) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = viewModel::goToPrevious) {
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous")
            }
            Text(
                text = report?.let { periodLabel(uiState.period, it) } ?: "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = viewModel::goToNext, enabled = uiState.periodOffset < 0) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next",
                    tint = if (uiState.periodOffset < 0) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading && report == null -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            report == null || report.habitStats.isEmpty() -> {
                Text(
                    text = "No active habits to report on yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
            else -> {
                ReportContent(report)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun ReportContent(report: PeriodReport) {
    insightMessage(report)?.let { insight ->
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = insight,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(14.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReportStatTile(
            emoji = "📈",
            value = "${report.overallCompletionRate.roundToInt()}%",
            label = deltaLabel(report),
            modifier = Modifier.weight(1f)
        )
        ReportStatTile(
            emoji = "✅",
            value = report.totalCompletions.toString(),
            label = "Total Completions",
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(28.dp))

    Text(
        text = "Best Performing Habits",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(10.dp))

    if (report.bestHabits.isEmpty()) {
        Text(
            text = "No completions this period yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        report.bestHabits.forEach { stat ->
            HabitStatRow(stat)
        }
    }

    if (report.needsAttentionHabits.isNotEmpty()) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Needs Attention",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        report.needsAttentionHabits.forEach { stat ->
            HabitStatRow(stat)
        }
    }
}

@Composable
private fun HabitStatRow(stat: HabitPeriodStat) {
    val habitColor = Color(stat.color.toColorInt())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stat.icon ?: "🎯", fontSize = 18.sp)

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stat.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (stat.completionRate / 100f).coerceIn(0f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(habitColor)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "${stat.completionRate.roundToInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = habitColor
        )
    }
}

@Composable
private fun ReportStatTile(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PeriodChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
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

private fun periodLabel(period: ReportPeriod, report: PeriodReport): String {
    return if (period == ReportPeriod.MONTH) {
        monthLabelFormat.format(dateKeyFormat.parse(report.startDateKey)!!)
    } else {
        val start = weekLabelFormat.format(dateKeyFormat.parse(report.startDateKey)!!)
        val end = weekLabelFormat.format(dateKeyFormat.parse(report.endDateKey)!!)
        "$start – $end"
    }
}

private fun deltaLabel(report: PeriodReport): String {
    val previous = report.previousOverallCompletionRate ?: return "Completion Rate"
    val delta = (report.overallCompletionRate - previous).roundToInt()
    return when {
        delta > 0 -> "+$delta% vs last period"
        delta < 0 -> "$delta% vs last period"
        else -> "Same as last period"
    }
}

private fun insightMessage(report: PeriodReport): String? {
    if (report.habitStats.isEmpty()) return null

    val previous = report.previousOverallCompletionRate
    val delta = previous?.let { (report.overallCompletionRate - it).roundToInt() }

    return when {
        delta != null && delta >= 10 -> "You're up $delta% from last period — keep it going! 🎉"
        delta != null && delta <= -10 -> "You're down ${-delta}% from last period. Let's turn it around."
        report.needsAttentionHabits.size >= 2 -> "${report.needsAttentionHabits.size} habits could use some attention this period."
        report.bestHabits.isNotEmpty() && report.bestHabits.first().completionRate >= 90f -> {
            "${report.bestHabits.first().name} is crushing it at ${report.bestHabits.first().completionRate.roundToInt()}%! 🔥"
        }
        else -> null
    }
}
