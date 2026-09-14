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
import androidx.compose.ui.unit.sp
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
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

// Same 7-rows-per-column (weekday x week) layout ContributionGraph uses in-app, but covering a
// full year (53 weeks fully covers 365 days) instead of the app's 182-day window.
private const val GRAPH_ROWS = 7
private const val GRAPH_COLUMNS = 53
private const val GRAPH_WIDGET_DAYS = 365
private const val GRAPH_GAP_DP = 1.5f

class HabitWidgetMonth : GlanceAppWidget() {

    // Lets LocalSize.current reflect the widget's actual current size (rather than one fixed
    // layout) so the grid bitmap is rendered to fill however big this instance is placed —
    // needed since the grid is drawn to a bitmap sized in code, not laid out by RemoteViews.
    override val sizeMode: SizeMode = SizeMode.Exact

    // See HabitWidget3Day for why state is read reactively via currentState() instead of
    // fetched once up front.
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val habitId = prefs[WidgetPrefs.HABIT_ID]
            val availableWidthDp = LocalSize.current.width.value

            var data by remember { mutableStateOf<HabitWidgetData?>(null) }
            var graphBitmap by remember { mutableStateOf<Bitmap?>(null) }
            var graphHeightDp by remember { mutableStateOf(0f) }

            LaunchedEffect(habitId, availableWidthDp) {
                val loaded = habitId?.let { loadHabitWidgetData(context, it, days = GRAPH_WIDGET_DAYS) }
                data = loaded
                if (loaded != null) {
                    // Grid card padding is 14dp a side (see WidgetCard); the rest of the
                    // available width is the grid's budget, divided evenly across 53 columns.
                    val gridWidthDp = (availableWidthDp - 28f).coerceAtLeast(GRAPH_COLUMNS * 2f)
                    val cellSizeDp = (gridWidthDp - (GRAPH_COLUMNS - 1) * GRAPH_GAP_DP) / GRAPH_COLUMNS
                    val density = context.resources.displayMetrics.density
                    graphHeightDp = GRAPH_ROWS * cellSizeDp + (GRAPH_ROWS - 1) * GRAPH_GAP_DP
                    graphBitmap = renderDayGridBitmap(
                        days = loaded.days,
                        targetCount = loaded.targetCount,
                        habitColor = Color(loaded.colorHex.toColorInt()),
                        rows = GRAPH_ROWS,
                        columns = GRAPH_COLUMNS,
                        cellSizePx = (cellSizeDp * density).toInt(),
                        gapPx = (GRAPH_GAP_DP * density).toInt()
                    )
                } else {
                    graphBitmap = null
                }
            }

            HabitWidgetGraphContent(data, graphBitmap, graphHeightDp)
        }
    }
}

@Composable
private fun HabitWidgetGraphContent(data: HabitWidgetData?, graphBitmap: Bitmap?, graphHeightDp: Float) {
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
        Column(modifier = GlanceModifier.fillMaxWidth()) {
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

            if (graphBitmap != null) {
                Image(
                    provider = ImageProvider(graphBitmap),
                    contentDescription = "Progress graph",
                    modifier = GlanceModifier.fillMaxWidth().height(graphHeightDp.dp)
                )
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
