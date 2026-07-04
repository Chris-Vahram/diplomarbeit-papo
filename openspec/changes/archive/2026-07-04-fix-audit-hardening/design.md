## Context

MVP is feature-complete and merged. Audit findings (2026-07-04) span `data-table.js` (shared render path), the import pipeline, partner CRUD validation, and `OrderService`. Reference client is Austrian — ä/ö/ü/ß correctness in the import pipeline is a hard requirement. Three follow-up feature changes (`add-list-search-pagination`, `add-company-dashboard`, `add-csv-export`) build on the code touched here, so this change applies first.

## Goals / Non-Goals

**Goals:**
- Close the stored-XSS hole across all rendering paths.
- Make partner import duplicate detection and header auto-suggest correct for German/Austrian data (case, umlauts, charset).
- Prevent silently-invalid commission values (0% via empty field, >100%).
- Give the company an admin surface for linking Keycloak logins.
- Enforce forward-only order status transitions.
- Remove the order-list N+1.

**Non-Goals:**
- No new features (those are separate changes).
- No Keycloak provisioning automation — linking only, provisioning stays manual (locked decision).
- No schema changes.

## Decisions

**XSS fix lives in `data-table` escape-by-default.** All cell values are HTML-escaped before insertion; columns that intentionally carry markup (`actions`, `statusBadge`, `statusSelect`, `enabledCheckbox`) opt in via `html: true` on the column def. Alternative — escaping at each call site — rejected: one forgotten site reopens the hole; the component is the chokepoint. A shared `escapeHtml` helper (in `frontend/src/lib/`) covers the pages that build HTML strings outside `data-table`: import preview (headers, sample cells, mapping `<option>`s), order confirmation + simulated e-mail, order-form product/customer `<option>` labels, enablement modal.

**Partner import DB duplicate check goes case-insensitive** (`lower(partnerNumber) = ?1`), matching the in-file check (already lowercased) and the product definition's existing `lower(name)` pattern.

**Custom commission:** the partner form marks the custom-value field `required` while preset "Individuell" is selected (today `Number("")` → silently 0%); backend `PartnerRequest.commissionPercent` gains `@DecimalMax("100")` to match the form's `max=100`.

**`keycloakUsername` becomes company-editable.** Optional field on `PartnerRequest`, applied in `PartnerService.applyCompanyFields`, shown in the partner modal. Uniqueness conflict → 409 with German message (same pattern as `partnerNumber`). This is *linking* an existing manually-provisioned Keycloak user to a Partner record — the "Keycloak users provisioned manually" decision stands, import still never sets the field.

**Forward-only status guard in `OrderService.updateStatus`.** Only `submitted→reviewed` and `reviewed→fulfilled` pass; everything else → 400 with a clear German message. Rationale: dashboard/settlement totals count fulfilled orders only — a revert would retroactively shift already-reported numbers. Frontend order list keeps the `<select>`; on a rejected transition the existing error path (toast + reload) resets it.

**Import auto-suggest transliterates before normalizing:** ä→ae, ö→oe, ü→ue, ß→ss, then lowercase + strip non-alphanumerics. "Straße" → `strasse` matches alias `strasse`; "Verfügbar" → `verfuegbar` matches alias `verfuegbar`. Manual mapping remains the fallback for anything unmatched.

**CSV import charset fallback:** decode with a strict UTF-8 `CharsetDecoder`; on `MalformedInputException`/`UnmappableCharacterException` re-decode as ISO-8859-1 (the German-locale Excel default) rather than importing replacement characters. xlsx is unaffected (zip container, charset-safe).

**Order-list N+1:** `listAll`/`listForPartner` fetch the relevant `CommissionRecord`s in one query and map by order id, replacing the per-order `findForOrder` calls.

## Risks / Trade-offs

- **[Risk]** Escape-by-default breaks columns that legitimately render markup → **Mitigation**: audit of all call sites is part of the task (five pages, four markup column types); browser-verify pass checks every list still renders controls.
- **[Risk]** Forward-only guard removes the company's ability to correct a misclick → **Mitigation**: accepted for MVP scale; a deliberate revert can be done in the DB if ever truly needed, and the guard is one method — trivially relaxed later if the client asks.
- **[Risk]** ISO-8859-1 fallback misdecodes a genuinely broken file as garbage instead of failing → **Mitigation**: preview step shows decoded sample rows before commit; the user sees wrong umlauts before anything is persisted.

## Migration Plan

No data migration. Bug-fix branch off `main`, normal phase-branch review + merge. Apply **before** the three feature changes.

## Open Questions

None.
