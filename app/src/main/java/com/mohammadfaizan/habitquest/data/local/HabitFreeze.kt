package com.mohammadfaizan.habitquest.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// A frozen day counts toward streak continuity without being a real completion — lets a
// habit survive a missed day without breaking its streak.
@Entity(
    tableName = "habit_freezes",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habitId"]),
        Index(value = ["dateKey"]),
        Index(value = ["habitId", "dateKey"], unique = true)
    ]
)
data class HabitFreeze(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val dateKey: String // YYYY-MM-DD format
)
