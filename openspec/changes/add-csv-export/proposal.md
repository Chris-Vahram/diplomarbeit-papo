## Why

Optional enhancement 9.5 from the MVP build's task list, selected by Chris. The company handles fulfillment and (per FSD) commission accounting outside the portal — Excel is the client's working tool (BMD interface permanently cut, data already enters via Excel/CSV). Exporting orders and commissions as CSV closes that loop in the other direction.

## What Changes

- `GET /api/company/orders/export`: all orders as CSV (Datum, Partner, Kunde, Wert, %-Satz, Provision, Status) — commission columns come straight from `OrderDto`.
- `GET /api/company/commissions/export`: distinct commission-focused CSV — only orders **with** a commission record (Partner, Bestell-Nr, Basiswert, %-Satz, Provision).
- "CSV exportieren" buttons on the company order list and the company dashboard's settlement view.
- **Authenticated download**: the frontend sends a Keycloak Bearer token via fetch; a plain link would omit it and 401. New `apiDownload(path, filename)` helper (fetch → blob → temp `<a download>`).
- **Umlaut-safe**: UTF-8 BOM so Excel (Austrian client) renders ä/ö/ü/ß correctly.

## Capabilities

### New Capabilities

- `data-export`: CSV export of orders and commission records.

### Modified Capabilities

None.

## Impact

- Backend: two export endpoints (company role); CSV via opencsv `CSVWriter` (already a dependency for import — no new library).
- Frontend: `apiDownload` helper in `lib/api.js`; export buttons on `company-order-list-page.js` and the settlement section of `company-dashboard-page.js`.
- **Depends on `add-company-dashboard`** (settlement view hosts one button) and `fix-audit-hardening` (stable fulfilled data). Order-list button has no dependency beyond hardening.
