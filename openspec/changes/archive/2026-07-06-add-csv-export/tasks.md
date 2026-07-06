## 1. Backend

- [x] 1.1 `GET /api/company/orders/export` (`@RolesAllowed("company")`, `text/csv;charset=UTF-8`): opencsv `CSVWriter`, separator `;`, UTF-8 BOM written to the stream BEFORE the writer; columns Datum;Partner;Kunde;Wert;Provision %;Provision;Status
- [x] 1.2 `GET /api/company/commissions/export` (same pattern): all `CommissionRecord`s in one query; columns Partner;Bestell-Nr;Basiswert;Provision %;Provision; only orders with a commission record
- [x] 1.3 Tests: header row + one row per record for both; ä/ö/ü/ß names survive UTF-8 round trip incl. BOM; commission export excludes commission-less orders; partner role → 403

## 2. Frontend

- [x] 2.1 `apiDownload(path, filename)` in `lib/api.js`: authenticated fetch (Bearer) → blob → object URL → temp `<a download>` → click → `revokeObjectURL`; 401 routes through existing logout path
- [x] 2.2 "CSV exportieren" button on `company-order-list-page.js` → `apiDownload("/company/orders/export", "bestellungen.csv")`
- [x] 2.3 "CSV exportieren" button on the settlement section of `company-dashboard-page.js` → `apiDownload("/company/commissions/export", "provisionen.csv")`

## 3. Verification

- [x] 3.1 Browser-verify: both downloads land as files while logged in as company; files open in spreadsheet software with correct ä/ö/ü/ß and correct `;` column split
- [x] 3.2 Full backend test suite green
