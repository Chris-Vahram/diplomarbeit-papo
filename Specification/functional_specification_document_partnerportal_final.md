# Functional Specification Document (v2)

> **Version note:** This v2 reflects the client meetings (17.06.2026 + 24.06.2026), which override the earlier FSD where they conflict (newer = authoritative). The MVP is an **internal B2B partner-ordering portal**. The external end-customer shop from v1 is deferred (see `phase2_backlog.md`, B-01). The full original vision (v1 scope incl. external shop) is preserved in `functional_specification_document_full_vision.md`; there is no separate v1 backup file.

## 1. Project Overview

### Project Title
**Partner Portal / Coordination Software for Partner Sales Models**

Working title: **Web-Based Partner Portal for Managing Sales Partners, Products, and Partner-Related Ordering Processes**

### Short Description
This project is a web-based partner portal for partner-driven sales models. The system follows the chain **Company -> Partner -> End Customer**. The company centrally manages partners and products (imported and maintained), and selectively enables products for specific partners. The **core MVP process** is partner-driven ordering: a partner logs in, selects or creates an end customer, and places an order (articles + quantities) on that customer's behalf. Each order is automatically assigned to the partner, forms the basis for a per-partner commission, and the company handles fulfillment (Konfektionierung and shipping) to the end customer.

### Background / Current Situation
The reference client currently manages roughly **200 partners and 800 articles**. Partner orders today arrive by e-mail (~10–15/day) and phone (~3–4/day), processed manually. Central article management and partner management are the highest-priority pain points, followed by a reliable partner-driven ordering process with automatic order confirmation and shipping confirmation.

Existing solutions (PRM systems, partner portals, affiliate platforms) usually cover only parts of the required process and are often too broad, too expensive, or too complex. In particular, the combination of **central article management, partner management, product enablement, partner-driven ordering, and per-partner commission assignment** is rarely available as one clear, lean process.

### Problem Statement
The main problem: partner ordering is manual (e-mail/phone), article and partner data are not centrally coordinated, product visibility per partner is not controlled, orders are not systematically assigned to partners, and commission is not transparently tied to orders.

Sub-problems:
- The company needs a central way to import and manage partners and products.
- Products must be enabled selectively per partner (not visible to all by default).
- Partners need a reliable internal way to place orders for their end customers.
- Orders must be automatically assigned to the correct partner.
- Each partner has an individual commission level; commission must be traceable per order.
- The company handles fulfillment; the partner and company both need order visibility for their scope.

### Project Goals
Develop a web-based system that bundles central partner-related ordering processes in one platform. The system shall:
- Import and manage partners centrally (incl. per-partner commission level)
- Import and manage products/articles centrally
- Enable products selectively per partner
- Provide partners an internal area: master data, enabled products, own orders, dashboard
- Let partners place orders for end customers (select/create customer, articles + quantities)
- Automatically assign each order to the responsible partner
- Provide automatic on-screen order confirmation and a shipping confirmation (simulated e-mail in the prototype)
- Calculate a per-partner commission per order and store it as its own record
- Give the company full order visibility and prototype fulfillment status control
- Improve transparency and reduce manual administrative effort

The project is successful when the full core chain **Company -> Partner -> End Customer** can be demonstrated consistently via partner-driven ordering in a working prototype.

---

## 2. Scope

### In Scope (MVP)
- Central management of partners by the company, incl. per-partner commission level
- Central management of products/articles by the company
- **Excel/CSV import** of partners and articles (with column mapping, duplicate detection, and a validation report)
- Product enablement for individual partners
- Restricted product visibility (partner sees only enabled products)
- Internal partner area: partner master data, enabled products, own orders, dashboard
- **Partner-driven ordering**: partner selects or creates an end customer and places an order (articles + quantities)
- End-customer (Kunde) management as a reusable entity (select existing or create new)
- Multiple articles per order (article + quantity per line)
- Automatic assignment of each order to the responsible partner
- Order visibility: company sees all orders; partner sees only its own
- Order status handling: submitted -> reviewed -> fulfilled (prototype fulfillment control)
- On-screen order confirmation with summary + simulated order/shipping confirmation e-mail (rendered in-app)
- Per-partner commission calculation on total order value, stored as its own record
- Role-based access (Keycloak): company and partner roles
- A usable demo prototype that shows the complete internal core process

