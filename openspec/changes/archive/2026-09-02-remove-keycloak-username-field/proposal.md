## Why

The manual "Keycloak-Benutzername" field on the company's partner create/edit form confuses users: the client asked for it to go (meeting 07.07.2026, "Partner anlegen: … Keycloak Feld weg"), and Chris confirmed on 10.07.2026 that the field is unclear and unimportant to the people who fill in the form. The field only exists to link a manually provisioned Keycloak login to a Partner record — a job the already-mandatory, already-unique `Partner.partnerNumber` can do instead, without asking the company to understand what Keycloak is.

## What Changes

- **Decision (locked 2026-07-13, do not re-litigate):** the Partner↔Keycloak link key becomes `Partner.partnerNumber`. Convention: Keycloak users are provisioned manually with username == the partner's partner number. (`Partner.email` was rejected: optional and non-unique. Keycloak `sub` UUID was rejected: partners are imported before their Keycloak users exist.)
- `CurrentPartner.resolve()` matches the Keycloak username claim against `Partner.partnerNumber` instead of the dropped `keycloakUsername` column. Partner numbers are canonicalized to upper-case on write and the claim is upper-cased before an **exact** match — so Keycloak's lowercased usernames still resolve, without the case-variant-duplicate ambiguity a naive `lower()` match would leave (see design Decision 2).
- **Application-model + API removal:** `Partner.keycloakUsername` is removed from the entity, `PartnerDto`, and `PartnerRequest`; the duplicate check `assertKeycloakUsernameFree` and its 409 message disappear. Company partner create/edit payloads and responses no longer carry the attribute. (This is not a hard DB migration: dev/test drop-and-create, and the demo profile's Hibernate `update` mode leaves the now-unused nullable column in place — harmless, optional manual cleanup only.)
- The "Keycloak-Benutzername" input is removed from the partner create/edit form (`company-partner-list-page.js`); the edit form gains a hint that the Partnernummer is now the login identity.
- Dev/test seed data is realigned: the demo partner that the Keycloak realm user `partner-demo` logs in as gets canonical partnerNumber `PARTNER-DEMO` (see design.md for the alternative that was rejected).
- Docs follow: `docs/DATA_MODEL.md`, `docs/SETUP.md` (provisioning convention "Keycloak-Username = Partnernummer"), `Specification/CONTINUATION_PROMPT.md` entity list.

Explicitly **not** in this change: the address-field split and every other 07.07 meeting item; Keycloak automation of any kind (provisioning stays manual, import still never creates Keycloak users).

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `partner-management`: the published spec today only covers list search/pagination — no published requirement mentions `keycloakUsername`, so the delta consists of ADDED requirements: login linking via partner number, and the partner form/API carrying no Keycloak username field. (The archived change `2026-07-04-fix-audit-hardening` proposed a "Company links Keycloak login to partner" requirement, but it was never merged into the published spec; this change supersedes it. The auth-access-control spec inside the unarchived `build-partnerportal-mvp` change stays literally true — logins are still resolved "via the Keycloak username claim" — only the DB column it matches against changes.)

## Impact

- **Backend:** `Partner.java` (drop column), `PartnerDto.java`, `PartnerRequest.java`, `PartnerService.java` (drop `assertKeycloakUsernameFree` + call sites + field mapping), `security/CurrentPartner.java` (lookup by partnerNumber), `seed/DemoDataSeeder.java` (drop parameter, rename demo partnerNumber).
- **Backend tests:** `PartnerResourceTest` (keycloakUsername create/update/conflict assertions, PUT full-replace body), `OrderResourceTest` (update body + `P-1001` lookups), `EnablementResourceTest`, `PartnerImportResourceTest` (seeded-duplicate rows reference `P-1001`).
- **Frontend:** `frontend/src/pages/company-partner-list-page.js` (form field, edit-fill, submit payload).
- **Docs:** `docs/DATA_MODEL.md`, `docs/SETUP.md`, `Specification/CONTINUATION_PROMPT.md`.
- **Schema/demo profile:** dev/test use drop-and-create — no migration. The demo profile runs Hibernate `update`, which never drops columns: an existing demo database keeps a stale nullable `keycloakUsername` column (and its unique constraint) that is simply no longer read or written. Harmless, but documented in design.md.
- **No Keycloak realm change**, no realm-export change, demo logins (`partner-demo` / `company-demo`) unchanged.
