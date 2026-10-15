## Context

Consent infrastructure exists since `add-shop-legal-consent` (`hasConsent('statistik')`). Orders and revenue come from shop orders (`Order.origin = SHOP`). Decided: in-house, no external analytics (data sovereignty), cookieless by default.

## Goals / Non-Goals

**Goals:** useful KPIs with minimal data; consent-respecting visitor metrics.

**Non-Goals:** session paths, heatmaps, company-wide shop analytics, exports.

## Decisions

1. **Daily aggregate table `shop_stat_daily(shop, day, metric, dimension, count)`** with upsert increments on event receipt; no raw event table. Device class derived from user-agent in memory, then discarded.
2. **Unique visitors via in-memory per-day sets** (or a temporary table cleared after nightly aggregation) of random ids; only counts persist.
3. **Events batched with `navigator.sendBeacon`**, max. 20 per request.
4. **Revenue/orders read from `orders`** to stay consistent with the portal.
5. **Period logic reuses the dashboard's previous-period approach** (client-side comparison pattern from B-08 stub, server delivers both periods).

## Risks / Trade-offs

- [Bots inflate counts] → simple bot user-agent filter, rate limit.
- [In-memory unique sets lost on restart] → acceptable inaccuracy; documented.

## Migration Plan

Additive tables.

## Open Questions

- Should the company see statistics of all shops? (not decided; partner-only in this change)