### Out of Scope (MVP) / Deferred
Tracked in `phase2_backlog.md`:
- **External end-customer partner shop** and direct end-customer ordering (B-01)
- **Shop design customization** (logo, colors, shop name, layout variants) (B-02)
- **Automatic commission level-up** by sales threshold (B-03) — MVP sets level manually
- **Real e-mail sending** via SMTP (B-04) — MVP simulates the e-mail in-app
- **Shop inquiry auto-forwarding** (B-05)
- **BMD interface** (B-06) — permanently excluded; data enters via Excel import
- **Commission settlement / payout workflow** (B-07)

### Permanently Out of Scope
- Full ERP, general-purpose CRM, full enterprise PRM
- Real payment gateway / checkout with real payment
- Full inventory and detailed shipping-workflow management
- Financial bookkeeping / accounting
- General learning / enablement portal
- Full recreation of large enterprise platforms (Salesforce, Magentrix, etc.)

### Explicit Prototype Boundary Decisions
- No real payment integration; orders are prototype orders.
- Order status limited to **submitted / reviewed / fulfilled**.
- Commission = fixed percentage per partner (per-partner level), on total order value; no settlement/payout.
- Confirmation e-mails are **simulated** (rendered in-app), not really sent.
- Excel import is the data-entry path for the ~200 partners / ~800 articles; no BMD interface.

---

## 3. Users and Stakeholders

### Primary Users

#### Company / Operator / Manufacturer
Central system owner and administrative authority. Can:
- Import and manage partners (incl. commission level)
- Import and manage products/articles
- Enable products for selected partners
- View all orders and control prototype order status (submitted/reviewed/fulfilled)
- Review commission records

#### Partner / Sales Partner
Uses the internal partner area to:
- View and edit limited own master data (e.g. contact data)
- View enabled products and own commission level
- Select or create an end customer and place orders on the customer's behalf
- View own orders and their status
- Access a dashboard (commission, revenue, orders)

### Secondary Users

#### End Customer (Endkunde)
Does not use the portal in the MVP. The partner places the order on the end customer's behalf. The end customer is the recipient of the goods and receives an (simulated) order/shipping confirmation. Direct end-customer self-ordering via an external shop is deferred (B-01).

### Stakeholders
- Company management / project owner
- Sales partners
- End customers
- Diploma project team (**Chris, Tobias**; Julian left the project)
- Supervising teacher / reviewers

---

## 4. Use Cases / User Scenarios

### Use Case 1: Company Imports and Manages Partners and Products
The company logs in and imports partners and articles from Excel/CSV (column mapping, duplicate detection, validation report). It can then create/edit partner records (incl. commission level) and product records (name, price, description, availability). The company decides which products are enabled for which partners.

### Use Case 2: Partner Places an Order for an End Customer (core process)
The partner logs in (Keycloak) and sees profile, dashboard, enabled products, and own orders. To place an order, the partner selects an existing end customer or creates a new one (Nachname + address mandatory), adds one or more enabled articles with quantities, and submits. The order is automatically assigned to the partner. The partner receives an on-screen confirmation with a summary, and a simulated order confirmation e-mail is shown.

### Use Case 3: Company Reviews and Fulfills Orders
The company reviews incoming orders from all partners, sees which partner generated each order, sets status reviewed, performs Konfektionierung (Bestellung -> Lager -> einpacken -> verschicken), and sets status fulfilled. On shipping, a simulated shipping confirmation is shown.

### Use Case 4: Partner Reviews Own Orders and Commission
The partner views only its own orders and their status, plus the commission derived from each order and dashboard totals (revenue, commission, order count).

### Use Case 5: Unavailable / Non-Enabled Product Handling
If a product becomes unavailable, it cannot be added to a new order even if previously enabled. A partner cannot order or see products not enabled for that partner.

---

## 5. Functional Requirements

### FR-01 Partner Management
The system shall allow the company to create, view, edit, and manage partner records centrally, including per-partner commission level and active/inactive status.

### FR-02 Product Management
The system shall allow the company to create, view, edit, and manage products/articles centrally.

### FR-03 Product Data Storage
The system shall store product data including at least product name, price, description, availability status, and optionally category and image reference.

### FR-04 Data Import
The system shall allow the company to import partners and articles from Excel/CSV, with column mapping, duplicate detection, and a validation report of accepted/rejected rows.

### FR-05 Product Enablement
The system shall allow the company to enable specific products for specific partners.

### FR-06 Restricted Product Visibility
The system shall ensure that each partner can view and order only the products enabled for that partner.

### FR-07 Internal Partner Area
The system shall provide each partner an internal area containing own master data, enabled products, own orders, and a dashboard.

### FR-08 Partner Master Data Editing
The system shall allow a partner to edit limited own master data (e.g. contact data) but not commission level, role, or central system data.

