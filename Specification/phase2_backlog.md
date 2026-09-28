# Phase-2 / Deferred Feature Backlog — Partnerportal

Purpose: single place for every feature that is **cut from MVP**, **simulated/stubbed in MVP**, or **not yet fully built**.
When Chris wants a full build, reference the ID here (e.g. "implement B-01").

Status legend:
- `CUT` = not in MVP at all, only specified here.
- `STUB` = partially in MVP (simulated / manual / mock), full version deferred.
- `PLANNED` = agreed for later, not started.
- `REOPENED FOR DECISION` = Chris wants to revisit the previously excluded scope;
  business rules, school/company approval and implementation contract remain open.

Last updated: 2026-09-25. Chris möchte Phase 2 und weitere Produktfunktionen bis zur
Herbstferien-Staging-Version verfolgen. Das hebt die bisherigen MVP-Abgrenzungen nicht
rückwirkend auf. Fachliche Regeln und Freigaben durch Firma/Schule sind teils noch offen;
die Reihenfolge und Fragen stehen in `PRODUCT_COMPLETION_PLAN.md`.

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
**What full version is:** Real SMTP/service sends an order confirmation to the Endkunde when an address is present, an order notice to the responsible partner users, and a new-order notice to the company. Shipping confirmation remains a later extension unless separately specified.
**MVP state (STUB):** On-screen confirmation + simulated email rendered in-app (no real send).
**Why stub:** Prototype-safe, no mail infra. Meeting wanted "automatisierte Bestellbestätigung + Versandbestätigung".
**Decision 2026-08-25:** Chris confirmed the three order-related recipients above. The feature remains `STUB` until a separate OpenSpec change defines delivery status, retries, duplicate prevention, optional recipient addresses, privacy, and the SMTP integration.
**Trigger:** "implement B-04".

## B-05 — Shop inquiry auto-forward (Anfragen)  `CUT`
**What full version is:** Inquiries coming from the external shop auto-forward to the responsible partner; other inquiries partner enters manually in portal (Prozess 1).
**Why cut:** Auto-forward half depends on B-01 (shop). Manual entry part = already covered by MVP core order flow.
**Trigger:** "implement B-05". Needs B-01.

## B-06 — BMD interface  `REOPENED FOR DECISION`
**What:** Direct BMD accounting-system interface.
**Status:** Explicitly excluded from the MVP by meeting 17.06 ("Keine BMD Schnittstelle, zuerst Daten importieren"). Chris reopened the product goal on 25.09.2026; the company and school have not confirmed scope. Excel/CSV remains the current implemented transfer. Product/version, interface, direction (articles in; possibly orders/commissions out), conflict rules and test access are unknown. Do not implement from an invented contract.

## B-07 — Commission settlement / payout workflow  `REOPENED FOR DECISION`
**What full version is:** Real payout/settlement of accumulated commission.
**Why cut from MVP:** The FSD only shows commission transparently (own record + dashboard sums).
**Current direction (25.09.2026):** Chris confirmed the sequence: first implement an auditable periodic settlement with approval and export; then separately specify and plan real bank transfers, and implement them later. This is a product-direction decision, not company approval of the formula, period, approvers or bank interface. The settlement rules remain unconfirmed by the company. Transfers need payment-provider/bank access, authorization, reconciliation and legal review; neither stage is implementation-ready until its own rules and acceptance criteria are confirmed.

