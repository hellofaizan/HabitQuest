package com.mohammadfaizan.habitquest.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mohammadfaizan.habitquest.ui.components.InputField

@Composable
fun AddHabitScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    BackHandler(onBack = onBack)
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        Column {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go Back")
            }

            Spacer(modifier = Modifier.height(16.dp))

            InputField(
                value = name,
                onValueChange = { name = it },
                label = "Habit Name",
                placeholder = "Enter Habit name",
                keyboardType = KeyboardType.Text
            )

            InputField(
                value = description,
                onValueChange = { description = it },
                label = "Habit Description",
                placeholder = "Enter Habit description",
                keyboardType = KeyboardType.Text
            )
        }
    }
}