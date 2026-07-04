## 1. Frontend: XSS fix (escape by default)

- [x] 1.1 `data-table.js`: HTML-escape all cell values by default; column defs gain `html: true` opt-in; update every call site's column defs (`actions`, `statusBadge`, `statusSelect`, `enabledCheckbox` columns across company/partner list pages and dashboards)
- [x] 1.2 Add shared `escapeHtml` helper in `frontend/src/lib/` and apply to pages building HTML strings directly: import preview (headers, sample cells, mapping `<option>`s), order confirmation + simulated e-mail, order-form product/customer `<option>` labels, enablement modal (also hardened `modal-dialog.js`'s initial title render, which used raw `innerHTML` interpolation while the attribute-update path already used safe `textContent`)
- [x] 1.3 Browser-verify: `<script>`/`<img onerror>` payload in a Kunde name, partner name, and import-file cell renders as literal text in every list/preview; all action buttons, status selects/badges, and checkboxes still work (verified live via Playwright against dev environment)

## 2. Backend: import correctness (Austrian data)

- [x] 2.1 `PartnerImportDefinition.existsInDb`: case-insensitive check `lower(partnerNumber) = ?1`; test: file "p-100" vs existing "P-100" → rejected as duplicate
- [x] 2.2 `ImportService.normalize()`: transliterate ä→ae/ö→oe/ü→ue/ß→ss before lowercasing/stripping; tests: header "Straße" auto-maps via alias "strasse", "Verfügbar" via "verfuegbar"
- [x] 2.3 `ImportedFileParser.parseCsv`: strict UTF-8 decode with `CharsetDecoder`, on malformed input re-decode ISO-8859-1; test: windows-1252-encoded file with ä/ö/ü/ß imports with correct characters

## 3. Backend + frontend: validation and login link

- [x] 3.1 `PartnerRequest.commissionPercent`: add `@DecimalMax(value = "100.0")` with German message; test >100 rejected
- [x] 3.2 Partner form: custom commission value `required` while preset "Individuell" selected — empty custom value can no longer submit as 0%
- [x] 3.3 Optional `keycloakUsername` in `PartnerRequest` + `PartnerService.applyCompanyFields` + uniqueness check (409, German message, same pattern as `partnerNumber`); import remains untouched (also added to `PartnerDto` so the modal can show the current value)
- [x] 3.4 Partner modal: optional "Keycloak-Benutzername" field (create + edit)
- [x] 3.5 Tests: create/edit with keycloakUsername, duplicate link → 409 (login-resolves-via-CurrentPartner path already covered by existing partner-demo tests; resolve() logic itself is unchanged by this task)

## 4. Backend: order status guard + N+1

- [x] 4.1 `OrderService.updateStatus`: forward-only guard (only `submitted→reviewed`, `reviewed→fulfilled`; else 400 German message); tests: skip (submitted→fulfilled) and revert (fulfilled→reviewed) rejected, order keeps status
- [x] 4.2 `company-order-list-page.js`: rejected transition resets the select — no code change needed, the existing catch-block `this.load()` already rebuilds `table.rows` from the reloaded (unchanged) order status, which resets the `<select>`'s `selected` option
- [x] 4.3 `OrderService.listAll`/`listForPartner`: batch-fetch commission records in one query (`CommissionService.findForOrders`), map by order id (drop per-order `findForOrder`)

## 5. Verification

- [x] 5.1 Full backend test suite green (40/40, incl. new tests from every group above; found + fixed 2 pre-existing test bugs surfaced by the forward-only guard and the keycloakUsername field: `DashboardResourceTest` skipped straight to FULFILLED, and `PartnerResourceTest.setActive()`'s full-PUT helper silently wiped `keycloakUsername` since it wasn't in the reconstructed body)
- [x] 5.2 Browser-verify end-to-end: order flow, import flow (preview + commit with umlaut file, incl. a genuinely ISO-8859-1-encoded file to prove the charset fallback), partner CRUD incl. login link (create/edit + duplicate 409), status advance + rejected skip/revert — all confirmed live via Playwright against the running dev stack
