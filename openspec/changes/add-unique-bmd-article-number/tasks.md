## 1. BMD sample gate — essential

- [ ] 1.1 Obtain anonymized BMD article export or binding rule sheet and document column, type, leading zeros, case, whitespace, allowed characters, maximum length and create/update/deactivation semantics; add representative CSV/XLSX fixtures. Stop implementation while this input is absent.

## 2. Persistence and article API — essential

- [ ] 2.1 Add nullable textual BMD number, backfill explicit synthetic demo numbers, detect collisions, then enforce required unique canonical value without changing technical IDs/relations; verify migration twice and rollback-safe failure.
- [ ] 2.2 Add number to article requests/responses, company form, company/partner lists and search; verify leading zeros, same name with different numbers, manual duplicate/update collision, enabled-article isolation, escaping and desktop/mobile behavior.

## 3. Import correctness — essential

- [ ] 3.1 Change article import mapping and identity from name to definition-specific BMD-number canonicalization while preserving category validation; verify missing number, formatted XLSX/CSV leading zeros, file/database duplicates, same-name rows and no silent upsert.
- [ ] 3.2 Translate preview/commit and concurrent unique conflicts into German row/conflict results with full transaction rollback; verify parallel imports, retry, no partial persistence and no misleading accepted report.

## 4. Completion

- [ ] 4.1 Update data/import documentation to state BMD master, create-only import and no reverse master export; run focused tests, full test/package, JS syntax, strict validation, diff check, partnerportal-quality and independent data/security review; fix findings.
