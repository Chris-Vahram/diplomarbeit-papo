## 1. Component

- [x] 1.1 `data-table.js`: support `image: true` column type — renders `<img class="data-table__thumb" src="<escaped value>" alt="" loading="lazy">` (src escaped via existing `escapeHtml`, never raw; `image` wins over `html` if both set); empty value → `data-table__thumb--empty` placeholder span; capture-phase `error` listener on the host swaps a failed `<img>` for the same placeholder (no inline `onerror`). `.data-table__thumb` CSS in `base.css` (fixed ~40px, object-fit cover)
- [x] 1.2 Regression check: columns without `image` render identically to before; escape-by-default and delegated listeners on other tables unaffected

## 2. Wire into lists + seed data

- [x] 2.1 `company-product-list-page.js`: add `{ key: "imageUrl", label: "Bild", image: true }` as first column + map `imageUrl` into the row objects
- [x] 2.2 `partner-product-list-page.js`: same — first column + `imageUrl` in the row mapping
- [x] 2.3 Demo assets: one fitting image per category (Werkzeug, Malerbedarf, Baustoffe, Arbeitsschutz, Elektro) into `frontend/assets/products/` (self-authored flat-color SVG icons, not downloaded stock photos — no internet access in this environment; each ~0.3 KB); `DemoDataSeeder` seeds relative paths (`/assets/products/<name>.svg`); "Wasserwaage 100cm" left with `imageUrl = null` to exercise the placeholder path

## 3. Verification

- [x] 3.1 Browser-verify (company + partner): thumbnails load from local assets (no external requests); the empty-URL product shows the placeholder, not a broken image; a deliberately dead URL falls back to the placeholder via the error listener; umlaut product names still render
- [x] 3.2 Security check: a product whose `imageUrl` contains markup (e.g. `"><img src=x onerror=alert(1)>`) does not execute — src is escaped, no script runs
- [x] 3.3 Full backend test suite green (no backend logic change — sanity run) — 46/46 pass
