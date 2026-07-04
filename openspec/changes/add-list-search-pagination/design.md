## Context

`data-table.js` is the shared list component (columns + rows as JS properties, Light DOM, delegated event listeners on the host element). Three lists will hit real scale after import: company products (~800), company partners (~200), partner's enabled products. `fix-audit-hardening` lands escape-by-default rendering in the same component first.

## Goals / Non-Goals

**Goals:**
- Search + pagination on the three large lists, usable at ~800 rows.
- Zero behavior change for existing `data-table` usages that don't opt in.
- Correct matching for Austrian names (ä/ö/ü/ß, case-insensitive).

**Non-Goals:**
- No backend pagination/search API (no `?page=`/`?search=` contracts) — data volume doesn't justify it; the browser filters the already-fetched list.
- No sorting (not requested; column order = server order).
- 9.3 (category/image UI) stays out.

## Decisions

**Extend `data-table` in place, not a new component.** Optional `.searchKeys` (array of data-field keys) and `.pageSize` (number). When set, render a search input + pager and filter/slice `this._rows` before rendering. Alternative — separate `<data-table-paginated>` — rejected: duplicates the row-render logic.

**Search matches raw data values at `.searchKeys`, never rendered cell HTML.** Cells contain markup (`actions` buttons, status badges/selects, checkboxes); matching against rendered HTML would hit `button`/`select` for every row. Callers pass `searchKeys: ["name"]`.

**Umlaut handling:** JS `toLowerCase()` correctly lowercases ä/ö/ü — plain case-insensitive substring match covers "müller" ↔ "Müller GmbH". No ASCII-folding (searching "mueller" does not match "Müller") — that mapping is ambiguous for names and not worth the complexity at this scale; the client types umlauts on Austrian keyboards.

**Delegated listeners survive re-render.** Click/change listeners sit on the `<data-table>` host element (verified in all three pages), so re-rendering the body on search/page change keeps edit buttons, status selects, and enablement checkboxes working without changes.

**Search resets to page 1; pager shows "Seite X von Y".** German UI text, consistent with the rest of the app.

## Risks / Trade-offs

- **[Risk]** Touching the shared component affects dashboards/order lists that don't use search → **Mitigation**: both properties optional; unset = exact current behavior; regression check is an explicit task.
- **[Trade-off]** Full list always fetched. Negligible at ~800 rows; documented limit if the catalog grows 10×.

## Migration Plan

No migration. Feature branch after `fix-audit-hardening` merges. Frontend-only.

## Open Questions

None.
