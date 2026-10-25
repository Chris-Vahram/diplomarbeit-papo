## Context

PAPO runs one Quarkus backend, one PostgreSQL database and one Keycloak realm (`partnerportal`, roles `company`/`partner`) on a single Hetzner VM; every merge into `main` deploys there. Schema management in prod is Hibernate `update` (no Flyway). Earlier schema tightening used an idempotent startup service (`BusinessTaxIdentifierMigrationService`: lock, backfill, verify, `ALTER … SET NOT NULL`), called from `DemoDataSeeder.onStart`.

Today the backend is single-company by construction:

- `CompanyProfile` is a singleton (`singletonKey = 'CURRENT'`, unique, NOT NULL), read by the profile page, order mails (`OrderEmailContent`) and the shop footer ("Verkauf durch …").
- Company endpoints use `listAll()`/`findById()` without any owner filter; `@RolesAllowed("company")` is the only check.
- Partner endpoints are already scoped through `CurrentPartner.resolve()` (verified `sub` → `PartnerLogin` → `Partner`).
- `Partner.partnerNumber` and `Category.normalizedName` are globally unique; `PartnerShop.kennung` is platform-unique by decision (`ENDKUNDENSHOP_DECISIONS.md`).
- The company order-notice recipient is a deployment env value (`PAPO_MAIL_COMPANY_RECIPIENT`).
- Company users have no binding at all; any `company`-role token sees everything.

The VM holds demo data only (one profile "Huemer GmbH", three partners, eight articles, demo orders); demo logins with known passwords are active. Mail is off on the VM.

## Goals / Non-Goals

**Goals:** two (later more) companies on one deployment with strict separation of all data and files; tenant derived only from verified identity; existing data becomes company 1 without loss; manual onboarding of company 2; an automated cross-tenant test per resource; safe migration and rollback on the single VM.

**Non-Goals:** subdomains/host routing, personal accounts, admin/employee roles, audit log, staging provisioning, billing, self-service signup, global/shared categories, platform-admin UI, Keycloak Organizations, Hibernate discriminator multi-tenancy, OIDC multi-tenancy (separate realms).

## Decisions

### D1 — `CompanyProfile` is the tenant root (no new `Company` table)

The company already exists as one row with name, tax and register identity. Its id becomes the company id; owned tables reference `companyprofile.id` through a column named `company_id`. A separate `Company` table would duplicate the name and need a 1:1 join for nothing. `singletonKey` is retired: the entity drops the field, the migration drops the column's NOT NULL and unique constraint (Hibernate `update` never drops columns; without this a second profile insert fails). A slug for later subdomains is deliberately not added (YAGNI; subdomains undecided). Alternative kept open: rename to `Company` when personal accounts or billing need more company-level fields.

### D2 — `company_id` only on roots; everything else is scoped through its parent

Roots that company users list or reference directly get `company_id` (FK, NOT NULL after migration): `Partner`, `Product`, `Category`, `ProductImage`, and `CompanyProfile` is the root itself. Derived data is reached only through a scoped parent (`Kunde.ownerPartner`, `Order.partner`, `OrderItem.order`, `CommissionRecord.order/partner`, `ProductEnablement.partner`, `PartnerLogin.partner`, `PartnerTradeProof.partner`, `PartnerShop.partner`, `ShopPage/ShopTheme/ShopAsset.shop`, `OrderEmail.order`). Five columns instead of eighteen, no denormalization drift (e.g. an order's company cannot disagree with its partner's). Consistency rule: a link between two roots must stay inside one company (product ↔ category, product ↔ image, enablement partner ↔ product, order item product ↔ order partner); services check it, tests prove it.

### D3 — Explicit service-level scoping, not Hibernate discriminator tenancy

The codebase already scopes partner data explicitly via `CurrentPartner`. Company scoping follows the same pattern: the central finders that every resource already funnels through (`PartnerService.findOrThrow`, `ProductService.findOrThrow/lockProduct/findForReadOrThrow`, `ProductImageService.findOrThrow`, `CategoryService` lookups, `OrderService` order lookups, `EnablementService.findPartnerOrThrow/findProductOrThrow`, `PartnerShopService.findForPartnerId`, `OrderEmailService.listForOrder/retry`) take the company and return 404 for foreign rows; list queries add `where … company = ?`. Hibernate's `@TenantId` discriminator would filter automatically but needs the column on all tables, a tenant for background jobs (mail sender), startup migrations and the public shop before its company is known, and does not filter native queries (two exist). Kept as a later hardening option.

### D4 — Tenant resolution from verified identity only

