## Why

A full-code audit (2026-07-04, all backend classes + all frontend files) of the finished MVP found one security defect, two bugs, and a handful of robustness gaps. These must land **before** the planned post-MVP features, because two of them live in code those features build on (`data-table.js`, `OrderService`). The client is Austrian — correct handling of ä/ö/ü/ß in imports is a hard requirement, not polish.

## What Changes

- **Stored-XSS fix (security):** `data-table.js` renders cell values raw via `innerHTML`; partner-controlled text (own name, Kunde names) and import-file cells execute in the *company* session. Escape all cell values by default; markup columns opt in via `html: true`. Shared `escapeHtml` helper for pages building HTML strings directly (import preview, order confirmation, `<option>` labels).
- **Partner import duplicate check case-insensitive:** DB check is exact-match while the in-file check lowercases — `p-100` slips past an existing `P-100`. Align to `lower(partnerNumber)`.
- **Custom commission validation:** empty custom value currently saves silently as 0% (`Number("")` → 0); require the field when preset "Individuell" is active, add `@DecimalMax(100)` backend-side.
- **Company can link a Keycloak login:** optional `keycloakUsername` on `PartnerRequest` + partner form. Today only the dev seeder sets it — a real deployment needs raw SQL. Linking only; Keycloak provisioning stays manual, import never touches it.
- **Forward-only order status:** `submitted → reviewed → fulfilled`, no skips/reverts. Protects fulfilled-only dashboard totals from retroactive shifts.
- **Umlaut-aware import auto-suggest:** transliterate ä→ae/ö→oe/ü→ue/ß→ss before normalizing so headers like "Straße"/"Verfügbar" match aliases "strasse"/"verfuegbar".
- **CSV import charset fallback:** strict UTF-8, on malformed input re-decode ISO-8859-1 (German Excel default) instead of silently importing mojibake umlauts.
- **N+1 fix in order lists:** `OrderService.listAll`/`listForPartner` query commissions per order; batch-fetch instead.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `partner-management`: company can set an optional, unique `keycloakUsername` when creating/editing a partner.
- `order-management`: status workflow becomes forward-only (no skips, no reverts).

The XSS, import, validation, and N+1 items are bug fixes within existing requirements (rendering a name must not execute it; duplicate detection must not be case-dependent) — no spec-level behavior change.

## Impact

- Backend: `PartnerRequest`/`PartnerService` (keycloakUsername, @DecimalMax), `OrderService` (status guard, batch commission fetch), `PartnerImportDefinition` (lower()), `ImportService.normalize()` (transliteration), `ImportedFileParser` (charset fallback). No schema changes (`Partner.keycloakUsername` column already exists, already unique).
- Frontend: `data-table.js` (escape by default + `html:` flag — all call sites' column defs updated), new `escapeHtml` in `lib/`, `company-import-page.js`, `partner-order-new-page.js`, `company-partner-list-page.js` (escaping, custom-commission required, keycloakUsername field), `company-order-list-page.js` (select reset on rejected transition).
- Blocks: `add-list-search-pagination` (same `data-table` render path) and `add-company-dashboard`/`add-csv-export` (rely on stable fulfilled totals) should apply **after** this change.
