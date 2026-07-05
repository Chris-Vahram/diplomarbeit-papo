## 1. Component

- [ ] 1.1 `data-table.js`: support `image: true` column type — renders `<img class="data-table__thumb" src="<escaped value>" alt="" loading="lazy">` (src escaped via existing `escapeHtml`, never raw; `image` wins over `html` if both set); empty value → `data-table__thumb--empty` placeholder span; capture-phase `error` listener on the host swaps a failed `<img>` for the same placeholder (no inline `onerror`). `.data-table__thumb` CSS in `base.css` (fixed ~40px, object-fit cover)
- [ ] 1.2 Regression check: columns without `image` render identically to before; escape-by-default and delegated listeners on other tables unaffected

## 2. Wire into lists + seed data

- [ ] 2.1 `company-product-list-page.js`: add `{ key: "imageUrl", label: "Bild", image: true }` as first column + map `imageUrl` into the row objects
- [ ] 2.2 `partner-product-list-page.js`: same — first column + `imageUrl` in the row mapping
- [ ] 2.3 Demo assets: download one fitting free-license photo per category (Werkzeug, Malerbedarf, Baustoffe, Arbeitsschutz, Elektro) into `frontend/assets/products/` (small, ≲100 KB each); `DemoDataSeeder` seeds relative paths (`/assets/products/<name>.jpg`); leave one product's `imageUrl` empty to exercise the placeholder path

## 3. Verification

- [ ] 3.1 Browser-verify (company + partner): thumbnails load from local assets (no external requests); the empty-URL product shows the placeholder, not a broken image; a deliberately dead URL falls back to the placeholder via the error listener; umlaut product names still render
- [ ] 3.2 Security check: a product whose `imageUrl` contains markup (e.g. `"><img src=x onerror=alert(1)>`) does not execute — src is escaped, no script runs
- [ ] 3.3 Full backend test suite green (no backend logic change — sanity run)
