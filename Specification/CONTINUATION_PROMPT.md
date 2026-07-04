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
- openspec/changes/build-partnerportal-mvp/ = MVP OpenSpec change (schema spec-driven), NOT archived yet (8.3-8.5/FD.x still open, see WHAT'S LEFT). tasks.md checkboxes = progress truth.
- openspec/changes/archive/2026-07-04-fix-audit-hardening/ = archived: post-MVP audit fixes (XSS, import umlaut/charset, commission validation, keycloakUsername link, forward-only order status, N+1). Fully implemented + tested.
- openspec/changes/add-list-search-pagination/, add-company-dashboard/, add-csv-export/ = planned (not yet implemented), dependency-ordered in that sequence. One OpenSpec change per feature, deliberately NOT bundled into one mega-change (Chris's explicit preference 2026-07-04).
- CLAUDE.md (repo root) = project memory aligned with FSD v2; where anything conflicts, FSD v2 + backlog win.
- docs/SETUP.md = how to run everything (docker compose, quarkus:dev, static frontend server, demo users).
- docs/DATA_MODEL.md = ER diagram + entity descriptions (Phase 7).
- docs/CONVENTIONS.md = backend/frontend naming and structure conventions.
Note: .claude and .codex live ONLY inside diplomarbeit-papo/ (outer copies deleted).

# GLOBAL WORKING RULES (already in ~/.claude/CLAUDE.md, apply automatically - no need to repeat the OpenSpec schema itself, it's global)
- Spec-driven via OpenSpec: propose -> apply tasks.md top-down -> validate -> archive. Proactively use plan mode before new features/changes.
- Branch per phase/task-cluster (not always main); commit in small logical chunks; pause at natural checkpoints for review rather than grinding through everything unattended.
- Git commit/push/merge: Chris runs these himself, always - just hand him a ready-to-use commit message and which branch to act on. Do not run commit/push/merge yourself even mid-implementation, even if asked to "continue" - only on an explicit in-the-moment ask (e.g. "commit this", "merge now").

# STATUS (2026-07-04, verify against tasks.md before trusting)
MVP core (Phases 0-6) is DONE and merged to main: 41/53 tasks complete.
- Phase 0 Foundation, Phase 1 Data Model & CRUD, Phase 2 Access Control & Enablement,
  Phase 3 Ordering Core, Phase 4 Commission & Dashboard, Phase 5 Data Import,
  Phase 6 Testing, Phase 7 Documentation (partial: 8.1/8.2 done) - all merged to `main`.
- Backend: 33/33 tests pass (pre-hardening baseline; 40/40 after the hardening change below).
- Branch-per-phase history on origin: phase-1/data-model-crud through phase-7/documentation,
  all merged into main via --no-ff merge commits (keeps phase boundaries visible in history).

Post-MVP work (2026-07-04): a full-code audit found 1 security defect (stored XSS via
unescaped `innerHTML` in `data-table.js` + a few pages) + several robustness gaps (case-sensitive
import dup-check, umlaut-blind header auto-suggest, UTF-8-only CSV import, unbounded order
status transitions, no admin way to link a Keycloak login, unvalidated commission bounds).
Fixed in OpenSpec change `fix-audit-hardening` - IMPLEMENTED, TESTED (40/40 backend +
live Playwright verification with real XSS payloads and umlaut/ISO-8859-1 files), ARCHIVED
to `openspec/changes/archive/2026-07-04-fix-audit-hardening/`. NOT YET COMMITTED to git -
working tree has the changes staged for a commit (see git status), Chris runs commit/push himself.

Three more OpenSpec changes are planned but NOT YET IMPLEMENTED, in this dependency order:
1. `add-list-search-pagination` (9.4) - search/pagination on the 3 large lists (data-table
   `.searchKeys`/`.pageSize`, umlaut-aware matching). Depends on fix-audit-hardening (same
   data-table render path) - satisfied.
2. `add-company-dashboard` (9.1+9.2) - replaces the company-dashboard placeholder with
   real totals + per-partner settlement, one endpoint, no N+1.
3. `add-csv-export` (9.5) - order + commission CSV export, opencsv + UTF-8 BOM, authenticated
   blob-download helper (plain links would 401 - no Bearer token).

# WHAT'S LEFT
- The 3 planned changes above (9.1/9.2/9.4/9.5) - build via `/opsx:apply <change-name>` in order.
- 9.3 [opt] product category/image UI - not selected/planned yet, ask if wanted.
- 8.3 (T) UI/UX documentation - Tobias's task alone (T, not C/C+T). Not attempted by Chris/Claude.
- 8.4 (C+T) Diploma writeup linking FSD requirements -> implementation -> tests, noting
  deferred B-01..B-07 - thesis-body writing in Chris's voice. Paused pending Chris deciding
  scope (outline/traceability draft vs full prose vs skip) - ask, don't assume, if picking this up.
- 8.5 (C+T) Research question wording - blocked on supervisor confirmation, not something to
  finalize unilaterally.
- FD.1-FD.4 (T/C+T) Frontend visual design (tokens, base.css integration, polish pass) -
  Tobias's deliverable, async, does not block anything else. Wait for Tobias's Claude-design
  output before touching base.css; do not build ahead of it.
- Archiving `build-partnerportal-mvp` itself - still open because 8.3-8.5/FD.x aren't done;
  not blocking anything, can stay open indefinitely.

# HOW TO RESUME
Default next step (if Chris doesn't say otherwise): implement `add-list-search-pagination` via
`/opsx:apply add-list-search-pagination`, then the other two in order. Otherwise ask what he
wants: continue the planned changes, help with the diploma writeup at whatever scope he
specifies, or something else entirely.

# HARD RULE
Meetings override the FSD when they conflict (newer = authoritative). FSD v2 + phase2_backlog.md already encode the meeting decisions. Do not reintroduce v1 assumptions.

# LOCKED SCOPE (MVP) - already built, described here for orientation only
Internal B2B partner-ordering portal. Core process = partner logs in (Keycloak) -> selects or creates end customer (Kunde) -> places order (multiple articles + quantities) on customer's behalf -> order auto-assigned to partner -> on-screen confirmation + simulated email (no real send) -> company advances status submitted -> reviewed -> fulfilled and handles fulfillment.
- Commission: field on Partner (Partner.commissionPercent). Company sets via dropdown 5/10/15 + optional custom value. Percentage of total order value. Stored as own CommissionRecord. NO separate CommissionLevel entity. Auto level-up deferred (B-03).
- Active status: DB-only flag Partner.active, enforced server-side after login (CurrentPartner.resolve()). Keycloak stays simple (no enabled sync).
- Kunde = reusable entity (select existing or create new); mandatory last name + address; other fields optional. Scoped to the creating partner - a partner never sees another partner's Kunden.
- Multiple articles per order (article + quantity per line). OrderItem stores unit-price snapshot.
- Partner dashboard totals (commission/revenue/order count) = fulfilled orders only; latest-orders list shows any status.
- Excel/CSV import of partners + articles: column mapping (with auto-suggested defaults) + duplicate detection + validation report.
- Roles: company, partner (Keycloak). End customers do NOT use the portal in MVP. Keycloak users provisioned manually, never by import.

# FIXED TECH STACK (NO ALTERNATIVES - do not suggest others)
Frontend: HTML, CSS, native Web Components / Custom Elements (no framework, no Bootstrap/Angular/React/etc).
Backend: Quarkus (Java), Hibernate ORM Panache, Quarkus REST (formerly RESTEasy Reactive), OIDC.
Database: PostgreSQL.
Auth: Keycloak.
Import libs: Apache POI (xlsx) + opencsv (csv).

# DATA MODEL (entities) - see docs/DATA_MODEL.md for full ER diagram + descriptions
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
- Import file column formats for the partner + article Excel/CSV files - client sample data still not provided. Not a blocker: the import UI's column-mapping step handles any layout, with a best-guess default mapping based on known German header aliases.

# ALREADY RESOLVED (FSD section 13 - do not reopen)
- Dashboard totals: fulfilled orders only (commission/revenue/order count); latest-orders list shows any status.
- Commission: Partner.commissionPercent, dropdown 5/10/15 + custom. No level entity.
- Optional Kunde fields on order form: first name, e-mail, phone.
- Kunde scoping: per creating partner, not shared across partners.
- Affiliate identifier: deferred, not in MVP.
- Schema management: Hibernate auto-generation, no Flyway (drop-and-create + programmatic seed for dev/test, update for demo/prod).
- Keycloak user provisioning: import creates Partner records only; logins created manually, linked via username claim.

# DEV ENVIRONMENT GOTCHAS (learned the hard way - check before re-debugging)
- An unrelated `my_postgres` container on this machine often squats port 5432; `docker stop my_postgres` before `docker compose up -d` if the compose stack fails to bind.
- The frontend is a hash-router SPA served by a plain static server - navigating between hash routes does NOT reload the document, so edited JS files can appear stale. Force a real navigation (change the path/query, not just the hash) and, if testing via Playwright, disable the browser cache via a CDP session (`Network.clearBrowserCache` + `setCacheDisabled`) before trusting what renders.
- Keycloak access tokens expire in ~5-6 minutes; a Playwright session left idle will silently bounce to /login on the next API call - not a bug, just relogin.
- Quarkus dev mode (`quarkus:dev`) uses `drop-and-create` schema + `DemoDataSeeder` - every JVM restart wipes and reseeds the dev DB. Any manually-created test data (partners, orders, Kunden) from a prior session is gone after a restart.

# HOW TO WORK
- Strict, precise, realistic for a 2-person HTL diploma. No enterprise overkill, no generic PRM filler.
- Stay on the Company -> Partner -> End Customer model; keep roles/areas clean.
- Requirements/tasks concrete and testable.

# HARD REQUIREMENT: UMLAUTS
Client is Austrian - ä/ö/ü/ß correctness is a hard requirement in every text I/O path (import
column headers/parsing, CSV export, search matching, any rendered name/address), not polish.
Every change touching text I/O needs an explicit umlaut test/scenario.

# NEXT STEP
Default: continue with the 3 planned changes in order (see HOW TO RESUME). If Chris wants
something else, ask rather than assume. Fill in his answer here: <STATE WHAT YOU WANT NEXT>.
```
