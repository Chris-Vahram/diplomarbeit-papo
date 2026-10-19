## Why

B-01 (end-customer shop) is now being specified (provisional decisions of 2026-10-15 in `Specification/ENDKUNDENSHOP_DECISIONS.md`; Protokoll 12.10.2026: "Mein Webshop" in the partner sidebar). Every later shop change (renderer, builder, checkout, legal, statistics, inquiries) needs a shop record per partner, a stable public address, a company-controlled release and a strictly separated public entry point. This change creates that foundation without any shop content yet.

## What Changes

- New entity `PartnerShop` (1 partner : 1 shop) with a platform-unique, URL-safe identifier (`Kennung`), release state and takedown reason.
- The identifier is suggested from the partner name and set/changed only by the company; it is never the partner number.
- Company releases a shop once; company can take it offline at any time with a mandatory reason shown to the partner.
- Shop is automatically offline when the partner is inactive or has no enabled, available articles.
- Public route `paportal.at/shop/<kennung>` served from a separate frontend entry (`shop.html`) that loads no portal code and no tokens.
- New unauthenticated API namespace `/api/shop/<kennung>/…` with only read access to the shop's public status in this change; rate-limited; no internal API reachable.
- All shop responses carry `noindex` until a later change enables indexing for released live shops; staging is always `noindex`.
- Partner sidebar gets the menu item "Mein Webshop" showing shop status (not released / live / offline with reason) and the public link.
- Not included: pages, blocks, editor, checkout, legal texts, statistics (later changes).

## Capabilities

### New Capabilities

- `partner-shop`: Shop record per partner, identifier rules, company release and takedown, automatic offline rules, partner status view.
- `shop-public-access`: Public shop route and API namespace, isolation from internal APIs, rate limiting, noindex rules.

### Modified Capabilities

- `frontend-shell`: Partner navigation gains "Mein Webshop".

## Impact

Backend: new `shop` package (`PartnerShop`, service, company/partner/public resources), security config for the public path, rate-limit filter. Frontend: new `shop.html` entry + minimal public status page, partner page "Mein Webshop", company partner detail section "Webshop". Deploy: Caddy rule rewriting `/shop/*` to `shop.html`; `X-Robots-Tag` header. Multi-tenancy later inherits the company via `Partner`.