- **Company users:** new entity `CompanyLogin` (`company_login`: `id`, `oidcSubject` unique NOT NULL, `company` FK NOT NULL, `createdAt`, `createdBy`, `revokedAt`, `revokedBy`) — same shape as `PartnerLogin`, so personal accounts can extend both. New `@RequestScoped security/CurrentCompany.resolve()` reads only `JsonWebToken.getSubject()`; no active binding → 403 "Kein Firmenkonto mit diesem Login verknüpft."; revoked → same 403. No username/e-mail/claim fallback. Chosen over a Keycloak user attribute + protocol mapper or Keycloak Organizations because realm changes are not reapplied on deploy (DEPLOYMENT.md), it needs no Keycloak configuration on the VM, and it is testable like `PartnerLogin` (see decision 2).
- **Partner users:** `CurrentPartner.resolve()` unchanged; company = `partner.company`. A partner endpoint never needs `CurrentCompany`.
- **One subject, one organization:** a subject may be bound either in `company_login` or in `partner_login`, never both; binding attempts across tables are rejected (409). A token with both roles but only a partner binding gets 403 on company endpoints.
- **Ignored inputs:** `Host`, `X-Forwarded-Host`, any custom header (e.g. `X-Company-Id`), query parameters and body fields never influence the company. Tests send a conflicting header and expect own-company data.
- **Public shop:** `kennung` → `PartnerShop` → `partner.company`; seller name and categories come from that company.
- **Background jobs:** `OrderEmailSender` processes rows by id across companies (no request context); content and recipients are taken from `email.order.partner.company`.

### D5 — Uniqueness scopes

| Key | Today | Target | Reason |
|---|---|---|---|
| `Partner.partnerNumber` | global unique | unique `(company_id, partnernumber)` | Roadmap: each company may have `P-0001`; imported numbers come from each company's own system. |
| `Category.normalizedName` | global unique | unique `(company_id, normalized_name)` | Each company owns its catalogue; global categories are a later option. |
| Product name (import dedupe) | global, app-level | per company, app-level | Same article names in two companies are normal. |
| BMD article number (future) | — | per company | Dependency for `add-unique-bmd-article-number`. |
| `PartnerLogin.oidcSubject`, `CompanyLogin.oidcSubject` | global unique | global unique | A subject is one person/organization in the shared realm. |
| `PartnerShop.kennung` | platform unique | platform unique | The public path `/shop/<kennung>` carries no company; a 409 on a taken identifier reveals only that a (possibly public) shop name exists. |
| `ShopPage` segment/slot, `ProductEnablement (partner, product)`, `PartnerTradeProof.partner`, `OrderEmail (order, type, role)` | per parent | unchanged | Parent is already company-scoped. |

Service-level duplicate checks use the same scope as the database constraint so users get the German message instead of a constraint error.

### D6 — Mail per company

`OrderEmailContent` signs with `order.partner.company.businessName` (no singleton lookup). The company notice recipient becomes a nullable company field (`CompanyProfile.orderNoticeEmail`, edited on the existing profile page); empty → the COMPANY row is created as SKIPPED with reason `NO_RECIPIENT`. The migration copies a non-empty `papo.mail.company-recipient` into company 1 once (mail is enabled on the VM since `send-order-emails` 4.2); afterwards `PAPO_MAIL_COMPANY_RECIPIENT` is removed. `papo.mail.redirect-to`, sender and SMTP stay deployment-wide. See decision 5.

### D7 — Provisioning is a manual operator task

No self-service, no admin endpoint. The operator (Chris) follows a runbook in `docs/DEPLOYMENT.md` after a database backup: (1) create the Keycloak users for the new company (company user, one partner user) in the VM admin console and copy their IDs; (2) run a checked-in, parameterized SQL script (`deploy/tenancy/provision-company.sql`, psql variables, single transaction) that inserts the `companyprofile` row with canonical tax/register values and the `company_login` binding; (3) log in as the new company user, create the partner in the UI, bind the partner login through the existing "Logins" dialog; (4) verify that each company sees only its own data. The SQL script refuses to run if the subject is already bound. Dev/test: `DemoDataSeeder` seeds company 1 (existing data) and a small company 2 (profile, one partner, two articles, one category, enablements) with bindings to fixed realm user IDs.

### D8 — Error semantics

Foreign or unknown ids → 404 with the existing German not-found message of that resource; lists simply omit foreign rows; a write that references a foreign id in its payload (category, image URL, product, partner) → 404 (path) or 400 with the same message as for an unknown id (payload), never 403, so existence in another company is not revealed. 403 stays reserved for role/binding failures.

