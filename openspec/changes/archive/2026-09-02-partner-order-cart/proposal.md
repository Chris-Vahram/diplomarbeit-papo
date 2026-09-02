## Why

The partner order-compose UI is a per-line searchable combobox (from `improve-order-form`). It works but has real gaps the client's priorities expose:

- **No browsing.** A partner must already know a product's name to search it — there is no way to *see* the enabled catalog. Chris's direct feedback (2026-07-14): "I don't like that the items available aren't listed anymore, I have to search them up — I want both."
- **No product images**, though the data exists (`ProductDto.imageUrl`, resolved thumbnails already power the partner product list).
- **Cramped display.** In a narrow container the suggestion truncates the name to "B…" — the partner can't read what they're picking.
- The per-line "search box + quantity" pattern is unusual for the non-technical partners and small mobile screens the client emphasized (meeting 10.07.2026).

Chris's proposed model (2026-07-14): a **shopping cart** — browse/search a product catalog, add items to a cart (or order a single item directly), then place the order. This gives "list AND search", shows products with images, reads naturally for non-technical users, and works well on mobile.

This is a frontend UX change only: it is still exactly "place an order for multiple articles + quantities" (FSD MVP), the same `{ kundeId|newKunde, items:[{productId, quantity}] }` request. It is the *partner's internal* order-composition tool — explicitly **not** the external end-customer shop (backlog B-01, permanently cut).

## What Changes

- The canonical partner order page (`/partner/orders/new`) is redesigned into: an **Endkunde** selector, a **searchable/filterable product catalog** (cards with thumbnail, full name, category, price, availability), and a **Warenkorb** (cart) with per-line quantity, running Gesamtwert, and a commission preview. "Bestellung aufgeben" submits from the cart.
- **Add-to-cart and direct-order both supported:** a product card's "In den Warenkorb" accumulates items; a single-item order is just a one-item cart, so "direct" and "cart" are one mechanism at two speeds (no separate backend path — every order still needs the mandatory Endkunde).
- **Absorbs the shelved `consolidate-partner-order-creation` change:** the dashboard and order-list "Neue Bestellung" buttons navigate to this page instead of opening their own modals; both partner order-creation modals (the shared-module one on the dashboard and the hand-rolled one on the order list) are removed, leaving one canonical partner order surface.
- The partner order page stops using the per-line combobox (`wireArticleLines`); the combobox stays in the codebase because the **company manual-order flow keeps using it** (out of scope here).

## Capabilities

### New Capabilities
- `partner-order-cart`: How a partner composes an order by browsing/searching a product catalog and building a cart — catalog display and filtering, add-to-cart and direct single-item ordering, live cart totals and commission preview, submit, and the single-canonical-page entry-point behavior (which absorbs the consolidation intent).

### Modified Capabilities
<!-- None. Order fields, validation, the submit payload, and commission arithmetic are unchanged; the order-form-experience combobox capability still governs the company manual-order flow, which is untouched. -->

## Impact

- **Frontend only.** New catalog + cart UI on `frontend/src/pages/partner-order-new-page.js` (likely factored into a `frontend/src/lib/order-cart.js` module for clarity), reusing: `money.js` (cart totals + commission), `product-images.js` (`resolveImageUrls` thumbnails), the `kundePickerHtml`/`wireKundePicker`/`readKundePayload` Endkunde helpers and `renderOrderConfirmationHtml` from `order-form.js`, and the category-filter pattern already in `partner-product-list-page.js`. Plus catalog/cart styles in `base.css`.
- **Deletion** of both partner order-creation modals and the hand-rolled form in `partner-order-list-page.js` / `partner-dashboard-page.js` (absorbing the consolidation), with their now-unused imports removed. The dashboard + order-list "Neue Bestellung" buttons become navigations.
- **No backend change.** The submit request (`/partner/orders`, `{kundeId|newKunde, items:[{productId,quantity}]}`) is byte-for-byte the same. No REST, entity, or authorization change.
- **Out of scope:** the company manual-order flow stays a modal with the combobox (a cart in that narrow modal, where the company first selects a partner, is a separate question). Cross-browser-tab cart persistence is out of scope; the cart is in-memory per page visit. External end-customer shop (B-01) remains cut.
- German UI throughout (de-AT), correct umlauts, all product-derived text escaped.
