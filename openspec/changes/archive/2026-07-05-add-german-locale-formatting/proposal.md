## Why

The client is Austrian and the whole UI is German, but every money value renders in US format (`38940.20` via `toFixed(2)`) and every date as raw ISO (`2026-07-05`). Tobi's design mockups (2026-07-05) correctly show German formatting (`€ 38.940,20`, `04.07.2026`) — the code should match the locale the client actually reads. Same defect on the CSV exports: German-locale Excel (the reason we chose the `;` separator) treats dot-decimal values like `12.30` as text, so the client cannot sum the Provision column — the export's whole purpose.

## What Changes

- New `frontend/src/lib/format.js`: `formatEuro` (metric cards, e-mail preview: `€ 391,80`), `formatNumber` (table cells under a `(€)` header: `38.940,20`), `formatDate` (`05.07.2026`) — all via `Intl` with `de-AT`.
- Applied everywhere the frontend currently uses `toFixed(2)` or raw ISO dates: both dashboards, both order lists, both product lists, partner list, order confirmation + simulated e-mail.
- CSV exports (orders + commissions): numbers with decimal comma, dates as `dd.MM.yyyy`, so German-locale Excel parses them as numbers/dates instead of text.

## Capabilities

### New Capabilities

- `locale-formatting`: German (de-AT) formatting of currency, numbers, and dates across the UI.

### Modified Capabilities

- `data-export`: CSV number and date values switch to German locale format (decimal comma, dd.MM.yyyy).

## Impact

- Frontend: new `lib/format.js`; mechanical call-site swap in 8 page files. `data-table` search is unaffected (search keys are names, never formatted prices/dates).
- Backend: `CsvExportService` formats `BigDecimal`/`LocalDate` German-style; `CsvExportResourceTest` expectations updated.
- No schema, no endpoint, no entity changes.
