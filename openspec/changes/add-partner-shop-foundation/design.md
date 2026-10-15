## Context

All existing endpoints are role-protected (`@RolesAllowed`), the frontend is one hash-routed SPA with a login-first ROPC flow, and Caddy serves the SPA with an `index.html` fallback. A public shop is PAPO's first unauthenticated surface. Provisional decisions: path URL, company-set identifier, one-time company release, takedown with reason, inactive partner → offline (`Specification/ENDKUNDENSHOP_DECISIONS.md` §3).

## Goals / Non-Goals

**Goals:** shop record and lifecycle; public route with hard isolation; rate limit and noindex from day one.

**Non-Goals:** pages/blocks/theme (`add-shop-page-renderer`), editor (`add-shop-builder-editor`), checkout, legal texts, statistics, subdomains, custom domains.

## Decisions

1. **`PartnerShop` as own entity (`@OneToOne Partner`, unique)** instead of fields on `Partner`: the shop grows (theme, pages, quota) and must not bloat partner DTOs. Columns: `kennung` (unique), `releasedAt`, `offlineByCompany` (bool), `offlineReason`, `createdAt`. Effective status is computed, not stored: `NOT_RELEASED | ONLINE | OFFLINE_COMPANY | OFFLINE_INACTIVE | OFFLINE_NO_ARTICLES`.
2. **Identifier rules in a `ShopKennung` value helper** (normalise, transliterate, reserved list, suffix on collision). Alternative: partner number — rejected, it can change with BMD.
3. **Separate entry `frontend/shop.html` + `src/shop/` modules.** Caddy: `handle /shop/* { rewrite * /shop.html; file_server }` before the SPA fallback. The shop entry never imports `auth.js`. Alternative: route inside the SPA — rejected, it would load portal code and tokens on a public page.
4. **Public resource `/api/shop/{kennung}` without `@RolesAllowed`, annotated `@PermitAll`** and covered by an explicit HTTP permission entry so the default stays deny for everything else. Unknown and offline shops return the same 404 body.
5. **Rate limit as a JAX-RS `ContainerRequestFilter` bound to the shop namespace**, in-memory token bucket per IP (`X-Forwarded-For` from Caddy only), default 60 requests/min, configurable. No new dependency. IPs stay in memory only.
6. **noindex via response filter and meta tag**, controlled by `papo.shop.indexing-enabled` (default false).
7. **Kill switch `papo.shop.public-enabled` (default false).** Every merge into `main` deploys to the only public VM; there is no staging. While false, every public shop route and `/api/shop/*` call returns the neutral offline response, regardless of shop status. Company and partner management stay usable. The switch stays off on the VM until `add-shop-legal-consent` is merged, so no public shop runs without Impressum, Datenschutz and consent. Alternative: long-lived feature branch — rejected, it breaks the one-change-one-merge increment.

## Risks / Trade-offs

- [In-memory rate limit resets on restart and is per instance] → Acceptable for one VM; revisit with multiple instances.
- [Identifier change breaks shared links] → Only company can change it; UI warns "Bisherige Links funktionieren dann nicht mehr."
- [Enumerating identifiers] → Uniform 404 for unknown/offline shops; rate limit.

## Migration Plan

New table only; no data migration. Rollback: previous image ignores the table; remove the Caddy rule. Deploy with `papo.shop.public-enabled=false`.

## Open Questions

- Subdomain variant (`huber.paportal.at`) remains open per Protokoll 12.10.; path is the provisional decision.
