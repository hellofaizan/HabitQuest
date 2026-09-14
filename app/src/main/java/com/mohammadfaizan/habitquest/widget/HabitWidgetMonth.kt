package com.mohammadfaizan.habitquest.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

// 4 full weeks so the grid divides evenly into rows.
private const val MONTH_WIDGET_DAYS = 28
private val MonthWidgetBackground = Color(0xFF1C1C1E)
private val MonthWidgetMutedCell = Color(0xFF3A3A3C)

class HabitWidgetMonth : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        val habitId = prefs[WidgetPrefs.HABIT_ID]
        val data = habitId?.let { loadHabitWidgetData(context, it, days = MONTH_WIDGET_DAYS) }

        provideContent {
            HabitWidgetMonthContent(data)
        }
    }
}

@Composable
private fun HabitWidgetMonthContent(data: HabitWidgetData?) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(MonthWidgetBackground)
            .padding(12.dp)
    ) {
        if (data == null) {
            Text(
                text = "No habit selected",
                style = TextStyle(color = ColorProvider(Color.White))
            )
            return@Column
        }

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "${data.icon ?: "🎯"} ${data.name}",
                    maxLines = 1,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = "🔥 ${data.currentStreak}-day streak",
                    style = TextStyle(color = ColorProvider(Color.White))
                )
            }

            Box(
                modifier = GlanceModifier
                    .size(36.dp)
                    .background(
                        if (data.isFullyCompletedToday) Color(data.colorHex.toColorInt()) else MonthWidgetMutedCell
                    )
                    .clickable(
                        actionRunCallback<CompleteHabitMonthAction>(
                            actionParametersOf(HabitIdParam to data.habitId)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (data.isFullyCompletedToday) "✓" else "+",
                    style = TextStyle(color = ColorProvider(Color.White))
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(10.dp))

        val weeks = data.days.chunked(7)
        weeks.forEachIndexed { weekIndex, week ->
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                week.forEachIndexed { dayIndex, day ->
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .height(14.dp)
                            .background(
                                if (day.isCovered) Color(data.colorHex.toColorInt()) else MonthWidgetMutedCell
                            )
                    ) {}
                    if (dayIndex != week.lastIndex) {
                        Spacer(modifier = GlanceModifier.width(3.dp))
                    }
                }
            }
            if (weekIndex != weeks.lastIndex) {
                Spacer(modifier = GlanceModifier.height(3.dp))
            }
        }
    }
}

class CompleteHabitMonthAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val habitId = parameters[HabitIdParam] ?: return
        completeHabitFromWidget(context, habitId)
        HabitWidgetMonth().update(context, glanceId)
    }
}

class HabitWidgetMonthReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HabitWidgetMonth()
}
