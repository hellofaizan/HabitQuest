package com.mohammadfaizan.habitquest.domain.usecase

import com.mohammadfaizan.habitquest.data.local.HabitCompletion
import com.mohammadfaizan.habitquest.domain.repository.HabitCompletionRepository
import com.mohammadfaizan.habitquest.domain.repository.HabitRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class GenerateRandomDataResult(
    val success: Boolean,
    val habitsProcessed: Int = 0,
    val totalCompletionsGenerated: Int = 0,
    val error: String? = null
)

class GenerateRandomDataUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val habitCompletionRepository: HabitCompletionRepository
) {

    suspend operator fun invoke(
        days: Int = 182,
        completionProbability: Float = 0.7f
    ): GenerateRandomDataResult {
        return try {
            val activeHabits = habitRepository.getActiveHabits().first()
            
            if (activeHabits.isEmpty()) {
                return GenerateRandomDataResult(
                    success = false,
                    error = "No active habits found"
                )
            }

            var totalCompletions = 0
            val random = kotlin.random.Random.Default
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            // Generate completions for each habit
            activeHabits.forEach { habit ->
                val completions = mutableListOf<HabitCompletion>()

                // Generate completions for the past N days
                for (dayOffset in 0 until days) {
                    calendar.time = Date()
                    calendar.add(Calendar.DAY_OF_YEAR, -dayOffset)
                    val date = calendar.time
                    val dateKey = dateFormat.format(date)

                    // Randomly decide if this day should have completions
                    if (random.nextFloat() < completionProbability) {
                        // Generate 1 to targetCount completions per day
                        val completionsForDay = if (habit.targetCount == 1) {
                            1
                        } else {
                            // Random number between 1 and targetCount, with bias towards full completion
                            val rand = random.nextFloat()
                            when {
                                rand < 0.6f -> habit.targetCount // 60% chance of full completion
                                rand < 0.9f -> habit.targetCount - 1 // 30% chance of almost full
                                else -> {
                                    // 10% chance of partial - ensure at least 1
                                    if (habit.targetCount > 2) {
                                        random.nextInt(1, habit.targetCount)
                                    } else {
                                        1
                                    }
                                }
                            }
                        }

                        // Create multiple completions for the same day if needed
                        repeat(completionsForDay) {
                            // Add some randomness to the time within the day
                            calendar.set(Calendar.HOUR_OF_DAY, random.nextInt(6, 22))
                            calendar.set(Calendar.MINUTE, random.nextInt(0, 60))
                            val completionDate = calendar.time

                            completions.add(
                                HabitCompletion(
                                    habitId = habit.id,
                                    completedAt = completionDate,
                                    dateKey = dateKey,
                                    notes = null
                                )
                            )
                        }
                    }
                }

                // Insert all completions for this habit
                if (completions.isNotEmpty()) {
                    habitCompletionRepository.insertCompletions(completions)
                    totalCompletions += completions.size
                }
            }

            GenerateRandomDataResult(
                success = true,
                habitsProcessed = activeHabits.size,
                totalCompletionsGenerated = totalCompletions
            )

        } catch (e: Exception) {
            GenerateRandomDataResult(
                success = false,
                error = "Failed to generate random data: ${e.message}"
            )
        }
    }
}

