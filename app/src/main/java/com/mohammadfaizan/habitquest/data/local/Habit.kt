package com.mohammadfaizan.habitquest.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val color: String,
    val icon: String? = null,
    val targetCount: Int = 1,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val reminderTime: String? = null,
    val reminderEnabled: Boolean = false,
    // Comma-separated java.util.Calendar.DAY_OF_WEEK values (1=Sunday..7=Saturday); defaults to every day.
    val reminderDays: String = "1,2,3,4,5,6,7",
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCompletion: Int = 0,
    val freezesAvailable: Int = 3,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),

    val category: String? = null,
)

enum class HabitFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM
}