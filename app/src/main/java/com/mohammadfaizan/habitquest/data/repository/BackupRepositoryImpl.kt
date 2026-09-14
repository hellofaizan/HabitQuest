package com.mohammadfaizan.habitquest.data.repository

import androidx.room.withTransaction
import com.mohammadfaizan.habitquest.data.local.AppDatabase
import com.mohammadfaizan.habitquest.data.local.AppPreferences
import com.mohammadfaizan.habitquest.data.local.AppPreferencesDao
import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.local.HabitCompletion
import com.mohammadfaizan.habitquest.data.local.HabitCompletionDao
import com.mohammadfaizan.habitquest.data.local.HabitDao
import com.mohammadfaizan.habitquest.data.local.HabitFreeze
import com.mohammadfaizan.habitquest.data.local.HabitFreezeDao
import com.mohammadfaizan.habitquest.data.local.HabitFrequency
import com.mohammadfaizan.habitquest.domain.repository.BackupRepository
import com.mohammadfaizan.habitquest.domain.repository.BackupSummary
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val BACKUP_FORMAT_VERSION = 1

class BackupRepositoryImpl(
    private val db: AppDatabase,
    private val habitDao: HabitDao,
    private val habitCompletionDao: HabitCompletionDao,
    private val habitFreezeDao: HabitFreezeDao,
    private val appPreferencesDao: AppPreferencesDao
) : BackupRepository {

    override suspend fun exportBackup(): String {
        val habits = habitDao.getAllHabitsSnapshot()
        val completions = habitCompletionDao.getAllCompletionsSnapshot()
        val freezes = habitFreezeDao.getAllFreezesSnapshot()
        val preferences = appPreferencesDao.getPreferences()

        val root = JSONObject().apply {
            put("formatVersion", BACKUP_FORMAT_VERSION)
            put("exportedAt", System.currentTimeMillis())
            put("habits", JSONArray(habits.map { it.toJson() }))
            put("completions", JSONArray(completions.map { it.toJson() }))
            put("freezes", JSONArray(freezes.map { it.toJson() }))
            put("hasSeenOnboarding", preferences?.hasSeenOnboarding ?: true)
        }
        return root.toString(2)
    }

    override suspend fun importBackup(json: String): BackupSummary {
        val root = try {
            JSONObject(json)
        } catch (e: Exception) {
            throw IllegalArgumentException("This file isn't a valid Habit Quest backup", e)
        }

        val habitsJson = root.optJSONArray("habits")
            ?: throw IllegalArgumentException("This file isn't a valid Habit Quest backup")
        val completionsJson = root.optJSONArray("completions") ?: JSONArray()
        val freezesJson = root.optJSONArray("freezes") ?: JSONArray()

        val habits = (0 until habitsJson.length()).map { habitsJson.getJSONObject(it).toHabit() }
        val completions = (0 until completionsJson.length()).map { completionsJson.getJSONObject(it).toCompletion() }
        val freezes = (0 until freezesJson.length()).map { freezesJson.getJSONObject(it).toFreeze() }

        db.withTransaction {
            habitDao.deleteAllHabits()
            habitCompletionDao.deleteAllCompletionsTable()
            habitFreezeDao.deleteAllFreezesTable()

            habitDao.insertHabits(habits)
            habitCompletionDao.insertCompletions(completions)
            habitFreezeDao.insertFreezes(freezes)

            if (root.has("hasSeenOnboarding")) {
                appPreferencesDao.insertPreferences(
                    AppPreferences(hasSeenOnboarding = root.optBoolean("hasSeenOnboarding", true))
                )
            }
        }

        return BackupSummary(
            habitCount = habits.size,
            completionCount = completions.size,
            freezeCount = freezes.size
        )
    }

    // A flat, spreadsheet-friendly log — unlike exportBackup(), this isn't meant to be
    // restorable, just readable in Excel/Sheets: one row per completion instead of the
    // JSON backup's relational habit/completion/freeze structure.
    override suspend fun exportCsv(): String {
        val habitsById = habitDao.getAllHabitsSnapshot().associateBy { it.id }
        val completions = habitCompletionDao.getAllCompletionsSnapshot()
            .sortedWith(
                compareByDescending<HabitCompletion> { it.dateKey }
                    .thenBy { habitsById[it.habitId]?.name ?: "" }
            )
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val builder = StringBuilder()
        builder.append("Habit,Category,Date,Time,Notes,Has Photo\n")
        for (completion in completions) {
            val habit = habitsById[completion.habitId]
            val row = listOf(
                habit?.name ?: "Unknown",
                habit?.category ?: "",
                completion.dateKey,
                timeFormat.format(completion.completedAt),
                completion.notes ?: "",
                if (completion.photoPath != null) "Yes" else "No"
            )
            builder.append(row.joinToString(",") { csvEscape(it) })
            builder.append("\n")
        }
        return builder.toString()
    }
}

