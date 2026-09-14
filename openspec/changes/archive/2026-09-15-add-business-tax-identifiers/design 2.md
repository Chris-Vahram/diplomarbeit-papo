## Context

No Company entity exists; company-role users currently operate one global portal company. Partner addresses contain a free-text country and cannot serve as tax jurisdiction.

## Goals / Non-Goals

**Goals:** One tax identifier per business; shared API/import validation; minimal single-company profile.

**Non-Goals:** Multi-tenancy, multiple identifiers, register/VIES verification, tax advice, direct BMD API.

## Decisions

- Add singleton `CompanyProfile` with a fixed unique singleton key; do not add tenant foreign keys to existing entities.
- Store `taxCountry`, `taxIdentifierType`, `taxIdentifierValue` as explicit fields for both CompanyProfile and Partner. Country uses ISO alpha-2; value max 64 characters.
- Supported types are `VAT_ID` and `NATIONAL_TAX_ID` for every ISO country. Values are trimmed and uppercased. For `AT`, `VAT_ID` must match `ATU` plus eight digits and `NATIONAL_TAX_ID` exactly nine digits. Other countries receive only conservative 2–64 character validation (`A–Z`, digits, space, `.`, `_`, `/`, `-`); PAPO explicitly labels this syntax-only, not country-specific validity.
- Existing demo rows receive explicit synthetic identifiers. Because production has only demo data, no compatibility fallback or nullable production period is needed after migration verification.
- Validation proves syntax only. No remote registry call or new dependency.

## Risks / Trade-offs

- [Risk] Official formats change. Mitigation: isolated validator plus source/date documentation and tests.
- [Risk] Singleton mistaken for tenancy foundation. Mitigation: no tenant ID, routing or per-company ownership changes.

## Migration Plan

Add profile and nullable partner fields, seed synthetic values, backfill verified demo rows, then enforce required fields. Rollback removes UI enforcement first; additive data may remain.
