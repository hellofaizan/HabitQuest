package com.mohammadfaizan.habitquest.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.mohammadfaizan.habitquest.data.local.AppDatabase
import com.mohammadfaizan.habitquest.data.repository.HabitCompletionRepositoryImpl
import com.mohammadfaizan.habitquest.data.repository.HabitManagementRepositoryImpl
import com.mohammadfaizan.habitquest.data.repository.HabitRepositoryImpl
import com.mohammadfaizan.habitquest.domain.usecase.CompleteHabitUseCase

// Manual DAO/repository wiring, same pattern as StreakResetReceiver — widget action callbacks
// run outside any Activity/ViewModel scope, so there's no existing repository instance to reuse.
suspend fun completeHabitFromWidget(context: Context, habitId: Long) {
    val db = AppDatabase.getInstance(context)
    val habitRepo = HabitRepositoryImpl(db.habitDao())
    val completionRepo = HabitCompletionRepositoryImpl(db.habitCompletionDao(), db.habitFreezeDao())
    val managementRepo = HabitManagementRepositoryImpl(habitRepo, completionRepo, db)
    CompleteHabitUseCase(managementRepo).completeHabitForToday(habitId)
}

object HabitWidgetUpdater {
    // Refreshes every placed instance of both widget variants — cheap enough (a couple of
    // DB reads per widget) to call unconditionally after any in-app action that could change
    // what a widget shows: completing/undoing a habit, freezing a streak, completing all.
    suspend fun updateAll(context: Context) {
        HabitWidget3Day().updateAll(context)
        HabitWidgetMonth().updateAll(context)
    }
}
