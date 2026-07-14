> Frontend-only. Reuses `money.js`, `product-images.js` (`resolveImageUrls`), `order-form.js` (`kundePickerHtml`/`wireKundePicker`/`readKundePayload`/`renderOrderConfirmationHtml`), and the category-filter pattern from `partner-product-list-page.js`. No backend change; submit payload `{ kundeId|newKunde, items:[{productId,quantity}] }` is unchanged.

## 1. Catalog + cart module (`frontend/src/lib/order-cart.js`)

- [ ] 1.1 Render a product catalog from the enabled products: card per product with thumbnail (via `resolveImageUrls`), full name, category tag, unit price, availability. All product-derived text through `escapeHtml`. Unavailable products render disabled and are not addable.
- [ ] 1.2 Catalog narrowing: a search field (name + category, `toLowerCase().includes`, umlaut-correct, no ASCII folding) and a category filter built from the loaded products (as `partner-product-list-page.js` does). Empty search/neutral category impose no restriction.
- [ ] 1.3 Cart state (array of `{ product, quantity }`): "In den Warenkorb" adds or increments; render a line per item with thumbnail/name, quantity stepper (min 1), unit price, line total, and remove.
- [ ] 1.4 Live cart totals + commission preview via `money.js` (`sumCents`/integer cents for Gesamtwert, `commissionCentsHalfUp` for the preview so it matches the stored `CommissionRecord`); "(Vorschau)" hint. Empty state "Warenkorb ist leer".
- [ ] 1.5 Expose a reader that turns the cart into `items: [{ productId, quantity }]` for the submit payload.

## 2. Canonical page redesign (`frontend/src/pages/partner-order-new-page.js`)

- [ ] 2.1 Compose the page from the Endkunde picker (`kundePickerHtml`/`wireKundePicker`, existing/new) + the catalog/cart module; drop the per-line combobox (`articleLinesHtml`/`wireArticleLines`) usage here (leave `order-form.js` intact — the company flow still uses it).
- [ ] 2.2 Keep fetching `/partner/products`, `/partner/customers`, `/partner/profile` (commissionPercent for the preview); keep the "no enabled products" German empty-state.
- [ ] 2.3 Submit: build `items` from the cart reader + `readKundePayload(form)`, `POST /partner/orders`, render `renderOrderConfirmationHtml` on success; double-submit guard (disable the button in-flight). Block submit on an empty cart.
- [ ] 2.4 Add a "Zurück zu Meine Bestellungen" affordance (navigates to `#/partner/orders`) on the form and after the confirmation.

## 3. Absorb the consolidation (remove both partner modals)

- [ ] 3.1 `partner-order-list-page.js`: "Neue Bestellung" button → `location.hash = "#/partner/orders/new"`. Delete the `#order-modal` and the hand-rolled methods (`openModal`, `loadOrderForm`, `renderForm`, `addLine`, `toggleKundeMode`, `handleSubmit`, `renderConfirmation`) + now-unused imports (`formatEuro`, `formatAddress`, `addressFieldsHtml`, `readAddress`, `setAddressRequired`). Keep the order list, details modal, and the Endkunde/Datum/Status filters.
- [ ] 3.2 `partner-dashboard-page.js`: "Neue Bestellung" button → `location.hash = "#/partner/orders/new"`. Delete the `#order-modal` and order-creation methods (`openOrderModal`, `handleOrderSubmit`, `renderSummary`) + now-unused `order-form.js` imports. Keep metrics, charts, latest-orders table, period picker.

## 4. Styling (`frontend/src/styles/base.css`)

- [ ] 4.1 Catalog grid + product cards and the cart panel, on the calm-green foundation, responsive to mobile (single column at narrow width, no horizontal overflow of `.app-root`).

## 5. Verify (dev stack + Playwright, partner-demo)

- [ ] 5.1 Catalog browses all enabled products with thumbnails and full (untruncated) names; search narrows by an umlaut term ("Müll") and by category; "muller" does not match "Müller".
- [ ] 5.2 Unavailable product is shown disabled and cannot be added.
- [ ] 5.3 Add multiple products, change quantities, remove a line — line totals, Gesamtwert, and commission preview update live and in integer cents; the previewed commission equals the stored `CommissionRecord` (custom % exercising HALF_UP, e.g. 7% on an odd total).
- [ ] 5.4 Single-item ("direct") order and multi-item order both create a correct order via the same submit; double-click does not duplicate.
- [ ] 5.5 HTML-special-char product name/category render as literal text in the catalog and cart.
- [ ] 5.6 Dashboard and order-list "Neue Bestellung" both navigate to `/partner/orders/new` (no modal); "Zurück zu Meine Bestellungen" returns and the new order appears in the freshly loaded list.
- [ ] 5.7 Empty-state: a partner with no enabled products sees the German message; no dead code (`grep` confirms `#order-modal` + deleted method names gone), no console errors on dashboard/order-list load.
- [ ] 5.8 Mobile (~375px): catalog + cart layout holds with no horizontal overflow.
- [ ] 5.9 Backend suite stays green (no backend change); `openspec validate partner-order-cart` and the `partnerportal-quality` review pass before handover. Cross-model (Codex) diff review — this is a meaty change.
