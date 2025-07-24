package com.mohammadfaizan.habitquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.Room
import com.mohammadfaizan.habitquest.data.local.AppDatabase
import com.mohammadfaizan.habitquest.domain.repository.PreferencesRepositoryImpl
import com.mohammadfaizan.habitquest.ui.onbording.OnboardingScreen
import com.mohammadfaizan.habitquest.ui.theme.HabitQuestTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitQuestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val context = this
                    val db = remember {
                        Room.databaseBuilder(
                            context,
                            AppDatabase::class.java,
                            "app-db"
                        ).build()
                    }
                    val repo = remember { PreferencesRepositoryImpl(db.appPreferencesDao()) }
                    var hasSeenOnboarding by remember { mutableStateOf<Boolean?>(null) }
                    val scope = rememberCoroutineScope()

                    LaunchedEffect(Unit) {
                        hasSeenOnboarding = repo.hasSeenOnboarding()
                    }

                    when (hasSeenOnboarding) {
                        null -> {
                            // Loading state (optional)
                            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                                Text(
                                    text = "Loading...",
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }

                        false -> {
                            OnboardingScreen(
                                onContinue = {
                                    scope.launch {
                                        repo.setOnboardingSeen()
                                        hasSeenOnboarding = true
                                    }
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        else -> {
                            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                                Greeting(
                                    name = "Android",
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HabitQuestTheme {
        Greeting("Android")
    }
}