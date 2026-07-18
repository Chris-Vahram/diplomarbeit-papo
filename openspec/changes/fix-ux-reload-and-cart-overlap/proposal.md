## Why

The phase-4 UX audit (`docs/partner_portal_audit_suite/Audit/04_UX_REVIEW_REPORT.md`) found two High findings that break the core experience and are a live-demo risk for the next client Termin:

- **UX-001:** A hard reload, bookmark, or deep-link on any list page (Meine Endkunden, Meine Bestellungen, company Bestellungen/Partner/Produkte) renders an **empty table** — bare `<tr>` rows without cells or headers. Root cause (verified): `router.js` fires `import("../components/index.js")` and all page modules as unordered parallel dynamic imports; no page imports `data-table.js` itself. On a cold load the page module can win the race and assign `.columns`/`.filters`/`.searchKeys` to a **not-yet-upgraded** `<data-table>` element — the own properties permanently shadow the class setters (classic custom-element upgrade shadowing). Only the async-assigned `.rows` lands after upgrade, producing exactly the observed DOM. F5 is precisely what a non-technical partner does when something looks off.
- **UX-002:** In the cart panel on `/partner/orders/new`, `.cart-line__name` collapses to **0px width** (measured) and its text paints over the unit price — at every viewport including the 1440×900 desktop default, because the fixed-width siblings (thumb 40px + price + quantity 56px + total + remove button + gaps) exceed the 22rem panel and `flex-wrap` only exists ≤48em.

## What Changes

- **Router bootstrap ordering:** the router waits for the shared component library (`components/index.js`) to be registered before mounting the first page, so `<data-table>` (and every other shared element) is always upgraded before a page assigns properties to it. No page-level API change.
- **Cart line layout:** `.cart-line` wraps at all viewports and `.cart-line__name` gets a minimum width, so name and unit price never overlap; pure `base.css` change, no markup/JS change.
- Regression check for both via Playwright (direct-load vs. SPA-load; cart-line box measurement).

## Capabilities

### New Capabilities
- `frontend-shell`: pages render their complete content (table headers, cells, filters) regardless of entry path — direct URL load, hard reload, or SPA navigation.

### Modified Capabilities
<!-- None. UX-002 is a visual defect fix inside the existing partner order-cart behavior; no requirement changes. -->

## Impact

- **Frontend only:** `frontend/src/lib/router.js` (await component registration before first render), `frontend/src/styles/base.css` (`.cart-line` rules). No backend change, no new dependencies, no API/markup changes.
- Fixes audit findings UX-001 and UX-002 (both "fix before next Termin").
