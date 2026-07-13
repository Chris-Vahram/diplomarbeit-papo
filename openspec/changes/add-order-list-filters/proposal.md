## Why

The client asked twice for better filtering on the order lists: "Bestellliste: Filter wichtig! (v.A. zeitlich)" (client meeting 07.07.2026) and "Filter bei Bestellungen nach Endkunde/Partner" (client meeting 10.07.2026). The time filter (Von/Bis) and a status filter already shipped with `frontend-fixes-and-additions`, but the explicitly requested Partner and Endkunde filters do not exist yet: the company page only has a combined free-text search over partner and Kunde names, and the partner page has no name-based narrowing at all. There is also no way to clear all active filters at once.

## What Changes

- The company order list gets a dedicated Partner filter (dropdown over the partners appearing in the loaded orders) and an Endkunde filter (text match on the Kunde name), alongside the existing date-range and status filters.
- The partner order list gets the same Endkunde filter. It deliberately gets **no** Partner filter — a partner only ever sees their own orders, so filtering by partner is meaningless there.
- `data-table.js` gains a `"text"` filter type (case-insensitive substring match, umlaut-preserving — filtering for "Müller" matches "Müller", no ASCII folding) and a "Filter zurücksetzen" control that clears search, all filter values, and pagination in one click.
- All filters combine with AND semantics and with the existing search; an empty filter imposes no restriction; a filter combination with no matches shows the existing German empty-state message ("Keine Treffer.").
- Everything stays client-side on the already-loaded list, consistent with the `order-list-efficiency` decision that rejected backend pagination/query-splitting at this scale (~10–15 orders/day).

## Capabilities

### New Capabilities
- `order-list-filtering`: How the company and partner order lists are narrowed by partner, Endkunde, time range, and status — which filters each role sees, how filters combine, umlaut-correct matching, reset behavior, and the filtered-empty state. (No published order capability exists yet in `openspec/specs/`; the base `order-management` spec is still pending inside `build-partnerportal-mvp`, so this is a new capability rather than a delta, and it absorbs the already-implemented date/status filters as documented behavior.)

### Modified Capabilities
<!-- None. No published spec covers order lists today. -->

## Impact

- **Frontend only:** `frontend/src/components/data-table.js` (text filter type, reset control), `frontend/src/pages/company-order-list-page.js` (Partner + Endkunde filter wiring, `partnerId` added to row data), `frontend/src/pages/partner-order-list-page.js` (Endkunde filter wiring). Possibly a few lines of `base.css` for the reset button.
- **No backend changes.** `OrderDto` already carries `partnerId`, `partnerName`, and the full `KundeDto` (first/last name), so every filter field is already in the payload of the single existing list request. No new endpoints, no query parameters — deliberately consistent with the client-side-list decision in `openspec/changes/order-list-efficiency/design.md`.
- All new UI texts German with correct umlauts ("Filter zurücksetzen", "Alle", "Endkunde", "Partner").
- Optional, only if trivial: persisting filter state in the URL hash. Flagged as out of scope by default (see design.md).
