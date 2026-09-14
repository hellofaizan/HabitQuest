package com.mohammadfaizan.habitquest.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
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
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
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
                .padding(14.dp),
            // A widget instance placed taller than its content needs (the launcher decides the
            // actual size, not this layout) otherwise left all the leftover space stranded below
            // the content — centering distributes it evenly instead.
            contentAlignment = Alignment.CenterStart
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

// Weight-based sizing (matches ContributionDay's Modifier.weight(1f) in-app) inside a
// fillMaxWidth() Row — a fixed .size() inside an unconstrained Row rendered as a handful of
// stretched bars instead of evenly-spaced squares on-device, so this is the reliable pattern.
// One Box, not a wrapper-plus-inner pair: padding first insets, background then fills the
// remainder, so the gap between cells comes free without a second view per cell — a real
// consideration here since RemoteViews chokes on a large widget view count (confirmed
// on-device: a 182-cell grid rendered as a garbled, truncated ~4x10 grid).
@Composable
fun RowScope.WidgetDayCell(day: WidgetDayState?, targetCount: Int, habitColor: Color, cellHeight: Dp) {
    Box(
        modifier = GlanceModifier
            .defaultWeight()
            .height(cellHeight)
            .padding(1.dp)
            .background(
                if (day != null) dayCellColor(day, targetCount, habitColor) else WidgetMutedCell.copy(alpha = 0.15f)
            )
            .cornerRadius(3.dp)
    ) {}
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
