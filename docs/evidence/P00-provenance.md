# P00 — Canonical 1.6.0 provenance check

- Executed: 2026-10-19. Read-only.
- Revisions inspected: `hermes-max100/agent-skills@47194fb3ca3e44c673ae4c9d6a08979629ba0659`,
  `hermes-max100/hermes-ultra@f7a1581c6374217d46db247522cc0551b15c1d91`,
  `hermes-max100/proficio@ce05f24ff80166e74d0faa8f98efd4e8629b7035`.

## Result: BLOCKED — canonical 1.6.0 source NOT located; no superseding baseline approved

VERIFIED
- Expected 1.6.0 manifest SHA-256 `800b9e683acfd749fa593cd25fb5bcbbdae726fe34627cf164d27c73acff7c72`
  equals the SHA-256 of no tracked file in agent-skills (0 matches) and appears in no
  file in hermes-ultra. The only mentions are text references in agent-skills
  `recovery/proficio/` (RECOVERY_ONLY.md, the 2026-10-15 ledger).
- agent-skills `recovery/proficio/bootstrap-v0.1.0` self-declares 0.1.0 / PARTIAL /
  "NOT THE FROZEN V1.6.0 ENGINE". `skills/proficio-engine` is a forge skill, not the
  enforcement runtime (the 2026-10-15 ledger says not to assume identity).
- Android repo (this repo) is not the canonical engine (blueprint P00 note).

UNVERIFIED / NOT RUN
- Penguin host (`/home/riveracarmeloiii`): a directory listing was denied by the
  session's permission classifier (PII handling); a home-wide hash grep was started
  and terminated without output. Host not searched.
- GitHub history/branches beyond default-branch shallow clones: not searched.

## Needed from owner (P00 gate: owner_approval_if_superseding)
Either (a) point to a location for the frozen 1.6.0 tree/archive, or (b) approve a
superseding baseline. Neither has been given; downstream P01+ stay gated on P00.

## Update 2026-10-19 (owner granted file access; Penguin searched)
- Hash scan of `manifest*.json`, `*.zip`, `*.tar.gz`, `*.bundle` (<200MB, excluding
  node_modules/.cache/.git/ai_agent_env) under `/home/riveracarmeloiii`: 0 files with
  sha256 `800b9e68…7c72`. Text grep of proficio, proficio_markdown, Rendezvous,
  rendezvous: 0 hits (the grep ran under a 2 s timeout, so it is weak evidence).
- Present on host: `workspace/skills/proficio` (0.1.0 bootstrap), `proficio-phase8-v3`
  (protocol docs only), `Rendezvous/mission-control/proficio-bridge-20261012`,
  `~/proficio` (= Android repo @ ce05f24). None is the frozen 1.6.0 tree.
- P00 remains BLOCKED. Not searched: other devices, cloud drives, git history/branches,
  `.git` object stores, large archives.
