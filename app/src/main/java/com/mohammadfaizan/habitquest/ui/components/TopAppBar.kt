package com.mohammadfaizan.habitquest.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.core.view.HapticFeedbackConstantsCompat
import com.mohammadfaizan.habitquest.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarComponent(
    title: String,
    onMenuClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
) {
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current

    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_PRESS)
                    } catch (e: Exception) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    onMenuClick()
                }
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            /*IconButton(
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_PRESS)
                    } catch (e: Exception) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    onStatsClick()
                }
            ) {
                Icon(painter = painterResource(R.drawable.ic_dashboard), contentDescription = "Layout")
            }
            IconButton(
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_PRESS)
                    } catch (e: Exception) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    onStatsClick()
                }
            ) {
                Icon(painter = painterResource(R.drawable.ic_chart), contentDescription = "Stats")
            }*/
            IconButton(
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_PRESS)
                    } catch (e: Exception) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    onAddClick()
                }
            ) {
                Icon(Icons.Outlined.AddCircle, contentDescription = "Add Habit")
            }
        }
    )
}