## Context

Money renders via `toFixed(2)` in 8 pages (dashboards, order lists, product lists, partner list, order confirmation/e-mail preview); dates pass through as ISO strings from `LocalDate` JSON. CSV exports write `BigDecimal.toString()` (dot decimal) and ISO dates. UI language is German throughout; the design mockups show `€ 38.940,20` / `04.07.2026`.

## Goals / Non-Goals

**Goals:**
- Every user-visible money value and date in German (de-AT) format, one shared helper.
- CSV exports parse as numbers/dates in German-locale Excel (the export's target consumer).

**Non-Goals:**
- No locale switching/i18n framework — the app is German-only by requirement; `de-AT` is hardcoded.
- No backend JSON changes — APIs keep returning raw numbers/ISO dates (machine format); formatting is presentation-side. CSV is the exception because the CSV **is** the presentation for Excel.
- No timezone handling — `orderDate` is a plain date.

## Decisions

**One `lib/format.js`, three functions, `Intl` built-ins (no library).**
- `formatEuro(value)` → `Intl.NumberFormat("de-AT", { style: "currency", currency: "EUR" })` → `€ 391,80`. For metric cards and the confirmation/e-mail text, replacing hand-built `${x.toFixed(2)} €`.
- `formatNumber(value)` → same locale, `minimumFractionDigits: 2` → `38.940,20`. For table cells whose column header already carries `(€)` — keeps columns compact, mirrors the mockups' settlement table.
- `formatDate(isoString)` → `Intl.DateTimeFormat("de-AT")` on a `Date` parsed from the ISO string → `05.07.2026`.
`null`/`undefined` stay the caller's concern (existing `"–"` fallbacks unchanged).

**CSV numbers via `String.replace('.', ',')` on the BigDecimal plain string, dates via `DateTimeFormatter.ofPattern("dd.MM.yyyy")`.** `BigDecimal` scale is already fixed at 2 by the entities; a locale-aware `NumberFormat` on the backend would add grouping separators (`1.234,50`), which German Excel then misreads inside a `;`-CSV — plain decimal-comma without grouping is the robust form. opencsv quoting already protects the comma from being read as a separator (and `;` is the separator anyway).

**Search/pagination unaffected by construction.** `data-table` search matches raw values at `searchKeys` (`name`, `partnerNumber`) — never price/date labels — so formatting cannot break filtering.

## Risks / Trade-offs

- **[Risk]** CSV test expectations break silently elsewhere → **Mitigation**: `CsvExportResourceTest` assertions updated in the same change; suite must be green.
- **[Trade-off]** `€` symbol position/space from `Intl` (`€ 391,80`) may differ cosmetically from Tobi's final design → CSS/copy-level tweak later at FD.3, not a formatting-logic change.
- **[Trade-off]** Backend CSV formatting duplicates the "German format" idea in two places (JS + Java) → accepted: they format different artifacts for different consumers (DOM vs Excel).
