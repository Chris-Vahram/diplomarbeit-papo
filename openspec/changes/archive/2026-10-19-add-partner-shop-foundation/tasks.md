## 1. Backend (essential)

- [x] 1.1 Create `PartnerShop` entity, repository and computed status
- [x] 1.2 Implement `ShopKennung` normalisation, transliteration, reserved words, collision suffix; unit tests
- [x] 1.3 Company endpoints: get/set identifier, release, take offline with reason (10–500 chars), re-release
- [x] 1.4 Partner endpoint: own shop status (creates record on first call)
- [x] 1.5 Public `GET /api/shop/{kennung}` with minimal DTO and uniform 404
- [x] 1.6 Rate-limit filter for `/api/shop/*` with 429 message; tests
- [x] 1.7 noindex response filter and `papo.shop.indexing-enabled` config
- [x] 1.8 Tests: partner cannot change identifier, internal APIs still 401, inactive partner / no articles → offline

## 2. Frontend (essential)

- [x] 2.1 `shop.html` entry and `src/shop/` bootstrap without portal imports; neutral offline page; noindex meta
- [x] 2.2 Partner page "Mein Webshop" (status, link, offline reason) and sidebar item
- [x] 2.3 Company partner detail section "Webshop" (identifier edit with link warning, release, takedown dialog)

## 3. Deployment

- [x] 3.1 Caddy rule `/shop/*` → `shop.html`; verify SPA routes unaffected
- [x] 3.2 Dev setup (`docs/SETUP.md`) describes how to open a local shop

## 4. Verification

- [x] 4.1 Backend tests + package green; `git diff --check`
- [x] 4.2 Browser check: shop page loads no portal requests (network log), desktop/mobile
- [x] 4.3 Update traceability and `phase2_backlog.md` B-01 status

## 5. Review feedback (15.10.2026)

- [x] 5.1 Shop URL also locally `/shop/<kennung>` via `tools/dev_frontend_server.py`; no `?kennung=` parameter
- [x] 5.2 Company "Webshop" dialog restructured (status head, sections Freigabe/Kennung, buttons in one row)
- [x] 5.3 Signal colours aligned with order status badges; coloured "Webshop" button per partner row; `GET /api/company/shops` overview with test
- [x] 5.4 Code-review fixes: stale dialog responses ignored, malformed shop URL shows neutral page, partner list loads without shop overview, bare `/shop` routed to shop entry, race-safe shop creation, bounded rate-limiter, shared status rendering, indexing scope documented
- [x] 5.5 Kill switch `papo.shop.public-enabled` (default false in prod; dev/test on): public route and `/api/shop/*` gated, portal hint, `PublicShopSwitchOffTest`; `SHOP_PUBLIC_ENABLED` in `docker-compose.prod.yml`/`docs/DEPLOYMENT.md` (from closed PR #42)
