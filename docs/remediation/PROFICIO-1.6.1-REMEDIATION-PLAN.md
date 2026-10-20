# Proficio 1.6.1 — remediation plan (DESIGN ONLY; no engine source here)

Date: 2026-10-20. Status: PROPOSED. Baseline: frozen Proficio 1.6.0 (59 files; manifest sha256 `800b9e68…7c72`; recovery CLAIMED by the build host,
independent verification of the manifest still pending — see `docs/evidence/P00-provenance.md`). **1.6.0 is not to be modified or rebuilt.**
1.6.1 is a NEW tree; nothing below is implemented.

Source of findings: `~/codex-council-20261005/task2/FINDINGS-ADJUDICATION.md` (Penguin; ChatGPT adjudication of the Perplexity Phase 8 review, 2026-10-05).
The adjudicator saw only 5 of 59 files (approvals, binder, envelope, policy, runner — sha256 recorded in the evidence file). Line numbers below are the adjudicator's, against those five files.
Verdict then: **Phase 8 clean-tree = FAIL** (5 TP, 0 FP, 1 partially verified, plus 5 reviewer misses). Production readiness: NOT APPROVED.

## Work items (ordered by risk)

| ID | Sev (proposed) | Defect (adjudicated) | Where | Proposed fix | Failing-first test |
|---|---|---|---|---|---|
| R1 | Critical | F1 TP: executed effect not bound to approved action/args; gate compares only envelope target to bind target; runner calls an arbitrary callback | policy.py:139-155; runner.py:77-82,306-319; envelope.py:34-49 | Runner takes (operation, args) and invokes the effect itself; recompute canonical args hash and transaction hash and compare to signed `arguments_hash`/`transaction_hash`; add gate conditions for action==bind operation, schema_version==bind schema pin, policy_version, environment; remove opaque `effect_fn` | Adjudicator probe: envelope action `read_customer` vs bind `delete_customer`, mismatched schema/policy/env, same target → must return a FAIL listing the new conditions (currently `(True, [])`) |
| R2 | Critical | F2 TP: `route` discarded, `data_class` caller-declared; RESTRICTED packet can authorize PUBLIC step; local-only enforced only if caller says so | policy.py:63-86,193-210; runner.py:77-93 | Derive step data class from bind/signed envelope, never from caller; require equality (or explicit allowed downgrade policy); validate route against a bind-recorded allowlist; RESTRICTED ⇒ bind.local_only==1 AND route local | Probe: RESTRICTED packet, PUBLIC step, local_only=0, route `remote` → must fail |
| R3 | High | F4 TP: relative filesystem targets bypass protected-target check and FD identity (`_resolved_target` returns None; verify skips) | binder.py:117-123,145-166,276-287; runner.py:599-607,316-319 | Reject relative path-like targets at bind (fail closed) or anchor them to a declared root dirfd; protected-target check on resolved absolute path; no zero-arg effect for path targets | `_canonical_target("../proficio/runtime/policy.py")` must be refused; protected=True |
| R4 | High | F5 TP: TOCTOU for to-be-created targets (parent FD held but effect uses pathname) | binder.py:290-298; runner.py:640-668 | Give the effect a (parent_fd, basename) capability and create via `openat`/`os.open(dir_fd=…, O_NOFOLLOW\|O_EXCL)`; runner (not effect) performs the create | Race test: rename parent between verify and create → create must fail or land in the verified dir |
| R5 | Medium | FN-2: completed-idempotency fast path returns before signed-key check | runner.py:103-120 vs 203-217 | Do envelope.idempotency_key==idem_key check first; bind idempotency row to packet/bind/run | Valid packet + unrelated completed key must not return that key's response |
| R6 | Medium | FN-4: re-arm never checks `action=="rearm"` | runner.py:487-519 | Add action check | Valid approval with other action but matching target/args must be rejected |
| R7 | Medium | FN-1: `run_id` discarded; bind/packet/run association not enforced | policy.py:85; binder.py:239-247; approvals.py:271-277 | Enforce run association (or document an explicit cross-run policy) | Probe with run `current-run`, bind `other-run`, packet `approval-run` must fail |
| R8 | Medium | FN-3: absolute-target identity capture fails open (`except Exception: return None`) | binder.py:288-300; runner.py:602-605 | Absolute governed targets: fail bind when identity capture fails | Unreadable parent at bind time must fail bind |
| R9 | Medium (partial) | F3 PARTIAL: no explicit ledger-integrity gate condition | policy.py:16-24; runner.py:306-319,352-367 | Add `LedgerIntegrity` condition (verify chain/sequence before effect, fail closed). Needs the absent `ledger`/`store` modules to confirm end-to-end | Corrupt a ledger row → execution must be refused |
| R10 | Low–Med | F6 TP (host trust boundary): clock watermark unauthenticated; same-process cache doesn't detect row rollback | approvals.py:101-106,138-183,232-239 | Detect cached-vs-row regression; authenticate watermark (daemon-signed time attestation or hash-chained append-only) or explicitly document host-boundary assumption | Probe: store 2030, update row to 2020 with mac NULL → `_clock_regressed(2020)` must be True |
| R11 | Low | FN-5: `canonical_arguments` accepts Infinity (`allow_nan` not False) | envelope.py:199-223 | `allow_nan=False`; reject non-finite | `{'x': inf}` must raise |

## Method (non-negotiable)
1. Work in a NEW tree `proficio-skill-1.6.1-candidate/`; frozen 1.6.0 stays byte-identical (verify with its manifest before and after).
2. Failing-first: encode each adjudicator probe as a test that fails on 1.6.0 unchanged, then passes on the candidate. Keep negative controls.
3. Run the full 59-file suite plus the new tests; report counts honestly (the builder's 163-pass claim for 1.6.0 is unverified by us).
4. Freeze the candidate with a new manifest + sha256; do not label it 1.6.1 until Phase 8 is re-run: fresh clean-tree review, seeded K=5 recall, deterministic canary/evasion, independent adjudication by a different model family.
5. No production claims, no enforcement activation, no spend; owner approval before any promotion.

## Blockers / what is needed
- The full 59-file tree is only on the build host. Remediation needs it (the five files alone cannot run R9 or integration tests). Options: (a) the build-host agent does the work there under this plan; (b) the owner copies the zip + manifest to Penguin/Drive and Claude/ChatGPT work and verify there.
- Independent check of the manifest hash (`base64 -w0 1.6.0-MANIFEST.txt` from the host) is still open.
- Phase 8 seeded-recall and canary artifacts (not seen) are needed to avoid regressing prior results.

## Backup performed (Penguin, read-only copies; originals untouched)
`~/Rendezvous/code-handoffs/frozen-1.6.0-partial-backup-20261020/` — 5 runtime files + the two Phase 8 docs, chmod 444/555, `SHA256SUMS` verified (7/7 OK) and byte-identical to the originals.
This is a PARTIAL backup (5 of 59). A full verified backup requires the zip + manifest from the build host.
