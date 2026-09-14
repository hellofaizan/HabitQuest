package com.mohammadfaizan.habitquest.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mohammadfaizan.habitquest.ui.components.FrozenDayColor

// Same idea as ShareableProgressCard's vertical gradient (habitColor tint over the app's
// surface) — Glance backgrounds are flat, so this approximates it as a solid blend instead.
private val WidgetCardBase = Color(0xFF17171B)
val WidgetMutedCell: Color = Color(0xFF3A3A3C)

@Composable
fun WidgetCard(habitColor: Color, content: @Composable () -> Unit) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetCardBase)
            .cornerRadius(20.dp)
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(habitColor.copy(alpha = 0.18f))
                .padding(14.dp)
        ) {
            content()
        }
    }
}

@Composable
fun WidgetHeader(icon: String?, name: String, streak: Int, habitColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = GlanceModifier
                .size(28.dp)
                .background(habitColor.copy(alpha = 0.35f))
                .cornerRadius(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon ?: "🎯", style = TextStyle(fontSize = 14.sp))
        }

        Spacer(modifier = GlanceModifier.width(8.dp))

        Column {
            Text(
                text = name,
                maxLines = 1,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )
            Text(
                text = "🔥 $streak-day streak",
                maxLines = 1,
                style = TextStyle(
                    color = ColorProvider(habitColor),
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
fun WidgetCheckButton(habitColor: Color, isCompleted: Boolean, onClick: Action) {
    Box(
        modifier = GlanceModifier
            .size(36.dp)
            .background(if (isCompleted) habitColor else WidgetMutedCell)
            .cornerRadius(18.dp)
            .clickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isCompleted) "✓" else "+",
            style = TextStyle(color = ColorProvider(Color.White), fontWeight = FontWeight.Bold)
        )
    }
}

// Mirrors ContributionDay in ui/components/biannualgraph.kt exactly, so a widget's grid reads
// the same as the in-app graph and the share-image card.
fun dayCellColor(day: WidgetDayState, targetCount: Int, habitColor: Color): Color {
    return when {
        day.completionCount > 0 -> {
            val alpha = if (day.completionCount >= targetCount) {
                1.0f
            } else {
                (day.completionCount.toFloat() / targetCount.toFloat()) * 0.8f + 0.2f
            }
            habitColor.copy(alpha = alpha)
        }
        day.isFrozen -> FrozenDayColor.copy(alpha = 0.55f)
        else -> WidgetMutedCell.copy(alpha = 0.5f)
    }
}
