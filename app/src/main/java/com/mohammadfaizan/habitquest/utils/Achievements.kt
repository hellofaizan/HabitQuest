package com.mohammadfaizan.habitquest.utils

object Achievements {
    val STREAK_MILESTONES = listOf(7, 30, 100, 365)
    val COMPLETION_MILESTONES = listOf(10, 50, 100, 500)

    // The highest milestone crossed by going from previousStreak to newStreak in one completion,
    // or null if none was crossed (e.g. newStreak already past it before this completion).
    fun streakMilestoneCrossed(previousStreak: Int, newStreak: Int): Int? {
        return STREAK_MILESTONES.lastOrNull { it in (previousStreak + 1)..newStreak }
    }

    private val motivationalMessages = listOf(
        "Nice work! 💪",
        "Keep it up!",
        "You're on a roll! 🔥",
        "Consistency wins!",
        "One step closer to your goal!",
        "Great job today!",
        "Building momentum!",
        "Habit checked! ✅"
    )

    fun randomMotivationalMessage(): String {
        return motivationalMessages.random()
    }

    fun milestoneMessage(streak: Int): String {
        return "🎉 $streak-day streak! Amazing work!"
    }
}
