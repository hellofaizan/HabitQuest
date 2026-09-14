package com.mohammadfaizan.habitquest.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

// Renders the day-cell grid to a single bitmap instead of one Glance view per cell. A real
// device showed even a 35-cell RemoteViews grid rendering as garbled/truncated (a 182-cell one
// was worse) — this sidesteps that entirely: however many days, it's still exactly one Image
// view. Colors/rounding mirror ContributionDay in ui/components/biannualgraph.kt via
// dayCellColor() below, so this reads as the same graph as the in-app card and share image.
fun renderDayGridBitmap(
    days: List<WidgetDayState>,
    targetCount: Int,
    habitColor: Color,
    rows: Int,
    columns: Int,
    cellSizePx: Int,
    gapPx: Int
): Bitmap {
    val width = (columns * cellSizePx + (columns - 1) * gapPx).coerceAtLeast(1)
    val height = (rows * cellSizePx + (rows - 1) * gapPx).coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val cornerRadius = cellSizePx * 0.3f
    val rect = RectF()

    for (col in 0 until columns) {
        for (row in 0 until rows) {
            val dayIndex = row + col * rows
            val day = days.getOrNull(dayIndex) ?: continue
            paint.color = dayCellColor(day, targetCount, habitColor).toArgb()
            val left = col * (cellSizePx + gapPx)
            val top = row * (cellSizePx + gapPx)
            rect.set(left.toFloat(), top.toFloat(), (left + cellSizePx).toFloat(), (top + cellSizePx).toFloat())
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        }
    }
    return bitmap
}
