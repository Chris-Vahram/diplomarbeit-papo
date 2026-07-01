# Continuation Prompt — Partnerportal (Diplomarbeit)

Copy the block below into a fresh session to resume with full context.

```text
You are continuing work on an HTL Diplomarbeit software project. Treat this as the authoritative context. Do not re-derive or re-litigate settled decisions. If something is unclear, ask targeted questions instead of assuming.

# PROJECT
Web-based Partner Portal (Partnerportal). Model: Company -> Partner -> End Customer.
Reference client: PAPO (~200 partners, ~800 articles). HTL diploma demo prototype, not enterprise.

# REPO / FILE LOCATIONS
Working repo (git): /Users/mitgl/Desktop/Schule/4CHITM/Diplomarbeit/diplomarbeit-papo
- Specification/functional_specification_document_partnerportal_final.md = authoritative FSD (v2). READ FIRST.
- Specification/functional_specification_document_partnerportal_v1_backup.md = old v1 (superseded, ignore).
- Specification/phase2_backlog.md = deferred features B-01..B-07 + decisions log. HONOR THIS.
- Specification/IMPLEMENTATION_PLAN.md = flat implementation plan (8 sections, WP1-WP13, phases).
- Specification/CONTINUATION_PROMPT.md = this file.
- Documentation/Besprechungsprotokolle/ = client meeting notes (17.06 + 24.06.2026).
- openspec/ = OpenSpec (schema spec-driven). Change "build-partnerportal-mvp" is PROPOSED (proposal + design + specs + tasks complete, validated). Not yet applied.
- CLAUDE.md (repo root) = older notes; where it conflicts with FSD v2 / backlog, FSD v2 + backlog win.
Note: .claude and .codex live ONLY inside diplomarbeit-papo/ (outer copies deleted).

# HARD RULE
Meetings override the FSD when they conflict (newer = authoritative). FSD v2 + phase2_backlog.md already encode the meeting decisions. Do not reintroduce v1 assumptions.

# LOCKED SCOPE (MVP)
Internal B2B partner-ordering portal. Core process = partner logs in (Keycloak) -> selects or creates end customer (Kunde) -> places order (multiple articles + quantities) on customer's behalf -> order auto-assigned to partner -> on-screen confirmation + simulated email (no real send) -> company advances status submitted -> reviewed -> fulfilled and handles fulfillment.
- Commission: field on Partner (Partner.commissionPercent). Company sets via dropdown 5/10/15 + optional custom value. Percentage of total order value. Stored as own CommissionRecord. NO separate CommissionLevel entity. Auto level-up deferred (B-03).
- Active status: DB-only flag Partner.active, enforced server-side after login. Keycloak stays simple (no enabled sync).
- Kunde = reusable entity (select existing or create new); mandatory last name + address; other fields optional.
- Multiple articles per order (article + quantity per line). OrderItem stores unit-price snapshot.
- Excel/CSV import of partners + articles (real, full: column mapping + duplicate detection + validation report).
- Roles: company, partner (Keycloak). End customers do NOT use the portal in MVP.

# FIXED TECH STACK (NO ALTERNATIVES - do not suggest others)
Frontend: HTML, CSS, native Web Components / Custom Elements (no framework, no Bootstrap/Angular/React/etc).
Backend: Quarkus (Java), Hibernate ORM Panache, RESTEasy Reactive, OIDC.
Database: PostgreSQL.
Auth: Keycloak.
Import libs: Apache POI (xlsx) + opencsv (csv).

# DATA MODEL (entities)
Partner (incl. commissionPercent, active), Product, ProductEnablement (partner x product), Kunde,
Order, OrderItem (unit-price snapshot), CommissionRecord. No CommissionLevel entity.

# OUT OF SCOPE / DEFERRED (phase2_backlog.md - do NOT build into MVP)
B-01 external end-customer shop + direct ordering (CUT)
B-02 shop design customization (CUT)
B-03 automatic commission level-up (STUB; MVP = manual)
B-04 real email sending (STUB; MVP = simulated in-app)
B-05 shop inquiry auto-forward (CUT)
B-06 BMD interface (CUT, permanent; data via Excel import instead)
B-07 commission settlement/payout (CUT)
To build any later, user says "implement B-XX".

# TEAM
Chris + Tobias only (Julian left). Split is in IMPLEMENTATION_PLAN.md and tasks.md (C/T tags).

# STILL-OPEN QUESTIONS (do not assume - ask if they block you)
- Optional Kunde fields on the order form beyond mandatory last name + address.
- Affiliate-identifier field visibility in the MVP.
- Research question exact wording (direction decided: reframe toward internal Partner <-> Company chain; confirm with supervisor).
- Schema management: Hibernate auto-generation vs explicit migrations (Flyway).

# OPENSPEC WORKFLOW
Change "build-partnerportal-mvp" is proposed. To implement: /opsx:apply (works tasks.md top-down by dependency, starting Phase 0). CLI: use "npx --yes @fission-ai/openspec@latest <cmd>". Two plan representations exist (flat IMPLEMENTATION_PLAN.md + OpenSpec change); keep in sync or pick one source of truth.

# HOW TO WORK
- Strict, precise, realistic for a 2-person HTL diploma. No enterprise overkill, no generic PRM filler.
- Stay on the Company -> Partner -> End Customer model; keep roles/areas clean.
- Requirements/tasks concrete and testable.

# NEXT STEP
Read FSD v2 + IMPLEMENTATION_PLAN.md + phase2_backlog.md + openspec change, then tell me: <STATE WHAT YOU WANT NEXT - e.g. /opsx:apply to build WP1 foundation, produce ER diagram for WP2/8.2, build a clickdummy, or refine the plan>.
```

Fill the last line with what you want next.
```
