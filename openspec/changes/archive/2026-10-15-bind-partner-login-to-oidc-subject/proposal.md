## Why

`CurrentPartner` resolves a partner login by matching the Keycloak username against `Partner.partnerNumber`. Login identity and business number are therefore the same value: a later BMD partner number or a renamed Keycloak user would lock the partner out or link the wrong record. Personal company/partner accounts and tenant isolation (confirmed target, 25.09.2026) also need a stable identity first. This change splits the identity part out of `decouple-partner-number-from-login`, which can proceed without BMD sample data.

## What Changes

- New entity `PartnerLogin`: binds a verified OIDC subject (`sub`) to one Partner. The subject is unique; one Partner may have several bindings (shared login today, personal accounts later).
- `CurrentPartner` resolves partners by the token subject. Username, e-mail and partner number are no longer identity.
- Company-only operations to list, add and remove a partner's login bindings, with a small "Logins" dialog in the partner list. Ordinary partner forms, self-edit and import never accept a subject.
- Transitional fallback (Release A): a token whose subject is unbound may still resolve via username → partner number, but only to a Partner that has no binding yet. Release B removes the fallback after the existing demo login on the VM is bound.
- Dev/test realm gives `partner-demo` a fixed user ID; `DemoDataSeeder` creates its binding.
- No automatic provisioning, no first-login claiming, no employee accounts, no tenancy, no BMD rules, no partner-number change.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `auth-access-control`: "Partner login linking" uses the OIDC subject binding.
- `partner-management`: "Partner login linking via partner number" and "Partner form and API without Keycloak username field" describe subject bindings and the separate company-only binding operation.

## Impact

Backend: new `PartnerLogin` entity/table (`partner_login`, Hibernate update, additive), `CurrentPartner`, new company resource under `/api/company/partners/{id}/logins`, `DemoDataSeeder`, realm export, auth/partner tests. Frontend: company partner list ("Logins" dialog). Docs: `docs/DATA_MODEL.md`, `docs/SETUP.md` (binding procedure). Technical Partner IDs and all relations stay unchanged. `decouple-partner-number-from-login` keeps only the BMD-number part and depends on this change.
