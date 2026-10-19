# B00 — ICP, scoped service offers, limitations, quote assumptions

Status: DRAFT for owner review. Internal only; nothing sent externally.
Basis: repo `hermes-max100/proficio@ce05f24` (+ S00/P00 evidence in `docs/evidence/`).
Market, pricing and demand claims below are PROPOSED/UNVERIFIED unless a source is cited.

## What can honestly be sold today
VERIFIED from `AGENTS.md`: Proficio (Android) provides Prompt Studio (CO-STAR / RISEN),
a deterministic, template-driven AutoForge pipeline (goal → 10/10 prompt → skills →
FastMCP specs → goal-package markdown, no LLM in the loop), an encrypted Room vault,
and conventions for secret masking, output validation and retry policy.

NOT sellable as of 2026-10-19 (Proficio release verdict HOLD; P00 blocked):
- the "canonical 1.6.0 enforcement engine" (source unlocated)
- any enterprise / GA / SLA / multi-tenant / "governed autonomous execution" claim
- trading signals or results (T-lane has no backtest evidence)
- legal advice or filings
Offers therefore sell **human-led services that use the tooling**, not a hosted product.

## ICP (PROPOSED)
Small teams (1–50 staff) already using LLMs in operations — agencies, solo-run
software/data shops, internal-tools teams — who have: ad-hoc prompts in docs/chats,
no versioning or evaluation, and at least one workflow touching credentials or client
data. Buyer: founder/ops lead. Disqualify: regulated-data processors needing
attestations, anyone wanting guaranteed outcomes, anyone needing 24/7 support.
Validation of this ICP: NOT DONE (needs B03 lead list + owner judgment).

## Offer 1 — Prompt & Skill Pack Build (fixed scope)
Deliver, for up to N agreed goals: a structured master prompt (CO-STAR or RISEN),
skill specs, and FastMCP tool specs generated via the AutoForge pipeline, then
hand-reviewed and edited. Output: markdown goal packages + review notes.
Excludes: running the packages in the client's production, model-quality guarantees,
ongoing maintenance.
Acceptance: client-approved package per goal; each ships with 5+ client-supplied
test inputs and the observed outputs (no claim beyond those).

## Offer 2 — Prompt-Library Hardening Audit (fixed scope)
Review a client's existing prompts/agent configs for: secret leakage paths, missing
output validation, unbounded retries, injection exposure, versioning gaps. Output:
findings ranked by severity with evidence excerpts, plus a fix list.
Excludes: pentesting live systems, code changes in client repos (separate quote),
compliance certification.
Acceptance: findings each cite a file/line from the supplied material; items not
inspected are listed as NOT IN PROVIDED SOURCE.

## Offer 3 — Agent-Ops Readiness Review (assessment)
Gap assessment of a client's agent workflow against an evidence-first checklist
(authoritative state, approval-to-action binding, idempotency, rollback, receipts).
Output: gap report + a staged remediation plan with measurable acceptance tests.
Excludes: implementing the remediation, uptime/SLO commitments, "enterprise ready"
sign-off. Clients get PASS/FAIL per check only for what was actually inspected.

## Honest limitations (must appear in every proposal)
1. Services are human-reviewed; outputs are not guaranteed to improve model results.
2. The AutoForge pipeline is deterministic templating, not an LLM or an autonomous agent.
3. Proficio itself has not passed its own release gates; no product warranty implied.
4. No ROI promised; baseline-vs-after measurement is offered in B01, not assumed.
5. Client data stays out of any third-party service unless the client approves in writing.

## Quote assumptions (parameters, not prices)
Price = (hours × rate) + fixed expenses, where hours = intake + per-unit work × units
+ review + one revision round. All numbers below are PLACEHOLDERS for the owner.
- rate: <owner to set>
- Offer 1 hours per goal: <estimate after one pilot goal is timed>
- Offer 2 hours per 10 prompts/configs reviewed: <estimate after one timed sample>
- Offer 3 fixed days: <owner to set>
- Revision rounds included: 1; change-requests re-quoted.
- Payment terms, deposits, contracts: OWNER APPROVAL REQUIRED (binding, external).
No market-rate, conversion or demand figures are asserted here.

## Open items for owner
1. Confirm ICP and which offers to keep/cut (recommend keeping Offers 2 and 3 first:
   they depend least on the unverified engine).
2. Set rate and terms. 3. Decide if Offer 1 may be described as using "Proficio".
Independent verifier (blueprint gate): NOT RUN.
