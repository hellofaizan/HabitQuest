package com.mohammadfaizan.habitquest.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp

// Spacing values matching Tailwind CSS
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val xxxl = 64.dp
}

// Border radius values matching Tailwind CSS
object BorderRadius {
    val none = 0.dp
    val sm = 2.dp
    val md = 8.dp
    val lg = 12.dp
    val xl = 16.dp
    val xxl = 20.dp
    val icon = 45.dp
    val full = 9999.dp
}

// Shadow values matching Tailwind CSS
object Shadows {
    val none = Shadow(Color.Transparent, blurRadius = 0f, offset = Offset(0f, 0f))
    val sm = Shadow(Color(0x2A000000), blurRadius = 4f, offset = Offset(0f, 1f))
    val md = Shadow(Color(0x2A000000), blurRadius = 6f, offset = Offset(0f, 4f))
    val lg = Shadow(Color(0x2A000000), blurRadius = 15f, offset = Offset(0f, 10f))
    val xl = Shadow(Color(0x2A000000), blurRadius = 25f, offset = Offset(0f, 20f))
}

// Predefined shapes matching Tailwind CSS
object Shapes {
    val roundedSm = RoundedCornerShape(BorderRadius.sm)
    val roundedMd = RoundedCornerShape(BorderRadius.md)
    val roundedLg = RoundedCornerShape(BorderRadius.lg)
    val roundedXl = RoundedCornerShape(BorderRadius.xl)
    val roundedXxl = RoundedCornerShape(BorderRadius.xxl)
    val roundedSplashIcon = RoundedCornerShape(BorderRadius.icon)
    val roundedFull = RoundedCornerShape(BorderRadius.full)
}

// Theme-aware color accessors
@Composable
fun getBackgroundColor(): Color = MaterialTheme.colorScheme.background

@Composable
fun getForegroundColor(): Color = MaterialTheme.colorScheme.onBackground

@Composable
fun getPrimaryColor(): Color = MaterialTheme.colorScheme.primary

@Composable
fun getSecondaryColor(): Color = MaterialTheme.colorScheme.secondary

@Composable
fun getSurfaceColor(): Color = MaterialTheme.colorScheme.surface

@Composable
fun getErrorColor(): Color = MaterialTheme.colorScheme.error

@Composable
fun getBorderColor(): Color = MaterialTheme.colorScheme.outline

@Composable
fun getMutedColor(): Color = MaterialTheme.colorScheme.surfaceVariant

@Composable
fun getMutedForegroundColor(): Color = MaterialTheme.colorScheme.onSurfaceVariant

// Status colors
@Composable
fun getSuccessColor(): Color = Success

@Composable
fun getWarningColor(): Color = Warning

@Composable
fun getInfoColor(): Color = Info

// Chart colors
@Composable
fun getChartColor1(): Color = Chart1

@Composable
fun getChartColor2(): Color = Chart2

@Composable
fun getChartColor3(): Color = Chart3

@Composable
fun getChartColor4(): Color = Chart4

@Composable
fun getChartColor5(): Color = Chart5 