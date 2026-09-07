package com.aistudio.promptforge.abcd.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {
    // AutoForge Packs
    @Query("SELECT * FROM autoforge_packs ORDER BY createdAt DESC")
    fun getAllAutoForgePacks(): Flow<List<AutoForgePack>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutoForgePack(pack: AutoForgePack)

    @Query("DELETE FROM autoforge_packs WHERE id = :id")
    suspend fun deleteAutoForgePack(id: String)

    // Saved Skills
    @Query("SELECT * FROM saved_skills ORDER BY createdAt DESC")
    fun getAllSavedSkills(): Flow<List<SavedSkill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedSkill(skill: SavedSkill)

    @Query("DELETE FROM saved_skills WHERE id = :id")
    suspend fun deleteSavedSkill(id: String)

    // Saved MCPs
    @Query("SELECT * FROM saved_mcps ORDER BY createdAt DESC")
    fun getAllSavedMcps(): Flow<List<SavedMcp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedMcp(mcp: SavedMcp)

    @Query("DELETE FROM saved_mcps WHERE id = :id")
    suspend fun deleteSavedMcp(id: String)

    // Saved Prompts
    @Query("SELECT * FROM saved_prompts ORDER BY createdAt DESC")
    fun getAllSavedPrompts(): Flow<List<SavedPrompt>>

    @Query("SELECT * FROM saved_prompts WHERE title LIKE '%' || :query || '%' OR assembled LIKE '%' || :query || '%' OR frameworkId LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchSavedPrompts(query: String): Flow<List<SavedPrompt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPrompt(prompt: SavedPrompt)

    @Query("DELETE FROM saved_prompts WHERE id = :id")
    suspend fun deleteSavedPrompt(id: String)

    // Playground Runs
    @Query("SELECT * FROM playground_runs ORDER BY at DESC")
    fun getAllPlaygroundRuns(): Flow<List<PlaygroundRun>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaygroundRun(run: PlaygroundRun)

    @Query("DELETE FROM playground_runs")
    suspend fun clearPlaygroundRuns()

    // Eval Cases
    @Query("SELECT * FROM eval_cases")
    fun getAllEvalCases(): Flow<List<EvalCase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvalCase(evalCase: EvalCase)

    @Query("DELETE FROM eval_cases WHERE id = :id")
    suspend fun deleteEvalCase(id: String)

    // Favorite Prompts
    @Query("SELECT promptId FROM favorite_prompts")
    fun getAllFavoritePromptIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoritePrompt)

    @Query("DELETE FROM favorite_prompts WHERE promptId = :promptId")
    suspend fun deleteFavorite(promptId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_prompts WHERE promptId = :promptId)")
    suspend fun isFavorite(promptId: String): Boolean

    // Prompt Statistics
    @Query("SELECT * FROM prompt_stats")
    fun getAllPromptStats(): Flow<List<PromptStat>>

    @Query("SELECT * FROM prompt_stats WHERE promptId = :promptId")
    suspend fun getPromptStat(promptId: String): PromptStat?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePromptStat(stat: PromptStat)

    // Prompt Revisions
    @Query("SELECT * FROM prompt_revisions WHERE promptId = :promptId ORDER BY revisionNumber DESC")
    fun getRevisionsForPrompt(promptId: String): Flow<List<PromptRevisionEntity>>

    @Query("SELECT * FROM prompt_revisions WHERE promptId = :promptId AND isActive = 1 LIMIT 1")
    suspend fun getActiveRevision(promptId: String): PromptRevisionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevision(revision: PromptRevisionEntity)

    @Query("UPDATE prompt_revisions SET isActive = 0 WHERE promptId = :promptId")
    suspend fun deactivateAllRevisions(promptId: String)

    @Query("UPDATE prompt_revisions SET isActive = 1 WHERE id = :revisionId")
    suspend fun activateRevision(revisionId: String)

    @Query("DELETE FROM prompt_revisions WHERE promptId = :promptId")
    suspend fun deleteRevisionsForPrompt(promptId: String)

    // Execution Provenance
    @Query("SELECT * FROM execution_provenance ORDER BY timestamp DESC")
    fun getAllExecutionProvenance(): Flow<List<ExecutionProvenanceEntity>>

    @Query("SELECT * FROM execution_provenance WHERE promptId = :promptId ORDER BY timestamp DESC")
    fun getProvenanceForPrompt(promptId: String): Flow<List<ExecutionProvenanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExecutionProvenance(provenance: ExecutionProvenanceEntity)

    @Query("DELETE FROM execution_provenance")
    suspend fun clearAllExecutionProvenance()

    @Query("DELETE FROM execution_provenance WHERE id = :id")
    suspend fun deleteExecutionProvenanceById(id: String)

    // LLM Credentials & BYOK / OAuth
    @Query("SELECT * FROM llm_credentials ORDER BY createdAt DESC")
    fun getAllLlmCredentials(): Flow<List<LlmCredentialEntity>>

    @Query("SELECT * FROM llm_credentials WHERE isActive = 1 LIMIT 1")
    fun getActiveLlmCredential(): Flow<LlmCredentialEntity?>

    @Query("SELECT * FROM llm_credentials WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveLlmCredentialSync(): LlmCredentialEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLlmCredential(credential: LlmCredentialEntity)

    @Query("DELETE FROM llm_credentials WHERE id = :id")
    suspend fun deleteLlmCredential(id: String)

    @Query("UPDATE llm_credentials SET isActive = 0")
    suspend fun deactivateAllLlmCredentials()

    @Query("UPDATE llm_credentials SET isActive = 1 WHERE id = :id")
    suspend fun activateLlmCredential(id: String)

    // Durable Autonomous Runs
    @Query("SELECT * FROM durable_runs ORDER BY updatedAt DESC")
    fun getAllDurableRuns(): Flow<List<DurableRunEntity>>

    @Query("SELECT * FROM durable_runs WHERE id = :id")
    fun getDurableRunById(id: String): Flow<DurableRunEntity?>

    @Query("SELECT * FROM durable_runs WHERE id = :id")
    suspend fun getDurableRunByIdSync(id: String): DurableRunEntity?

    @Query("SELECT * FROM durable_runs WHERE idempotencyKey = :key AND state IN ('PENDING', 'RUNNING', 'PAUSED') LIMIT 1")
    suspend fun findActiveRunByIdempotencyKey(key: String): DurableRunEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDurableRun(run: DurableRunEntity)

    @Query("UPDATE durable_runs SET state = :newState, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateRunState(id: String, newState: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE durable_runs SET state = 'PAUSED', updatedAt = :updatedAt WHERE state IN ('PENDING', 'RUNNING')")
    suspend fun pauseAllActiveRuns(updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM durable_runs WHERE id = :id")
    suspend fun deleteDurableRun(id: String)

    @Query("DELETE FROM durable_runs")
    suspend fun clearAllDurableRuns()
}