### FR-09 Partner Dashboard
The system shall provide each partner a dashboard showing at least commission total, revenue, order count, enabled-product count, and latest orders. Commission total, revenue, and order count are computed only over the partner's own orders with status **fulfilled** (decided 2026-07-03, see §13); the latest-orders list shows recent orders of any status. The dashboard is the partner's landing screen and is kept compact.

### FR-10 End-Customer (Kunde) Management
The system shall allow a partner to select an existing end customer or create a new one. End-customer records are reusable across orders. Mandatory fields: last name (Nachname) and address; other contact fields optional.

### FR-11 Partner-Driven Order Creation
The system shall allow a partner to create an order for a selected end customer, containing one or more enabled articles each with a quantity.

### FR-12 Order-to-Partner Assignment
The system shall automatically assign each order to the partner who created it.

### FR-13 Order Data
The system shall store per order at least: order identifier, order date, assigned partner, end customer, ordered articles with quantities, order status, and derived order value.

### FR-14 Order Visibility
The system shall show all orders to the company and only own orders to each partner.

### FR-15 Order Status Handling
The system shall support order statuses **submitted**, **reviewed**, and **fulfilled**. Only the company may change status; the partner may view it.

### FR-16 On-Screen Confirmation
The system shall display a clear on-screen confirmation with an order summary after a valid order submission.

### FR-17 Simulated Confirmation E-mail
The system shall render a simulated order confirmation and shipping confirmation (in-app, not really sent) containing an order summary. (Real sending: B-04.)

### FR-18 Company-Controlled Product Content
The system shall ensure product price and description are controlled by the company; partners may not modify central product master data.

### FR-19 Per-Partner Commission Calculation
The system shall calculate a commission per order as a percentage of the total order value, using the assigned partner's commission percentage (`Partner.commissionPercent`), and store it as its own commission record. The company sets the percentage via a dropdown with presets 5% / 10% / 15% plus an optional custom value; it is changeable at any time.

### FR-20 Role Separation and Access Control
The system shall enforce role-based access (Keycloak) for company and partner roles. Protected views shall not be reachable via direct URL without authorization.

### FR-21 Validation of Required Inputs
The system shall validate required partner, product, end-customer, and order fields before saving.

### FR-22 User Feedback
The system shall provide understandable feedback for successful saves, failed validations, unauthorized actions, and invalid order submissions.

### FR-23 Inactive Partner Protection
The system shall prevent inactive/disabled partners from logging into the internal area and from placing new orders. The active/inactive state is stored solely as a flag on the Partner record (`Partner.active`) and enforced server-side after authentication (Keycloak is not used to hold this business state). Existing orders of that partner remain visible to the company.

### FR-24 Unavailable Product Handling
The system shall prevent adding products marked unavailable to a new order, even if previously enabled for the partner.

### FR-25 No-Enabled-Products Handling
The system shall handle the case where a partner has no enabled products by showing a clear message and preventing order creation.

> **Deferred FRs (see `phase2_backlog.md`):** external partner shop + direct end-customer ordering (B-01); shop design customization (B-02); automatic commission level-up (B-03); real e-mail sending (B-04); shop inquiry auto-forward (B-05).

---

## 6. Non-Functional Requirements

### Usability
- Clear, understandable UI with simple, structured navigation.
- Partner dashboard is the partner landing screen and is compact (should motivate: commission, revenue, orders).
- Error messages understandable and helpful.
- Clean, modern UI/UX suitable for a school-project prototype.

### Performance
- Typical pages (product list, partner views, dashboard, order list) load within ~2s in a normal demo environment.
- The system should handle the reference scale (~200 partners, ~800 articles) responsively in demo conditions.
- Standard actions feel immediate.

### Reliability
- Data is saved reliably.
- Role-based visibility of products and orders is enforced consistently in all tested scenarios.
- Invalid actions must not silently corrupt data.
- Excel import must not create inconsistent or partially imported data without a clear report.

### Maintainability
- Functional areas (partner management, product management, import, order logic, commission logic, dashboard) shall be separable in the implementation.
- Frontend built from reusable Web Components / Custom Elements.

### Security and Access Control
- Role-based access via Keycloak for company and partner roles.
- Protected internal views not accessible via direct URL without authorization.

### Constraints
- Realistic for an HTL diploma prototype; only core-demonstrating functionality in v1.

---

## 7. Technology Stack (fixed)

The technology stack is fixed. **No other technologies may be used.**

- **Frontend:** HTML, CSS, and native **Web Components / Custom Elements** (no external frontend framework).
- **Backend:** **Quarkus** (Java).
- **Database:** **PostgreSQL**.
- **Authentication / Authorization:** **Keycloak** (roles: company, partner).

