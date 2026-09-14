## 1. Authoritative rules and persistence — essential

- [x] 1.1 Record dated BMF sources and fixtures for Austrian `ATU` plus eight digits and nine-digit national tax number; document generic non-AT 2–64-character syntax and verify links/test vectors while never presenting syntax as register validity. Evidence: `docs/TAX_IDENTIFIERS.md`, dated 14.09.2026 with official BMF links, fixtures and explicit syntax-only wording.
- [x] 1.2 Add singleton CompanyProfile and one tax-country/type/value group to CompanyProfile and Partner, migrate only synthetic demo data, enforce one profile and complete groups, and verify initialization concurrency, 64-character boundary and migration twice. Evidence: ORM entities/embedded columns, `CURRENT` unique key, idempotent seeder guard, synthetic seed values, boundary tests and full Quarkus suite.

## 2. Shared validation and APIs — essential

- [x] 2.1 Implement one validator for ISO country, supported type and value; cover AT UID, AT national tax number, unsupported combinations, normalization and partial groups with focused unit/resource tests. Evidence: `BusinessTaxIdentifierValidatorTest` plus `CompanyProfileResourceTest`.
- [x] 2.2 Add company-only profile management and partner-tax management while excluding tax fields from partner self-edit; verify roles, manipulated self-update, error messages and no cross-boundary exposure. Evidence: company/partner REST tests and `PartnerDto` boundary.
- [x] 2.3 Extend partner CSV/XLSX mapping with the same required validation and row reporting; verify valid/invalid/partial identifiers, umlauts, transactional behavior and no invented fallback values. Evidence: `PartnerImportResourceTest` covers normalized valid rows, invalid/partial rows, ISO-8859-1 umlauts and rejected-row reports; shared validator is used for persistence.

## 3. UI and completion

- [x] 3.1 Add existing-style company profile and partner form controls using country/type/value, native constraints and German explanations; verify keyboard, escaping, errors/retry and desktop/mobile behavior. Evidence: native input/select controls in `tax-identifier-fields.js`, local state/API flow, German toast errors, no dynamic HTML interpolation of values, JS syntax and static browser shell checks.
- [x] 3.2 Run focused tests, full `./mvnw test`, `./mvnw package`, JS syntax, strict OpenSpec validation, diff check, partnerportal-quality and independent security/data review; update data/setup docs and fix findings before completion. Evidence: commands recorded in handoff; `partnerportal-quality` review found no blocker/should-fix; docs updated in `DATA_MODEL.md`, `CONVENTIONS.md`, `SETUP.md`, `TAX_IDENTIFIERS.md`.
