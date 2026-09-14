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
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
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
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

val HabitIdParam = ActionParameters.Key<Long>("habit_id")

private val WidgetBackground = Color(0xFF1C1C1E)
private val WidgetMutedCell = Color(0xFF3A3A3C)

class HabitWidget3Day : GlanceAppWidget() {

    // Read reactively via currentState() below rather than fetched once up front — Preferences
    // written by the config Activity (a different GlanceId-holding call site/process moment)
    // otherwise isn't guaranteed to be visible to the very first render of a freshly placed
    // widget, which showed as a permanent "No habit selected".
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val habitId = prefs[WidgetPrefs.HABIT_ID]

            var data by remember { mutableStateOf<HabitWidgetData?>(null) }
            LaunchedEffect(habitId) {
                data = habitId?.let { loadHabitWidgetData(context, it, days = 3) }
            }

            HabitWidget3DayContent(data)
        }
    }
}

@Composable
private fun HabitWidget3DayContent(data: HabitWidgetData?) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetBackground)
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (data == null) {
            Text(
                text = "No habit selected",
                style = TextStyle(color = ColorProvider(Color.White))
            )
        } else {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
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
                    Spacer(modifier = GlanceModifier.height(6.dp))
                    Row {
                        data.days.forEachIndexed { index, day ->
                            Box(
                                modifier = GlanceModifier
                                    .size(16.dp)
                                    .background(
                                        if (day.isCovered) Color(data.colorHex.toColorInt()) else WidgetMutedCell
                                    )
                            ) {}
                            if (index != data.days.lastIndex) {
                                Spacer(modifier = GlanceModifier.width(4.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = GlanceModifier.width(10.dp))

                Box(
                    modifier = GlanceModifier
                        .size(40.dp)
                        .background(
                            if (data.isFullyCompletedToday) Color(data.colorHex.toColorInt()) else WidgetMutedCell
                        )
                        .clickable(
                            actionRunCallback<CompleteHabit3DayAction>(
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
