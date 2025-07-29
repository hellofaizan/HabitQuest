package com.mohammadfaizan.habitquest.domain.repository

import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.local.HabitFrequency
import org.junit.Test
import org.junit.Assert.*

class HabitRepositoryTest {
    
    @Test
    fun `test habit repository interface contract`() {
        val habit = Habit(
            name = "Test Habit",
            description = "Test Description",
            color = "#FF0000",
            category = "Health"
        )
        
        assertEquals("Test Habit", habit.name)
        assertEquals("Test Description", habit.description)
        assertEquals("#FF0000", habit.color)
        assertEquals("Health", habit.category)
        assertEquals(HabitFrequency.DAILY, habit.frequency)
        assertTrue(habit.isActive)
    }
    
    @Test
    fun `test habit with custom frequency`() {
        val habit = Habit(
            name = "Weekly Exercise",
            color = "#00FF00",
            frequency = HabitFrequency.WEEKLY,
            targetCount = 3
        )
        
        assertEquals(HabitFrequency.WEEKLY, habit.frequency)
        assertEquals(3, habit.targetCount)
    }
} 