private fun csvEscape(value: String): String {
    return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
        "\"" + value.replace("\"", "\"\"") + "\""
    } else {
        value
    }
}

private fun Habit.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("name", name)
    put("description", description)
    put("color", color)
    put("icon", icon)
    put("targetCount", targetCount)
    put("frequency", frequency.name)
    put("reminderTime", reminderTime)
    put("reminderEnabled", reminderEnabled)
    put("reminderDays", reminderDays)
    put("isActive", isActive)
    put("sortOrder", sortOrder)
    put("currentStreak", currentStreak)
    put("longestStreak", longestStreak)
    put("totalCompletion", totalCompletion)
    put("freezesAvailable", freezesAvailable)
    put("createdAt", createdAt.time)
    put("updatedAt", updatedAt.time)
    put("category", category)
}

private fun JSONObject.toHabit(): Habit = Habit(
    id = getLong("id"),
    name = getString("name"),
    description = optStringOrNull("description"),
    color = getString("color"),
    icon = optStringOrNull("icon"),
    targetCount = optInt("targetCount", 1),
    frequency = HabitFrequency.valueOf(optString("frequency", HabitFrequency.DAILY.name)),
    reminderTime = optStringOrNull("reminderTime"),
    reminderEnabled = optBoolean("reminderEnabled", false),
    reminderDays = optString("reminderDays", "1,2,3,4,5,6,7"),
    isActive = optBoolean("isActive", true),
    sortOrder = optInt("sortOrder", 0),
    currentStreak = optInt("currentStreak", 0),
    longestStreak = optInt("longestStreak", 0),
    totalCompletion = optInt("totalCompletion", 0),
    freezesAvailable = optInt("freezesAvailable", 3),
    createdAt = Date(optLong("createdAt", System.currentTimeMillis())),
    updatedAt = Date(optLong("updatedAt", System.currentTimeMillis())),
    category = optStringOrNull("category")
)

private fun HabitCompletion.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("habitId", habitId)
    put("completedAt", completedAt.time)
    put("notes", notes)
    // Points at a file under this device's private storage — restoring on a different device
    // (or after a fresh install) won't have that file, so the photo just won't load. The path
    // itself is still worth keeping for same-device restores.
    put("photoPath", photoPath)
    put("dateKey", dateKey)
}

private fun JSONObject.toCompletion(): HabitCompletion = HabitCompletion(
    id = getLong("id"),
    habitId = getLong("habitId"),
    completedAt = Date(optLong("completedAt", System.currentTimeMillis())),
    notes = optStringOrNull("notes"),
    photoPath = optStringOrNull("photoPath"),
    dateKey = getString("dateKey")
)

private fun HabitFreeze.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("habitId", habitId)
    put("dateKey", dateKey)
}

private fun JSONObject.toFreeze(): HabitFreeze = HabitFreeze(
    id = getLong("id"),
    habitId = getLong("habitId"),
    dateKey = getString("dateKey")
)

// org.json's put(key, null) omits the key entirely rather than storing a JSON null, so a
// missing key is the normal way a nullable field round-trips here.
private fun JSONObject.optStringOrNull(key: String): String? {
    if (!has(key) || isNull(key)) return null
    return optString(key)
}
