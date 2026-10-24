Prerequisite: Chris has answered the open decisions in design.md (at least 1–7). Tasks assume the recommendations; adjust specs/design first if a decision differs.

## 1. Test fixtures and identity — essential

- [ ] 1.1 Realm exports (main and `partnerportal-test-realm.json`): give `company-demo` a fixed `id`; add `company-zwei` (role `company`) and `partner-zwei` (role `partner`) with fixed ids. Verify: tokens for all three can be fetched in a test via `KeycloakTestTokens`.
- [ ] 1.2 Add `support/TenantTestData` (test helper) that returns company 2's seeded ids (profile, partner, article, category, image, order) for cross-tenant tests. Verify: compiles, used by 2.x tests.

## 2. Tenant model and resolution — essential

- [ ] 2.1 Write failing tests in `CompanyTenancyResourceTest`: bound company user → own profile; unbound company-role token → 403 German message; revoked binding → 403; header `X-Company-Id`/`Host` naming the other company ignored; partner-zwei resolves to company 2 data; binding a partner login whose subject is a company login → 409. Verify: tests fail for the expected reason.
- [ ] 2.2 Add `company_id` (`@ManyToOne`, column nullable in DDL) to `Partner`, `Product`, `Category`, `ProductImage`; remove `singletonKey` from `CompanyProfile`, add nullable `orderNoticeEmail`; add `CompanyLogin` entity and `security/CurrentCompany`; per-company unique constraints in entity annotations. Verify: dev schema (drop-and-create) shows the columns/constraints.
- [ ] 2.3 `DemoDataSeeder`: seed company 1 as today plus company 2 (dev/test only) with bindings to the fixed realm ids; NORMAL+flag seeds company 1 only; adapt `BusinessTaxIdentifierMigrationService.initializeCompanyProfile`. Verify: `DemoDataSeederTest` updated and green, 2.1 resolution tests pass.

## 3. Migration — essential

- [ ] 3.1 Write failing `CompanyTenancyMigrationTest`: unassigned rows + one profile → all assigned, NOT NULL and composite uniques present, old single-column uniques gone, `singleton_key` nullable; unassigned rows + zero/two profiles → German error, no change; second run → no change.
- [ ] 3.2 Implement `CompanyTenancyMigrationService` per design Migration Plan (lock, backfill, drop constraints found via `pg_constraint`, set NOT NULL, create unique indexes, verify), called from `DemoDataSeeder.onStart` after the tax migration. Verify: 3.1 green.
- [ ] 3.3 Upgrade check: dump a database created by current `main` (schema `update`, demo data), start this branch on it; verify every row in company 1, constraints, `partner-demo` login, a new order and the shop editor. Verify: written note of the commands and results in the PR description.

## 4. Company-side scoping — essential

Each task: first the cross-tenant tests in the named `<Capability>ResourceTest` (list omits company 1 rows for `company-zwei`, direct ids → 404 without change, payload references → rejected like unknown), then the fix through the central scoped finder.

- [ ] 4.1 Profile and partners: `CompanyProfileService` per company; `PartnerService.list/findOrThrow/duplicate` per company (covers customers, logins, trade proof, shop, enablement paths that call it). Tests: `CompanyProfileResourceTest`, `PartnerResourceTest`, `PartnerLoginTest`, `PartnerTradeProofResourceTest` (download 404). Verify: tests green.
- [ ] 4.2 Catalogue: `ProductService` list/find/lock and category/image reference checks; `CategoryService` list/lock/duplicate per company; `ProductImageService` list/find/upload, `GET /api/product-images/{id}` own company for company and partner. Tests: `ProductResourceTest`, `CategoryResourceTest`, `ProductImageResourceTest`. Verify: tests green.
- [ ] 4.3 Enablement and partner catalogue: `EnablementService` checks partner and product company; partner dashboard article count per company. Tests: `EnablementResourceTest`, `DashboardResourceTest`. Verify: tests green.
- [ ] 4.4 Orders, mails, counters: `OrderService.listAll/countUnseen/updateStatus/markSeen/createForPartnerId`, `OrderEmailService.listForOrder/retry` via scoped order. Tests: `OrderResourceTest`, `OrderEmailTest`. Verify: tests green.
- [ ] 4.5 Dashboard and exports: `DashboardService.company`, `CsvExportService` orders/commission. Tests: `CompanyDashboardResourceTest`, `CsvExportResourceTest` (company 2 file contains no company 1 row). Verify: tests green.
- [ ] 4.6 Import: created rows get caller's company; duplicate and category lookup per company. Tests: `PartnerImportResourceTest`, `ProductImportResourceTest` (same number/name in other company accepted). Verify: tests green.
- [ ] 4.7 Shops: `CompanyPartnerShopResource` via scoped partner, `PartnerShopService.overview` per company, shop block `categoryExists` per shop company, seller name from shop partner's company in editor preview and public API. Tests: `PartnerShopResourceTest`, `PartnerShopEditorResourceTest`, `PublicShopContentTest`. Verify: tests green.
- [ ] 4.8 Grep review: no remaining unscoped `listAll(`, `findById(`, `count(`, `CURRENT_KEY` or global lock in company code paths (shop bootstrap, mail sender and migrations excepted with comment). Verify: list of hits and justification in the PR.

## 5. Mail — essential

- [ ] 5.1 `OrderEmailContent` signs with the order's company; COMPANY recipient from `CompanyProfile.orderNoticeEmail`, SKIPPED `NO_RECIPIENT` when empty; remove `papo.mail.company-recipient`; profile page field "E-Mail für Bestellhinweise". Tests: `OrderEmailTest` (company 2 order signed and addressed for company 2). Verify: tests green; coordinate wording with `send-order-emails` 4.2.

## 6. Docs and runbook — essential

- [ ] 6.1 `deploy/tenancy/provision-company.sql` (psql variables, one transaction, aborts if subject already bound in `company_login` or `partner_login`; bind-only mode for an existing company). Verify: run against a local Docker database for company 1 bind and company 2 creation, then log in as both.
- [ ] 6.2 Update `docs/DEPLOYMENT.md` (backup, rollout order, binding `company-demo`, provisioning, rollback SQL and the "no code rollback after company 2" rule), `docs/DATA_MODEL.md`, `docs/SETUP.md` (realm recreate, two demo companies), `docs/CONVENTIONS.md` (company scoping via `CurrentCompany`; fix stale "username claim" line). Verify: `git diff --check`.

## 7. Final verification — essential

- [ ] 7.1 Full `./mvnw test` and `./mvnw package` green; all cross-tenant tests from 2.1 and 4.x present and green.
- [ ] 7.2 Browser check (desktop 1280 px and mobile 390 px): log in as `company-demo` and `company-zwei`, `partner-demo` and `partner-zwei`; each sees only its own partners, articles, orders, dashboard, exports and shop status.
- [ ] 7.3 `npx --yes @fission-ai/openspec@latest validate add-company-tenancy --strict`, `partnerportal-quality`, independent security and database review (cross-model review recommended: high-stakes change); fix findings.
- [ ] 7.4 With Chris' approval only: VM rollout per DEPLOYMENT.md (backup, deploy, bind `company-demo`, verify, then provision company 2), record results.
