# H00 — Hermes registry task-artifact verification (read-only)

- Executed: 2026-10-19. Source: Penguin `~/rendezvous/registry/tasks.sqlite3` (176128 B,
  mtime Oct 19 01:28; no -wal present at copy time). Verified a COPY at `/tmp/h00/` on the
  device (copy sha256 `8fe6d1e72f126dad81530773959e8d002808bde69df491c36b223385aeb190e5`);
  the live DB and output files were only read.
- Analysis script: `/tmp/h00/verify.py` on device (not committed; sqlite `mode=ro`).

## Observed (VERIFIED, bounded to that DB copy)
- 43 tasks: 34 SUCCEEDED, 5 BLOCKED, 2 INTERRUPTED, 1 FAILED, 1 QUEUED —
  matches the blueprint's 2026-10-19 baseline exactly.
- `outputs`: 32 rows; all 32 files exist and match recorded sha256 and byte length.
- `events`: 168 rows; `prev_hash` linkage unbroken (0 breaks).
- `intents`: 37 rows; `spec_sha256` matches sha256(spec) for all 37.

## Anomalies / gaps
1. 2 SUCCEEDED tasks have no `outputs` row AND no `intents` row:
   `621f8940-7495-401e-8b8a-75006da4d741`, `08d2cf05-b9a4-472b-8be9-9bf998a82d91`
   (attempt 1). Their results are therefore not hash-bound. Cause UNKNOWN.
2. 6 tasks have no intent: those 2, plus the INTERRUPTED (`0673f182…`, `407dd933…`),
   FAILED (`245f1681…`) and QUEUED (`e5437597…`) tasks.
3. Event-chain `hash` values were NOT recomputed (hash recipe not inspected); linkage
   proves ordering consistency only, not tamper resistance.
4. Original job/result bind receipt not reviewed. Candidate:
   `~/rendezvous/enterprise-readiness-20261015/g1-g5-binding-hardening-20261015/G1_G5_EXACT_BINDING_RECEIPT.json`.
5. Other registry DBs under `registry/evidence/*` and `~/proficio_gate_test/` not examined.

## H00 gate status: PARTIAL — NOT PASSED
Output-hash portion passes for the 32 recorded outputs; the 2 unbound SUCCEEDED tasks and
the unreviewed bind receipt block a full pass. Independent verifier: NOT RUN.
