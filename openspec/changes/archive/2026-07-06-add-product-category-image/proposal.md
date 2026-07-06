## Why

Optional enhancement 9.3 from the MVP build's task list, selected by Chris. The `Product` entity and the company product form already carry `category` and `imageUrl`, and both product lists already show the category **as a text column** — but nothing renders the product **image**, so `imageUrl` is dead data end to end and the partner's product list is a wall of text. For a hardware catalogue (~800 articles across Werkzeug/Malerbedarf/Baustoffe/…), a small product thumbnail is the fastest way for a partner to recognise an article. This closes the image half of 9.3; the category half already ships.

## What Changes

- Partner enabled-products list and company product list gain a small **image thumbnail** column, rendered from `Product.imageUrl`.
- **Graceful fallback** when `imageUrl` is empty or the image fails to load: a neutral placeholder box (no broken-image icon), so rows without a picture still render cleanly.
- `DemoDataSeeder` demo products get fitting placeholder image URLs so the demo shows real pictures out of the box.
- No new entity, no schema change, no new endpoint — `imageUrl`/`category` already flow through the product APIs and DTOs.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `product-management`: company product list gains an image thumbnail column.
- `product-enablement`: partner's enabled-products list gains an image thumbnail column.

## Impact

- Frontend: `data-table` needs a way to render an image cell safely (extend the existing `html`-column escaping story, not raw `imageUrl` interpolation); `company-product-list-page.js` and `partner-product-list-page.js` add the thumbnail column.
- Backend: `DemoDataSeeder` demo products get `imageUrl` values (data only — the field already exists). No schema/endpoint change.
- Depends only on the existing product-management + product-enablement capabilities and `fix-audit-hardening` (escape-by-default `data-table` render path, so a Kunde/import-supplied `imageUrl` can never inject markup).
