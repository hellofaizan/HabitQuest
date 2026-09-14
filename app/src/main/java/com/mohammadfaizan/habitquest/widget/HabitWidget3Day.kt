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
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

val HabitIdParam = ActionParameters.Key<Long>("habit_id")
private const val WEEKLY_WIDGET_DAYS = 7

class HabitWidget3Day : GlanceAppWidget() {

    // See the comment in provideGlance below for why state is read reactively.
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // Read reactively via currentState() rather than fetched once up front — Preferences
            // written by the config Activity otherwise isn't guaranteed to be visible on a
            // freshly placed widget's very first render.
            val prefs = currentState<Preferences>()
            val habitId = prefs[WidgetPrefs.HABIT_ID]

            var data by remember { mutableStateOf<HabitWidgetData?>(null) }
            LaunchedEffect(habitId) {
                data = habitId?.let { loadHabitWidgetData(context, it, days = WEEKLY_WIDGET_DAYS) }
            }

            HabitWidgetWeeklyContent(data)
        }
    }
}

@Composable
private fun HabitWidgetWeeklyContent(data: HabitWidgetData?) {
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
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                WidgetHeader(
                    icon = data.icon,
                    name = data.name,
                    streak = data.currentStreak,
                    habitColor = habitColor
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                Row {
                    data.days.forEachIndexed { index, day ->
                        Box(
                            modifier = GlanceModifier
                                .size(18.dp)
                                .background(dayCellColor(day, data.targetCount, habitColor))
                                .cornerRadius(4.dp)
                        ) {}
                        if (index != data.days.lastIndex) {
                            Spacer(modifier = GlanceModifier.width(4.dp))
                        }
                    }
                }
            }

            Spacer(modifier = GlanceModifier.width(10.dp))

            WidgetCheckButton(
                habitColor = habitColor,
                isCompleted = data.isFullyCompletedToday,
                onClick = actionRunCallback<CompleteHabit3DayAction>(
                    actionParametersOf(HabitIdParam to data.habitId)
                )
            )
        }
    }
}

class CompleteHabit3DayAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val habitId = parameters[HabitIdParam] ?: return
        completeHabitFromWidget(context, habitId)
        HabitWidget3Day().update(context, glanceId)
    }
}

class HabitWidget3DayReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HabitWidget3Day()
}
