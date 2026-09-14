## Context

Four independent, small UI fixes in the partner area, found during live use. No shared code path between them except that three live in `partner-order-new-page.js` / `order-cart.js`. Kept as one change because they're one "UI polish pass," not because they're technically coupled.

## Goals / Non-Goals

**Goals:**
- Dashboard metric cards always show a trend arrow, without changing the all-time-exact headline numbers `partner-dashboard/spec.md` locks in.
- Endkunde selection becomes a visible list a partner clicks, not a hidden `<select>` driven by an unrelated search box, while keeping every existing "Searchable own customer selection" scenario (`partner-order-wizard/spec.md:106-124`) true, including keyboard-only operation.
- Catalog gets the larger share of the article-step row; cart preview gets the smaller share.
- Wizard step buttons read "Zurück" / "Weiter".
- Profile field reads "Benutzername" instead of "Partnernummer".

**Non-Goals:**
- No change to what counts toward the headline totals (still fulfilled-only, all-time when no range picked).
- No change to the underlying login-identity model — `decouple-partner-number-from-login` (separate, active change) owns that; here we only reword a display label.
- No redesign of the article catalog's own filtering/search, card layout, or the "Übersicht" step.
- No company-side dashboard changes.

## Decisions

### 1. Trend arrow: fixed trailing-30-day window, computed client-side, decoupled from the headline value

`dashboard-data.js` already has `filterByPeriod`, `fulfilledTotals`, `trendInfo`. Add one helper, e.g. `trailingWindowTrend(orders, days = 30)`, that:
- builds `[today-2*days, today]`-style current/previous windows using the same `previousPeriod`-style math already used for the explicit-picker case,
- runs the existing `fulfilledTotals` + `trendInfo` over them,
- returns `{ revenueTrend, commissionTrend, countTrend }`.

In `partner-dashboard-page.js#render()`, when `this.period` is `null`, call this helper for the trend attrs instead of hardcoding `null` — but keep `revenue`/`commission`/`count` (the big numbers) sourced from `this.base` (backend all-time) exactly as today. When `this.period` is set, behavior is unchanged (explicit-period trend, as now).

Alternative considered: change the headline numbers themselves to a trailing-30-day default. Rejected — directly contradicts the locked `partner-dashboard/spec.md` requirement ("Without a range, the dashboard SHALL show the backend's all-time totals ... not a default trailing window"); would need a spec-breaking change, not asked for.

Spec delta needed: `partner-dashboard/spec.md` currently only defines trend behavior implicitly via the metric-card story; add a scenario stating the no-range trend basis is a fixed trailing 30-day window while totals stay all-time.

### 2. Endkunde picker: list-of-buttons replacing `<select>`, same filter predicate

Replace the `<select name="kundeId">` in `kundeStepHtml()` with a `<ul role="listbox">`/button-per-row list rendered by the same `matches` array `renderOptions()` already computes (`wireKundeStep` in `partner-order-new-page.js`). Each row is a `<button type="button" role="option">` showing name + address (unchanged content), click sets `this.state.kundeId` and a `selected` visual state directly — no more indirection through a native `<select>`'s `value`.

Keyboard operability (spec scenario "Keyboard selection"): a native `<ul>`/`<button>` list is keyboard-operable by default (Tab moves through buttons, Enter/Space activates) without needing full ARIA listbox roving-tabindex machinery — simpler than reimplementing `<select>`'s keyboard model, and the spec only requires "every control has a visible label and the partner can complete the step without a pointer," not arrow-key roving selection. `role="listbox"`/`role="option"` is used for assistive-tech semantics; interaction stays plain Tab/Enter, not arrow-key navigation.

"Filter hides selected entry" scenario: unchanged logic — if the current `kundeId` selection drops out of the filtered `matches`, clear it, same as `renderOptions()` does today for `select.value`.

Alternative considered: keep `<select>` but add a live `aria-live` region announcing match count. Rejected — user explicitly asked for a rebuild into a clickable list, not incremental feedback on the same widget.

No spec delta: the "Searchable own customer selection" requirement doesn't mandate a `<select>` element, only the described matching/labeling/keyboard behavior, which is preserved.

### 3. Catalog/cart column ratio: flip `.produkte-step-layout` grid-template-columns

Change `grid-template-columns: 21rem minmax(0, 1fr)` to give the catalog the flexible/majority column and the cart preview a smaller fixed column, e.g. `grid-template-columns: minmax(0, 1fr) 20rem`. Must preserve the existing height-sync behavior documented at `base.css:3009-3020` (no explicit row height; `align-items: stretch` makes `.cart--preview` match the catalog's natural height) — only the column-width values change, not the row/height mechanism.

No spec delta: `partner-order-wizard/spec.md`'s "Catalog precedes order panel" requirement fixes left/right order, not width ratio.

### 4. Button label + profile label: literal text-only renames

Straightforward string changes in `partner-order-new-page.js` (4 button texts) and `partner-profile-page.js` (1 `<dt>` text). No id/class/behavior changes, so no other code or spec references them by label text.

## Risks / Trade-offs

- [Trailing-30-day trend on a very new/low-volume partner could look noisy or misleadingly volatile (e.g. 0→1 order swings)] → `trendInfo` already special-cases `previous === 0` as "(neu)" rather than a raw percentage; that behavior carries over unchanged.
- [Rebuilding the Endkunde picker touches the "Filter hides selected entry" and "Keyboard selection" scenarios directly] → keep `wireKundeStep`'s existing filter/predicate and clearing logic verbatim, only swap the rendering target from `<select>` options to list buttons, so scenario coverage doesn't regress.
- [Column-ratio flip could resurface the height-desync bug the existing CSS comments warn about] → do not touch row/height rules, only the two column-width values.

