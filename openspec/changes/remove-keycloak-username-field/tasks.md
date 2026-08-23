## 1. Seed and login resolution (backend core)

- [x] 1.1 `DemoDataSeeder`: rename the demo partner's `partnerNumber` from `"P-1001"` to the canonical upper-case `"PARTNER-DEMO"` (all three lookups: `seedPartners`, `seedEnablements`, `seedOrders`) and drop the `keycloakUsername` parameter/assignment from `persistPartner` — the other two seed partners simply lose their `null` argument; keep/adjust the explanatory comments (P-1002 still demonstrates "no Keycloak user = cannot log in"). The realm user stays `partner-demo`; `resolve()` upper-cases it to match — this exercises the real case-folding path.
- [x] 1.2 `security/CurrentPartner.resolve()`: replace `Partner.find("keycloakUsername", username)` with an **exact** canonical-form lookup `Partner.find("partnerNumber", jwt.getName().toUpperCase(Locale.ROOT))` (import `java.util.Locale`); leave both German 403 messages byte-identical; update the class comment (no more "Keycloak user provisioning" / auth-access-control "Partner login linking" wording that names the old column).

## 2. Drop the field from entity, API, and service

- [x] 2.1 `Partner.java`: remove the `keycloakUsername` field and its comment
- [x] 2.2 `PartnerDto.java`: remove the `keycloakUsername` component and its use in `from(...)`
- [x] 2.3 `PartnerRequest.java`: remove the `keycloakUsername` component and its comment
- [x] 2.4 `PartnerService.java`: remove `assertKeycloakUsernameFree(...)`, both call sites (create/update), and the `keycloakUsername` mapping in `applyCompanyFields` (the 409 text "Keycloak-Benutzername ist bereits …" disappears with it)
- [x] 2.5 **Canonicalize partner number to upper-case (`Locale.ROOT`) on every write:** in `PartnerService` create + update (where the number is set / after trim, alongside `assertPartnerNumberFree`) and in the import path (`PartnerImportDefinition`/import mapping, which already trims). Keep the case-insensitive dedup check as a friendly pre-DB error. This is what makes the case-sensitive DB unique constraint actually prevent case-variant duplicates (review finding #3).
- [x] 2.6 Backend compiles: `./mvnw -q compile` in `backend/`

## 3. Frontend form

- [x] 3.1 `frontend/src/pages/company-partner-list-page.js`: remove the `form-field` with label "Keycloak-Benutzername" (line ~35), the edit-fill line `this.form.elements.keycloakUsername.value = …` (line ~153), and the `keycloakUsername` property in the submit payload (line ~219)
- [x] 3.2 On the partner **edit** form, add a short German hint on the Partnernummer field that changing it changes the partner's Login (the Keycloak-Username must be renamed to match) — review-flagged, since the number is now the login identity.
- [x] 3.3 Confirm no other frontend reference remains: `grep -rin keycloak frontend/src` shows only `lib/auth.js` / `lib/product-images.js` infrastructure comments and URLs

## 4. Backend tests

- [x] 4.1 `PartnerResourceTest`: replace seeded-number references `"P-1001"` with the canonical `"PARTNER-DEMO"` (duplicate-create test, `seededPartnerId()` lookup, `partnerNumber` assertion); remove `keycloakUsername` from create/rename request bodies and response assertions; delete the now-obsolete keycloakUsername-conflict (409) test; drop `keycloakUsername` from the PUT full-replace helper body and its comment
- [x] 4.2 `OrderResourceTest`: `"P-1001"` → `"PARTNER-DEMO"` (comment + `seededPartnerId` lookup); remove the `keycloakUsername` line from the status-update partner body (~line 350)
- [x] 4.3 `EnablementResourceTest`: `"P-1001"` → `"PARTNER-DEMO"` in the partner lookup
- [x] 4.4 `PartnerImportResourceTest`: seeded-duplicate CSV rows `P-1001` → `PARTNER-DEMO`; keep a mixed-case duplicate row (e.g. `partner-demo`) so the case-insensitive-dedup assertion genuinely tests case folding after canonicalization; adjust comments
- [x] 4.5 **New login-resolution test** (`PartnerResourceTest` or a small `CurrentPartner`-focused test): a partner-role token whose Keycloak username is lowercase (`partner-demo`) resolves to the seeded `PARTNER-DEMO` partner and reaches partner-scoped data — proves the upper-case canonical folding, not just an exact match. Plus: an unlinked partner-role login (no matching number) yields the unchanged German 403 "Kein Partnerkonto mit diesem Login verknüpft."
- [x] 4.6 **Canonicalization test:** creating a partner with a lower/mixed-case partner number stores it upper-cased, and a second create differing only by case is rejected with the German 409 "Partnernummer bereits vergeben." (guards the finding-#3 case-variant-duplicate hole).
- [x] 4.7 Full backend test run green: `./mvnw -q test` in `backend/` (Dev Services realm import unchanged — `tokenFor("partner-demo", "partner-demo")` now resolves via canonical partnerNumber)

## 5. Documentation sync

- [x] 5.1 `docs/DATA_MODEL.md`: remove `keycloakUsername` from the PARTNER block in the ER diagram; rewrite the Partner-detail paragraph — login linking now via `partnerNumber` == Keycloak-Username (case-insensitive), renaming a partner number relinks the login
- [x] 5.2 `docs/SETUP.md`: add the provisioning convention near the demo-login table — Keycloak-User werden manuell mit Username = Partnernummer angelegt; the dev/test seed partner for `partner-demo` carries exactly that partner number
- [x] 5.3 `Specification/CONTINUATION_PROMPT.md`: update the Partner entity mention (line ~233) — drop `keycloakUsername`, note partnerNumber-based login linking

## 6. Verify (against the running dev stack)

- [x] 6.1 Login as `partner-demo` / `partner-demo` lands on the partner dashboard with the seeded data (orders, enabled products) — proves partnerNumber linking end-to-end incl. Keycloak's lowercase username against the seeded value
- [x] 6.2 Unlinked login still yields the unchanged German 403: authenticate a partner-role user with no matching partner number (e.g. temporarily rename the seeded partner via API or use a REST call with a fabricated token through the test harness) and assert "Kein Partnerkonto mit diesem Login verknüpft."
- [x] 6.3 Company view: "Neuer Partner" and "Partner bearbeiten" forms show no "Keycloak-Benutzername" input; creating and editing a partner works; duplicate partner number still yields the German 409 "Partnernummer bereits vergeben."
- [x] 6.4 `grep -rn keycloakUsername backend/src frontend/src docs Specification` returns no hits outside `openspec/` history
