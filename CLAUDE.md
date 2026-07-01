# CLAUDE.md – Partnerportal (Diplomarbeit)

## Project owner
This project belongs to **Chris**.

## Project name
Webbasiertes Partnerportal zur Verwaltung von Vertriebspartnern, Produkten, Partner-Shops und partnerbezogenen Vertriebsprozessen.

## Project context
Chris works on this as part of an **HTL Diplomarbeit** in Informatik / IT Medientechnik. No code repo exists yet — this folder holds spec/FSD documents only.

## Official FSD
The authoritative Functional Specification Document for this project is `functional_specification_document_partnerportal_final.md` (in this folder).

For deep work (requirements drafting, data model, acceptance criteria, NFRs), read this file in full — it contains FR-01..FR-27, NFRs, use cases, the full entity model, business rules, error handling, and acceptance criteria.

## Core model
Unternehmen → Partner → Endkunde

## Core idea
The system goes beyond classic PRM. It does not only manage the relationship between company and partners, but also supports product-based and shop-based resale through partners.

## Goal
Develop a web-based system that bundles central partner-related sales processes in one platform and closes the gap between classic PRM and product-related partner-shop logic.

The system should:
- manage partners centrally
- manage products/articles centrally
- allow companies to release products for selected partners
- give partners their own dashboard and management area
- allow partners to build an external partner shop from released products
- allow end customers to order through partner shops
- assign sales to the correct partner
- represent commission logic between company and partner
- make administrative and sales processes more transparent and efficient

## Roles

### Company / manufacturer / operator
- central instance
- manages articles/products
- manages partners
- releases products for partners
- defines price, description and availability
- receives/processes orders from partner shops
- handles fulfillment/delivery to end customers
- handles billing/commission assignment

### Partner / sales partner
- has master data
- receives released products
- can build a partner-branded shop from released products
- may customize design
- sees relevant data in a dashboard
- receives orders or requests assigned to their shop
- receives commission
- does **not** define central product data or prices
- does **not** define central descriptions
- uses company-defined product data

### End customer
- does not interact with the internal partner portal
- interacts with the external partner shop
- sees products
- orders there
- is assigned to a partner in the background
- receives delivery ultimately from the company

## Internal area — Company ↔ Partner
- part of the partner portal
- not a full second shop
- internal catalog, management and release area
- includes partner management, partner master data, article/product data, product release, dashboard functions, partner-related process visibility, billing/commission reference and internal coordination

## External area — Partner → End customer
- external partner shop
- visible to end customers
- based on centrally maintained products
- shows only released products
- customizable in partner design
- allows direct ordering
- assigns sales to the partner
- leads to fulfillment by the company in the background

## Difference from classic PRM
Classic PRM systems often focus on partner onboarding, deal registration, leads, incentives/commissions, reporting, enablement/training and content sharing.

This project expands the focus with:
- central article/product management
- product-based partner releases
- partner-specific shop creation
- clear separation between internal partner portal and external partner shop
- direct connection between partner shop, end customer order and commission assignment

## Core functions
- article management
- partner management
- product release/internal catalog
- partner dashboard
- partner shop in partner design
- order logic
- commission logic

## Core entities (from FSD)
- Partner — partner identity and partner-specific data
- Product — company-managed product information (name, price, description, availability, optional category/image)
- ProductEnablement — links Products to Partners
- PartnerShop — shop configuration for a partner (1 partner : 1 shop in prototype)
- Order — created via a partner shop (id, date, assigned partner, products, quantities, customer name/contact, status)
- CommissionRecord — derived from an order (related order id, assigned partner, calculation basis, percentage, calculated value)

## Order status model (prototype)
Prototype order statuses (FR-24): **submitted -> reviewed -> fulfilled**

## Commission rule (prototype)
Fixed-percentage commission on order value (FR-14, exact percentage TBD — see Open questions). No settlement/payout logic in v1.

