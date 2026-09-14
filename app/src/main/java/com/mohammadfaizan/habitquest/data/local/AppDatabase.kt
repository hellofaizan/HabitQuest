package com.mohammadfaizan.habitquest.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AppPreferences::class,
        Habit::class,
        HabitCompletion::class,
        HabitFreeze::class
    ],
    version = 4
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appPreferencesDao(): AppPreferencesDao
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun habitFreezeDao(): HabitFreezeDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habits ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Defaults to every day, preserving existing reminders' current behavior.
                db.execSQL("ALTER TABLE habits ADD COLUMN reminderDays TEXT NOT NULL DEFAULT '1,2,3,4,5,6,7'")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habits ADD COLUMN freezesAvailable INTEGER NOT NULL DEFAULT 3")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `habit_freezes` (" +
                        "`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                        "`habitId` INTEGER NOT NULL, " +
                        "`dateKey` TEXT NOT NULL, " +
                        "FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_habit_freezes_habitId` ON `habit_freezes` (`habitId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_habit_freezes_dateKey` ON `habit_freezes` (`dateKey`)")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_habit_freezes_habitId_dateKey` " +
                        "ON `habit_freezes` (`habitId`, `dateKey`)"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app-db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build().also { instance = it }
            }
        }
    }
}