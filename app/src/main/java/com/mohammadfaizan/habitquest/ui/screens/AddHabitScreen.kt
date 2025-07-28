package com.mohammadfaizan.habitquest.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.mohammadfaizan.habitquest.ui.components.InputField
import com.mohammadfaizan.habitquest.ui.components.CategoryChips
import com.mohammadfaizan.habitquest.ui.components.ColorOption
import com.mohammadfaizan.habitquest.ui.viewmodel.AddHabitViewModel

@Composable
fun AddHabitScreen(
    onBack: () -> Unit,
    onCreateHabit: (name: String, description: String?, color: String, category: String?, frequency: String, targetCount: Int, reminderEnabled: Boolean, reminderTime: String?) -> Unit = { _, _, _, _, _, _, _, _ -> },
    modifier: Modifier = Modifier,
    viewModel: AddHabitViewModel? = null
) {
    BackHandler(onBack = onBack)

    val formState by viewModel?.formState?.collectAsState() ?: remember { mutableStateOf(null) }
    val validation by viewModel?.validation?.collectAsState() ?: remember { mutableStateOf(null) }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#FF0000") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf("DAILY") }
    var targetCount by remember { mutableStateOf(1) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf("09:00") }

    val currentName = formState?.name ?: name
    val currentDescription = formState?.description ?: description
    val currentColor = formState?.color ?: selectedColor
    val currentCategory = formState?.category ?: selectedCategory
    val currentFrequency = formState?.frequency ?: selectedFrequency
    val currentTargetCount = formState?.targetCount ?: targetCount
    val currentReminderEnabled = formState?.reminderEnabled ?: reminderEnabled
    val currentReminderTime = formState?.reminderTime ?: reminderTime
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

    val updateCategory = { newCategory: String ->
        if (viewModel != null) {
            viewModel.updateCategory(newCategory)
        } else {
            selectedCategory = newCategory
        }
    }

    val updateFrequency = { newFrequency: String ->
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

    val updateReminderEnabled = { enabled: Boolean ->
        if (viewModel != null) {
            viewModel.updateReminderEnabled(enabled)
        } else {
            reminderEnabled = enabled
        }
    }

    val updateReminderTime = { time: String ->
        if (viewModel != null) {
            viewModel.updateReminderTime(time)
        } else {
            reminderTime = time
        }
    }

    // Available color options
    val availableColors = viewModel?.getAvailableColors() ?: listOf(
        "#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF",
        "#00FFFF", "#FFA500", "#800080", "#008000", "#FFC0CB"
    )

    val availableCategories = viewModel?.getAvailableCategories() ?: listOf(
        "Health", "Fitness", "Learning", "Productivity",
        "Mindfulness", "Social", "Finance", "Creative", "Other"
    )

    val availableFrequencies =
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
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go Back"
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "Create New Habit",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
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
                onClick = { if (currentTargetCount > 1) updateTargetCount(currentTargetCount - 1) },
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
                onClick = { updateTargetCount(currentTargetCount + 1) },
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

            OutlinedTextField(
                value = currentReminderTime,
                onValueChange = updateReminderTime,
                label = { Text("Reminder Time") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
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
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid
        ) {
            Text("Create Habit")
        }
    }
}