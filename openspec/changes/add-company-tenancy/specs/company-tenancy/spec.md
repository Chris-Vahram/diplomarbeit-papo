## ADDED Requirements

### Requirement: Company is the tenant
Each company SHALL be represented by exactly one company profile, which is the tenant root. Partners, articles, article categories and product images SHALL each belong to exactly one company. All other company data SHALL belong to the company of its partner, order or shop.

#### Scenario: Partner belongs to one company
- **WHEN** a company user creates or imports a partner
- **THEN** the partner is stored for the caller's company and for no other

#### Scenario: Derived data follows its parent
- **WHEN** an order is created for a partner of company A
- **THEN** the order, its items, its commission record, its outbox mails and its end customer belong to company A

#### Scenario: Cross-company link rejected
- **WHEN** a write would link an article, category, image, enablement or order item of company A with a record of company B
- **THEN** the write is rejected and nothing is stored

### Requirement: Tenant context from trusted identity
The system SHALL derive the caller's company only from the verified OIDC subject: company users through an active company login binding, partner users through their partner binding. Client-supplied ids, headers, host names, query or body values SHALL NOT select or change the company.

#### Scenario: Company user resolves to own company
- **WHEN** a company-role token carries a subject with an active company binding to company A
- **THEN** every company endpoint operates on company A only

#### Scenario: Unbound company user
- **WHEN** a company-role token's subject has no active company binding
- **THEN** the system responds 403 with "Kein Firmenkonto mit diesem Login verknüpft."

#### Scenario: Conflicting header ignored
- **WHEN** a company user of company A sends a header or host naming company B
- **THEN** the response contains company A data only

#### Scenario: Partner user resolves through partner
- **WHEN** a partner user of a partner in company B calls a partner endpoint
- **THEN** all data is scoped to that partner and therefore to company B

### Requirement: Company login binding
A company login binding SHALL map one verified OIDC subject to exactly one company. A subject SHALL be bound at most once across company and partner bindings. Revocation SHALL be soft and SHALL deny access immediately. Company bindings SHALL be created only by the operator provisioning procedure in this change.

#### Scenario: Partner login for a company subject
- **WHEN** a company user binds a partner login whose subject already has a company binding
- **THEN** the request is rejected with 409 and existing bindings are unchanged

#### Scenario: Company login for a partner subject
- **WHEN** the operator provisions a company binding for a subject that already has a partner binding
- **THEN** the provisioning aborts without changes

#### Scenario: Revoked company login
- **WHEN** a company user whose binding was revoked calls a company endpoint
- **THEN** the system responds 403 with "Kein Firmenkonto mit diesem Login verknüpft."

### Requirement: Foreign records are indistinguishable from missing ones
Company and partner endpoints SHALL treat a record of another company exactly like a non-existing record: lists omit it, path ids return 404 with the resource's usual German not-found message, and payload references are rejected like unknown ids. The system SHALL NOT answer 403 for foreign records.

#### Scenario: Direct foreign id
- **WHEN** a company user of company B requests, updates, downloads or changes the status of a partner, article, category, image, order, mail or trade proof of company A by id
- **THEN** the response is 404 and company A data is unchanged

#### Scenario: Foreign id in payload
- **WHEN** a company user of company B saves an article with a category or image id of company A
- **THEN** the request is rejected like an unknown id and nothing is stored

#### Scenario: Partner of company B
- **WHEN** a partner user of company B requests a product image or references a category of company A
- **THEN** the response is 404 or a rejection like an unknown id

### Requirement: Uniqueness scopes
Partner numbers, article category names and import duplicate checks SHALL be unique per company. OIDC subjects and shop identifiers SHALL remain unique across the platform. Service checks SHALL use the same scope as the database constraint.

#### Scenario: Same partner number in two companies
- **WHEN** company A and company B each create a partner numbered `P-0001`
- **THEN** both are stored

#### Scenario: Duplicate within one company
- **WHEN** company A creates a second partner `p-0001` or a second category "Werkzeug"
- **THEN** the request is rejected as a duplicate with the existing German message

#### Scenario: Shop identifier stays platform-unique
- **WHEN** company B sets a shop identifier already used by a shop of company A
- **THEN** the request is rejected with "Diese Kennung ist bereits vergeben."

### Requirement: Existing data migrates to company one
On startup the system SHALL assign every company-owned row without company to the single existing company profile, then enforce NOT NULL and per-company unique constraints. The migration SHALL be idempotent and atomic and SHALL stop startup without changes if data cannot be assigned unambiguously.

#### Scenario: Existing demo database
- **WHEN** the new release starts on a database with one company profile and unassigned partners, articles, categories and images
- **THEN** all rows belong to that company, constraints are enforced and existing orders, logins and shops keep working

#### Scenario: Ambiguous data
- **WHEN** unassigned rows exist while no or more than one company profile exists
- **THEN** startup stops with a German migration error and no row is changed

#### Scenario: Repeated start
- **WHEN** the application restarts on a migrated database
- **THEN** the migration changes nothing

### Requirement: Manual company provisioning
A new company SHALL be onboarded only by the operator through the documented runbook (backup, Keycloak users, provisioning script, binding, verification). The system SHALL offer no self-service signup and no company-creation endpoint. Dev and test SHALL seed two companies with fixed login bindings.

#### Scenario: Second test company provisioned
- **WHEN** the operator runs the provisioning procedure for a second company
- **THEN** its company user sees an empty portal of its own and none of company 1's data

#### Scenario: Seeded dev/test companies
- **WHEN** dev or test starts with an empty database
- **THEN** two companies exist, each with a bound company login, and the second has its own partner, articles and category

### Requirement: Order mails use the order's company
Order mails SHALL be signed with the business name of the order's company. The company notice SHALL be sent to that company's configured notice address; without an address the notice SHALL be recorded as skipped.

#### Scenario: Mail for company B
- **WHEN** an order of a company-B partner queues mails
- **THEN** the mails are signed with company B's name and the company notice targets company B's address

#### Scenario: No notice address
- **WHEN** a company has no notice address
- **THEN** the company notice is stored as skipped and no mail is sent for it
