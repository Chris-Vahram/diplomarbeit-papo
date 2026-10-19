## 1. Data model (essential)

- [x] 1.1 Entities `ShopTheme`, `ShopPage` with draft/published/previous JSON snapshots, page kind, URL segment, menu name, order
- [x] 1.2 `BlockType` registry and per-type validators (fields, lengths, references, max 40 blocks/page); tests incl. XSS payloads
- [x] 1.3 Limits: 6 designable online pages, 20 total; mandatory pages protected; tests
- [x] 1.4 Default template JSON for Startseite, Artikelübersicht, Artikeldetail; idempotent creation for new and existing shops

## 2. Public API (essential)

- [x] 2.1 `GET /api/shop/{kennung}/theme`, `/navigation`, `/pages/{segment}`
- [x] 2.2 Public catalogue `/articles`, `/articles/{id}`, `/categories` (enabled ∧ available), gross price + rate
- [x] 2.3 Public image endpoint with membership check; tests for foreign ids

## 3. Renderer (essential)

- [x] 3.1 `<shop-page-view>` and theme CSS custom properties; override handling
- [x] 3.2 Block elements: hero, heading-text, image, image-text, article-grid, featured-articles, article-detail, contact-person, spacer
- [x] 3.3 Shop layout with generated navigation and fixed footer placeholder; responsive check
- [x] 3.4 Curated self-hosted fonts added under `vendor/fonts` with licences; font registry

## 4. Verification

- [x] 4.1 Backend tests + package; `git diff --check`
- [x] 4.2 Browser desktop/mobile: default shop renders, no external requests
- [x] 4.3 Update traceability
