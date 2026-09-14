## 1. Data and demo migration — essential

- [ ] 1.1 Obtain anonymized BMD partner-number sample or binding rule sheet covering column, length, leading zeros, case, whitespace, allowed characters and corrections; add fixtures. Stop BMD-number implementation while absent, but identity decoupling may proceed separately.
- [ ] 1.2 Release A: add unique nullable OIDC-subject binding without changing technical Partner IDs; give only fresh dev/test realm demo user an explicit stable ID, retain old resolver, and verify existing DB migration twice with `SEED_DEMO_DATA=false`, duplicate-subject rejection and unchanged relations. Stop if any non-demo production partner exists.
- [ ] 1.3 Add a company-only compare-and-set binding operation that never accepts identity through partner self-edit/import; verify first bind, corrected rebind with exact current value, stale current value, unknown partner, blank/duplicate subject and unauthorized roles.

## 2. Authentication and business number — essential

- [ ] 2.1 Before Release B, retrieve existing production demo subject through authorized Keycloak inspection, bind and verify it without logging token/credentials; then resolve exclusively from validated token `sub`, retain inactive blocking/German error, and verify bound/unbound/duplicate/inactive/cross-partner tests plus username changes and safe rollback while numbers are unchanged.
- [ ] 2.2 Release C only after task 1.1: update partner API/UI/import/docs for BMD number independent of login; verify leading zeros, duplicate numbers, no provisioning, preserved relations and that auth rollback after renumbering cannot fall back to username without restoring exact old mappings.

## 3. Completion

- [ ] 3.1 Run focused auth/import tests, full `./mvnw test`, `./mvnw package`, relevant `node --check`, `openspec validate decouple-partner-number-from-login --strict`, `git diff --check`, partnerportal-quality and independent security/data review; fix findings before completion.
