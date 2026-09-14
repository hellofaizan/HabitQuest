package com.mohammadfaizan.habitquest.utils

data class HabitTemplate(
    val name: String,
    val description: String,
    val icon: String,
    val color: String,
    val category: String,
    val targetCount: Int = 1
)

// A curated "quick start" list for new users — tapping one pre-fills the Add Habit form
// (name/icon/color/category/target), reusing the same palette already validated for the
// color picker and the same emoji set already offered in the icon picker.
val HABIT_TEMPLATES = listOf(
    HabitTemplate("Exercise", "Get moving for at least 20 minutes", "🏃", "#2a78d6", "Fitness"),
    HabitTemplate("Meditate", "Take a few quiet minutes to reset", "🧘", "#4a3aa7", "Mindfulness"),
    HabitTemplate("Read", "Read a few pages of a book", "📚", "#eb6834", "Learning"),
    HabitTemplate("Drink Water", "Stay hydrated through the day", "💧", "#1baf7a", "Health", targetCount = 8),
    HabitTemplate("Sleep 8 Hours", "Aim for a full night's rest", "😴", "#2a78d6", "Health"),
    HabitTemplate("Eat Healthy", "Choose a nutritious meal", "🥗", "#008300", "Health"),
    HabitTemplate("Journal", "Write down today's thoughts", "✍️", "#e87ba4", "Creative"),
    HabitTemplate("Cycle", "Go for a bike ride", "🚴", "#1baf7a", "Fitness"),
    HabitTemplate("Gratitude", "Note something you're grateful for", "🙏", "#eda100", "Mindfulness"),
    HabitTemplate("Clean Up", "Tidy a space for a few minutes", "🧹", "#e34948", "Productivity"),
    HabitTemplate("Track Expenses", "Log today's spending", "💰", "#4a3aa7", "Finance"),
    HabitTemplate("No Smoking", "Stay smoke-free today", "🚭", "#e34948", "Health")
)
