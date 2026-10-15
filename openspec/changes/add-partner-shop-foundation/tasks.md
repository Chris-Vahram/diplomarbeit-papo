## 1. Backend (essential)

- [ ] 1.1 Create `PartnerShop` entity, repository and computed status
- [ ] 1.2 Implement `ShopKennung` normalisation, transliteration, reserved words, collision suffix; unit tests
- [ ] 1.3 Company endpoints: get/set identifier, release, take offline with reason (10–500 chars), re-release
- [ ] 1.4 Partner endpoint: own shop status (creates record on first call)
- [ ] 1.5 Public `GET /api/shop/{kennung}` with minimal DTO and uniform 404
- [ ] 1.6 Rate-limit filter for `/api/shop/*` with 429 message; tests
- [ ] 1.7 noindex response filter and `papo.shop.indexing-enabled` config
- [ ] 1.8 Tests: partner cannot change identifier, internal APIs still 401, inactive partner / no articles → offline

## 2. Frontend (essential)

- [ ] 2.1 `shop.html` entry and `src/shop/` bootstrap without portal imports; neutral offline page; noindex meta
- [ ] 2.2 Partner page "Mein Webshop" (status, link, offline reason) and sidebar item
- [ ] 2.3 Company partner detail section "Webshop" (identifier edit with link warning, release, takedown dialog)

## 3. Deployment

- [ ] 3.1 Caddy rule `/shop/*` → `shop.html`; verify SPA routes unaffected
- [ ] 3.2 Dev setup (`docs/SETUP.md`) describes how to open a local shop

## 4. Verification

- [ ] 4.1 Backend tests + package green; `git diff --check`
- [ ] 4.2 Browser check: shop page loads no portal requests (network log), desktop/mobile
- [ ] 4.3 Update traceability and `phase2_backlog.md` B-01 status
