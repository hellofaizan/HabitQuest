package com.mohammadfaizan.habitquest.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

// 7 rows (weekdays) x 26 columns (weeks) — the exact same grid ContributionGraph and
// ShareableProgressCard use in-app, so the widget reads as the same graph.
private const val GRAPH_ROWS = 7
private const val GRAPH_COLUMNS = 26
private const val GRAPH_WIDGET_DAYS = GRAPH_ROWS * GRAPH_COLUMNS

class HabitWidgetMonth : GlanceAppWidget() {

    // See HabitWidget3Day for why this is read reactively via currentState() instead of
    // fetched once up front.
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val habitId = prefs[WidgetPrefs.HABIT_ID]

            var data by remember { mutableStateOf<HabitWidgetData?>(null) }
            LaunchedEffect(habitId) {
                data = habitId?.let { loadHabitWidgetData(context, it, days = GRAPH_WIDGET_DAYS) }
            }

            HabitWidgetGraphContent(data)
        }
    }
}

@Composable
private fun HabitWidgetGraphContent(data: HabitWidgetData?) {
    if (data == null) {
        WidgetCard(habitColor = WidgetMutedCell) {
            Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No habit selected", style = TextStyle(color = ColorProvider(Color.White)))
            }
        }
        return
    }

    val habitColor = Color(data.colorHex.toColorInt())

    WidgetCard(habitColor = habitColor) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = GlanceModifier.defaultWeight()) {
                    WidgetHeader(
                        icon = data.icon,
                        name = data.name,
                        streak = data.currentStreak,
                        habitColor = habitColor
                    )
                }

                WidgetCheckButton(
                    habitColor = habitColor,
                    isCompleted = data.isFullyCompletedToday,
                    onClick = actionRunCallback<CompleteHabitMonthAction>(
                        actionParametersOf(HabitIdParam to data.habitId)
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(10.dp))

            // Column-major, same mapping as ContributionGraph: each column is one week,
            // each row one weekday, oldest week on the left.
            for (row in 0 until GRAPH_ROWS) {
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    for (col in 0 until GRAPH_COLUMNS) {
                        val dayIndex = row + col * GRAPH_ROWS
                        val day = data.days.getOrNull(dayIndex)
                        Box(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .height(8.dp)
                                .padding(horizontal = 0.5.dp)
                        ) {
                            Box(
                                modifier = GlanceModifier
                                    .fillMaxSize()
                                    .background(
                                        if (day != null) dayCellColor(day, data.targetCount, habitColor) else WidgetMutedCell.copy(alpha = 0.2f)
                                    )
                                    .cornerRadius(1.dp)
                            ) {}
                        }
                    }
                }
                if (row != GRAPH_ROWS - 1) {
                    Spacer(modifier = GlanceModifier.height(2.dp))
                }
            }

            Spacer(modifier = GlanceModifier.height(10.dp))

            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Text(
                    text = "🏆 Best: ${data.longestStreak}",
                    style = TextStyle(color = ColorProvider(Color.White), fontSize = 11.sp),
                    modifier = GlanceModifier.defaultWeight()
                )
                Text(
                    text = "✅ ${data.totalCompletion} total",
                    style = TextStyle(color = ColorProvider(Color.White), fontSize = 11.sp)
                )
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
