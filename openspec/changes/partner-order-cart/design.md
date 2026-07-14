## Context

The canonical partner order page (`partner-order-new-page.js`, route `/partner/orders/new`) currently composes an order with the shared `order-form.js` per-line combobox + live summary. It fetches `/partner/products` (each `ProductDto`: id, name, price, description, available, category, imageUrl), `/partner/customers`, and `/partner/profile` (commissionPercent). Two other partner surfaces open their own "Neue Bestellung" modals: the dashboard (shared-module combobox + summary) and the order list (hand-rolled plain `<select>`). The company order list has a separate manual-order modal that also uses the combobox — that one is out of scope and keeps the combobox.

Reusable pieces already in the tree:
- `money.js` — `sumCents`, `commissionCentsHalfUp` (backend-exact HALF_UP), integer-cent math.
- `product-images.js` — `resolveImageUrls(rows, key)` resolves internal `/api/product-images/...` refs to displayable (auth'd blob) URLs; already used by `partner-product-list-page.js`.
- `order-form.js` — `kundePickerHtml`, `wireKundePicker`, `readKundePayload` (Endkunde existing/new), `renderOrderConfirmationHtml` (confirmation + simulated e-mail).
- `partner-product-list-page.js` — pattern for building a category dropdown from the loaded products and rendering thumbnails.
- `escapeHtml` — all product-derived text.

The submit payload is unchanged: `{ kundeId | newKunde, items: [{ productId, quantity }] }` → `POST /partner/orders`.

## Goals / Non-Goals

**Goals:**
- Browse **and** search: the catalog shows the partner's enabled products (thumbnail, full name, category, price, availability) and narrows by a search field + category filter.
- A cart the partner builds up (add item, change quantity, remove), with live Gesamtwert and a commission preview matching the backend to the cent.
- Both "add to cart, then order" and "order a single item directly" — one mechanism, two speeds.
- One canonical partner order surface; the dashboard and order-list buttons navigate to it (absorbing `consolidate-partner-order-creation`); both partner modals removed.
- Umlaut-correct search, escaped output, mobile-friendly, no backend change.

**Non-Goals:**
- The company manual-order flow is untouched (keeps the combobox modal). A cart there is a separate future question.
- No cross-tab/persistent cart — the cart is in-memory for the page visit (sessionStorage persistence is possible later polish, explicitly out of scope now).
- No new backend endpoint, no order-payload change, no change to validation, commission arithmetic, or the confirmation/e-mail content.
- Not the external end-customer shop (B-01, permanently cut). This is the partner's internal composition tool.

## Decisions

- **Catalog = the enabled product list, always visible, narrowed by search + category.** Default view shows all enabled products as cards (browse). A search field (name + category, `toLowerCase().includes`, umlaut-correct, no ASCII folding — same semantics as the existing filters) and a category dropdown (built from the loaded products, as `partner-product-list-page.js` already does) narrow it. Unavailable products render disabled ("nicht verfügbar") and cannot be added. Thumbnails via `resolveImageUrls` with lazy loading. At the reference ~800 articles, browse-all stays usable because search/category narrow instantly on already-loaded data; if the full grid proves heavy, cap the rendered cards with a "weiter eingrenzen" hint (same approach as the combobox's 20-item cap) — a rendering optimization, not a behavior change.
- **Cart in component state.** An array of `{ product, quantity }`. "In den Warenkorb" adds (or increments if already present). The cart panel renders a line per item (thumbnail/name, quantity stepper min 1, unit price, line total, remove) plus Gesamtwert and the commission preview. Totals and commission reuse `money.js` verbatim, so the preview equals the stored `CommissionRecord` to the cent, exactly as the current summary does. Empty state: "Warenkorb ist leer".
- **Direct order = single-item cart, not a separate path.** Every order needs the mandatory Endkunde, so a true zero-click "buy now" is impossible. "Direct" is served by: add one item → the cart's "Bestellung aufgeben" is right there. A per-card affordance may focus/scroll to Endkunde+submit as a shortcut, but it funnels through the identical submit. No second code path, no divergent payload.
- **Submit is unchanged.** On "Bestellung aufgeben": build `items` from the cart (`{ productId: product.id, quantity }`), combine with `readKundePayload(form)` for the Endkunde, `POST /partner/orders`, then render `renderOrderConfirmationHtml`. Double-submit guard (disable the button in-flight) as the other flows have.
- **Absorb the consolidation.** The dashboard and order-list "Neue Bestellung" buttons set `location.hash = "#/partner/orders/new"`; both partner order modals and their handler methods are deleted, along with now-unused imports. The page gets a "Zurück zu Meine Bestellungen" affordance. Returning to a list/dashboard re-mounts it fresh (`connectedCallback` → `load()`), so a new order shows without any cross-component refresh — this removes stale-state coupling rather than adding it.
- **Where the code lives.** Factor the catalog + cart rendering/wiring into a new `frontend/src/lib/order-cart.js` (partner-page-only for now), matching the existing `lib/` module pattern (`order-form.js`, `order-details.js`). The page orchestrates fetch + Endkunde + cart + submit. Keep `order-form.js` intact — the company manual flow still imports its combobox.

## Risks / Trade-offs

- **Biggest frontend change in the queue.** Mitigated by heavy reuse (money, images, Endkunde helpers, confirmation, category-filter pattern all already exist and are tested); only the catalog+cart shell is genuinely new.
- **Rendering ~800 cards.** Lazy-loaded thumbnails + instant client-side narrowing keep it usable; a render cap with a "narrow further" hint is the fallback. Demo data is tiny, so this is a scale-hardening note, not a v1 blocker.
- **UX shift from per-line form to catalog+cart.** This is the point — it directly serves the browse+search+images+mobile asks. The combobox is not lost (company flow keeps it).
- **Cart is in-memory; navigating away loses it.** Accepted for v1 — the entry points open a fresh page, so a cart is built and submitted in one visit. sessionStorage persistence is a small, clearly-scoped later add if wanted.
- **Scope-creep toward B-01 (external shop).** Guarded by keeping this strictly partner-internal: same auth, same `/partner/orders` submit, no end-customer-facing surface. The "cart" is an order-composition metaphor, not a storefront.

## Migration Plan

Frontend-only, additive to the canonical page + deletion of two modals. No data/API migration. Rollback = revert the commit.

## Open Questions

- Explicit per-card "Sofort bestellen" shortcut vs. relying on a one-item cart + always-visible submit — decide during implementation by which reads cleaner; both end at the same submit, so this is a UI-polish call, not an architectural one.
- Catalog card density / whether a category filter is chips vs a dropdown — visual-design detail, can follow Tobias's direction; functionally either works.