## B-08 — Dashboard "ggü. Vorperiode" trend indicators  `STUB`
**What full version is:** Metric cards across the relevant dashboards show a +/-x% delta vs the previous equal-length period (e.g. "+4.6% ggü. Vorperiode"), with a red/green trend arrow and sufficient historical data for meaningful comparison.
**MVP state (STUB):** The partner dashboard already computes the previous equal-length period client-side from the loaded order list and displays trend indicators when an explicit date range is selected. The company dashboard and a broader cross-dashboard trend analysis remain deferred.
**Why stub:** The small partner-side comparison is implemented and useful without a backend change; complete company-wide analytics would add marginal MVP value and needs representative historical data.
**Trigger to complete:** "implement full B-08".

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
- 2026-07-03: Dashboard totals decided — commission/revenue/order count on the partner dashboard count only `fulfilled` own orders; latest-orders list still shows any status.
- 2026-07-03: Phase 1 backend implemented on branch `phase-1/data-model-crud` (entities, Product/Partner CRUD + self-edit, programmatic dev/test seeder). Kunde records decided to be scoped to the creating partner (new requirement, was an unspecified gap).
- 2026-07-06: Tobias pushed the visual design as working code (`76bbe40 Frontend Design`), which rewrote both dashboards and added widgets referencing backend data that doesn't exist. Scope call (Chris): reconcile via OpenSpec change `integrate-frontend-design` — keep the period-picker + the cheap client-side-derivable widgets (monthly chart, Bestellstatus-Donut, Top-Kunden — all computable from the order list), vendor+pin the Lucide icon dependency (was `unpkg.com/lucide@latest`), seed historical demo orders so the dashboards demo well. Only the "ggü. Vorperiode" trend is fully deferred → B-08. The planned change `add-dashboard-date-filter` is superseded by Tobias's period-picker (do not implement).
- 2026-07-13: Client meeting 10.07.2026 (Teams; Prof. Hammer, Chris, Tobias, Firma) captured in `Documentation/Besprechungsprotokolle/Besprechungsprotokoll10_07_2026.txt`. MVP well received. New client priorities: (1) simple Benutzerführung + good mobile version (partners often not tech-savvy — Tobias's lane), (2) partner can see Endkunden who haven't ordered in a while, (3) product picker in the order form needs Suchleiste + Filter, (4) order-list filters by Endkunde/Partner (time filter re-confirmed from 07.07). Hosting: company provides **Hetzner** instances (Christian Bachl contacts Chris) — supersedes the Oracle-Cloud assumption in `ci-cd-deployment-with-gh-actions` (re-scope before building). Company delivers real data/Vorgehensweisen within 1–2 weeks → implement → new Termin. Also from Chris's Nachtrag: fix the quick Keycloak logout (→ change `add-keycloak-session-refresh`), remove the unclear "Keycloak-Username" field from partner create (repeat of 07.07; decided 2026-07-13: link via `Partner.partnerNumber` instead).
- 2026-08-24: B-08 reclassified from `CUT` to `STUB` to match the existing client-side previous-period comparison in the partner dashboard. Full/company-wide trend analytics remain deferred.
- 2026-08-25: PAPO is to be designed company-independently; the reference company is a test case. A shared multi-company platform with company subdomains and Keycloak Organizations is the recommended target, but its thesis/MVP scope still requires supervisor confirmation. This is not the same as the B-01 end-customer shop.
- 2026-08-31: The thesis result is a functioning, extensible product; the current one-company demo is not the permanent boundary. Multi-company tenancy/subdomains are the correct architecture when a second company is onboarded, but are explicitly not the next change and have no fixed delivery date yet.
- 2026-08-31: Current partner access is one shared organizational login per partner company; personal partner-employee accounts remain later. User-facing terminology is `Artikel`; internal `Product` names may stay. BMD is the leading article-master source via import. Use its unique article number plus PAPO's technical DB id; no extra visible article id and no price-list-area field. Category remains relevant; Fachbereich, product groups, bundles/BOMs, training permissions and PP/margin logic are future directions pending a simplified company follow-up.
- 2026-08-25: Real order-email recipients clarified for B-04: Endkunde if an address exists, responsible partner users, and company. Current code remains a simulation until B-04 is implemented.
- 2026-08-25: Partner number target clarified: generated `P-####` within a company, imported legacy number as an external reference, and a global technical ID for relations. Whether a partner company receives one shared access or personal employee accounts remains open pending the supervisor's recommendation. The current MVP behavior remains documented in the FSD.
- 2026-09-25: Chris confirmed personal employee logins on both company and partner sides (admin and employee; admin manages/suspends accounts) and audit events with actor/time, without performance scoring. Two manually provisioned test companies suffice initially. The public shop should support guest orders and optional customer accounts, with paid orders fulfilled later and no confirmed online payment. This is a requested future scope, not a statement of current implementation or school/company approval. B-06/B-07 are reopened for decision as above.
- 2026-09-25: B-07 sequence confirmed by Chris: prüfbare Abrechnung/Freigabe/Export zuerst; echte Banküberweisungen danach separat spezifizieren und planen, später implementieren. Firmenregeln und Freigaben bleiben offen.
