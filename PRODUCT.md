# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

- **Company (Unternehmen):** internal staff of the reference company (unnamed, roughly 100 partners). They manage partners, commission rates, articles and categories, import partner/article data, enable articles per partner, review all orders and advance their status (submitted → reviewed → fulfilled). Mainly desktop.
- **Partner:** sales partner companies, one shared organizational login per partner company in the current stage. They select or create an end customer (Kunde), order several articles on the customer's behalf, and follow their own orders and commission. Mainly desktop; mobile use is occasional but must work.
- **End customer (Endkunde):** receives the goods and the order confirmation; does not use the internal portal. A public partner shop is a separate, later stage (Tobias' shop changes).

## Product Purpose

PAPO is a company-independent, extensible B2B partner-ordering platform for partner-driven sales (Company → Partner → End Customer). The first stage replaces orders the company used to receive by e-mail and telephone with a structured internal portal. Success: a partner places a correct multi-article order for an end customer in a few steps, the order is assigned to the partner automatically, and the company processes it without re-typing.

## Positioning

Built around the partner chain rather than a generic shop: every order belongs to a partner and one of their end customers, each partner sees only their enabled articles and their own customers, orders and commission, and the commission is recorded per order. Article master data comes from the company's leading system (BMD) via import; PAPO does not compete with it.

## Operating Context

- Company staff work through lists, filters, dialogs and status changes in an office setting; partners use the dashboard as their landing screen and the guided "Neue Bestellung" wizard as the main action.
- Data volumes at reference scale: ~200 partners, ~800 articles.
- Deployed at `paportal.at` (demo data only; no real customer operation approved yet).
- Thesis project (HTL Diplomarbeit): Chris implements, Tobias owns the visual design (`Design/`).

## Capabilities and Constraints

- Implemented: partner and article management, categories, Excel/CSV import with column mapping (dummy until company data exists), per-partner article enablement, end customers (create/edit, partner-scoped), multi-article orders with unit-price snapshot, status pipeline, per-order commission, dashboards with period presets and trends, order e-mails (prepared, disabled on the VM), personal partner logins bound to OIDC subjects, company profile and trade-proof upload.
- Stack is fixed: static HTML/CSS/JavaScript with native Web Components (light DOM), no framework, build step, npm or CDN; Quarkus backend, PostgreSQL, Keycloak.
- Terminology: user-facing term is **Artikel** (never alternating with "Produkt"); "Endkunde"/"Kunde", "Bestellung", "Provision". Status labels in German.
- All UI text, validation and error messages in German with correct ä/ö/ü/ß.
- Undecided: multi-company tenancy and personal company accounts (requested, separate changes), shop URL as path or subdomain, BMD article-number rules, commission settlement/payout.

## Brand Commitments

- Name: **PAPO** (the product, not the client). Domain `paportal.at`.
- Voice: friendly and encouraging but factual. Partners should feel supported (their orders, revenue and commission are visible and easy to reach); no marketing language, no exaggeration. Formal address ("Sie").
- Visual identity is defined by Tobias in `Design/` (role colours: green for company, blue for partner). It is authoritative; changes go through Tobias.

## Evidence on Hand

- Demo data only (`DemoDataSeeder`, demo users `company-demo` / `partner-demo`). No real customers, testimonials, figures or partner names; do not invent any.
- Article images: `frontend/assets/products/`.
- Meeting notes in `Documentation/Besprechungsprotokolle/`; newest feedback (12.10.2026): order process "bereits sehr gut umgesetzt".

## Product Principles

1. The order for an end customer is the core path; it stays the most visible and quickest action for partners.
2. Each role sees exactly its own scope; the interface never suggests data a user cannot access.
3. Clarity over density tricks: plain German labels, visible status, money always unambiguous.
4. Extend, don't fork: new stages (shop, tenants) build on the same entities and design system.
5. No invented business rules: open company decisions stay visibly open.

## Accessibility & Inclusion

Target: WCAG 2.2 AA for the internal portal — visible keyboard focus, text contrast ≥ 4.5:1, touch targets ≥ 44 px on mobile, `prefers-reduced-motion` respected, status never conveyed by colour alone. Known exception documented in `Design/guidelines/colors.md` (§ Contrast Ratios) must be resolved or justified.
