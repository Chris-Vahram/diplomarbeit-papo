## 1. Authoritative rules and persistence — essential

- [ ] 1.1 Record dated BMF sources and fixtures for Austrian `ATU` plus eight digits and nine-digit national tax number; document generic non-AT 2–64-character syntax and verify links/test vectors while never presenting syntax as register validity.
- [ ] 1.2 Add singleton CompanyProfile and one tax-country/type/value group to CompanyProfile and Partner, migrate only synthetic demo data, enforce one profile and complete groups, and verify initialization concurrency, 64-character boundary and migration twice.

## 2. Shared validation and APIs — essential

- [ ] 2.1 Implement one validator for ISO country, supported type and value; cover AT UID, AT national tax number, unsupported combinations, normalization and partial groups with focused unit/resource tests.
- [ ] 2.2 Add company-only profile management and partner-tax management while excluding tax fields from partner self-edit; verify roles, manipulated self-update, error messages and no cross-boundary exposure.
- [ ] 2.3 Extend partner CSV/XLSX mapping with the same required validation and row reporting; verify valid/invalid/partial identifiers, umlauts, transactional behavior and no invented fallback values.

## 3. UI and completion

- [ ] 3.1 Add existing-style company profile and partner form controls using country/type/value, native constraints and German explanations; verify keyboard, escaping, errors/retry and desktop/mobile behavior.
- [ ] 3.2 Run focused tests, full `./mvnw test`, `./mvnw package`, JS syntax, strict OpenSpec validation, diff check, partnerportal-quality and independent security/data review; update data/setup docs and fix findings before completion.