---

## 8. Data and Content

### Inputs
- **Product:** name, price, description, availability; optional category, image.
- **Partner:** name, master/contact data, partner identifier, optional affiliate identifier, commission level, active/inactive status.
- **Enablement:** mapping of products to partners.
- **End customer (Kunde):** last name (mandatory), address (mandatory), other contact fields optional.
- **Order:** end customer, selected articles, quantity per article, status.
- **Import:** Excel/CSV files for partners and articles.

### Outputs
- Product lists; enabled-product views per partner
- Partner dashboard (commission, revenue, order count, latest orders)
- Order lists for company (all) and partner (own)
- Commission records per order
- On-screen confirmation + simulated confirmation/shipping e-mail
- Import validation report

### Core Entities and Relationships
- **Partner** — partner identity, commission level, status
- **Product** — company-managed product/article data
- **ProductEnablement** — links products to partners
- **Kunde (End Customer)** — reusable end-customer record, created/selected by a partner
- **Order** — created by a partner for a Kunde
- **OrderItem** — one line of an order (product + quantity)
- **CommissionRecord** — commission derived from an order

> Commission percentage is a field on the Partner record (`Partner.commissionPercent`, dropdown 5/10/15 or custom), not a separate entity.

Main relationships:
- One company manages many partners and many products.
- One partner can have many enabled products.
- One partner can create/select many end customers; a Kunde can be reused across many orders.
- One partner creates many orders; one order is assigned to exactly one partner and one Kunde.
- One order contains one or more order items.
- One order creates one commission record.
- One partner has exactly one commission percentage, set by the company (dropdown 5/10/15 or custom value).

### Minimum Field Expectations

**Order:** order identifier, order date, assigned partner, end customer, order items (product + quantity), order status, order value.

**OrderItem:** product reference, quantity (, unit price snapshot recommended).

**CommissionRecord:** related order identifier, assigned partner, calculation basis (order value), commission percentage used, calculated commission value.

**Kunde:** last name (mandatory), address (mandatory), optional first name / e-mail / phone.

### Data Rules
- Product price and description are centrally defined by the company; partners may not modify them.
- Products must be enabled for a partner before that partner can order them.
- Orders must be linked to the responsible partner and to a Kunde.
- Products marked unavailable are not orderable.
- Commission percentage comes from the partner's assigned commission level.

---

## 9. Business Rules / Logic

### Validation Rules
- Required fields for partners, products, end customers, and orders must not be empty.
- Invalid/incomplete order data must not be accepted without feedback.
- A partner must not order products not enabled for that partner.
- Duplicate partner/product records must be detected (especially on import).

### Permissions
- Company: manage partners (incl. commission level), manage products, import data, enable products, view all orders, change order status, view commission records.
- Partner: edit limited own master data, view enabled products, select/create end customers, create own orders, view own orders and commission, view own dashboard.
- Partner may not change commission level, order status, or central product data.

### Process Rules
- Company imports/maintains central partner and product data.
- Company enables products per partner.
- Partner creates orders from enabled products for a selected/created end customer.
- Orders are automatically assigned to the creating partner.
- Company handles fulfillment (Konfektionierung: Bestellung -> Lager -> einpacken -> verschicken).
- Commission is derived per order from the partner's commission level.

### Commission Rule
Commission = percentage of the total order value, where the percentage is the assigned partner's `commissionPercent`. The company sets it on the Partner record via a dropdown with presets **5% / 10% / 15%** plus an optional custom value, and can change it at any time. There is no separate commission-level entity; the preset values act as the "level" label. Automatic level-up by sales threshold is deferred (B-03). Each order produces one commission record.

