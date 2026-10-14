## 1. Backend (Release A) — essential

- [ ] 1.1 Write failing tests first: subject-bound resolution, subject → A while username → B resolves A, unbound subject → 403 German message, fallback only for partners that never had a binding, revoked last binding + same token → 403, inactive bound partner → 403 without fallback, partner-number change keeps access (replace any test that expects renumbering → 403), concurrent duplicate adds → one success and one 409; binding API add/list/remove, blank/whitespace/too-long subject → 400, duplicate (same and other partner) → 409, unknown partner/login and login of another partner → 404 without change, re-adding a revoked subject reactivates, partner/anonymous → 403/401, partner DTOs and self-edit carry no subject.
- [ ] 1.2 Add `PartnerLogin` entity (unique `oidcSubject`, Partner FK, `createdAt`, `createdBy`, `revokedAt`, `revokedBy`), `PartnerLoginResource`/service under `/api/company/partners/{id}/logins` with constraint-violation → 409 mapping; switch `CurrentPartner` to subject resolution with the transitional fallback; make 1.1 pass.
- [ ] 1.3 Give `partner-demo` a fixed `id` in both realm exports (main and `partnerportal-test-realm.json`) and bind it in `DemoDataSeeder`; verify with real issued tokens in tests and dev, and document recreating an existing local Keycloak container.

## 2. Frontend — essential

- [ ] 2.1 Add a "Logins" row action and dialog in the company partner list (list, add, remove, German messages, escaped subject, help text "Keycloak-Benutzer-ID"); verify add/duplicate/remove, desktop 1280 px and mobile 390 px.

## 3. Release A completion — essential

- [ ] 3.1 Upgrade check: start Release A against a database created by the current `main` release with schema `update`; verify existing rows, NOT NULL/UNIQUE/FK constraints of `partner_login` and an authenticated bind plus partner login.
- [ ] 3.2 Update `docs/DATA_MODEL.md` and `docs/SETUP.md` (binding procedure on the VM); run focused tests, full `./mvnw test`, `./mvnw package`, `node --check`, `openspec validate bind-partner-login-to-oidc-subject --strict`, `git diff --check`, partnerportal-quality and an independent security/database review; fix findings.
- [ ] 3.3 After deploying Release A: bind the VM `partner-demo` user ID via the dialog without logging tokens or passwords, then confirm a real partner login and dashboard.

## 4. Release B — essential

- [ ] 4.1 Only after 3.3: remove the fallback, adapt tests (unbound subject with matching username → 403), full test suite, deploy and confirm the VM partner login again.
