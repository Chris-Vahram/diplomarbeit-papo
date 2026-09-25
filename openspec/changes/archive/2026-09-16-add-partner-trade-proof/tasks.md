## 1. Private storage and validation — essential

- [x] 1.1 After `add-business-tax-identifiers`, add one-to-one current proof persistence with opaque version and required 1–64 character register identifiers (`AT/GISA`, otherwise `OTHER_TRADE_REGISTER`) for Partner proof and CompanyProfile; verify country/type/value rules, unique ownership, states, timestamps and migration.
- [x] 1.2 Deliberately add a 2 MiB import-file limit and pre-read enforcement for imports/images, then raise global multipart ceiling to measured 11 MiB and enforce 10 MiB proof validation/atomic replacement; verify 413/no-persistence for every oversized route, multipart overhead, MIME mismatch, forged/defective signatures, failed replacement preservation and concurrency.

## 2. Authorization and review — essential

- [x] 2.1 Add partner-own upload/status/download and version-checked company review/download endpoints with explicit roles, private no-store/nosniff attachment headers and no ordinary-DTO bytes; verify stale review returns 409 plus anonymous, cross-partner, partner-to-company and nonexistent-resource denial.
- [x] 2.2 Add partner submission/status UI and company review UI with pending/error/retry states; verify replacement resets review, rejection reason escaping, keyboard use and desktop/mobile behavior. Confirm proof review never changes `Partner.active`.

## 3. Operations and completion

- [x] 3.1 Document GISA terminology, purpose, current-document-only retention, deletion/backup implications and absence of malware guarantee; verify no legal expiry or history claim is made.
- [x] 3.2 Run focused upload/role tests, full test/package, JS syntax, strict validation, diff check, partnerportal-quality and independent security/database review; fix findings before completion.
