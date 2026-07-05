## Context

`data-table.js` renders cells two ways: HTML-escaped by default, or raw markup when a column def has `html: true` (action buttons, badges, checkboxes). `Product` already has `imageUrl`/`category`; both product lists already show `category` as a text column. `fix-audit-hardening` deliberately made cell rendering escape-by-default because product/Kunde/import text is attacker-influenced. `DemoDataSeeder` products currently have no `imageUrl`.

## Goals / Non-Goals

**Goals:**
- Show a small product thumbnail in the company product list and the partner enabled-products list.
- Clean fallback when `imageUrl` is empty or fails to load — never a broken-image glyph.
- Demo products show real, category-fitting pictures out of the box.
- Keep the escape-by-default safety: an `imageUrl` value must never be able to inject markup.

**Non-Goals:**
- No image upload/hosting — `imageUrl` stays a plain URL field (company types/imports it).
- No image on the order form, order lists, or dashboard — product lists only.
- No thumbnail for the company *enablement* modal list (kept lean; can follow later).
- No category changes — category display already ships.

## Decisions

**Add an `image: true` column type to `data-table`, rather than building `<img>` strings in each page.** A page that did `` `<img src="${imageUrl}">` `` and passed it as `html: true` would interpolate raw `imageUrl` into `innerHTML` — exactly the stored-XSS class `fix-audit-hardening` closed (an `imageUrl` of `"><script>…` would execute). Instead `data-table` renders image cells itself: `<img class="data-table__thumb" src="<escapeHtml(value)>" alt="" loading="lazy">`. Callers pass the raw URL as normal row data (`{ key: "imageUrl", label: "Bild", image: true }`); escaping stays centralised in the component, consistent with the existing escape-by-default cells. Precedence is explicit: `image` wins over `html` if both are set (image cells never render caller markup).

**Empty/failed images fall back to a CSS placeholder, not a broken glyph.** Empty `imageUrl` → render a `<span class="data-table__thumb data-table__thumb--empty">` placeholder box instead of an `<img>`. A URL that fails at load time → a **capture-phase `error` listener on the `<data-table>` host** (error events don't bubble, but they are capturable) swaps the failed `<img>` for the same placeholder. No inline `onerror` attribute inside `innerHTML` — consistent with the component's existing delegated-listener pattern and keeps the rendered HTML free of inline JS.

**Demo images: local assets committed to the repo, seeded as relative paths.** Fitting free-license photos (one per category: Werkzeug, Malerbedarf, Baustoffe, Arbeitsschutz, Elektro) are downloaded once into `frontend/assets/products/` and `DemoDataSeeder` stores relative paths (`/assets/products/<name>.jpg`), which the browser resolves against the frontend origin regardless of host/port. Rejected alternative — hotlinking a stock-photo host: the Diplomarbeit demo must work with school Wi-Fi flakiness or fully offline, and external links rot; local assets freeze the demo's appearance permanently. Client-entered absolute `imageUrl`s keep working unchanged (`<img src>` accepts both).

**German label.** New column header: `Bild`.

## Risks / Trade-offs

- **[Risk]** Demo environment without internet (school Wi-Fi, offline demo) would break hotlinked images → **Mitigation**: demo images are local repo assets served by the frontend itself; no network dependency. The error-fallback still guards client-entered external URLs that rot.
- **[Trade-off]** Thumbnails add row height / a network fetch per row → **Mitigation**: small fixed thumb size (~40px), `loading="lazy"`, and pagination (already shipped) keep it cheap even at ~800 rows.
- **[Trade-off]** Touching the shared `data-table` again → **Mitigation**: the `image` column type is opt-in; unset = current behaviour; existing tables (orders, dashboard) are unaffected.
