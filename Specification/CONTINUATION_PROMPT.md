# Continuation Prompt — Partnerportal (Diplomarbeit)

Copy the block below into a fresh session to resume with full context.

```text
You are continuing work on an HTL Diplomarbeit software project. Treat this as the authoritative context. Do not re-derive or re-litigate settled decisions. If something is unclear, ask targeted questions instead of assuming.

# PROJECT
Web-based Partner Portal (Partnerportal). Model: Company -> Partner -> End Customer.
Reference client: PAPO (~200 partners, ~800 articles). HTL diploma demo prototype, not enterprise.

# REPO / FILE LOCATIONS
Working repo (git): /Users/mitgl/Desktop/Schule/4CHITM/Diplomarbeit/diplomarbeit-papo
- Specification/functional_specification_document_partnerportal_final.md = authoritative FSD (v2), MVP-only. READ FIRST.
- Specification/functional_specification_document_full_vision.md = whole-project vision (MVP + all deferred B-01..B-07 folded into one narrative + roadmap). Reference only, not the build target.
- Specification/phase2_backlog.md = deferred features B-01..B-07 + decisions log. HONOR THIS.
- Specification/IMPLEMENTATION_PLAN.md = flat implementation plan (8 sections, WP1-WP13, phases).
- Specification/CONTINUATION_PROMPT.md = this file.
- Documentation/Besprechungsprotokolle/ = client meeting notes (17.06 + 24.06.2026).
- openspec/ = OpenSpec (schema spec-driven). Change "build-partnerportal-mvp". tasks.md checkboxes = progress truth. See STATUS below for what's actually done.
- CLAUDE.md (repo root) = project memory aligned with FSD v2; where anything conflicts, FSD v2 + backlog win.
- docs/SETUP.md = how to run everything (docker compose, quarkus:dev, static frontend server, demo users).
Note: .claude and .codex live ONLY inside diplomarbeit-papo/ (outer copies deleted).

# GLOBAL WORKING RULES (already in ~/.claude/CLAUDE.md, apply automatically)
- Spec-driven via OpenSpec: propose -> apply tasks.md top-down -> validate -> archive. Proactively use plan mode before new features/changes.
- Branch per phase/task-cluster (not always main); commit in small logical chunks; pause at natural checkpoints for review rather than grinding through everything unattended.
- Git commit/push: user (Chris) runs these himself now - just hand him the commit message, don't run git commit/push without being asked.

# STATUS (2026-07-03, verify against tasks.md before trusting)
- Phase 0 (Foundation) done, on main.
- Phase 1 (Data Model & Core CRUD) done on branch `phase-1/data-model-crud`, NOT yet merged to main:
  - Backend: entities (Partner, Product, ProductEnablement, Kunde, Order->table "orders", OrderItem, CommissionRecord), Product+Partner REST/service CRUD, partner self-edit (FR-08), programmatic dev/test seeder (DemoDataSeeder - NOT import.sql, see design.md for why). 12/12 backend tests pass.
  - Frontend: company-product-list-page, company-partner-list-page (commission dropdown 5/10/15+custom), partner-profile-page, nav links, modal-dialog got a title-reactivity fix.
  - CAVEAT: frontend UI was built and syntax-checked (node --check) plus backend-contract-verified (test JSON shapes), but NEVER manually browser-tested - no browser tool was available in that session and docker-compose's Postgres port conflicted with an unrelated already-running container. Chris now has Playwright installed.

# FIRST THING TO DO IN THIS SESSION
Playwright is now available. Before anything else: start the stack per docs/SETUP.md (docker compose up -d, backend `quarkus:dev`, frontend static server), then use Playwright to actually click through what was built on `phase-1/data-model-crud`:
- Login as company-demo and partner-demo (both work, correct role landing).
- Company: Produkte list loads (8 seeded), create a product, edit a product, availability toggle reflected.
- Company: Partner list loads (3 seeded), create a partner, edit a partner, commission dropdown (5/10/15 + individuell) works both ways (preset <-> custom value round-trips correctly on edit).
- Partner (partner-demo, linked to seeded partner P-1001): Mein Profil loads own data (partnerNumber/commission/status read-only, contact fields editable), self-edit saves and does NOT expose/alter commissionPercent or active.
- Deep-link protection still holds (partner hitting /company/products redirects away).
Fix whatever Playwright finds. Report findings plainly - this is the first real browser verification of this UI, treat any bug found as expected, not alarming.

# HARD RULE
Meetings override the FSD when they conflict (newer = authoritative). FSD v2 + phase2_backlog.md already encode the meeting decisions. Do not reintroduce v1 assumptions.

# LOCKED SCOPE (MVP)
Internal B2B partner-ordering portal. Core process = partner logs in (Keycloak) -> selects or creates end customer (Kunde) -> places order (multiple articles + quantities) on customer's behalf -> order auto-assigned to partner -> on-screen confirmation + simulated email (no real send) -> company advances status submitted -> reviewed -> fulfilled and handles fulfillment.
- Commission: field on Partner (Partner.commissionPercent). Company sets via dropdown 5/10/15 + optional custom value. Percentage of total order value. Stored as own CommissionRecord. NO separate CommissionLevel entity. Auto level-up deferred (B-03).
- Active status: DB-only flag Partner.active, enforced server-side after login. Keycloak stays simple (no enabled sync).
- Kunde = reusable entity (select existing or create new); mandatory last name + address; other fields optional. Scoped to the creating partner - a partner never sees another partner's Kunden.
- Multiple articles per order (article + quantity per line). OrderItem stores unit-price snapshot.
- Partner dashboard totals (commission/revenue/order count) = fulfilled orders only; latest-orders list shows any status.
- Excel/CSV import of partners + articles (real, full: column mapping + duplicate detection + validation report).
- Roles: company, partner (Keycloak). End customers do NOT use the portal in MVP. Keycloak users provisioned manually, never by import.

# FIXED TECH STACK (NO ALTERNATIVES - do not suggest others)
Frontend: HTML, CSS, native Web Components / Custom Elements (no framework, no Bootstrap/Angular/React/etc).
Backend: Quarkus (Java), Hibernate ORM Panache, Quarkus REST (formerly RESTEasy Reactive), OIDC.
Database: PostgreSQL.
Auth: Keycloak.
Import libs: Apache POI (xlsx) + opencsv (csv).

# DATA MODEL (entities)
Partner (incl. commissionPercent, active, keycloakUsername), Product, ProductEnablement (partner x product),
Kunde (incl. ownerPartner), Order (table "orders" - reserved SQL word), OrderItem (unit-price snapshot),
CommissionRecord. No CommissionLevel entity. Money = BigDecimal everywhere.

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
Chris implements (backend + functional frontend). Tobias does frontend visual design separately (Claude design), delivered async as CSS into frontend/src/styles/base.css - implementation does not wait for it. Julian left the project.

# STILL-OPEN QUESTIONS (do not assume - ask if they block you)
- Research question exact wording (direction decided: reframe toward internal Partner <-> Company chain; confirm with supervisor).
- Import file column formats for the partner + article Excel/CSV files - waiting on sample data from the client (blocks final import implementation only).

# ALREADY RESOLVED (FSD section 13 - do not reopen)
- Dashboard totals: fulfilled orders only (commission/revenue/order count); latest-orders list shows any status.
- Commission: Partner.commissionPercent, dropdown 5/10/15 + custom. No level entity.
- Optional Kunde fields on order form: first name, e-mail, phone.
- Kunde scoping: per creating partner, not shared across partners.
- Affiliate identifier: deferred, not in MVP.
- Schema management: Hibernate auto-generation, no Flyway (drop-and-create + programmatic seed for dev/test, update for demo/prod).
- Keycloak user provisioning: import creates Partner records only; logins created manually, linked via username claim.

# OPENSPEC WORKFLOW
Change "build-partnerportal-mvp": tasks.md is the task/progress source of truth; IMPLEMENTATION_PLAN.md is the narrative plan kept in sync. CLI: "npx --yes @fission-ai/openspec@latest <cmd>". Dev environment: docker-compose Keycloak+Postgres for dev/demo, Dev Services for tests only - see docs/SETUP.md.

# HOW TO WORK
- Strict, precise, realistic for a 2-person HTL diploma. No enterprise overkill, no generic PRM filler.
- Stay on the Company -> Partner -> End Customer model; keep roles/areas clean.
- Requirements/tasks concrete and testable.

# NEXT STEP
Do the Playwright verification pass above first. Then tell me: <STATE WHAT YOU WANT NEXT - e.g. fix what Playwright found, merge phase-1/data-model-crud, continue to Phase 2 (access control + enablement) via /opsx:apply>.
```

Fill the last line with what you want next.
```
