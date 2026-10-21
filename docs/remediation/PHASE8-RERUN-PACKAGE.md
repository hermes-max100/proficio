# Phase 8 re-run package — Proficio 1.6.1 CANDIDATE (index)

Status: PROPOSED — not approved, not released, not deployed. Frozen 1.6.0 baseline unchanged.
Location (Penguin, read-only, chmod 444/555): `~/Rendezvous/code-handoffs/phase8-rerun-1.6.1-candidate-20261021/`

| File | Purpose |
|---|---|
| `proficio-skill-1.6.1-candidate.zip` (219,727 B) sha256 `ef3601f92cbd416e3e41b65a3e627080d6352b349f7ad963ce97fde1e0b66663` | clean tree, 95 files, deterministic zip, no caches/keys/databases |
| `1.6.1-candidate-MANIFEST.txt` sha256 `dab28da0697fdb768b140d8af6760774f28edc1e8c8448163402276d68760efa` | per-file sha256 |
| `CHANGESET.txt`, `DIFF-vs-1.6.0.patch` | 8 changed files, 5 added tests, 0 removed |
| `FINDINGS-MAP.md` | F1–F6 / FN-1..5 → change → tests → residuals (claims to test) |
| `README-ADJUDICATOR.md` | instructions for a non-Claude independent adjudicator |
| `verify_candidate.py` | zip==manifest; 59 frozen-pinned files either identical or declared CHANGED |
| `results/` | failing-first on frozen; new tests from extracted zip; per-file candidate vs baseline runs |
| `SHA256SUMS` | package self-hashes |

## Verified (Claude, 2026-10-21, Penguin)
- `verify_candidate.py`: ALL PASS (95 files; 51 of 59 pinned files identical to 1.6.0, 8 declared CHANGED).
- Base extract used for the diff: 59/59 pinned files match the frozen manifest.
- 46 new tests on FROZEN 1.6.0: **27 failed, 19 passed** (19 = controls). Same 46 from the EXTRACTED candidate zip: **46 passed**.
- Per-file full suite (same exclusions as baseline): candidate 28 files rc=0 / baseline 23 files rc=0; `test_race_matrix.py` rc=124 (300 s timeout) on both — UNVERIFIED. No file passes on baseline and fails on candidate.
- Secret scan of zip contents (private-key headers, `api_key=`, `sk-…`, `password=`): no matches. Not a substitute for adjudicator review.

## Correction to earlier progress-log counts
The "N files rc=0" figures I logged for the R1/R2 … R8–R11 runs were each one lower than the actual `grep -c rc=0` of the saved outputs (actual: r1r2 24, r3r4 25, r5r7 26, r8r11 27, gaps 28; baseline 23). All runs: 0 failures; only `test_race_matrix.py` rc=124, same as baseline.

## Needs a human / independent party
1. Independent adjudicator (not Claude) runs the package per `README-ADJUDICATOR.md` and reports a verdict on the exact tree hash.
2. Review of the **daemon change** (`approverd/daemon.py`, digest skeleton `issued_at=1`) — trust-boundary component.
3. Owner decision: MAC/secret for the clock watermark (F6 residual); whether `PROFICIO_ENVIRONMENT` becomes mandatory; migration of adapters to `dispatch_step`.
4. Owner approval before any 1.6.1 label, deployment, or supersession of 1.6.0.
