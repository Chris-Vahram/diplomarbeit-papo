## Context

`CurrentPartner.resolve()` uppercases `jwt.getName()` and looks up `Partner.partnerNumber`; eight partner resources depend on it. Keycloak stores usernames lowercased and lets admins rename users, while the subject (`sub`, the Keycloak user ID) is stable and only issued inside a token whose issuer and signature Quarkus has already verified. The demo VM holds demo data only; its Keycloak `partner-demo` user has a server-generated ID.

## Goals / Non-Goals

**Goals:** Stable partner identity independent of username and partner number; explicit, auditable, company-controlled binding; no lockout of the existing demo login during rollout; a data shape that personal accounts can extend.

**Non-Goals:** Keycloak user provisioning or Admin-API access, first-login claiming, personal employee accounts, company-user bindings, tenancy, BMD number rules, changing existing partner numbers.

## Decisions

- **Separate table instead of `Partner.oidcSubject`.** `PartnerLogin` (`id`, `oidcSubject` unique not null, `partner` many-to-one not null, `createdAt`, `createdBy`, nullable `revokedAt`/`revokedBy`). Several logins per Partner are allowed, so personal accounts can add rows (and later name/role/status columns) without moving data. Replaces the single-column design in `decouple-partner-number-from-login`, written before personal accounts were confirmed.
- **Subject source.** Only `JsonWebToken.getSubject()` of the validated token. Never username, e-mail, partner number or a client-supplied value.
- **Binding operations (company role only).** `GET /api/company/partners/{id}/logins`, `POST …/logins` with `{ "oidcSubject": "…" }`, `DELETE …/logins/{loginId}` (revokes; the login must belong to `{id}`, otherwise 404 without change). Subject: trimmed, 1–255 characters, no whitespace; no UUID format check (issuer decides the format). A subject already used by another partner (active or revoked) → 409 "Dieses Login ist bereits mit einem Partner verknüpft."; an active binding of the same partner → 409 as well; a revoked binding of the same partner is reactivated. Unknown partner/login → 404. **Revocation is soft** (`revokedAt`/`revokedBy`), so the history stays visible and a partner that ever had a binding never qualifies for the fallback again. Rebinding = revoke + add; the unique constraint prevents cross-linking even under concurrent requests (constraint violation mapped to the same 409). `createdBy` stores the acting company user's subject.
- **Resolver order.** (1) active binding for token subject → its Partner; a revoked binding → 403 without fallback; (2) Release A only: subject never bound → old username → canonical partner number match, accepted only if that Partner has never had a binding (no active or revoked row); (3) otherwise 403 "Kein Partnerkonto mit diesem Login verknüpft." Inactive check and its German message stay unchanged after resolution. Fallback is plain code, not a config flag; Release B deletes it.
- **Dev/test identity.** Realm export sets a fixed `id` for `partner-demo`; `DemoDataSeeder` (dev/test only) creates the binding for `PARTNER-DEMO`. The test realm (`backend/src/test/resources/keycloak/partnerportal-test-realm.json`) gets the same fixed ID. An existing local Keycloak container keeps its old random ID; because the seeded binding exists, the fallback does not apply, so the local container must be recreated (`docker compose up -d --force-recreate keycloak`, no data volume) — documented in `docs/SETUP.md`.
- **UI.** Partner list row action "Logins" opens a dialog: list (subject, created), add field, remove button. Separate from the partner create/edit form. Subjects are rendered as escaped text. Help text explains that the value is the Keycloak user ID.
- **No logging** of tokens; subjects appear only in company responses, not in logs or partner-facing payloads.

## Risks / Trade-offs

- [Risk] Wrong subject links the wrong partner. Mitigation: company-only operation, unique constraint, list view for checking, removal possible, tests for collisions.
- [Risk] Fallback lets a renamed Keycloak user named like an unbound partner number log in as that partner. Mitigation: it is today's behaviour, limited to partners without bindings, and removed in Release B.
- [Risk] Two simultaneous adds of the same subject. Mitigation: database unique constraint; the losing request maps to 409.
- [Risk] Demo VM lockout after Release B. Mitigation: B only after the VM binding is verified by a real login.
- [Trade-off] Subject must be copied manually from the Keycloak admin console. Accepted until account administration exists.

## Migration Plan

1. **Release A** (this change, tasks 1–3): additive table via Hibernate `update`, binding API/UI, resolver with fallback. Deploy; behaviour on the VM is unchanged because the demo partner has no binding yet.
2. **Operator step:** read the `partner-demo` user ID in the VM Keycloak admin console (no tokens or passwords in logs), bind it through the "Logins" dialog, log in as partner and confirm the dashboard loads.
3. **Release B** (task 4): remove the fallback; unbound subjects get 403.
4. **Rollback:** B → A is safe: A's fallback never applies to partners that have or had a binding. A → the release before A is only allowed while it cannot change who resolves to which partner: no binding revoked, no Keycloak username renamed and no partner number changed since A. Otherwise fix forward. The table stays in place either way (Hibernate `update` never drops it).
5. **Upgrade check:** before deploying A, start it against a database created by the current release (schema `update`, not drop-and-create) and verify existing data, the new table's NOT NULL/UNIQUE/FK constraints and an authenticated binding plus login.

## Open Questions

None blocking. Company-user bindings and account administration belong to the personal-accounts change.
