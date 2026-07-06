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
- Specification/functional_specification_document_full_vision.md = whole-project vision (MVP + all deferred B-01..B-09 folded into one narrative + roadmap). Reference only, not the build target.
- Specification/phase2_backlog.md = deferred features B-01..B-09 + append-only decisions log. HONOR THIS.
- Specification/IMPLEMENTATION_PLAN.md = flat implementation plan (8 sections, WP1-WP13, phases).
- Specification/CONTINUATION_PROMPT.md = this file.
- Documentation/Besprechungsprotokolle/ = client meeting notes (17.06 + 24.06.2026).
- openspec/changes/build-partnerportal-mvp/ = MVP OpenSpec change (schema spec-driven), NOT archived yet (8.3-8.5/FD.x still open, see WHAT'S LEFT). tasks.md checkboxes = progress truth.
- openspec/changes/archive/ = 8 archived changes: 2026-07-04-fix-audit-hardening, 2026-07-05-add-german-locale-formatting, 2026-07-06-add-dashboard-date-filter (superseded, not built - see below), 2026-07-06-integrate-frontend-design, 2026-07-06-add-company-dashboard, 2026-07-06-add-csv-export, 2026-07-06-add-list-search-pagination, 2026-07-06-add-product-category-image.
- openspec/specs/ = published capability specs (company-dashboard, partner-dashboard, data-export, locale-formatting, partner-management, product-management, product-enablement) - source of truth for what's actually built, kept in sync on each archive.
- CLAUDE.md (repo root) = project memory aligned with FSD v2; where anything conflicts, FSD v2 + backlog win.
- docs/SETUP.md = how to run everything (docker compose, quarkus:dev, static frontend server, demo users).
- docs/DATA_MODEL.md = ER diagram + entity descriptions (Phase 7).
- docs/CONVENTIONS.md = backend/frontend naming and structure conventions.
- Design/ = Tobias's design-system reference docs (tokens.json, variables.css, guidelines/) - committed to main, kept for reference; the actual shipped design lives directly in frontend/ (see below), not copy-pasted from here.
- frontend/vendor/lucide-1.23.0.min.js = pinned local Lucide icon build (was a live unpkg.com CDN import - vendored for offline-safe demo, do not revert to a CDN script tag).
Note: .claude and .codex live ONLY inside diplomarbeit-papo/ (outer copies deleted).

# GLOBAL WORKING RULES (already in ~/.claude/CLAUDE.md, apply automatically - no need to repeat the OpenSpec schema itself, it's global)
- Spec-driven via OpenSpec: propose -> apply tasks.md top-down -> validate -> archive. Proactively use plan mode before new features/changes.
- Branch per phase/task-cluster (not always main); commit in small logical chunks; pause at natural checkpoints for review rather than grinding through everything unattended.
- Git commit/push/merge: Chris runs these himself, always - just hand him a ready-to-use commit message and which branch to act on. Do not run commit/push/merge yourself even mid-implementation, even if asked to "continue" - only on an explicit in-the-moment ask (e.g. "commit this", "merge now").
- End every implementation turn by stating the branch name + a ready commit message, unprompted (Chris juggles multiple parallel change branches).

# STATUS (2026-07-07, verify against `openspec list --json` before trusting)
MVP core (Phases 0-6, build-partnerportal-mvp: 46/53) is DONE and merged to main.
Post-MVP work also DONE and merged: fix-audit-hardening (security/robustness), add-list-search-pagination,
add-company-dashboard, add-csv-export, add-german-locale-formatting (de-AT money/date formatting incl. CSV),
add-product-category-image (product image thumbnails), a small order-form bugfix (hidden "neuer Kunde" fields kept
`required`, blocked submit - fixed), and the big one:

**Tobias pushed the full visual design as working code** (`76bbe40 Frontend Design`, 2026-07-05/06) - not just
tokens, a full rewrite of both dashboards, metric-card, router, base.css (+2024 lines), and a period-picker.
It initially referenced 7 backend fields that didn't exist (NaN/crashes) and pulled in an unpinned external
Lucide CDN. Change `integrate-frontend-design` (IMPLEMENTED, ARCHIVED 2026-07-06) reconciled this:
- Both dashboards now aggregate client-side from the order-list endpoints (new `frontend/src/lib/dashboard-data.js`)
  - the period-picker previously sent `?from&to` the backend silently ignored; it now actually filters.
- Kept (client-side, no backend change): month chart, Bestellstatus-Donut, Top-Kunden.
- Added one trivial backend field: `PartnerDashboardDto.totalProductCount` (`Product.count()`) for the products ring.
- Removed the "ggü. Vorperiode" trend indicators entirely -> deferred as **B-08** in phase2_backlog.md (not built).
- Lucide vendored + pinned locally (`frontend/vendor/lucide-1.23.0.min.js`) - no more live CDN script tag.
- `DemoDataSeeder` now seeds ~21 historical orders + Kunden (dev-only, gated on `LaunchMode.DEVELOPMENT`
  specifically - NOT the broader `!= NORMAL` check other seed methods use - tests assume a zero-order
  baseline for certain partners and would break if orders were seeded under `%test` too).
- `add-dashboard-date-filter` (planned separately, before Tobias's push landed) turned out to be fully
  superseded by his period-picker - archived as superseded, never built as its own change.
- Backend 46/46 tests green throughout. Both dashboards browser-verified: no NaN, umlauts render, period
  filter proven to change totals live.

Product image thumbnails (9.3, IMPLEMENTED, ARCHIVED 2026-07-06):
- `data-table` supports an opt-in `image: true` column type; image URLs are escaped into `src`, never rendered as raw HTML.
- Company product list and partner enabled-products list show a `Bild` thumbnail column.
- Empty or failed images fall back to a neutral placeholder via a capture-phase `error` listener, with no inline JS.
- Demo products use local SVG assets in `frontend/assets/products/`; "Wasserwaage 100cm" intentionally has no image to exercise the placeholder path.
- Browser-verified after merging Tobias's responsive layout changes: company + partner thumbnails load locally, no external image requests, dead URL falls back, malicious `imageUrl` does not execute, umlauts render.

Branch history: everything above is merged to `main` via --no-ff merges, main == origin/main, fully pushed.

# WHAT'S LEFT
- 8.3 (T) UI/UX documentation - Tobias's task alone (T, not C/C+T). Not attempted by Chris/Claude.
- 8.4 (C+T) Diploma writeup linking FSD requirements -> implementation -> tests, noting deferred
  B-01..B-09 - thesis-body writing in Chris's voice. Chris wants this THOROUGH (more effort, real
  prose + a mapping table, not just a checklist) - his explicit call 2026-07-05. Not started.
- 8.5 (C+T) Research question wording - Chris approved this wording on 2026-07-07 for supervisor review:
  "Wie kann ein webbasiertes Partnerportal gestaltet werden, das Unternehmen, Vertriebspartner und Endkunden
  in einem partnerbasierten Vertriebsmodell schrittweise verbindet und dabei Partnerverwaltung, Produktfreigabe,
  Bestellabwicklung, Provisionszuordnung und Auswertungen in einer benutzerfreundlichen und erweiterbaren
  Plattform zusammenfuehrt?" Supervisor sign-off is still pending.
- FD.1-FD.4 Frontend visual design - Tobias's core deliverable already landed + reconciled (see STATUS
  above). Any further visual polish passes from Tobias would still need the same reconciliation pattern
  (check for missing-backend-field references before trusting a new page redesign renders correctly).
