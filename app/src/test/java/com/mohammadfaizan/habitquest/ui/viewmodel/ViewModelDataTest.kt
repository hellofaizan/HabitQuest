package com.mohammadfaizan.habitquest.ui.viewmodel

import org.junit.Test
import org.junit.Assert.*

class ViewModelDataTest {
    
    @Test
    fun `test HabitUiState data class`() {
        val uiState = HabitUiState(
            habits = emptyList(),
            habitsWithCompletionStatus = emptyList(),
            error = "Test error",
            selectedHabit = null,
            dataLoaded = true
        )
        
        assertTrue(uiState.habits.isEmpty())
        assertTrue(uiState.habitsWithCompletionStatus.isEmpty())
        assertEquals("Test error", uiState.error)
        assertNull(uiState.selectedHabit)
        assertTrue(uiState.dataLoaded)
    }
    
    @Test
    fun `test HabitAction data class`() {
        val action = HabitAction(HabitActionType.ADD_HABIT, 1L)
        
        assertEquals(HabitActionType.ADD_HABIT, action.type)
        assertEquals(1L, action.data)
    }
    
    @Test
    fun `test HabitActionType enum`() {
        assertEquals(HabitActionType.ADD_HABIT, HabitActionType.valueOf("ADD_HABIT"))
        assertEquals(HabitActionType.COMPLETE_HABIT, HabitActionType.valueOf("COMPLETE_HABIT"))
        assertEquals(HabitActionType.DELETE_HABIT, HabitActionType.valueOf("DELETE_HABIT"))
        assertEquals(HabitActionType.REFRESH_HABITS, HabitActionType.valueOf("REFRESH_HABITS"))
        assertEquals(HabitActionType.SELECT_HABIT, HabitActionType.valueOf("SELECT_HABIT"))
        assertEquals(HabitActionType.SEARCH_HABITS, HabitActionType.valueOf("SEARCH_HABITS"))
        assertEquals(HabitActionType.FILTER_BY_CATEGORY, HabitActionType.valueOf("FILTER_BY_CATEGORY"))
    }
    
    @Test
    fun `test AnalyticsUiState data class`() {
        val uiState = AnalyticsUiState(
            isLoading = false,
            analytics = null,
            habitStats = null,
            selectedHabitId = 1L,
            selectedWeekStart = "2024-01-01",
            selectedMonth = "2024-01",
            error = null,
            isRefreshing = true
        )
        
        assertFalse(uiState.isLoading)
        assertNull(uiState.analytics)
        assertNull(uiState.habitStats)
        assertEquals(1L, uiState.selectedHabitId)
        assertEquals("2024-01-01", uiState.selectedWeekStart)
        assertEquals("2024-01", uiState.selectedMonth)
        assertNull(uiState.error)
        assertTrue(uiState.isRefreshing)
    }
    
    @Test
    fun `test AnalyticsAction data class`() {
        val action = AnalyticsAction(AnalyticsActionType.LOAD_OVERALL_ANALYTICS, "data")
        
        assertEquals(AnalyticsActionType.LOAD_OVERALL_ANALYTICS, action.type)
        assertEquals("data", action.data)
    }
    
    @Test
    fun `test AnalyticsActionType enum`() {
        assertEquals(AnalyticsActionType.LOAD_OVERALL_ANALYTICS, AnalyticsActionType.valueOf("LOAD_OVERALL_ANALYTICS"))
        assertEquals(AnalyticsActionType.LOAD_HABIT_ANALYTICS, AnalyticsActionType.valueOf("LOAD_HABIT_ANALYTICS"))
        assertEquals(AnalyticsActionType.LOAD_WEEKLY_ANALYTICS, AnalyticsActionType.valueOf("LOAD_WEEKLY_ANALYTICS"))
        assertEquals(AnalyticsActionType.LOAD_MONTHLY_ANALYTICS, AnalyticsActionType.valueOf("LOAD_MONTHLY_ANALYTICS"))
        assertEquals(AnalyticsActionType.SELECT_HABIT, AnalyticsActionType.valueOf("SELECT_HABIT"))
        assertEquals(AnalyticsActionType.SELECT_WEEK, AnalyticsActionType.valueOf("SELECT_WEEK"))
        assertEquals(AnalyticsActionType.SELECT_MONTH, AnalyticsActionType.valueOf("SELECT_MONTH"))
    }
    
