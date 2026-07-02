# Phase-2 / Deferred Feature Backlog — Partnerportal

Purpose: single place for every feature that is **cut from MVP**, **simulated/stubbed in MVP**, or **not yet fully built**.
When Chris wants a full build, reference the ID here (e.g. "implement B-01").

Status legend:
- `CUT` = not in MVP at all, only specified here.
- `STUB` = partially in MVP (simulated / manual / mock), full version deferred.
- `PLANNED` = agreed for later, not started.

Last updated: 2026-07-02

---

## B-01 — External end-customer partner shop  `CUT`
**What full version is:** Public shop per partner, partner-branded, shows enabled products, end customer orders directly. Order auto-assigned to partner.
**Why cut:** Meeting 17.06 → #1 process is partner-places-order. Shop = "später, nicht MVP". User decision: fully out of MVP (no clickdummy either).
**MVP replacement:** none. Partner enters orders internally on behalf of Endkunde.
**Trigger to build:** "implement B-01".
**Depends on:** B-02, B-05 (part of same shop).

## B-02 — Shop design customization  `CUT`
**What full version is:** Partner customizes logo, colors, shop name, + 2 layout variants. Central product data (price/description) stays locked.
**Why cut:** Belongs to B-01. Meeting: layout variants "später, nicht MVP".
**Was:** old FSD FR-08 / FR-09.
**Trigger:** "implement B-02". Needs B-01 first.

## B-03 — Automatic provision level-up  `STUB`
**What full version is:** 3 provision levels each with fixed %. System auto-promotes partner when sales cross a threshold ("mehr Verkäufe -> LevelUp").
**MVP state (STUB):** Level + % set manually by company at partner creation, changeable anytime. No auto logic.
**Why stub:** Meeting mixed "händisch vergeben" + "mehr Verkäufe -> LevelUp". User decision: manual now, threshold as vision.
**Open before build:** define the sales thresholds per level.
**Trigger:** "implement B-03".

## B-04 — Real email sending  `STUB`
**What full version is:** Real SMTP/service sends order confirmation + shipping confirmation to Endkunde.
**MVP state (STUB):** On-screen confirmation + simulated email rendered in-app (no real send).
**Why stub:** Prototype-safe, no mail infra. Meeting wanted "automatisierte Bestellbestätigung + Versandbestätigung".
**Trigger:** "implement B-04".

## B-05 — Shop inquiry auto-forward (Anfragen)  `CUT`
**What full version is:** Inquiries coming from the external shop auto-forward to the responsible partner; other inquiries partner enters manually in portal (Prozess 1).
**Why cut:** Auto-forward half depends on B-01 (shop). Manual entry part = already covered by MVP core order flow.
**Trigger:** "implement B-05". Needs B-01.

## B-06 — BMD interface  `CUT (permanent)`
**What:** Direct BMD accounting-system interface.
**Status:** Explicitly excluded by meeting 17.06 ("Keine BMD Schnittstelle, zuerst Daten importieren"). Not planned. Data comes via Excel import instead.

## B-07 — Commission settlement / payout workflow  `CUT`
**What full version is:** Real payout/settlement of accumulated commission.
**Why cut:** Out of scope all versions per FSD. MVP only shows commission transparently (own datensatz + dashboard sums).
**Trigger:** "implement B-07".

---

## Decisions log (so we don't re-litigate)
- 2026-07-01: Meetings override FSD when they conflict (newer = authoritative).
- 2026-07-01: MVP = internal B2B partner-ordering portal. External shop → B-01 (cut).
- 2026-07-01: Julian left DA. Work split Chris + Tobias only.
- 2026-07-01: Provision = 3 levels, per-partner, manual set/change. Auto level-up → B-03.
- 2026-07-01: Confirmations = on-screen + simulated email. Real send → B-04.
- 2026-07-01: Excel importer = real, full (dedupe + column mapping + validation report).
- 2026-07-01: Endkunde = own Kunde entity (select existing OR create new). Reusable across orders.
- 2026-07-01: Multiple Artikel per order (Artikel+Menge, plural). Old "one product per order" dead.
- 2026-07-01: Tech stack FIXED (no alternatives): HTML, CSS, Web Components/Custom Elements (frontend), Quarkus (backend), PostgreSQL (DB), Keycloak (auth).
- 2026-07-01: FSD rewritten to v2. [Correction 2026-07-02: the v1 backup file was never committed; the v1/full scope lives in functional_specification_document_full_vision.md + git history.]
- 2026-07-01: Commission = `Partner.commissionPercent` field, company sets via dropdown 5/10/15 + optional custom value. No separate CommissionLevel entity. Auto level-up still B-03.
- 2026-07-01: Active status = DB-only flag `Partner.active`, enforced server-side after login. Keycloak stays simple (no enabled sync).
- 2026-07-01: Research question reframed toward internal Partner <-> Company relationship; external shop (Endkunde -> Partner) = Phase-2 (B-01), future only. Exact wording TBD with supervisor.
- 2026-07-01: Project moved into git repo diplomarbeit-papo/. Specs in Specification/, meetings in Documentation/, IMPLEMENTATION_PLAN.md at root. OpenSpec removed (using flat implementation plan instead).
- 2026-07-01: OpenSpec re-introduced (supersedes "OpenSpec removed" above). Change `build-partnerportal-mvp` (proposal + design + 9 capability specs + tasks) is the task source of truth; `Specification/IMPLEMENTATION_PLAN.md` stays as the narrative plan, kept in sync. IMPLEMENTATION_PLAN.md lives in Specification/.
- 2026-07-01: Phase 0 (Foundation & Setup, tasks 1.1–1.8) implemented and committed: Quarkus backend skeleton (REST, Panache, OIDC, Postgres), docker-compose (Postgres + Keycloak + realm export, demo users company-demo/partner-demo), frontend shell (hash router, login, shared Web Components), docs/CONVENTIONS.md.
- 2026-07-02: Review/pre-mortem pass. Confirmed: import column mapping is MVP-essential (per "real, full" decision above) — tasks.md 6.4 de-optionalized. Decided: Keycloak users are provisioned manually (import creates Partner records only, link via username claim). Dev environment convention: docker-compose Keycloak/Postgres for dev+demo, Dev Services for tests only; schema dev/test = drop-and-create, demo/prod = update.
- 2026-07-02: Dashboard totals (all own orders vs fulfilled-only) = OPEN, Chris will decide with Tobias. Interim default: all own orders regardless of status. Import column formats: waiting on sample data from the client.
