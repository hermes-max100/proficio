# AGENTS.md — Proficio (Android Edition)

Guidance for AI coding agents working in this repository. Replaces the original
sandbox-scaffold AGENTS.md, which described the build environment rather than
this project.

## What this project is

Proficio is an autonomous prompt-creation, skills, and tooling-control platform
for AI operations, built with Kotlin and Jetpack Compose. It declares a
server-side Gemini API capability (see `metadata.json`). Core features: Prompt
Studio (structured master prompts with CO-STAR / RISEN frameworks), the Auto
Engine (goal synthesis → skill generation → FastMCP tooling compilation), Agent
Vault (encrypted Room DB persistence), and Provider-Aware Execution (telemetry
provenance, retry policies, output sanitization).

## Architecture map

Root package: `app/src/main/java/com/aistudio/promptforge/abcd/`

- `MainActivity.kt` — Compose entry point
- `api/` — `GeminiApi.kt`, `PromptForgeApiService.kt`, `UniversalLlmService.kt`
  (provider routing), `provider/`
- `data/` — `AutoForgeEngine.kt` (the forge pipeline), `Repository.kt`, Room
  layer (`PromptDatabase.kt`, `PromptDao.kt`, `PromptEntities.kt`), `repository/`
- `model/` — `AutoForgeModels.kt` (`GeneratedSkill`, `GeneratedMcp`,
  `AutoForgePackData`, `GOAL_PRESETS`, `PRESET_SKILLS_CATALOG`,
  `PRESET_MCPS_CATALOG`), `PromptRepositoryCatalog.kt`, `AppError.kt`,
  `FailureModeMapper.kt`, `DurableRunModels.kt`, `PromptVersioningModels.kt`
- `ui/` — Compose screens and themes. Theme names (AutoFlow Dark / Light / Neon
  / Cyberpunk) predate the Proficio rebrand — rename only in a dedicated refactor,
  never opportunistically.
- `util/` — `AiOutputValidator.kt`, `RetryPolicy.kt`, `SecretMasker.kt`,
  `VaultCryptoUtils.kt`, `VariableResolver.kt`, `DataPortabilityService.kt`,
  `ShareUtils.kt`, `DiffUtils.kt`

## The AutoForge Engine (core pipeline)

Deterministic, template-driven, no LLM in the loop:

1. `generateLocalPrompt10OutOf10(goal)` — wraps the goal in the fixed 10/10
   system prompt (persona, 5-step CoT protocol, guardrails, output contract)
2. `generateLocalSkills(goal)` — keyword-routes to preset skills (Web Scraper,
   Git Diff Auditor, SQLite Store) and always synthesizes one custom Python skill
3. `generateLocalMcps(goal, skills)` — keyword-routes to preset MCPs (Brave
   Search, Filesystem, SQLite) and always generates one custom FastMCP server
4. `assembleCompleteSpec(...)` — emits the full goal package markdown

A portable Python port of this engine lives in
`hermes-max100/agent-skills` under `skills/proficio-engine/` (SKILL.md +
references + `scripts/forge.py`). Keep the two in sync when the pipeline changes.

## Build & test

- `./gradlew assembleDebug` — build the app
- `./gradlew test` — unit tests (`app/src/test/`)
- `create_android_project.sh`, `setup_icon.sh` — scaffolding utilities

## Conventions (non-negotiable)

- Route all logging through `SecretMasker` — never log tokens or session data
- Validate all LLM output through `AiOutputValidator` before use
- External calls use `RetryPolicy` exponential backoff
- Branding: the project is **Proficio** (repo `hermes-max100/proficio`). Legacy
  names "Prompt-forge", "AutoFlow", and "Perficio" still appear in code — fix
  only as part of a rebrand-scoped change.

## Legacy residue

`.vercel/`, `package.json`, and `startup.sh` are leftovers from the original
web scaffold. They are inert for Android work; removal is tracked as a separate
cleanup task.
