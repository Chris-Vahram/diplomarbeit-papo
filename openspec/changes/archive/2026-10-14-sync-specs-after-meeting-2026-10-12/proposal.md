## Why

The specification documents lag behind decisions from 25.09.2026 and the meeting on 12.10.2026. Tobias pointed out that the full-vision FSD still lists BMD as "permanently excluded" (reopened on 25.09) and describes the end-customer shop without login (optional customer accounts were added on 25.09). The 12.10 meeting adds new facts: the shop becomes a partner-side homepage builder ("Mein Webshop"), data import stays a dummy until company data exists, and the next work block is the UI feedback. Agent context files (CLAUDE.md, AGENTS.md, `openspec/config.yaml`) still name 14.09 as newest meeting and list an archived change as active.

## What Changes

- Full-vision FSD: BMD (B-06) reopened for decision; shop with guest orders plus optional customer accounts and no online payment; shop as partner-side homepage builder "Mein Webshop" reachable by path or subdomain (details open); multi-company with two test companies, personal accounts with audit, B-08 and B-09 added; data model and roadmap updated; backlog range B-01…B-09.
- MVP FSD: add a dated "Stand 10/2026" note and correct the BMD exclusion lines; requirements FR-01…FR-25 stay unchanged as the implemented baseline. Mark the reference scale as to be verified instead of inventing numbers.
- Phase-2 backlog: update B-01/B-02 status with the 25.09 and 12.10 decisions; add a 2026-10-12 decisions-log entry (import stays dummy, company appointment pending, "KI einbauen?" only an idea without scope).
- Handoff, roadmap and completion plan: newest meeting 12.10; next block is `apply-dashboard-navigation-feedback` and `edit-partner-end-customers`; shop specification lies with Tobias; OIDC identity foundation follows.
- CLAUDE.md, AGENTS.md, `openspec/config.yaml`: newest meeting 12.10, current active-change list, BMD reopened, resolve the contradictory "Locked direction" wording in AGENTS.md. Keep all existing rules and Tobias' Claude files.

## Capabilities

### New Capabilities

- None. Documentation only; no spec deltas.

### Modified Capabilities

- None. Product behavior and interfaces stay unchanged.

## Impact

Only files under `Specification/`, `CLAUDE.md`, `AGENTS.md` and `openspec/config.yaml`. No backend, frontend, database or deployment change. Decisions still open with the company (BMD contract, shop legal/price questions, B-03/B-09 rules) are documented as open, not decided.
