## 1. Router bootstrap ordering (UX-001)

- [x] 1.1 In `frontend/src/lib/router.js`, await the `components/index.js` import before the first `render()` (keep page-module imports lazy/parallel as today); make the initial route render run only after component registration.
- [x] 1.2 Verify no other code path can mount a page before registration (hashchange listener installed before the await must not render earlier).

## 2. Cart line layout (UX-002)

- [x] 2.1 In `frontend/src/styles/base.css`, add `flex-wrap: wrap` to the base `.cart-line` rule and remove the now-redundant ≤48em `.cart-line { flex-wrap: wrap }` override.
- [x] 2.2 Give `.cart-line__name` a `min-width: 8rem` and `overflow-wrap: anywhere` so the name can never collapse to 0px or overpaint siblings.

## 3. Verify (dev stack + Playwright; use about:blank bounce before same-URL reloads)

- [x] 3.1 Direct load (fresh tab) of `#/partner/customers` and `#/partner/orders`: table renders headers + cells + filters; assert `thead th > 0` and `tbody td > 0`. Repeat once more after F5-style reload.
- [x] 3.2 SPA navigation across all partner + company list pages: no regression (headers/cells/filters present).
- [x] 3.3 Cart on `/partner/orders/new` with a product whose name contains umlauts and is long enough to previously overlap: `.cart-line__name` bounding-box width > 0 and no intersection with `.cart-line__unit-price`; check at 1440×900, 390×844, 1920×1080.
- [x] 3.4 Place one real order through the fixed cart (happy path) to confirm totals/commission preview and submit are untouched.
- [x] 3.5 Run `partnerportal-quality` + `/code-review` on the diff before handing the branch to Chris.
