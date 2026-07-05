## Why

Tobias delivered the visual design as working code, not just tokens: commit `76bbe40 Frontend Design` (on `main`) rewrites `base.css` (+2024 lines), `metric-card`, `router`, both dashboards, and the partner order list, and adds a Lucide icon dependency. This is FD.1–FD.4 landing at once. It needs a reconciliation pass before it's demo-safe, because parts of it were built against a richer backend than exists — Tobias himself flagged "passt mim backend noch nicht ganz zam" (2026-07-05). This change tracks that reconciliation and the scope decisions it forces.

## What Changes

Three groups:

**1. Backend data mismatches (dashboards currently break).** The redesigned dashboards read DTO fields the backend does not return — they render as `undefined` → `NaN`/empty charts at runtime:
- `CompanyDashboardDto` is missing: `monthLabels`, `monthlyRevenue`, `monthlyOrderCount`.
- `PartnerDashboardDto` is missing: `monthLabels`, `monthlyRevenue`, `previous` (for "ggü. Vorperiode"), `topClients`, `totalProductCount`.

**2. Out-of-scope widgets, now built in code.** The design realises widgets we had agreed to cut/defer for the MVP (memory `tobi-design-mockups-scope-gaps`): "ggü. Vorperiode" trend, Top-Kunden, monthly revenue/order sparklines & charts, enabled-vs-total-products ring, Bestellstatus-Donut. Each maps to a missing field above. **Per widget the team must decide: CUT (strip from the frontend, keep the MVP metric set) or BUILD (extend the backend DTO + aggregation to feed it).**

**3. External dependency + integration hygiene.** `index.html` loads Lucide from `https://unpkg.com/lucide@latest` — an external, **unpinned** CDN. For an Austrian school demo (offline/flaky wifi) and for reproducibility this must be vendored locally and pinned. Plus: the period-picker Tobias built supersedes the planned `add-dashboard-date-filter` change (see below); verify it filters correctly against real data, and confirm German number/date formatting from `lib/format.js` is used (not re-hardcoded `toFixed`).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `company-dashboard`: dashboard UI reconciled with backend; period filtering via the new picker.
- `partner-dashboard`: same.

## Impact

- Supersedes `add-dashboard-date-filter` (Tobias's period-picker already implements a Von/Bis filter in both dashboards) — that change should be archived as superseded rather than implemented.
- Frontend: `frontend/index.html` (Lucide), both dashboard pages, possibly `metric-card` (unused attributes if widgets are cut), `base.css` (kept as delivered). Backend: `CompanyDashboardDto`/`PartnerDashboardDto` + `DashboardService` **only if** any widget is chosen BUILD.
- **Blocked on a team decision (Chris + Tobias):** the CUT/BUILD call per out-of-scope widget. Recommendation below; do not implement widget tasks before that call.

## Recommendation (not yet decided)

Default to **CUT** for everything needing new backend aggregation (monthly charts, Vorperiode trend, Top-Kunden, total-products ring) to keep MVP + FSD scope, and **KEEP** what works on existing data (the period-picker; a Bestellstatus breakdown is derivable client-side from the already-fetched order list). Vendor + pin Lucide regardless of the widget decision. This keeps the visual design while not turning deferred B-items into unplanned backend work before the diploma deadline.
