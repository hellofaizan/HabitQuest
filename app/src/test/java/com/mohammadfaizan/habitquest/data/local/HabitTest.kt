package com.mohammadfaizan.habitquest.data.local

import org.junit.Test
import org.junit.Assert.*

class HabitTest {
    
    @Test
    fun `test habit creation with default values`() {
        val habit = Habit(
            name = "Test Habit",
            description = "Test Description",
            color = "#FF0000"
        )
        
        assertEquals("Test Habit", habit.name)
        assertEquals("Test Description", habit.description)
        assertEquals("#FF0000", habit.color)
        assertEquals(HabitFrequency.DAILY, habit.frequency)
        assertEquals(1, habit.targetCount)
        assertTrue(habit.isActive)
        assertEquals(0, habit.currentStreak)
        assertEquals(0, habit.longestStreak)
        assertEquals(0, habit.totalCompletion)
    }
    
    @Test
    fun `test habit with custom values`() {
        val habit = Habit(
            name = "Custom Habit",
            description = "Custom Description",
            color = "#00FF00",
            icon = "fitness",
            targetCount = 3,
            frequency = HabitFrequency.WEEKLY,
            category = "Health"
        )
        
        assertEquals("Custom Habit", habit.name)
        assertEquals("Custom Description", habit.description)
        assertEquals("#00FF00", habit.color)
        assertEquals("fitness", habit.icon)
        assertEquals(3, habit.targetCount)
        assertEquals(HabitFrequency.WEEKLY, habit.frequency)
        assertEquals("Health", habit.category)
    }
} 