## Why

`Partner.partnerNumber` is currently both the visible business identifier and the Keycloak username lookup key. BMD shall provide the business partner number, so login identity must be stable when that number changes.

## What Changes

- Treat the BMD partner number as the visible, unique business identifier.
- Bind each partner login to the verified OIDC subject (`sub`) instead of the mutable username or partner number.
- Migrate only the synthetic demo account; no real production partner data exists.
- Keep manual Keycloak provisioning and one shared organizational login per partner company.
- Do not add automatic Keycloak provisioning, employee accounts, tenant infrastructure or a direct BMD connector.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `partner-management`: Partner numbers originate from BMD/import and remain independent of login identity.
- `auth-access-control`: Partner resolution uses an explicitly stored OIDC subject and rejects unknown or duplicate bindings.

## Impact

Affected areas are the Partner entity and DTOs, `CurrentPartner`, demo seeding, partner import, company partner management, auth tests, data documentation and canonical specifications. Existing technical IDs, orders, customers, enablements and commission records remain unchanged.
