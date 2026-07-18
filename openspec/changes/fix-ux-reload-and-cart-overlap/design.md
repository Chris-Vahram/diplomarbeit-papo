## Context

Two audited High findings (Report 04), both frontend-only:

1. **UX-001.** `frontend/src/lib/router.js:269-281` kicks off `import("../components/index.js")` plus 12 page-module imports as fire-and-forget parallel dynamic imports, then renders on `hashchange`/load. Pages (e.g. `partner-customer-list-page.js:33-51`) assign `.columns`, `.filters`, `.searchKeys`, `.pageSize` synchronously in `connectedCallback`. On a cold load the page module can execute before `data-table.js` has called `customElements.define("data-table", …)` — the assignments then create **own properties on an un-upgraded element**, which permanently shadow the accessors defined on the class prototype (upgrade does not re-route existing own properties through setters). Only `.rows`, assigned after the API fetch resolves (by which time the element is upgraded), goes through its setter — hence bare `<tr>`s with `rowClass` but no `<td>`/`<th>`. Reproduced deterministically on direct load of `#/partner/customers` and `#/partner/orders`; SPA navigation is always fine because the element is defined by then.
2. **UX-002.** `.cart-line` (base.css) is a non-wrapping flex row above 48em. Fixed-width parts (thumb 2.5rem, unit price, quantity 3.5rem, line total min 4rem, remove button) plus gaps exceed the 22rem cart column; `.cart-line__name { flex: 1; min-width: 0 }` collapses to 0px and its unwrapped text overflows across the unit price. Measured at 1440×900 and 390×844.

## Goals / Non-Goals

**Goals:**
- Every page renders completely on direct load / F5 / bookmark — same result as SPA navigation.
- Cart lines stay readable (no overlapping text) at 390px, 1440px, 1920px.
- Minimal diff; no page-level API or markup changes.

**Non-Goals:**
- No general upgrade-safe property pattern in every component (only the bootstrap ordering is fixed; the shadowing hazard disappears once registration precedes mounting).
- No cart redesign (sticky mobile mini-cart etc. = separate finding M-002, post-audit batch).
- No router rewrite, no build step, no new dependencies.

## Decisions

1. **Fix at the router bootstrap, not per page.** `router.js` awaits `import("../components/index.js")` (single small module registering all shared elements) **before** the first `render()`; page-module imports stay parallel/lazy as today — pages are only instantiated by `render()`, and an unregistered *page* element upgrades later and runs its own `connectedCallback` correctly (pages read no pre-set properties), so only the shared-components import is ordering-critical. One-line-ish change, fixes all current and future pages at once. Alternative (static `import "../components/data-table.js"` in every page) rejected: 12 edit sites, easy to forget in new pages — exactly how this bug happened.
2. **Belt-and-suspenders is out of scope.** An upgrade-safe re-adoption loop in `DataTable.connectedCallback` (re-assigning own properties through setters) would also work but adds boilerplate for a race that no longer exists once decision 1 lands; skipped per Karpathy rule 2 (documented here so it isn't re-proposed later).
3. **Cart line: wrap always + name floor.** Move `flex-wrap: wrap` onto the base `.cart-line` rule (drop the ≤48em-only override) and give `.cart-line__name` `min-width: 8rem` (+ `overflow-wrap: anywhere` for pathological product names). Long names wrap onto a second visual row inside the line instead of overpainting the price. No markup change; the ≤48em media block keeps only what it still needs.
4. **Verification is part of the change.** Playwright: (a) `about:blank` → direct `goto` on `#/partner/customers` and `#/partner/orders`, assert `thead th` and `tbody td` counts > 0; (b) cart with a long-named product, assert `.cart-line__name` width > 0 and no bounding-box intersection with `.cart-line__unit-price`. Umlaut fixture name (per project rule).

## Risks / Trade-offs

- **Awaiting components delays first paint by one module load** (~ms, local static files; components/index.js is tiny). Acceptable — correctness over a micro-optimization.
- **`flex-wrap` on desktop cart lines** may produce two-row lines for long product names where before (broken) everything was one overlapping row. That is the intended trade-off: readable beats compact. Verified visually at all three audit viewports before merge.
- Other un-upgraded-element assignments elsewhere (e.g. `metric-card` uses attributes, not properties) were checked in the audit — no other property-based shared component is assigned pre-fetch, so decision 1 covers the real surface.
