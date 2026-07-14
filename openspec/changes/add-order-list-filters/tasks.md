## 1. data-table component

- [x] 1.1 Add `"text"` filter type to `frontend/src/components/data-table.js`: render a text input per `type: "text"` filter def, store its value in `_filterValues`, and match in `_matchesFilters()` via case-insensitive substring on `String(row[key])` (same `toLowerCase().includes()` semantics as search — no ASCII folding); restore focus/caret after re-render like the existing `data-table-search` handler
- [x] 1.2 Add a "Filter zurücksetzen" button to the toolbar (rendered whenever search or filters are configured) that clears `_searchQuery`, re-initializes all `_filterValues`, resets `_page` to 1, and re-renders
- [x] 1.3 Add minimal `base.css` styling for the text-filter input and reset button so they align with the existing toolbar controls (only if the existing styles don't already cover them)

## 2. Company order list

- [x] 2.1 Add `partnerId` to the row mapping in `company-order-list-page.js` (`renderRows()`; value already present on `OrderDto`)
- [x] 2.2 Add a Partner select filter (`key: "partnerId"`, label "Partner") with options built from the distinct partners in the loaded orders — `value: String(partnerId)`, `label: partnerName`, sorted with `localeCompare("de-AT")` — ahead of the existing Datum/Status filters
- [x] 2.3 Add an Endkunde text filter (`key: "kundeName"`, type `"text"`, label "Endkunde")

## 3. Partner order list

- [x] 3.1 Add an Endkunde text filter (`key: "kundeName"`, type `"text"`, label "Endkunde") to `partner-order-list-page.js`; confirm no Partner filter is added

## 4. Verify (browser, dev stack via docker-compose + demo users)

- [x] 4.1 Company list: each filter alone — Partner, Endkunde, Von/Bis, Status — narrows correctly; "Alle"/empty values impose no restriction
- [x] 4.2 Company list: combined filters AND together (e.g. Partner + date range + Endkunde); changing a filter returns to page 1 and the pager counts filtered rows; with >20 matching rows, pagination still pages within the filtered set (pagination-over-filtered-array logic is unchanged/pre-existing; verified AND-combination + pager count directly, dataset too small to exceed 20 filtered rows)
- [x] 4.3 Umlaut check: an Endkunde "Müller" is found by typing "mül" and "MÜLLER" but not by "Muller"; typing multi-character queries keeps input focus/caret
- [x] 4.4 "Filter zurücksetzen" clears search + all filters on both lists and restores the full list from page 1
- [x] 4.5 Filtered-empty: a no-match combination shows "Keine Treffer." with controls still usable; a partner with zero orders still sees "Noch keine Bestellungen vorhanden."
- [x] 4.6 Partner list (partner-demo): Endkunde/Datum/Status filters work, no Partner filter is rendered; no additional HTTP request fires when changing filters (network tab)
- [x] 4.7 Toolbar layout holds at narrow (mobile) viewport on both order lists; adjust `base.css` wrap only if broken (holds cleanly at 375px on both lists, no CSS change needed)
- [x] 4.8 Run `openspec validate add-order-list-filters` and the partnerportal-quality review before handing over branch + commit message
