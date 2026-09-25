## Why

PAPO lacks a consistent tax identity for the portal company and partner companies. An international model must support one understandable identifier while avoiding the false assumption that every Austrian business has a UID.

## What Changes

- Add one required tax identifier group: ISO country, explicit identifier type and value.
- Add a single current portal-company profile shared by company-role users; do not introduce multi-company tenancy.
- Store the same tax identifier group for every partner company.
- For Austria, support UID and national tax number as distinct types with type-specific syntax validation.
- Reuse the same validation for REST forms and partner Excel/CSV import.
- Do not add VIES/register calls, international validation libraries, multiple simultaneous identifiers or a direct BMD connector.

## Capabilities

### New Capabilities

- `business-tax-identifiers`: Required, country-aware tax identification for the portal company and partner companies.
- `company-profile`: One company-role-managed profile for the current portal company without tenant routing.

### Modified Capabilities

- `partner-management`: Company users manage the partner company's tax identifier; partner self-edit cannot change it.
- `data-import`: Partner import maps and validates tax country, type and value consistently with the REST API.

## Impact

Affected areas are new company-profile persistence/API/UI, Partner fields and DTO boundaries, company partner management, partner import definitions, validation, demo data, tests and documentation. Existing partner access and tenant model remain unchanged.
