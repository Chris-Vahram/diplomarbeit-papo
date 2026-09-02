## Why

Client meeting 10.07.2026 (authoritative): "Partner soll Endkunden, die länger nicht mehr bestellt haben, sehen können." A partner currently has no way to see which of their end customers have gone quiet — Kunden only appear inside the order-creation picker. Surfacing dormant Endkunden lets the partner proactively reach out and drive repeat sales, which is exactly the sales-supporting role the portal is meant to play.

## What Changes

- The partner's Kunde list endpoint (`GET /api/partner/customers`) additionally returns a nullable `lastOrderDate` per Endkunde, computed partner-scoped in a single grouped query (no per-Kunde lookups). A Kunde with no orders at all returns `null` — the most dormant case.
- New partner page "Meine Endkunden" (`#/partner/customers`, new sidebar entry): a table of the partner's own Endkunden with a "Letzte Bestellung" column (de-AT date format, "Noch keine Bestellung" for null), defaulting to oldest-first so dormant Endkunden surface at the top, plus a client-side filter to show only Endkunden without a recent order. Dormancy is computed on whole calendar days; threshold 60 days (decided 2026-07-13, confirm value with client at next Termin).
- Dormant rows get a subtle visual marker (badge "Inaktiv" + row highlight), following the existing `.data-table__row--unseen` pattern.
- No change to the shared `data-table` component (page-local ordering only — generalized column sorting deferred on review).

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `customer-management`: partners can additionally see, per own Endkunde, when they last ordered, and can filter for Endkunden without recent orders. (Base spec for this capability is still pending its first archive inside `build-partnerportal-mvp`; this change's delta applies on top of that pending spec — same pattern as the archived `2026-07-08-frontend-fixes-and-additions` change.)

## Impact

- **Backend:** `customer/KundeService.java` (grouped left-join query replacing the plain list), `customer/KundeDto.java` (nullable `lastOrderDate` field). No new endpoint, no entity or schema change. The company's order-on-behalf picker (`/api/company/partners/{id}/customers`) reuses the same service and simply carries the extra field.
- **Frontend:** new `pages/partner-customer-list-page.js`; route + nav entry in `lib/router.js`; small CSS additions in `styles/base.css`. No change to `components/data-table.js`.
- **Scope guard:** partner-side only — no company-side dormant view, no reminders/notifications (real e-mail stays deferred, B-04), no threshold configuration UI. Kunde scoping is untouched: the endpoint still resolves the caller via `CurrentPartner.resolve()`.
- All new UI texts in German with correct umlauts (ä/ö/ü/ß).
