package com.mohammadfaizan.habitquest.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mohammadfaizan.habitquest.R
import com.mohammadfaizan.habitquest.ui.theme.Shapes
import com.mohammadfaizan.habitquest.ui.viewmodel.HabitViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
    habitViewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        coroutineScope {
            val loadHabits = async { habitViewModel.loadHabits() }
            val loadCompletions = async { habitViewModel.loadHabitsWithCompletionStatus() }

            loadHabits.await()
            loadCompletions.await()

            delay(100)

            val minDelay = async { delay(400) }
            minDelay.await()

            onSplashComplete()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.applogo),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .clip(Shapes.roundedXxl)
        )
    }
}
