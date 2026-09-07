package com.aistudio.promptforge.abcd.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AutoForgePack::class,
        SavedSkill::class,
        SavedMcp::class,
        SavedPrompt::class,
        PlaygroundRun::class,
        EvalCase::class,
        FavoritePrompt::class,
        PromptStat::class,
        PromptRevisionEntity::class,
        ExecutionProvenanceEntity::class,
        LlmCredentialEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class PromptDatabase : RoomDatabase() {
    abstract fun promptDao(): PromptDao

    companion object {
        @Volatile
        private var Instance: PromptDatabase? = null

        fun getDatabase(context: Context): PromptDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, PromptDatabase::class.java, "autoforge_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