## Inventory

Legend: **FK** = gets `company_id`; **via X** = scoped through parent X; "Change" lists the code that must filter or check.

### Entities / tables

| Entity (table) | Scope | Change |
|---|---|---|
| `CompanyProfile` (`companyprofile`) | tenant root | drop singleton lookup (`CompanyProfileService.findCurrent`, `OrderEmailContent`, shop resources, seeder, tax migration); add `orderNoticeEmail` |
| `CompanyLogin` (`company_login`) — new | via company | new binding, global unique subject |
| `Partner` (`partner`) | **FK** | `PartnerService.list/findOrThrow/duplicate check`, `PartnerImportDefinition` duplicate, unique `(company_id, partnernumber)` |
| `PartnerLogin` (`partner_login`) | via partner | company endpoints go through scoped partner; reject subject bound as company login |
| `PartnerTradeProof` (`partner_trade_proof`) | via partner | company endpoints via scoped `findOrThrow`; `PartnerService.list` proof query already by partner ids |
| `Product` (`product`) | **FK** | `ProductService.list/find/lock`, `EnablementService` list/find, `OrderService` item lookup, import dedupe, dashboard `Product.count()` |
| `Category` (`article_categories`) | **FK** | `CategoryService` list/find/lock-all/duplicate, import category lookup, shop block `categoryExists`, unique `(company_id, normalized_name)` |
| `ProductImage` (`productimage`) | **FK** | `ProductImageService.list/findOrThrow`, `ProductService.applyImage`, `GET /api/product-images/{id}` for partner (own company only) |
| `ProductEnablement` (`product_enablement`) | via partner (+ product same company) | `EnablementService.enable/disable/list` check both sides; `PartnerShopService.overview` query |
| `Kunde` (`kunde`) | via partner | none for partner API; company `GET /partners/{id}/customers` via scoped partner |
| `Order` (`orders`) | via partner | `OrderService.listAll/countUnseen/updateStatus/markSeen/createForPartnerId`, `CsvExportService`, `DashboardService.company` |
| `OrderItem` (`order_item`) | via order | product must belong to order partner's company (enablement check covers it once enablements are consistent) |
| `CommissionRecord` (`commission_record`) | via order | `CsvExportService.commission` (`listAll`) |
| `OrderEmail` (`order_email`) | via order | `OrderEmailService.listForOrder/retry` via scoped order; content/recipient per company |
| `PartnerShop` (`partner_shop`) | via partner | `CompanyPartnerShopResource` via scoped partner; `overview()` filter; `kennung` stays global |
| `ShopPage`, `ShopTheme`, `ShopAsset` | via shop | partner editor already partner-scoped; block category reference must be own company |

### Endpoint groups (85 endpoints: 45 company, 31 partner, 9 public)

| Group | Endpoints | Scoping change |
|---|---|---|
| `/api/company/profile` | GET, PUT | own profile via `CurrentCompany` |
| `/api/company/partners` (+ `/{id}`, `/{id}/customers`) | 5 | list filter, scoped `findOrThrow`, per-company number check |
| `/api/company/partners/{id}/logins` | 3 | scoped partner; cross-table subject check |
| `/api/company/partners/{id}/trade-proof` (+ download, review) | 3 | scoped partner; download 404 for foreign |
| `/api/company/partners/{id}/products` (enablement) | 3 | partner and product both own company |
| `/api/company/partners/{id}/shop` (+ kennung, release, takedown) | 4 | scoped partner |
| `/api/company/shops` | 1 | filter by company |
| `/api/company/products` | 4 | list filter, scoped find/lock, category/image references own company |
| `/api/company/categories` | 3 | list filter, per-company duplicate, lock only own rows |
| `/api/product-images` (list, upload, `/{id}`) | 3 | list/upload own company; `/{id}` company → own, partner → own company |
| `/api/company/import/{partners,products}` (preview, commit) | 4 | rows created in caller's company; duplicates/categories per company |
| `/api/company/orders` (list, unseen-count, export, create, status, seen) | 6 | filter by `o.partner.company`; scoped id lookups; create uses scoped partner |
| `/api/company/orders/{id}/emails` (+ retry) | 2 | scoped order |
| `/api/company/commissions/export` | 1 | filter by company |
| `/api/company/dashboard` | 1 | totals/settlement over own partners only |
| `/api/partner/*` (profile, products, customers, orders, dashboard, trade-proof, shop, pages, assets) | 31 | already partner-scoped; fix dashboard `Product.count()` (company articles) and shop block category check |
| `/api/shop/{kennung}/*` (public) | 9 | company derived from shop partner; seller name per company |