- Client (Hammer) meeting was scheduled Di 2026-07-07 10:30 - if it already happened, ask Chris whether
  sample partner/article import files were obtained (the one remaining open import question - not a
  blocker, the import UI's column-mapping handles any layout regardless).

# HOW TO RESUME
Ask Chris what he wants next rather than assuming: diploma writeup (8.4), UI/UX docs coordination
(8.3, Tobias-owned), research-question follow-up (8.5), client import-sample follow-up, or something new entirely.

# HARD RULE
Meetings override the FSD when they conflict (newer = authoritative). FSD v2 + phase2_backlog.md already encode the meeting decisions. Do not reintroduce v1 assumptions.

# LOCKED SCOPE (MVP) - already built, described here for orientation only
Internal B2B partner-ordering portal. Core process = partner logs in (Keycloak) -> selects or creates end customer (Kunde) -> places order (multiple articles + quantities) on customer's behalf -> order auto-assigned to partner -> on-screen confirmation + simulated email (no real send) -> company advances status submitted -> reviewed -> fulfilled and handles fulfillment.
- Commission: field on Partner (Partner.commissionPercent). Company sets via dropdown 5/10/15 + optional custom value. Percentage of total order value. Stored as own CommissionRecord. NO separate CommissionLevel entity. Auto level-up deferred (B-03).
- Active status: DB-only flag Partner.active, enforced server-side after login (CurrentPartner.resolve()). Keycloak stays simple (no enabled sync).
- Kunde = reusable entity (select existing or create new); mandatory last name + address; other fields optional. Scoped to the creating partner - a partner never sees another partner's Kunden.
- Multiple articles per order (article + quantity per line). OrderItem stores unit-price snapshot.
- Partner dashboard totals (commission/revenue/order count) = fulfilled orders only; latest-orders list shows any status. Both dashboards now also support a Von/Bis period-picker (client-side filtering, see STATUS).
- Excel/CSV import of partners + articles: column mapping (with auto-suggested defaults) + duplicate detection + validation report.
- Roles: company, partner (Keycloak). End customers do NOT use the portal in MVP. Keycloak users provisioned manually, never by import.
- All money/dates render German (de-AT): `€ 1.234,56`, `dd.MM.yyyy`, incl. CSV exports (decimal comma, no thousands grouping, so German-locale Excel parses them as numbers).

# FIXED TECH STACK (NO ALTERNATIVES - do not suggest others)
Frontend: HTML, CSS, native Web Components / Custom Elements (no framework, no Bootstrap/Angular/React/etc). Lucide icons vendored locally (pinned version, no CDN).
Backend: Quarkus (Java), Hibernate ORM Panache, Quarkus REST (formerly RESTEasy Reactive), OIDC.
Database: PostgreSQL.
Auth: Keycloak.
Import libs: Apache POI (xlsx) + opencsv (csv, also used for CSV export).

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
B-08 dashboard "ggü. Vorperiode" trend indicators (CUT; Tobias's design built the UI, backend doesn't support it, marginal value for an internal tool - added 2026-07-06)
To build any later, user says "implement B-XX".

# TEAM
Chris implements (backend + functional frontend). Tobias does frontend visual design; delivered as working
code directly into frontend/ (not just CSS tokens as originally planned) - see STATUS for the reconciliation
that was needed. Julian left the project.

# STILL-OPEN QUESTIONS (do not assume - ask if they block you)
- Research question supervisor sign-off - wording is approved by Chris (2026-07-07); confirm whether the
  supervisor accepted it.
- Import file column formats for the partner + article Excel/CSV files - client sample data status unknown
  (client meeting was scheduled 2026-07-07). Not a blocker: the import UI's column-mapping step handles any
  layout, with a best-guess default mapping based on known German header aliases.

# ALREADY RESOLVED (do not reopen)
- Dashboard totals: fulfilled orders only (commission/revenue/order count); latest-orders list shows any status. Both dashboards additionally support a client-side Von/Bis period filter.
- Commission: Partner.commissionPercent, dropdown 5/10/15 + custom. No level entity.
- Optional Kunde fields on order form: first name, e-mail, phone.
- Kunde scoping: per creating partner, not shared across partners.
- Affiliate identifier: deferred, not in MVP.
- Schema management: Hibernate auto-generation, no Flyway (drop-and-create + programmatic seed for dev/test, update for demo/prod).
- Keycloak user provisioning: import creates Partner records only; logins created manually, linked via username claim.
- German (de-AT) formatting for all money/dates in the UI and CSV exports.
- Dashboard "ggü. Vorperiode" trend: deferred as B-08, not built.
- Sidebar color scheme / role-distinct accents: light sidebar, Blau (company) / Teal (partner) accents kept - provisional per Chris, may change when Tobias finalizes.

# DEV ENVIRONMENT GOTCHAS (learned the hard way - check before re-debugging)
- An unrelated `my_postgres` container on this machine often squats port 5432; `docker stop my_postgres` before `docker compose up -d` if the compose stack fails to bind.
- The frontend is a hash-router SPA served by a plain static server - navigating between hash routes does NOT reload the document, so edited JS files can appear stale. Force a real navigation (change the path/query, not just the hash) and, if testing via Playwright, disable the browser cache via a CDP session (`Network.clearBrowserCache` + `setCacheDisabled`) before trusting what renders - even that sometimes needs a full `page.goto` to `about:blank` first to actually drop cached ES modules.
- Keycloak access tokens expire in ~5-6 minutes; a Playwright session left idle will silently bounce to /login on the next API call - not a bug, just relogin.
- Quarkus dev mode (`quarkus:dev`) uses `drop-and-create` schema + `DemoDataSeeder` - every JVM restart wipes and reseeds the dev DB. Any manually-created test data (partners, orders, Kunden) from a prior session is gone after a restart. The seeder now also creates ~21 historical demo orders (dev-only, not under `%test`) so the dashboards/charts demo with real data out of the box.
- If a stale `quarkus:dev` process is already running on :8080 from a previous session, killing and restarting it is often necessary to pick up entity/seeder changes (dev-mode hot reload does not always cover new `@Observes StartupEvent` seed logic or new entity fields cleanly).

# HOW TO WORK
- Strict, precise, realistic for a 2-person HTL diploma. No enterprise overkill, no generic PRM filler.
- Stay on the Company -> Partner -> End Customer model; keep roles/areas clean.
- Requirements/tasks concrete and testable.
- When a large piece of external work lands (e.g. a design push, an import file), verify it against the
  real backend/data before trusting it renders correctly - don't assume it "just works" because it looks done.

# HARD REQUIREMENT: UMLAUTS
Client is Austrian - ä/ö/ü/ß correctness is a hard requirement in every text I/O path (import
column headers/parsing, CSV export, search matching, any rendered name/address), not polish.
Every change touching text I/O needs an explicit umlaut test/scenario.

# NEXT STEP
Ask Chris what he wants next (see HOW TO RESUME). Do not assume. Fill in his answer here: <STATE WHAT YOU WANT NEXT>.
```
