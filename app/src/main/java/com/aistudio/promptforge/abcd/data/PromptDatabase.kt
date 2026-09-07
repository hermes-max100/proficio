package com.aistudio.promptforge.abcd.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
        LlmCredentialEntity::class,
        DurableRunEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class PromptDatabase : RoomDatabase() {
    abstract fun promptDao(): PromptDao

    companion object {
        @Volatile
        private var Instance: PromptDatabase? = null

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `durable_runs` (
                        `id` TEXT NOT NULL,
                        `idempotencyKey` TEXT NOT NULL,
                        `goalTitle` TEXT NOT NULL,
                        `goalInput` TEXT NOT NULL,
                        `selectedModel` TEXT NOT NULL,
                        `state` TEXT NOT NULL,
                        `currentStepIndex` INTEGER NOT NULL,
                        `totalSteps` INTEGER NOT NULL,
                        `currentStepName` TEXT NOT NULL,
                        `retryCount` INTEGER NOT NULL,
                        `maxRetries` INTEGER NOT NULL,
                        `lastError` TEXT,
                        `lastErrorCode` TEXT,
                        `suggestedAction` TEXT,
                        `checkpointDataJson` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `completedAt` INTEGER,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): PromptDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, PromptDatabase::class.java, "autoforge_database")
                    .addMigrations(MIGRATION_6_7)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