    @Test
    fun `test AddHabitFormState data class`() {
        val formState = AddHabitFormState(
            name = "Test Habit",
            description = "Test Description",
            color = "#FF0000",
            category = "Health",
            frequency = "DAILY",
            targetCount = 3,
            reminderEnabled = true,
            reminderTime = "09:00",
            isLoading = false,
            error = null,
            isSuccess = true
        )
        
        assertEquals("Test Habit", formState.name)
        assertEquals("Test Description", formState.description)
        assertEquals("#FF0000", formState.color)
        assertEquals("Health", formState.category)
        assertEquals("DAILY", formState.frequency)
        assertEquals(3, formState.targetCount)
        assertTrue(formState.reminderEnabled)
        assertEquals("09:00", formState.reminderTime)
        assertFalse(formState.isLoading)
        assertNull(formState.error)
        assertTrue(formState.isSuccess)
    }
    
    @Test
    fun `test AddHabitFormValidation data class`() {
        val validation = AddHabitFormValidation(
            isNameValid = true,
            isTargetCountValid = false,
            isColorValid = true,
            isFormValid = false
        )
        
        assertTrue(validation.isNameValid)
        assertFalse(validation.isTargetCountValid)
        assertTrue(validation.isColorValid)
        assertFalse(validation.isFormValid)
    }
    
    @Test
    fun `test AddHabitAction data class`() {
        val action = AddHabitAction(AddHabitActionType.HABIT_CREATED, 1L)
        
        assertEquals(AddHabitActionType.HABIT_CREATED, action.type)
        assertEquals(1L, action.data)
    }
    
    @Test
    fun `test AddHabitActionType enum`() {
        assertEquals(AddHabitActionType.HABIT_CREATED, AddHabitActionType.valueOf("HABIT_CREATED"))
        assertEquals(AddHabitActionType.VALIDATION_ERROR, AddHabitActionType.valueOf("VALIDATION_ERROR"))
        assertEquals(AddHabitActionType.NETWORK_ERROR, AddHabitActionType.valueOf("NETWORK_ERROR"))
    }
    
    @Test
    fun `test SharedUiState data class`() {
        val uiState = SharedUiState(
            currentScreen = "analytics",
            showAddHabitScreen = true,
            showAnalyticsScreen = false,
            selectedHabitId = 1L,
            isLoading = true,
            error = "Test error",
            snackbarMessage = "Test message"
        )
        
        assertEquals("analytics", uiState.currentScreen)
        assertTrue(uiState.showAddHabitScreen)
        assertFalse(uiState.showAnalyticsScreen)
        assertEquals(1L, uiState.selectedHabitId)
        assertTrue(uiState.isLoading)
        assertEquals("Test error", uiState.error)
        assertEquals("Test message", uiState.snackbarMessage)
    }
    
    @Test
    fun `test SharedAction data class`() {
        val action = SharedAction(SharedActionType.NAVIGATE_TO_SCREEN, "home")
        
        assertEquals(SharedActionType.NAVIGATE_TO_SCREEN, action.type)
        assertEquals("home", action.data)
    }
    
    @Test
    fun `test SharedActionType enum`() {
        assertEquals(SharedActionType.NAVIGATE_TO_SCREEN, SharedActionType.valueOf("NAVIGATE_TO_SCREEN"))
        assertEquals(SharedActionType.SHOW_ADD_HABIT_SCREEN, SharedActionType.valueOf("SHOW_ADD_HABIT_SCREEN"))
        assertEquals(SharedActionType.HIDE_ADD_HABIT_SCREEN, SharedActionType.valueOf("HIDE_ADD_HABIT_SCREEN"))
        assertEquals(SharedActionType.SHOW_ANALYTICS_SCREEN, SharedActionType.valueOf("SHOW_ANALYTICS_SCREEN"))
        assertEquals(SharedActionType.HIDE_ANALYTICS_SCREEN, SharedActionType.valueOf("HIDE_ANALYTICS_SCREEN"))
        assertEquals(SharedActionType.SELECT_HABIT, SharedActionType.valueOf("SELECT_HABIT"))
        assertEquals(SharedActionType.SHOW_SNACKBAR, SharedActionType.valueOf("SHOW_SNACKBAR"))
        assertEquals(SharedActionType.HIDE_SNACKBAR, SharedActionType.valueOf("HIDE_SNACKBAR"))
        assertEquals(SharedActionType.SHOW_ERROR, SharedActionType.valueOf("SHOW_ERROR"))
        assertEquals(SharedActionType.HIDE_ERROR, SharedActionType.valueOf("HIDE_ERROR"))
    }
} 