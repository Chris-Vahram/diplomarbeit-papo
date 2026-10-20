## 1. Backend (essential)

- [x] 1.1 Partner endpoints: list pages, create (empty/template), update settings, delete free page, reorder navigation
- [x] 1.2 Draft save with version check; publish page; restore previous published; mandatory-page rules; tests
- [x] 1.3 Theme draft save, publish design; tests for override preservation
- [x] 1.4 `ShopAsset` upload (PNG/JPEG/WebP, 5 MB, rights flag), quota, list, delete with in-use check; tests
- [x] 1.5 Extend `ShopCatalogService.publicImage` and block image references to the shop's own `ShopAsset`s; tests for foreign asset ids (interim state from `add-shop-page-renderer`)

## 2. Builder UI (essential)

- [x] 2.1 Builder page layout: palette, preview iframe with desktop/mobile toggle, properties panel
- [x] 2.2 Preview channel via `postMessage` in `shop.html` preview mode
- [x] 2.3 Block add/edit/duplicate/delete; drag & drop and up/down buttons
- [x] 2.4 Properties forms per block type incl. article/category pickers and image library
- [x] 2.5 Colour override controls with "Auf Design zurücksetzen"
- [x] 2.6 Page list: create, rename, menu name, URL segment, order, delete; limit indicator
- [x] 2.7 Autosave with status, undo/redo, conflict reload dialog
- [x] 2.8 Design tab: colour pickers with hex input, font pair select, publish design
- [x] 2.9 SEO fields with explanation; small-screen hint

## 3. Verification

- [x] 3.1 Backend tests + package; `git diff --check`
- [x] 3.2 Browser test desktop and tablet: build a page, publish, restore, conflict case
- [ ] 3.3 Usability walkthrough with a non-technical tester; note findings (open: needs a human tester)
- [x] 3.4 Update traceability
