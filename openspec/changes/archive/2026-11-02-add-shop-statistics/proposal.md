## Why

Partners need to see whether their shop works: visits, interest in articles, orders. The team decided on a separate statistics page for partners, built in-house without external analytics services, cookieless by default, with visitor recognition only after consent (`Specification/ENDKUNDENSHOP_DECISIONS.md` §5).

## What Changes

- Server-side collection of aggregated, anonymous shop events: page views, article views, add-to-cart; orders and revenue from shop orders in the database; referrer domain only; device class (Mobil/Tablet/Desktop) only. No IP addresses, no full user agents, no full URLs of referrers stored.
- With statistics consent only: a random visitor identifier in the browser enables unique and returning visitor counts; identifiers are used for daily counting only and discarded after aggregation.
- New partner page "Webshop-Statistik" (submenu of "Mein Webshop") with period selection Diese Woche / Dieses Monat (default) / Dieses Jahr (consistent with Protokoll 12.10.), KPIs (Besuche, Artikelaufrufe, In den Warenkorb, Bestellungen, Umsatz brutto, Conversion-Rate), time chart, top articles, referrer domains, device classes, previous-period comparison.
- Not included: company-wide shop statistics (open), external tools, individual visitor tracking, export.

## Capabilities

### New Capabilities

- `shop-statistics`: Event collection rules, privacy constraints, consent-dependent metrics, partner statistics page.

### Modified Capabilities

None.

## Impact

Backend: public `POST /api/shop/{kennung}/events` (rate-limited, batched), daily aggregate table, scheduled aggregation, partner statistics endpoint. Frontend: event sender in the shop respecting `hasConsent('statistik')`, partner statistics page. Depends on `add-shop-legal-consent` and `add-shop-guest-checkout`.
