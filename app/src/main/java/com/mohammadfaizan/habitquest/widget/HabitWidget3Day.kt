package com.mohammadfaizan.habitquest.widget

import android.content.Context
import android.graphics.Bitmap
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
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

val HabitIdParam = ActionParameters.Key<Long>("habit_id")
private const val WEEKLY_WIDGET_DAYS = 7
private const val WEEKLY_GAP_DP = 3f
// Card padding (14dp/side), the check button (36dp) and the spacer before it (10dp).
private const val WEEKLY_CHROME_WIDTH_DP = 28f + 36f + 10f

class HabitWidget3Day : GlanceAppWidget() {

    // Lets LocalSize.current reflect this instance's actual size, so the day-cell row bitmap
    // is rendered to fill however wide it's placed rather than one fixed layout.
    override val sizeMode: SizeMode = SizeMode.Exact

    // See provideGlance below for why state is read reactively.
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // Read reactively via currentState() rather than fetched once up front — Preferences
            // written by the config Activity otherwise isn't guaranteed to be visible on a
            // freshly placed widget's very first render.
            val prefs = currentState<Preferences>()
            val habitId = prefs[WidgetPrefs.HABIT_ID]
            val availableWidthDp = LocalSize.current.width.value

            var data by remember { mutableStateOf<HabitWidgetData?>(null) }
            var rowBitmap by remember { mutableStateOf<Bitmap?>(null) }
            var rowHeightDp by remember { mutableStateOf(0f) }

            LaunchedEffect(habitId, availableWidthDp) {
                val loaded = habitId?.let { loadHabitWidgetData(context, it, days = WEEKLY_WIDGET_DAYS) }
                data = loaded
                if (loaded != null) {
                    val rowWidthDp = (availableWidthDp - WEEKLY_CHROME_WIDTH_DP)
                        .coerceAtLeast(WEEKLY_WIDGET_DAYS * 10f)
                    val cellSizeDp = (rowWidthDp - (WEEKLY_WIDGET_DAYS - 1) * WEEKLY_GAP_DP) / WEEKLY_WIDGET_DAYS
                    val density = context.resources.displayMetrics.density
                    rowHeightDp = cellSizeDp
                    rowBitmap = renderDayGridBitmap(
                        days = loaded.days,
                        targetCount = loaded.targetCount,
                        habitColor = Color(loaded.colorHex.toColorInt()),
                        rows = 1,
                        columns = WEEKLY_WIDGET_DAYS,
                        cellSizePx = (cellSizeDp * density).toInt(),
                        gapPx = (WEEKLY_GAP_DP * density).toInt()
                    )
                } else {
                    rowBitmap = null
                }
            }

            HabitWidgetWeeklyContent(data, rowBitmap, rowHeightDp)
        }
    }
}

@Composable
private fun HabitWidgetWeeklyContent(data: HabitWidgetData?, rowBitmap: Bitmap?, rowHeightDp: Float) {
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
            modifier = GlanceModifier.fillMaxWidth(),
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

                if (rowBitmap != null) {
                    Image(
                        provider = ImageProvider(rowBitmap),
                        contentDescription = "Last 7 days",
                        modifier = GlanceModifier.fillMaxWidth().height(rowHeightDp.dp)
                    )
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
