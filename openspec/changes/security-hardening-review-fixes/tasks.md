## 1. Frontend XSS escaping

- [x] 1.1 In `company-dashboard-page.js` ("Umsatz nach Partner" chart), wrap `partnerName` in `escapeHtml()` from `lib/html.js` for both the label text and any `title="..."` attribute
- [x] 1.2 In `partner-dashboard-page.js` ("Top-Endkunden" chart), wrap Kunde `name` in `escapeHtml()` for label text and `title`
- [x] 1.3 In `router.js` profile menu, wrap `session.username` in `escapeHtml()`
- [x] 1.4 Grep the frontend for other raw interpolations of partner/Kunde/user names into `innerHTML`; escape any additional sinks found (none — `order-form.js`, `partner-order-list-page.js`, `order-details.js`, `data-table.js` already escape; `modal-dialog.js` title uses `.textContent`, not innerHTML)

## 2. Backend CSV formula-injection neutralization

- [x] 2.1 Add a private helper in `CsvExportService` that prefixes a value with `'` when its first character is `=`, `+`, `-`, or `@`
- [x] 2.2 Route all user-controlled cell values (partner name, Kunde first/last name) in the order export and commission export through the helper
- [x] 2.3 Confirm ordinary names and money/date cells are written unchanged (helper is a no-op unless the first char matches `=+-@`; money/date/status/id fields untouched)

## 3. Import error-message hygiene

- [x] 3.1 In `ImportedFileParser`, replace the client-facing messages that concatenate `e.getMessage()` with a fixed German plain-language message
- [x] 3.2 Log the caught POI/opencsv exception server-side (with context) instead of returning it

## 4. Production secrets via environment

- [x] 4.1 In `application.properties` (`%prod`), change DB username/password and the OIDC issuer to `${ENV_VAR:default}` form with development-safe defaults
- [x] 4.2 Document the new environment variables in `docs/SETUP.md`

## 5. Product-image access decision

- [x] 5.1 Add a code comment in `ProductImageResource` recording that `GET /api/product-images/{id}` is intentionally readable by any authenticated partner (product images non-sensitive), so the decision is explicit

## 6. Verify

- [ ] 6.1 Manually confirm a partner name like `<img src=x onerror=alert(1)>` renders as literal text on the company dashboard (no execution)
- [ ] 6.2 Manually confirm an exported CSV cell for a name starting with `=` opens as text in a spreadsheet
- [x] 6.3 Run the backend test suite; confirm existing export/import tests still pass (64/64 pass, BUILD SUCCESS)
- [x] 6.4 Run `openspec validate security-hardening-review-fixes`