### Other touch points

- `DemoDataSeeder` (company 1 + company 2 in dev/test; NORMAL+flag seeds company 1 only), `BusinessTaxIdentifierMigrationService.initializeCompanyProfile` (no singleton key), `ShopPageBootstrap` (unchanged, cross-company by design).
- Realm exports: `company-demo` gets a fixed `id`; new users `company-zwei` (company role) and `partner-zwei` (partner role) with fixed ids in main and test realm.
- Frontend: no structural change; company name is not displayed in the company shell today (optional, not required).

### Riskiest spots

1. `CsvExportService` and `OrderService.listAll/countUnseen`, `DashboardService.company` use `listAll()`/global counts — a miss leaks all orders of the other company in one download.
2. `GET /api/product-images/{id}` is readable by every partner for any id (accepted risk 09.07.2026) — becomes a cross-company leak; also `ProductService.applyImage` accepts any image id.
3. Cross-root references in payloads: product → category/image, enablement partner ↔ product, shop block → `Category.findById` — a foreign id would silently link data across companies.
4. Migration DDL on prod: Hibernate-named unique constraints (`partnernumber`, `normalized_name`, `singletonkey`) in Release B must be found by column via `pg_constraint` and dropped; a wrong drop or missed backfill blocks startup on the only VM.
5. `CategoryService.lockAll` (`find("order by id").withLock(...)`) and similar global locks — must be limited to own rows, otherwise company A's edit blocks/reads company B's catalogue.

## Additional isolation details (Codex review 25.10.2026)

- **Image caching.** `GET /api/product-images/{id}` answers `Cache-Control: private, no-store` instead of `public, max-age=31536000, immutable`; browser and proxy caches ignore the `Authorization` header, so a cached image would bypass the company check after a login switch. The frontend blob cache (`frontend/src/lib/product-images.js`) is cleared and its object URLs revoked on logout and identity change (`auth.js`). Article imports accept an internal image reference (`/api/product-images/{id}`) only if the image belongs to the caller's company (`ProductImportDefinition`).
- **Subject exclusivity.** Separate unique constraints on `company_login` and `partner_login` cannot stop one subject being bound on both sides concurrently. Both binding paths (partner binding in `PartnerLoginService` and the company provisioning script) take the same transactional lock `pg_advisory_xact_lock(hashtext('login_subject:' || subject))` before checking both tables and inserting; the existing `partner_login:` key is replaced by this shared key.

## Migration Plan

**Physical names.** The project has no naming strategy: unquoted camelCase maps to lowercase (`companyprofile`, `productimage`, `partner.partnernumber`, `companyprofile.singletonkey`; `article_categories.normalized_name` is explicit). All migration SQL uses these existing names; new columns are explicitly named (`company_id`). No entity is renamed (a new `@Table` name would make Hibernate `update` create an empty new table).

**Why two releases.** The deploy gateway restores the previous image automatically when the new one fails readiness (`deploy/ssh/papo-deploy-gateway` `restore_backend`) and never reverts SQL. Every release must therefore run on the schema the *next* release leaves behind. As with the OIDC binding (Release A/B), the change ships as expand → contract:

**Release A (expand, compatible with the current `main` image):**
1. `CompanyTenancyMigrationService.expand()`, called from `DemoDataSeeder.onStart` after the tax-identifier migration, one transaction: `LOCK TABLE companyprofile, partner, product, article_categories, productimage IN ACCESS EXCLUSIVE MODE`.
2. No profile but owned rows → abort startup ("Mandanten-Übernahme abgebrochen: Daten ohne Firmenprofil."). Unassigned owned rows (`company_id is null`) → allowed only with exactly one profile → assign to it; otherwise abort ("… mehrere Firmen, nicht zuordenbare Daten.").
3. `company_id` stays nullable at database level; the old global unique constraints and `singletonkey` (value `CURRENT` on company 1) stay untouched. Entities declare only the composite uniques `(company_id, partnernumber)` / `(company_id, normalized_name)` and keep `singletonKey` as a nullable, unused field, so Hibernate `update` neither re-adds nor drops the old constraints.
4. All A code sets `company_id` on every insert and scopes every read; company users resolve via `CompanyLogin`. Company 2 is **not** provisioned in A (old global uniques and NOT NULL `singletonkey` would block it anyway).
- Automatic rollback A → old `main` image: works unchanged (nullable `company_id` and `company_login` are ignored, singleton row intact). Rows the old image writes meanwhile have no company; the next A start backfills them again (idempotent).

