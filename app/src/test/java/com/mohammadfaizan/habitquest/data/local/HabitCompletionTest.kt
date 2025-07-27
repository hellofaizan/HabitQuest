package com.mohammadfaizan.habitquest.data.local

import org.junit.Test
import org.junit.Assert.*

class HabitCompletionTest {
    
    @Test
    fun `test habit completion creation`() {
        val completion = HabitCompletion(
            habitId = 1L,
            notes = "Test completion",
            dateKey = "2024-01-01"
        )
        
        assertEquals(1L, completion.habitId)
        assertEquals("Test completion", completion.notes)
        assertEquals("2024-01-01", completion.dateKey)
    }
    
    @Test
    fun `test habit completion with default values`() {
        val completion = HabitCompletion(
            habitId = 1L,
            dateKey = "2024-01-01"
        )
        
        assertEquals(1L, completion.habitId)
        assertEquals("2024-01-01", completion.dateKey)
        assertNotNull(completion.completedAt)
    }
} 