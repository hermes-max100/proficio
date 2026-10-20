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

## Update 2026-10-19 (Drive search, owner-authorized)
Scope: Google Drive title/fullText/parentId searches. Results:
- Full-text search for the manifest hash prefix `800b9e683acff`: 0 results.
- Title search "proficio" (2 pages read of many; page 3+ not read): skill-spec lineage only —
  `proficio-skill-1.1.0`, `proficio-1.1.0-pass2` (+ two .zip copies, 43169 B),
  `Proficio_v1.1.2_review_bundle.zip` (59274 B, sha256 `9f3f3bac…cee5`), `proficio-chatgpt-knowledge.zip`,
  `proficio-engine-chatgpt.md`, `proficio_bridge.py`, `install-proficio-skill.sh`, `proficio-source-08.md`,
  and ~100 auto-named `proficio-NN` folders (2026-09-14 uploads; contents not opened).
- Opened `Proficio_v1.1.2_review_bundle.zip`: 18 files (SKILL.md, references/*, acceptance_tests.py,
  verify_package.py, MANIFEST.json, CHANGES*.md). Zero occurrences of "1.6.0" or the hash. It is a
  prompt-layer skill spec (v1.1.2), not the enforcement engine. Peer-council review in Drive
  (`proficio-peer-council-review.docx`) itself says a runtime must be split out of the spec.
- Not opened: the 1.1.0-pass2 zips, chatgpt-knowledge zip, `proficio-source-*.md` series (01–14 exist
  on Penguin), the ~100 proficio-NN folders, Drive pages after the 2nd.
P00 remains BLOCKED. Drive evidence suggests 1.6.0 may be a Hermes-side runtime that was never
uploaded to Drive (INFERENCE only).

## Update 2026-10-20 (Penguin `~/proficio_markdown` read)
Read all 14 bundles (`proficio-source-01..14.md`, 26,147 lines, 1,169,728 B, dated 2026-09-09) plus
`README-upload.md`. Method: grep for `1.6.0` / `800b9e68`; parse every `## \`path\`` section; hash each
reconstructed file; compare with `~/proficio` @ ce05f24 (`git ls-files`, 103 non-.gradle files).
- Hits for `1.6.0` or the manifest hash: 0.
- Bundles contain 103 paths = exactly the 103 tracked files; 0 missing, 0 extra.
- Content: 97 files byte-identical to the repo under the export's newline convention; 6 differ only
  by fenced-code/trailing-whitespace parsing (README.md and AutoForgeEngine.kt re-verified exact with a
  fence-tolerant parse; the other 4 equal modulo trailing whitespace). No real content differences found.
- Only version strings seen: `versionName = "1.0"` (app) and `"version": "1.0.0"` (MCP template).
Conclusion: the bundles are an upload-ready text export of the Android repo (README-upload.md says so),
not the 1.6.0 enforcement engine. P00 remains BLOCKED.

## Owner decision 2026-10-20 (relayed in chat): Option 1 — continue source recovery
Do NOT supersede the 1.6.0 requirement yet. If all read-only searches fail to produce a verifiable
match, a superseding baseline is a separate, documented engineering decision requiring owner approval.
Findings relayed with that decision (from ChatGPT's independent Penguin checks; NOT reproduced by me):
local Git object stores — no 1.6.0 source, two unreachable Hermes blobs inspected; Drive
proficio-1.1.0-pass2 — skill package, not enforcement runtime; sampled proficio-NN folders — small
task-result JSON, no source trees; known local archives — no match.
My additional check: Drive `proficio-chatgpt-knowledge.zip` (16497 B) member list = prompt-forge.md,
forge.py, plugin-forge.md, INSTRUCTIONS.md, skill-forge.md, goal-presets.md — the forge skill, not an
enforcement runtime (content not decoded; listing only).

## Update 2026-10-20 (Penguin whole-disk read-only scan, owner-authorized)
Scan script `/tmp/p00s/scan.sh` on Penguin (find/sha256sum/grep; read-only; output `/tmp/p00s/out.txt`).
- Step 1 — any file named *manifest*/SHA256SUMS*/*release* (<5 MB), whole filesystem (-xdev, excluding
  node_modules/.cache/ai_agent_env): 0 files with sha256 starting `800b9e68`. COMPLETE.
- Step 2 — archives named proficio|hermes|backup|1.6|enforce: 24 found, none named for 1.6.0.
  Large candidates (NOT opened): hermes-max-cloud-*.tar.gz (~45 MB, 2026-08-20..09-02, many copies),
  hermes-ultra-production-*.zip, opt-hermes-voice-retired.tgz, omniroute-backup-2026-10-13.tar.gz.
  Opened by listing: proficio_gate_R1R5_evidence_20261014.tgz, proficio_R6_R3_evidence_20261014.tgz,
  Hermes_Proficio_Enterprise_Evidence_20261015.tgz, Hermes_Recovery_R2_R3_R4_Evidence_20261015.tgz —
  they contain the Hermes registry gate-test code (registry.py, secure_registry.py, runner.py,
  worker.py, ...) plus receipts/sqlite; grep of those .py files and baseline/results JSON for
  `1.6.0`/`800b9e68`/`canonical`: 0 hits. This is registry test tooling, not the Proficio 1.6.0 engine.
- Step 3 — dirs named proficio*/enforce*/canonical*: ~/proficio, ~/proficio_markdown,
  ~/agent-skills/skills/proficio-engine, ~/rendezvous/bounded-outputs/proficio-1..54 (council outputs),
  ~/.claude/projects/...-agent-workstreams-20261012-proficio (memory dir only, empty).
- `~/rendezvous/agent-workstreams-20261012/proficio/` (WORK_ITEM.md, RELEASE_GATE.md, skill/): confirms
  the same 0.1.0 PARTIAL bootstrap (skill/manifest.json sha256 `1910b4d8…b692`); its own WORK_ITEM says
  "frozen v1.6.0 hash 800b9e...7c72 is NOT located". No 1.6.0 hits in that tree.
- Step 4 — text grep (proficio near 1.6.0) over ~: still running at time of writing; result NOT YET READ.
P00 remains BLOCKED. No verifiable 1.6.0 source found. Superseding baseline NOT approved.

## NEW LEAD 2026-10-20 — where 1.6.0 was built (Penguin text hits; source tree itself still not found)
Sources (read-only snippets on Penguin):
- `~/codex-cron/work/sweep-20261003-1800/memory-2026-10-03-tail.md`: "Proficio 1.6.0 F1/F2 build completed"
  (17:12 PDT 2026-10-03; builder-reported, external adjudication pending). New tree reported at
  `~/workspace/proficio-peer-tests/proficio-skill-1.6.0/`; build record
  `~/workspace/proficio-peer-tests/proficio-1.6.0-BUILD-20261003.md`; package
  `~/workspace/proficio-peer-tests/proficio-skill-1.6.0.zip`. Builder-reported: 25/25 new acceptance tests,
  163 passed / 0 failed full suite, Ed25519 issuance, `proficio-approverd`, 17-field typed envelope.
  (These are the builder's claims, not verified by me.)
- `~/codex-council-20261005/task2/prompt.txt`: "frozen 1.6.0 sources (approvals.py, policy.py, runner.py,
  binder.py, envelope.py) ... SHA-256 manifest: 800b9e683acfd749fa593cd25fb5bcbbdae726fe34627cf164d27c73acff7c72
  for the 59-file frozen tree at ~/workspace/proficio-peer-tests/proficio-skill-1.6.0/".
  This gives the FULL manifest hash (recorded text, not rehashed) and the file count (59).
Existence check 2026-10-20: `~/workspace/proficio-peer-tests/` does NOT exist on Penguin (ls: No such file).
The zip and BUILD note are also absent at those paths. Whole-home name search for `*proficio-skill-1.6*`,
`*proficio-1.6*`, `*proficio-peer-tests*` was started (result pending at time of writing).
Interpretation (INFERENCE): 1.6.0 existed as a 59-file tree built 2026-10-03; it was removed/moved after
2026-10-05. Candidate recovery paths: trash/other disk, other devices (penguin duplicates offline), the
council/peer reviewers' copies, cloud storage, shell history, ~/workspace backups.
P00 still BLOCKED.

## RECOVERY PROGRESS 2026-10-20 — 5 of 59 frozen 1.6.0 files located (source NOT copied into this repo)
Search of shell history + council reviewer folders + home-wide source-name search (read-only, Penguin).
- `~/.bash_history` (109 KB): 0 lines referencing proficio-peer-tests / proficio-skill-1.6 / proficio-1.6.
  (Agent-run commands from codex-cron are not necessarily in it, so this does not show how the tree vanished.)
- FOUND, 1.6.0-marked source (docstrings: "Ed25519-signed, verify-only runtime (F1, 1.6.0)", "In-process
  issuance was removed in 1.6.0 (F1)", "LEGACY PACKET: not valid under 1.6.0", "Typed canonical approval envelope (F1/F2, 1.6.0)"):
  | file | bytes | sha256 |
  | approvals.py | 12739 | 8d72125b2970984dbcf0c8937e238f61e8903716282cfced1e4eb38c95e65693 |
  | binder.py    | 13127 | cd0a632403980d8ab857351ba25d276619d89d85d1559bcfb10a5f1191e851a4 |
  | envelope.py  |  9738 | ebce7d2fe1b4da8ea6a3ccb6bd72900a5d04fa20479ae4caa1d57ebacca3018e |
  | policy.py    |  7536 | ade4aab11cc1727f11931e13c353445e0ffbbf52bce8be68ee49e21d1d9aa724 |
  | runner.py    | 29377 | 94faae3992b0bbb0643fc229c5ce8914a6c299eae6f7cb27d6765a98d69a012b |
  Copies with identical hashes at `~/codex-council-20261005/task2/`, `~/proficio-phase8/council-fanout/`
  (+ `council-fanout.tgz`), and (approvals/binder/envelope/policy confirmed by listing) `~/codex-cron/work/adjud-20261005-162112/`.
  These five hashes MATCH the digests recorded in `~/codex-council-20261005/task2/FINDINGS-ADJUDICATION.md`
  (written by an adjudicator at the time). That document also states the aggregate 59-file manifest digest is
  UNVERIFIED because the other 54 files and the manifest-construction procedure were not supplied.
- NOT reproducible from these: the manifest hash `800b9e68…7c72` (needs all 59 files + the exact construction
  procedure). Missing: 54 files incl. sister modules (store, inventory, idempotency, consumption, ledger, trust),
  `proficio-approverd`, tests, and the manifest file itself.
- Pre-1.6 ancestor (NOT 1.6.0): `~/redteam/code/runtime/` — 11 py files (approvals/binder/idempotency/inventory/
  ledger/policy/readiness/runner/store/validation/__init__), HMAC issuance, no "1.6.0"/Ed25519. Useful as the
  prior-version tree; cannot stand in for the frozen tree.
- 16 unique large/small Hermes archives (hermes-max-cloud-*, hermes-ultra-production-*, hermes-ultra-v1.0,
  HERMES-AUTONOMY, opt-hermes-voice-retired, omniroute-backup, HERMES_CURRENT_HOST_SOURCES) were extracted
  to /tmp, searched, and deleted: 0 manifest hashes matching, 0 text hits for 1.6.0/800b9e68 near proficio.
  One path hit only: `host_snapshot/arena-mission-control/proficio.py` (Arena module, not engine).
P00 status: STILL BLOCKED (partial source recovered: 5/59; manifest unreproducible).
Next: locate the other 54 files (reviewer lanes' own workdirs under `~/proficio-phase8/council-fanout/lane-responses`,
other devices, Drive "Hermes unified blueprints", offline penguin devices) or approve a reconstruction plan.
RULE: do not push these engine files to the public Android repo (per the 2026-10-15 ledger).

## 2026-10-20 — lane-responses / Drive blueprints / Gmail (relayed from ChatGPT; spot-verified by Claude)
Relayed (ChatGPT): `lane-responses` = 15 JSON security reviews of the five modules (no additional original
source); 3 Drive "Hermes unified blueprints" documents have identical text and describe 1.6.0 as
unverified / not production-ready, OBSERVE mode, no enforcement authorization.
Claude re-verified on Penguin (read-only):
- `~/rendezvous/code-handoffs/P00-LANE-DRIVE-REVIEW-20261020.md` exists (4371 B), sha256 = 2af3cd7e8a962da047317d44cc1aef18fa776551246d4576c9e711cab0b01520 (matches the claimed value).
- The five module sha256 values in `~/codex-council-20261005/task2/` and `~/proficio-phase8/council-fanout/`
  are identical to each other and to the table in FINDINGS-ADJUDICATION.md (prefixes 8d72125b/cd0a6324/ebce7d2f/ade4aab1/94faae39).
- `council-fanout.tgz` members: fanout.py, PHASE8-PROMPT-FINAL.txt + the same 5 modules only.
- `closure-20261012/recovery/perplexity-one-shot/inputs/` = Hermes host snapshot (Arena `proficio.py`, mission
  control), not the engine.
Claude additional: Drive title search (proficio-skill / peer-tests / frozen) → only the 1.1.0 skill folders and
unrelated pandas/frozenlist files; Gmail search ("proficio 1.6.0", with and without attachments) → 0 threads.
Drive file revisions/old versions: NOT SEARCHED (the Drive connector here exposes no revision listing).
P00: BLOCKED, 5/59 files accounted for; 54 files + canonical manifest still missing.
