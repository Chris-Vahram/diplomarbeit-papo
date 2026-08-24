## 1. Deliberate Endkunde selection (essential, UX-003)

- [x] 1.1 Update the shared existing-customer select in `frontend/src/lib/order-form.js` with a disabled selected `Bitte Endkunden wählen…` option and `required`, without changing new-customer mode.
- [x] 1.2 Wire and clear the German custom-validity message `Bitte Endkunden wählen.` in the shared picker so both partner and company manual-order forms block an empty selection clearly.
- [x] 1.3 Browser-verify both consumers: partner and company manual-order submission are blocked without a customer, then proceed after an explicit existing-customer choice; new-customer mode remains valid.

## 2. Newest partner order first (essential, UX-004)

- [x] 2.1 Add a focused assertion in the matching Quarkus order test that a partner with multiple orders receives ids in descending order and no foreign-partner order.
- [x] 2.2 Change only `OrderService.listForPartner` to order by descending id; keep the company list query and all partner scoping unchanged.
- [x] 2.3 Browser-verify that a newly submitted partner order appears first in `Meine Bestellungen` and that details/filter behavior remains intact.

## 3. Exact dashboard calendar dates (essential, TECH-001)

- [x] 3.1 Add `toLocalIsoDate(date)` to `frontend/src/lib/dashboard-data.js` using local calendar getters and zero-padding; do not use `toISOString()` or add a dependency.
- [x] 3.2 Import the shared helper in company and partner dashboard pages and remove both duplicate UTC-based helpers.
- [x] 3.3 Browser-verify winter and summer Austrian date boundaries on both dashboards: selected Von/Bis days remain the exact `YYYY-MM-DD` values, inclusive totals use the intended orders, reset still restores all-time data, and enabled-product count remains unchanged.

## 4. Verification and audit handoff (essential)

- [x] 4.1 Run the focused order test during implementation, then the complete documented backend test suite and `./mvnw package`; record exact results and any INFRA-001 workaround used.
- [x] 4.2 Run `openspec validate fix-pre-termin-order-and-dashboard` and `git diff --check`.
- [x] 4.3 Run `partnerportal-quality` and a generic correctness review on the final diff; resolve all in-scope findings.
- [x] 4.4 Create the Batch-B fix report under `docs/partner_portal_audit_suite/Audit/HISTORY/`, set only UX-003, UX-004, and TECH-001 to `Fixed` in the findings register after successful verification, and leave `Verified` for a separate regression review.

No optional implementation is included; work outside UX-003, UX-004, and TECH-001 remains out of scope.