**Release B (contract, compatible with the A image), only after A is live and `company-demo` is bound:**
1. `CompanyTenancyMigrationService.contract()`: same lock; backfill as in A if needed; verify no `company_id is null`.
2. `ALTER COLUMN company_id SET NOT NULL` on the four tables; drop the single-column unique constraints on `partner.partnernumber`, `article_categories.normalized_name`, `companyprofile.singletonkey` (looked up by table+column in `pg_constraint`, skipped if absent); `ALTER COLUMN singletonkey DROP NOT NULL`. Entity drops `singletonKey`.
3. Verify (null counts 0, composite uniques present, old ones gone); any failure rolls back the transaction and stops startup.
- Automatic rollback B → A image: works (A always writes `company_id`, does not use the singleton or the old uniques).
- Company 2 is provisioned only after B is verified (D7).

Both steps are idempotent; on a fresh schema (dev/test drop-and-create) they are no-ops.

**Rollout:**

1. Upgrade check locally for each release: start A on a dump created by the current `main` release (schema `update`, demo data), then B on the resulting database; verify rows in company 1, constraints, logins, both demo flows. Also force a readiness failure after a committed A/B migration in `deploy/ssh/test-papo-deploy-gateway.sh` and verify that writes still work on the restored previous image.
2. On the VM before each merge: `deploy/backup/backup.sh` (dump of `partnerportal` and `keycloak`), note the current image SHA.
3. Merge A → deploy. Company users get 403 until bound (no fallback, D4). Immediately bind `company-demo` (runbook bind-only mode), log in, verify data. The gateway readiness check only proves an unauthenticated 401, so this manual authenticated check is mandatory.
4. Merge B → deploy → verify again → provision company 2.

**Rollback:**

- After A: automatic or manual start of the previous SHA, no SQL needed.
- After B, before company 2: start the A SHA (automatic restore does this), no SQL needed.
- After company 2 exists: never start a pre-A image (it would show both companies' data to every company user); restore the pre-B backup or fix forward. The A image stays safe.

## Risks / Trade-offs

- [Risk] Single VM deploys on merge; a failing migration stops the backend. Mitigation: upgrade check against a real dump, backup before merge, expand/contract keeps every automatic restore schema-compatible, migration aborts atomically.
- [Risk] A missed query leaks data. Mitigation: central scoped finders, cross-tenant test per endpoint group, grep checklist for `listAll(`, `findById(`, `count(` in company code during review, independent security and database review.
- [Risk] Keycloak realm changes do not reapply on deploy. Mitigation: VM users are created manually in the admin console; realm JSON only affects dev/test and fresh installs; runbook step.
- [Risk] Demo logins with known passwords exist for both companies. Mitigation: VM contains demo data only; company-2 users get non-demo passwords stored only in Vaultwarden; before real data all demo logins are disabled (existing DEPLOYMENT.md rule).
- [Risk] Company users locked out between deploy and binding. Accepted (demo VM, minutes); avoids a fallback that could map the wrong user once two companies exist.
- [Trade-off] No database-enforced tenant filter (D3); relies on service code plus tests.
- [Trade-off] Platform-unique shop identifier reveals taken names across companies; accepted, names are public once online.

## Decisions (Chris, 25.10.2026)

All nine planning decisions were taken as recommended:

1. **Tenant root:** reuse `CompanyProfile` as the company (D1); rename only if later needed.
2. **Company claim source:** server-side `CompanyLogin` binding (D4); no Keycloak attribute or Organizations.
3. **Scoping technique:** explicit service scoping through central scoped finders (D3); Hibernate `@TenantId` is later hardening.
4. **Uniqueness:** partner number and category name per company; shop identifier and OIDC subject global (D5).
5. **Company notice recipient:** per-company field `CompanyProfile.orderNoticeEmail` (D6). Because mail is now enabled on the VM (`send-order-emails` 4.2), the migration copies a non-empty `papo.mail.company-recipient` into company 1 once, then the setting is removed.
6. **Provisioning:** operator Chris via SQL runbook with backup (D7); admin endpoint only with personal accounts.
7. **Login fallback:** none; `company-demo` gets 403 until bound after deploy (D4).
8. **Company 2 test data:** fictitious "Testfirma Zwei GmbH" with synthetic identifiers.
9. **Staging:** ask Herr Bachl; until then local Docker plus the demo VM with demo data only.

Business rules that need the client company (global categories, partner-number formats, real company onboarding, legal texts per company) remain open and are not decided here.
