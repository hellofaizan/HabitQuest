package com.mohammadfaizan.habitquest.ui.screens

import android.Manifest
import android.view.ViewTreeObserver
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.toColorInt
import androidx.core.view.HapticFeedbackConstantsCompat
import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.ui.components.CategoryChips
import com.mohammadfaizan.habitquest.ui.components.ColorOption
import com.mohammadfaizan.habitquest.ui.components.InputField
import com.mohammadfaizan.habitquest.ui.viewmodel.AddHabitViewModel
import com.mohammadfaizan.habitquest.ui.viewmodel.AVAILABLE_HABIT_ICONS
import com.mohammadfaizan.habitquest.ui.viewmodel.DEFAULT_REMINDER_DAYS
import com.mohammadfaizan.habitquest.utils.DateUtils
import com.mohammadfaizan.habitquest.utils.HABIT_TEMPLATES
import com.mohammadfaizan.habitquest.utils.HabitTemplate
import com.mohammadfaizan.habitquest.utils.PermissionUtils
import java.util.Calendar

@Composable
fun AddHabitScreen(
    onBack: () -> Unit,
    onCreateHabit: (name: String, description: String?, color: String, category: String?, frequency: String, targetCount: Int, reminderEnabled: Boolean, reminderTime: String?) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onUpdateHabit: (Long, name: String, description: String?, color: String, category: String?, frequency: String, targetCount: Int, reminderEnabled: Boolean, reminderTime: String?, reminderDays: String, icon: String) -> Unit = { _, _, _, _, _, _, _, _, _, _, _ -> },
    onDeleteHabit: (Long) -> Unit = { },
    onArchiveHabit: (Long) -> Unit = { },
    modifier: Modifier = Modifier,
    viewModel: AddHabitViewModel? = null,
    habitToEdit: Habit? = null
) {
    val focusManager = LocalFocusManager.current
    var isKeyboardVisible by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val r = android.graphics.Rect()
            view.getWindowVisibleDisplayFrame(r)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - r.bottom
            isKeyboardVisible = keypadHeight > screenHeight * 0.15
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
    }

    BackHandler {
        if (isKeyboardVisible) {
            focusManager.clearFocus()
            keyboardController?.hide()
            isKeyboardVisible = false
        } else {
            onBack()
        }
    }

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val formState by viewModel?.formState?.collectAsState() ?: remember { mutableStateOf(null) }
    val validation by viewModel?.validation?.collectAsState() ?: remember { mutableStateOf(null) }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#2a78d6") }
    var selectedIcon by remember { mutableStateOf(AVAILABLE_HABIT_ICONS.first()) }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf("DAILY") }
    var targetCount by remember { mutableStateOf(1) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf("09:00") }
    var reminderDays by remember { mutableStateOf(DEFAULT_REMINDER_DAYS) }

    val currentName = formState?.name ?: (habitToEdit?.name ?: name)
    val currentDescription = formState?.description ?: (habitToEdit?.description ?: description)
    val currentColor = formState?.color ?: (habitToEdit?.color ?: selectedColor)
    val currentIcon = formState?.icon ?: (habitToEdit?.icon ?: selectedIcon)
    val currentCategory = formState?.category ?: (habitToEdit?.category ?: selectedCategory)
    val currentFrequency =
        formState?.frequency ?: (habitToEdit?.frequency?.name ?: selectedFrequency)
    val currentTargetCount = formState?.targetCount ?: (habitToEdit?.targetCount ?: targetCount)
    val currentReminderEnabled =
        formState?.reminderEnabled ?: (habitToEdit?.reminderEnabled ?: reminderEnabled)
    val currentReminderTime = formState?.reminderTime ?: (habitToEdit?.reminderTime ?: reminderTime)
    val currentReminderDays = formState?.reminderDays
        ?: (habitToEdit?.reminderDays?.let { DateUtils.parseReminderDays(it) } ?: reminderDays)
    val isFormValid = validation?.isFormValid ?: true
    val nameError = validation?.isNameValid?.let { if (!it) "Habit name is required" else null }
    val targetCountError =
        validation?.isTargetCountValid?.let { if (!it) "Target count must be greater than 0" else null }

    val updateName = { newName: String ->
        if (viewModel != null) {
            viewModel.updateName(newName)
        } else {
            name = newName
        }
    }

    val updateDescription = { newDescription: String ->
        if (viewModel != null) {
            viewModel.updateDescription(newDescription)
        } else {
            description = newDescription
        }
    }

    val updateColor = { newColor: String ->
        if (viewModel != null) {
            viewModel.updateColor(newColor)
        } else {
            selectedColor = newColor
        }
    }

    val updateIcon = { newIcon: String ->
        if (viewModel != null) {
            viewModel.updateIcon(newIcon)
        } else {
            selectedIcon = newIcon
        }
    }

    val updateCategory = { newCategory: String ->
        if (viewModel != null) {
            viewModel.updateCategory(newCategory)
        } else {
            selectedCategory = newCategory
        }
    }

    { newFrequency: String ->
        if (viewModel != null) {
            viewModel.updateFrequency(newFrequency)
        } else {
            selectedFrequency = newFrequency
        }
    }

    val updateTargetCount = { newTargetCount: Int ->
        if (viewModel != null) {
            viewModel.updateTargetCount(newTargetCount)
        } else {
            targetCount = newTargetCount
        }
    }

    val applyTemplate = { template: HabitTemplate ->
        updateName(template.name)
        updateDescription(template.description)
        updateColor(template.color)
        updateIcon(template.icon)
        updateCategory(template.category)
        updateTargetCount(template.targetCount)
    }

    // Permission launcher for notification permission
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // If permission denied, disable reminder
            if (viewModel != null) {
                viewModel.updateReminderEnabled(false)
            } else {
                reminderEnabled = false
            }
        }
    }

    val updateReminderEnabled = { enabled: Boolean ->
        if (enabled) {
            // User is trying to enable reminder - check permission first
            if (PermissionUtils.isNotificationPermissionRequired() && 
                !PermissionUtils.hasNotificationPermission(context)) {
                // Request permission
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Permission already granted or not required, enable reminder
                if (viewModel != null) {
                    viewModel.updateReminderEnabled(true)
                } else {
                    reminderEnabled = true
                }
            }
        } else {
            // User is disabling reminder - no permission check needed
            if (viewModel != null) {
                viewModel.updateReminderEnabled(false)
            } else {
                reminderEnabled = false
            }
        }
    }

    val updateReminderTime = { time: String ->
        if (viewModel != null) {
            viewModel.updateReminderTime(time)
        } else {
            reminderTime = time
        }
    }

    val toggleReminderDay = { day: Int ->
        if (viewModel != null) {
            viewModel.toggleReminderDay(day)
        } else {
            reminderDays = if (day in reminderDays) reminderDays - day else reminderDays + day
        }
    }

    // Available color options
    val availableColors = viewModel?.getAvailableColors() ?: listOf(
        "#2a78d6", "#eb6834", "#1baf7a", "#eda100",
        "#e87ba4", "#008300", "#4a3aa7", "#e34948"
    )

    val availableIcons = viewModel?.getAvailableIcons() ?: AVAILABLE_HABIT_ICONS

    val availableCategories = viewModel?.getAvailableCategories() ?: listOf(
        "Health", "Fitness", "Learning", "Productivity",
        "Mindfulness", "Social", "Finance", "Creative", "Other"
    )

    viewModel?.getAvailableFrequencies() ?: listOf("DAILY", "WEEKLY", "MONTHLY")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_PRESS)
                    } catch (e: Exception) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    if (isKeyboardVisible) {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        isKeyboardVisible = false
                    } else {
                        onBack()
                    }
                }
            ) {

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go Back"
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = if (habitToEdit != null) "Edit Habit" else "Create New Habit",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            if (habitToEdit != null) {
                TextButton(
                    onClick = {
                        try {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_PRESS)
                        } catch (e: Exception) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onArchiveHabit(habitToEdit.id)
                    }
                ) {
                    Text("Archive")
                }

                IconButton(
                    onClick = {
                        try {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_PRESS)
                        } catch (e: Exception) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        showDeleteConfirmation = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Habit",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        if (habitToEdit == null) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Quick Start",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(HABIT_TEMPLATES) { template ->
                    TemplateOption(
                        template = template,
                        onSelected = { applyTemplate(template) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Habit Name (Required)
        InputField(
            value = currentName,
            onValueChange = updateName,
            label = "Habit Name *",
            placeholder = "Enter habit name",
            keyboardType = KeyboardType.Text,
            isError = nameError != null,
            errorMessage = nameError
        )

        Spacer(modifier = Modifier.height(16.dp))

        InputField(
            value = currentDescription,
            onValueChange = updateDescription,
            label = "Description (Optional)",
            placeholder = "Enter habit description",
            keyboardType = KeyboardType.Text,
            singleLine = false
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Choose Color",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(availableColors) { color ->
                ColorOption(
                    color = color,
                    isSelected = currentColor == color,
                    onColorSelected = { updateColor(color) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Choose Icon",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(availableIcons) { icon ->
                IconOption(
                    icon = icon,
                    isSelected = currentIcon == icon,
                    onIconSelected = { updateIcon(icon) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Category (Optional)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(availableCategories) { category ->
                CategoryChips(
                    category = category,
                    isSelected = currentCategory == category,
                    onCategorySelected = { updateCategory(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        /*
        Daily Weekly Frequescy selector
        Text(
            text = "Frequency",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(availableFrequencies) { frequency ->
                FrequencyChip(
                    frequency = frequency,
                    isSelected = currentFrequency == frequency,
                    onFrequencySelected = { updateFrequency(frequency) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        */

        Text(
            text = "Target Count",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_PRESS)
                    } catch (e: Exception) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    if (currentTargetCount > 1) updateTargetCount(currentTargetCount - 1)
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Target"
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = currentTargetCount.toString(),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(16.dp))

            OutlinedButton(
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_PRESS)
                    } catch (e: Exception) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    updateTargetCount(currentTargetCount + 1)
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Target"
                )
            }
        }

        if (targetCountError != null) {
            Text(
                text = targetCountError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Set Reminder",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Switch(
                checked = currentReminderEnabled,
                onCheckedChange = updateReminderEnabled
            )
        }

        if (currentReminderEnabled) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Remind on",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                reminderDayOptions.forEach { (day, label) ->
                    DayToggleChip(
                        label = label,
                        selected = day in currentReminderDays,
                        onClick = { toggleReminderDay(day) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reminder Time: $currentReminderTime")
            }

            if (showTimePicker) {
                ReminderTimePickerDialog(
                    initialTime = currentReminderTime,
                    onConfirm = { time ->
                        updateReminderTime(time)
                        showTimePicker = false
                    },
                    onDismiss = { showTimePicker = false }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                try {
                    view.performHapticFeedback(HapticFeedbackConstantsCompat.CONFIRM)
                } catch (e: Exception) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                if (habitToEdit != null) {
                    onUpdateHabit(
                        habitToEdit.id,
                        currentName.trim(),
                        currentDescription.trim().takeIf { it.isNotBlank() },
                        currentColor,
                        currentCategory.takeIf { it.isNotBlank() },
                        currentFrequency,
                        currentTargetCount,
                        currentReminderEnabled,
                        if (currentReminderEnabled) currentReminderTime else null,
                        DateUtils.formatReminderDays(currentReminderDays),
                        currentIcon
                    )
                } else {
                    onCreateHabit(
                        currentName.trim(),
                        currentDescription.trim().takeIf { it.isNotBlank() },
                        currentColor,
                        currentCategory.takeIf { it.isNotBlank() },
                        currentFrequency,
                        currentTargetCount,
                        currentReminderEnabled,
                        if (currentReminderEnabled) currentReminderTime else null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid
        ) {
            Text(if (habitToEdit != null) "Update Habit" else "Create Habit")
        }
    }

    if (showDeleteConfirmation && habitToEdit != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    text = "Delete Habit",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "This action is irreversible. Are you sure you want to delete \"${habitToEdit.name}\"?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        try {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_PRESS)
                        } catch (e: Exception) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_PRESS)
                        } catch (e: Exception) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onDeleteHabit(habitToEdit.id)
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Monday-first, matching the rest of the app's week convention; values are Calendar.DAY_OF_WEEK.
private val reminderDayOptions = listOf(
    Calendar.MONDAY to "Mon",
    Calendar.TUESDAY to "Tue",
    Calendar.WEDNESDAY to "Wed",
    Calendar.THURSDAY to "Thu",
    Calendar.FRIDAY to "Fri",
    Calendar.SATURDAY to "Sat",
    Calendar.SUNDAY to "Sun"
)

@Composable
private fun DayToggleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimePickerDialog(
    initialTime: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val (initialHour, initialMinute) = remember(initialTime) {
        val parts = initialTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        hour to minute
    }

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Reminder Time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                TimePicker(state = timePickerState)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(onClick = {
                        val hour = timePickerState.hour.toString().padStart(2, '0')
                        val minute = timePickerState.minute.toString().padStart(2, '0')
                        onConfirm("$hour:$minute")
                    }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateOption(
    template: HabitTemplate,
    onSelected: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onSelected() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(template.color.toColorInt()).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = template.icon, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = template.name,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun IconOption(
    icon: String,
    isSelected: Boolean,
    onIconSelected: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable { onIconSelected() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, fontSize = 20.sp)
    }
}
