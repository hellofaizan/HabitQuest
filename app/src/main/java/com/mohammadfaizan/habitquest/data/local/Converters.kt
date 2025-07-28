package com.mohammadfaizan.habitquest.data.local

import androidx.room.TypeConverter
import java.util.Date

class Converters {

    @TypeConverter
    fun fromDate(value: Date?): Long? {
        return value?.time
    }

    @TypeConverter
    fun toDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun fromHabitFrequency(frequency: HabitFrequency): String {
        return frequency.name
    }

    @TypeConverter
    fun toHabitFrequency(value: String): HabitFrequency {
        return HabitFrequency.valueOf(value)
    }
} 