## Not primary focus
- not a full ERP
- not a general CRM
- not a pure affiliate tool
- not a pure e-commerce system without partner logic
- not a general learning/enablement portal
- full inventory management
- full shipping workflow management in detail
- full accounting / financial bookkeeping
- recreation of large enterprise platforms (e.g. Salesforce, Magentrix)
- full payment gateway integration
- automated email notifications beyond simple prototype feedback
- advanced commission settlement / payout workflows

## Demo prototype focus
- role model/login
- partner management with master data
- article management
- product release for partners
- partner dashboard
- external partner shop in partner design
- direct ordering by end customers
- assignment of orders and commissions to partners

### Explicit prototype boundaries (from FSD)
- No real payment integration — orders are prototype orders only.
- Order status limited to submitted / reviewed / fulfilled.
- Commission = fixed percentage of order value, no settlement workflow.
- Partner shop shows ALL enabled products by default; partners do NOT curate a subset in v1 (FR-23). Q26 in `fsd_clarification_questions_with_recommendations.md` recommends keeping it this way — but Chris hasn't approved it yet.

## Non-functional requirements (from FSD)
- Usability: clear UI, simple navigation, visually distinct internal vs. external areas, understandable error/validation messages, clean modern UI/UX appropriate for a school prototype.
- Performance: typical pages (product list, partner views, dashboard) load in ~2s in a normal demo environment; standard actions feel immediate.
- Reliability: reliable saves; consistent role-based visibility in all tested scenarios; invalid actions must not silently corrupt data.
- Maintainability: partner mgmt, product mgmt, shop logic, order logic, commission logic should be separable in the implementation.
- Security/Access control: role-based access for company/partner/end-customer; protected internal views must not be reachable via direct URL without authorization.
- Constraints: must remain realistic for an HTL Diplomarbeit; only core-demonstrating functionality in v1.

## Team split

**Chris:**
- market analysis
- requirements analysis
- UI/UX
- article management

**Julian:**
- data model
- partner master data
- affiliate links / partner-related assignment

**Tobias:**
- dashboard
- shop
- further system-related requirements

## Stakeholders
Company management/project owner, sales partners, end customers, diploma project team (Chris, Julian, Tobias), supervising teacher/reviewers.

## Market analysis purpose
- identify relevant market products
- compare them systematically
- identify central problems of existing solutions
- show which products partially solve which problems
- include prices and review tendencies when useful
- identify improvement potential in the market
- justify why this planned system makes sense
- derive concrete requirements for the own system and demo prototype

## Important market analysis criteria
- article management/product catalog
- partner management with master data
- affiliate links/commission logic
- partner dashboard
- internal catalog/release area between company and partner
- external partner shop/storefront in partner design
- direct ordering by end customers
- clean separation between company, partner and end customer

## Central research question
Wie kann ein webbasiertes System gestaltet werden, das Unternehmen, Partner und Endkunden in einem partnerbasierten Vertriebsmodell sinnvoll verbindet und dabei Partnerverwaltung, Produktfreigabe, Partner-Shop, Bestelllogik und Provisionszuordnung in einer klaren und benutzerfreundlichen Plattform zusammenführt?

## Open questions (need team/Chris decision)
See `fsd_clarification_questions_with_recommendations.md` (in this folder) — 28 numbered questions across 10 sections (Commission, Shop Customization, Customer Order Data, Billing/Settlement, Order Status/Workflow, Product Availability, Internal Partner Area, Dashboard, Partner Activity/Lifecycle, Small-but-important), each with a recommendation. Chris has not yet approved these recommendations — work through them one section at a time and update the FSD as decisions are made. Frontend/backend/storage tech stack is still separately undecided (not covered by the 28 questions).

Also load on demand: `meta-spec-ausgefüllt.md` (filled-out German Vorbereitungstext that fed this FSD) and `meta-spec-TEST-VORBEREITUNG.md` (the blank template) — ITP `llm-start-package` methodology source documents (meta-spec -> Vorbereitungstext -> AI-FSD -> clarification-questions).

## Writing style
- clear German
- suitable for an HTL Diplomarbeit
- avoid exaggerated corporate language
- precise
- structured
- connect market analysis to requirements