### Order Status Rule
- **submitted:** order created by the partner (on the end customer's behalf)
- **reviewed:** order checked/processed by the company
- **fulfilled:** order marked completed (Konfektionierung/shipping done) for prototype purposes

Only the company changes status; partners view it.

---

## 10. Error Handling / Edge Cases

The system shall handle at least:
- Invalid/incomplete partner, product, end-customer, or order input
- Duplicate partner/product records (incl. during import)
- Import file with invalid rows -> clear validation report, no silent partial corruption
- Partner has no enabled products (blocks order creation with a message)
- Partner attempts to order a non-enabled or unavailable product
- Product becomes unavailable after being enabled
- Inactive/disabled partner attempts login or order creation
- Invalid actions attempted from the wrong role
- Unauthorized deep-link access to protected internal views
- Missing/undefined commission level for a partner
- Errors communicated in understandable, non-technical language where possible

---

## 11. Assumptions and Dependencies

### Technical Assumptions
- Web-based application on the fixed stack (Section 7).
- PostgreSQL for persistent storage; Keycloak for auth.
- Data initially loaded via Excel/CSV import.

### Organizational Assumptions
- The company is the central authority for product and partner data.
- Partners do not control central product master data.
- End customers do not use the portal in the MVP (partner orders on their behalf).
- The prototype focuses on the core internal ordering process, not enterprise completeness.
- **Login provisioning:** the Excel import creates Partner records but no logins. Keycloak users are created manually (demo users in the committed realm export) and linked to their Partner record via the username claim. An imported partner without a Keycloak user simply cannot log in yet.

### Dependencies
- Import file column sets for partners and articles to be specified (see Open Questions) — needed for default column mapping and import validation rules.
- External shop and related features tracked in `phase2_backlog.md`.

---

## 12. Acceptance Criteria

The project is functionally acceptable if the following can be demonstrated:

1. The company can import and manage partner records (incl. commission level).
2. The company can import and manage product records.
3. Products can be enabled for specific partners.
4. A partner sees and can order only the products enabled for that partner.
5. A partner can select an existing end customer or create a new one (Nachname + address mandatory).
6. A partner can place an order with one or more enabled articles and quantities for that end customer.
7. The order is automatically assigned to the responsible partner.
8. The partner receives an on-screen confirmation with summary and a simulated confirmation e-mail.
9. A per-partner commission value is calculated on total order value and stored as its own record.
10. The company can view all orders; a partner can view only its own.
11. The company can move an order through submitted -> reviewed -> fulfilled; the partner can view status.
12. The partner dashboard shows commission, revenue, order count, enabled products, and latest orders.
13. A partner cannot order a non-enabled or unavailable product.
14. Invalid order submission produces understandable validation feedback.
15. An inactive/disabled partner cannot log in or create new orders.
16. Role-based access (Keycloak) prevents unauthorized direct-URL access to internal views.
17. The full chain **Company -> Partner -> End Customer** works consistently via partner-driven ordering.
18. Excel import of partners and articles works with a validation report.

---

## 13. Open Questions

- **Research-question supervisor sign-off:** wording approved by Chris on 2026-07-07 and ready to send to the supervisor; final supervisor confirmation still pending.
- **Import file column formats:** the column sets of the real partner and article Excel/CSV files are not yet specified — waiting on sample data from the client. Needed to define the default column mapping and per-column validation rules for FR-04 (blocks final import implementation, not the rest of the MVP).

### Resolved (2026-07-03)
- **Dashboard totals (FR-09):** decided — commission/revenue/order-count totals on the partner dashboard count only orders with status **fulfilled**. The latest-orders list still shows recent orders of any status.

### Resolved (2026-07-02)
- **Keycloak user provisioning:** import creates Partner records only, never Keycloak users; logins are provisioned manually and linked via username claim (see §11 Organizational Assumptions).

### Resolved (2026-07-01)
- **Commission percentages:** decided — `Partner.commissionPercent` set by the company via dropdown 5/10/15 + optional custom value. No separate level entity.
- **Active status:** decided — DB-only flag `Partner.active`, enforced server-side; Keycloak stays simple.
- **Research question direction:** decided — reframe toward the internal Partner ↔ Company relationship (partner management, product enablement, partner-driven ordering, commission). The external partner shop + direct end-customer ordering (Endkunde → Partner) is Phase-2 (B-01), mentioned as future.
- **Optional Kunde fields:** decided — order form offers first name, e-mail, phone as optional fields (beyond mandatory Nachname + address), per §8 minimum field expectations.
- **Affiliate identifier:** decided — deferred, not part of MVP entity/UI.
- **Schema management:** decided — Hibernate auto-generation, no Flyway migrations for the prototype. Profiles: `drop-and-create` + seed script for dev/test, `update` for the demo/prod profile so demo data survives restarts.

### Resolved (2026-07-07)
- **Research question wording:** approved by Chris for supervisor review:
  > Wie kann ein webbasiertes Partnerportal gestaltet werden, das Unternehmen, Vertriebspartner und Endkunden in einem partnerbasierten Vertriebsmodell schrittweise verbindet und dabei Partnerverwaltung, Produktfreigabe, Bestellabwicklung, Provisionszuordnung und Auswertungen in einer benutzerfreundlichen und erweiterbaren Plattform zusammenfuehrt?

---

## 14. Deferred Features Reference

All deferred and stubbed features are tracked in `phase2_backlog.md` (B-01 … B-07) with full-version specs and activation triggers. Say "implement B-XX" to build the full version.
