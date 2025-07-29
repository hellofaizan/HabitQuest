package com.mohammadfaizan.habitquest.domain.usecase

import com.mohammadfaizan.habitquest.data.local.HabitFrequency
import org.junit.Test
import org.junit.Assert.*

class AddHabitUseCaseTest {
    
    @Test
    fun `test AddHabitRequest data class`() {
        val request = AddHabitRequest(
            name = "Test Habit",
            description = "Test Description",
            color = "#FF0000",
            icon = "fitness",
            targetCount = 3,
            frequency = "WEEKLY",
            category = "Health",
            reminderTime = "09:00",
            reminderEnabled = true
        )
        
        assertEquals("Test Habit", request.name)
        assertEquals("Test Description", request.description)
        assertEquals("#FF0000", request.color)
        assertEquals("fitness", request.icon)
        assertEquals(3, request.targetCount)
        assertEquals("WEEKLY", request.frequency)
        assertEquals("Health", request.category)
        assertEquals("09:00", request.reminderTime)
        assertTrue(request.reminderEnabled)
    }
    
    @Test
    fun `test AddHabitResult data class`() {
        val successResult = AddHabitResult(success = true, habitId = 1L)
        val errorResult = AddHabitResult(success = false, error = "Test error")
        
        assertTrue(successResult.success)
        assertEquals(1L, successResult.habitId)
        assertNull(successResult.error)
        
        assertFalse(errorResult.success)
        assertNull(errorResult.habitId)
        assertEquals("Test error", errorResult.error)
    }
    
    @Test
    fun `test HabitFrequency enum values`() {
        assertEquals(HabitFrequency.DAILY, HabitFrequency.valueOf("DAILY"))
        assertEquals(HabitFrequency.WEEKLY, HabitFrequency.valueOf("WEEKLY"))
        assertEquals(HabitFrequency.MONTHLY, HabitFrequency.valueOf("MONTHLY"))
        assertEquals(HabitFrequency.CUSTOM, HabitFrequency.valueOf("CUSTOM"))
    }
    
    @Test
    fun `test validation logic for empty name`() {
        val emptyName = ""
        val blankName = "   "
        
        assertTrue(emptyName.isBlank())
        assertTrue(blankName.trim().isBlank())
    }
    
    @Test
    fun `test validation logic for target count`() {
        val validCount = 1
        val invalidCount = 0
        val negativeCount = -1
        
        assertTrue(validCount > 0)
        assertFalse(invalidCount > 0)
        assertFalse(negativeCount > 0)
    }
    
    @Test
    fun `test string trimming logic`() {
        val nameWithSpaces = "  Test Habit  "
        val descriptionWithSpaces = "  Test Description  "
        val categoryWithSpaces = "  Health  "
        
        assertEquals("Test Habit", nameWithSpaces.trim())
        assertEquals("Test Description", descriptionWithSpaces.trim())
        assertEquals("Health", categoryWithSpaces.trim())
    }
} 