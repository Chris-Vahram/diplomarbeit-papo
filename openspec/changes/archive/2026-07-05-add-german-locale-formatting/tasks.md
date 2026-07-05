## 1. Frontend

- [x] 1.1 `frontend/src/lib/format.js`: `formatEuro(value)` (`Intl.NumberFormat("de-AT", {style:"currency", currency:"EUR"})`), `formatNumber(value)` (de-DE for reliable dot-grouping, 2 fraction digits, e.g. `38.940,20`), `formatDate(isoString)` (de-AT with explicit 2-digit day/month, `dd.MM.yyyy`; manual date-part parsing to avoid UTC-midnight timezone shift)
- [x] 1.2 Swap call sites — tables use `formatNumber` (header keeps `(€)`), cards/text use `formatEuro`, dates use `formatDate`: `partner-dashboard-page.js`, `company-dashboard-page.js`, `company-order-list-page.js`, `partner-order-list-page.js`, `company-product-list-page.js`, `partner-product-list-page.js`, `company-partner-list-page.js` (priceLabel in enablement modal), `partner-order-new-page.js` (line totals, Gesamtwert, e-mail preview); existing `"–"` null fallbacks unchanged

## 2. Backend CSV

- [x] 2.1 `CsvExportService`: money values as plain decimal-comma strings (BigDecimal plain string, `.` → `,`, no grouping); `orderDate` via `DateTimeFormatter.ofPattern("dd.MM.yyyy")`
- [x] 2.2 `CsvExportResourceTest`: update expectations (`12,30`, `dd.MM.yyyy`); assert German Excel-parsable form (comma decimal inside quoted `;`-separated fields)

## 3. Verification

- [x] 3.1 Browser-verify: dashboards, order lists, product lists, order confirmation + simulated e-mail all show `1.234,56`-style money and `dd.MM.yyyy` dates; product-list search (name) and partner search still work
- [x] 3.2 Download both CSVs, open with German locale: money columns summable (numbers, not text), dates recognized
- [x] 3.3 Full backend test suite green
