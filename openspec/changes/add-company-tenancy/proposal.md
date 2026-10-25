## Why

PAPO serves exactly one reference company today: `CompanyProfile` is a singleton, every `company`-role user sees every partner, article, order and file, and partner numbers and category names are globally unique. Chris confirmed (25.09.2026) two manually onboarded test companies with strict data separation on one deployment as the next product step (`PRODUCT_COMPLETION_PLAN.md`, Arbeitsblock item 2). The OIDC-subject binding for partners (`bind-partner-login-to-oidc-subject`, archived) is the identity basis this change builds on.

## What Changes

- **Company = tenant.** The existing `CompanyProfile` row becomes the tenant root; its id is the company id. The singleton key is retired. No separate `Company` table.
- **Owned roots get `company_id`:** `Partner`, `Product`, `Category` (`article_categories`), `ProductImage`, plus `CompanyProfile` itself as the root. All other company data (customers, orders, items, commission, enablements, logins, trade proofs, shops, pages, themes, assets, mail outbox) is scoped through its partner, order or shop; no extra column.
- **Tenant context only from trusted identity.** New `CompanyLogin` binding (verified OIDC subject → one company) resolved by a new request-scoped `CurrentCompany`; partner users get their company through `PartnerLogin` → `Partner.company`. Client-supplied ids, headers or hosts never select a company. Public shop requests derive the company from the shop's partner.
- **Isolation everywhere:** every company endpoint (45) filters by the caller's company; every foreign id (path or payload) answers 404 as if missing; exports, image/proof downloads, imports, dashboards, unseen counter, mail outbox and shop editor/public reads stay within one company.
- **Uniqueness per company:** partner number, category name, import duplicate checks (and later the BMD article number). Global stays: OIDC subject, shop identifier (`/shop/<kennung>` has no company segment).
- **Mail:** order mails are signed with the order's company name; the company notice recipient becomes per-company (see design, open decision).
- **Migration in two releases:** Release A (expand) assigns all existing rows to "company 1" and stays compatible with the previous image; Release B (contract) enforces NOT NULL and per-company uniqueness and stays compatible with A. The gateway's automatic restore therefore never starts an incompatible image. Company 2 is provisioned only after B; from then on a pre-A image must never run again (backup restore instead).
- **Isolation details:** product images `private, no-store` plus frontend cache reset on logout; one shared lock for company and partner login bindings of a subject.
- **Provisioning:** a second test company and its first company login are created manually by the operator (runbook); dev/test seed two companies; the realm exports gain fixed-id users for company 2. No self-service signup.
- **Cross-tenant test suite** for every resource, including direct foreign ids and company-B partner logins.

## Non-Goals

Subdomains or host-based routing (undecided), personal accounts/admin roles/audit (next change), staging environment provisioning (human task), billing, self-service signup, global/shared categories, Hibernate discriminator multi-tenancy, platform-admin UI.

## Capabilities

### New Capabilities

- `company-tenancy`: company as tenant, trusted tenant resolution, company login binding, cross-tenant 404 rule, uniqueness scopes, data migration, manual provisioning, per-company mail signature/recipient.

### Modified Capabilities

- `auth-access-control`: role-based authentication now grants company views only for the caller's own company.
- `company-profile`: one profile per company instead of a single portal profile.
- `partner-management`: partner number unique per company; company manages only own partners.
- `product-management`, `article-categories`, `product-images`, `product-enablement`: company-owned catalogue, per-company category names, images and enablements restricted to one company.
- `order-management`, `commission`, `order-notifications`, `company-dashboard`, `data-export`, `data-import`: lists, totals, counts, exports and imports limited to the caller's company.
- `shop-public-catalog`: seller and categories derived from the shop partner's company.

## Impact

Backend: `CompanyProfile`, `Partner`, `Product`, `Category`, `ProductImage` mappings; new `CompanyLogin` entity and `security/CurrentCompany`; every company resource/service listed in design.md; `CurrentPartner` unchanged in contract; `DemoDataSeeder`; new `CompanyTenancyMigrationService` (startup, after the tax-identifier migration); `OrderEmailContent`/`OrderEmailService`; shop services; both realm exports; tests. Frontend: no new screens; browser check with both companies. Docs: `docs/DATA_MODEL.md`, `docs/DEPLOYMENT.md` (backup, migration, provisioning, rollback), `docs/SETUP.md`, `docs/CONVENTIONS.md` (access-control line).

Dependencies/conflicts: `send-order-emails` (task 4.2 open) — company recipient moves from env to company data; `add-unique-bmd-article-number` and `decouple-partner-number-from-login` — their uniqueness becomes per company; Tobias' shop changes (`add-shop-guest-checkout`, `add-shop-inquiries`, `add-shop-statistics`, `add-shop-customer-accounts`, `add-shop-legal-consent`, `add-article-tax-rate`) inherit the company via partner/shop and must not add company-global queries. "Tenant" in `add-shop-customer-accounts` means a Quarkus OIDC tenant (realm), unrelated to company tenancy.
