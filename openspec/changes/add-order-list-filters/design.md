## Context

Both order list pages render through the shared `data-table` component (`frontend/src/components/data-table.js`), which already provides client-side search (`.searchKeys`, lowercase substring match), client-side pagination (`.pageSize`), and a `.filters` API with two filter types: `"select"` (exact match on a row key) and `"daterange"` (ISO-string comparison on a row key). `company-order-list-page.js` and `partner-order-list-page.js` both already wire a Datum daterange filter (`orderDateIso`) and a Status select filter; the company page additionally has free-text search over `partnerName` + `kundeName`. All predicates compose in `_filteredRows()` (AND), and a filtered-empty result already shows "Keine Treffer.".

What the client asked for beyond that (meetings 07.07 + 10.07.2026) is dedicated narrowing by Partner and by Endkunde, which today only exists indirectly through the company page's combined search. There is also no one-click way to clear all active filters.

`OrderDto` (`backend/src/main/java/at/papo/partnerportal/order/OrderDto.java`) already carries `partnerId`, `partnerName`, and the full `KundeDto`, so every filter field is present in the single list response — no backend gap.

## Goals / Non-Goals

**Goals:**
- Company order list: filter by Partner (dropdown) and Endkunde (text), combining with the existing Datum and Status filters.
- Partner order list: filter by Endkunde (text), combining with the existing Datum and Status filters.
- One-click "Filter zurücksetzen" that clears search, all filters, and resets pagination.
- Umlaut-correct matching: filtering for "Müller" matches the Kunde "Müller" (and lowercase/uppercase variants), with no ASCII folding.

**Non-Goals:**
- No backend changes of any kind — no query parameters, no new endpoints, no DTO changes. The `order-list-efficiency` design explicitly rejected server-side pagination/filtering as out of proportion for ~10–15 orders/day; this change stays on that decision.
- No partner filter on the partner page (a partner only sees own orders).
- No URL-hash persistence of filter state. It would need serialization/deserialization logic in every list page plus router coordination for a feature nobody asked for; can be a follow-up if the client requests shareable filtered views.
- No changes to search behavior, column set, sorting, or the CSV export (export continues to export the full list, not the filtered view — matching current behavior).

## Decisions

- **New `"text"` filter type in `data-table.js` instead of page-local filtering.** The Endkunde filter is a case-insensitive substring match on one row key (`kundeName`) — exactly the search semantics, but scoped to a single column. Adding `type: "text"` to the existing `.filters` API keeps all predicate logic in `_matchesFilters()`/`_filteredRows()` where select/daterange already live, and makes the type reusable (e.g. future per-column filters on other tables). Alternative considered: pre-filtering `rows` in each page before handing them to the table — rejected, it would duplicate toolbar rendering, empty-state, and page-reset logic per page and bypass the component's single filtering pipeline. Matching uses the same `String(value).toLowerCase().includes(query)` approach as search: JavaScript `toLowerCase()` handles ä/ö/ü/ß correctly ("MÜLLER" → "müller"), and deliberately does **not** fold umlauts to ASCII — "Muller" does not match "Müller", consistent with the project rule against ASCII-folding surprises.
- **Partner filter as a `"select"` over `partnerId`, options derived from the loaded orders.** The row data gains a `partnerId` field (already in the DTO) and the select uses `key: "partnerId"` with `value: String(partnerId)`, `label: partnerName` — id-keyed so two partners with the same display name never collapse into one filter entry. Options are the distinct partners present in the loaded order list, sorted with `localeCompare("de-AT")`. Alternative considered: fetching `/company/partners` for a complete dropdown — rejected: an extra request per page view to offer partners whose selection can only ever produce an empty list. `_matchesFilters()` already compares `String(row[key]) === value`, so numeric `partnerId` needs no special handling.
- **"Filter zurücksetzen" lives in the `data-table` toolbar, rendered whenever search or filters are configured.** One button that resets `_searchQuery`, re-initializes `_filterValues` (as the `filters` setter already does), and sets `_page = 1`. Putting it in the component gives every filtered table (orders, partners, products) the same reset for free. Alternative considered: only showing the button while a filter is active — nicer, but adds conditional-rendering state for marginal benefit; a always-visible button is simpler and predictable. (If trivially cheap during implementation, disabling it while nothing is active is acceptable polish, not a requirement.)
- **Von/Bis stays the existing `daterange` control.** The dashboards' period picker (`#period-from`/`#period-to`, native `<input type="date">`) and the table's daterange filter already use the same native date inputs, so they render consistently (de-AT format comes from the browser locale). No new date control is introduced.
- **Filtered-empty state reuses "Keine Treffer.".** Already German, already distinct from the no-data-at-all message ("Noch keine Bestellungen vorhanden."). No change needed; the spec pins it as required behavior.

## Risks / Trade-offs

- [Partner dropdown only lists partners that have orders in the loaded list — a company user might look for a partner that placed no orders yet] → Acceptable: selecting such a partner could only show an empty table; the dropdown reflecting reality is arguably clearer. Documented as a decision, revisit only on client feedback.
- [Re-rendering on each text-filter keystroke rebuilds the table's innerHTML; focus/caret must be restored like the existing search input already does] → Reuse the exact focus/caret-restore pattern from the `data-table-search` input handler; verify by typing multi-character queries including umlauts.
- [Toolbar with search + four filters + reset may crowd narrow viewports] → The toolbar already wraps (`.data-table__toolbar`/`.data-table__filters` flex styles); verify at mobile width during the browser check, adjust `base.css` flex-wrap only if needed.
- [Filters and client-side pagination interact: an active filter must reset to page 1, and the pager must count filtered rows] → Already the component's behavior for existing filters (`_page = 1` on change, pager computed from `_filteredRows()`); the new type follows the same code path and is covered by an explicit verify task.

## Migration Plan

Frontend-only, additive, no data or API migration. Rollback = revert the commit.

## Open Questions

None. Filter fields, per-role filter sets, and matching semantics were fixed by the client meetings and the existing component architecture.
