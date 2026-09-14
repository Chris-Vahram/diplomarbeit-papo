## Why

> Abschlussreview 13.09.2026: Technisches MVP 53/53 vollständig. Allgemeine Diplomarbeitsdokumentation ist unabhängig unter `complete-thesis-documentation` geplant; künftiger visueller Polish gehört in den jeweiligen UI-Change. Der folgende Text bleibt historischer MVP-Ausgangspunkt. Größenangaben 200/800 und Tagesmengen sind keine neu verifizierten Zahlen; Original 31.08 nennt rund 100 Partner, Artikel-/Bestellmengen bleiben unbelegt.

Partner orders at the reference client arrive manually by e-mail (~10–15/day) and phone (~3–4/day) across ~200 partners and ~800 articles, with no central coordination of article data, partner data, product visibility, order assignment, or commission. The MVP replaces this with a web-based internal B2B partner-ordering portal following the chain **Company -> Partner -> End Customer**, making the core ordering process fast, assignable, and traceable.

## What Changes

- New internal portal where the **company** imports/manages partners and articles, enables products per partner, and controls order status and per-partner commission percentage.
- New **partner** area: dashboard (landing), enabled products, own master data (limited edit), own orders.
- New **partner-driven ordering**: partner logs in (Keycloak), selects or creates an end customer (Kunde), and places an order with one or more enabled articles + quantities; order auto-assigned to the partner.
- New **Excel/CSV import** for partners and articles (column mapping, duplicate detection, validation report).
- New **per-partner commission**: `Partner.commissionPercent` set by the company via dropdown 5/10/15 + optional custom value; percentage of total order value; stored as its own record.
- New **order status** workflow submitted -> reviewed -> fulfilled (company-controlled; partner read-only).
- New **confirmations**: on-screen order summary + simulated order/shipping confirmation e-mail rendered in-app (no real send).
- Role-based access via Keycloak; inactive partners blocked via DB flag `Partner.active` (server-side, Keycloak stays simple).
- **Non-goals (deferred, see `phase2_backlog.md`):** external end-customer shop + direct ordering (B-01), shop design customization (B-02), automatic commission level-up (B-03), real e-mail sending (B-04), shop inquiry auto-forward (B-05), BMD interface (B-06), commission settlement/payout (B-07).

## Capabilities

### New Capabilities
- `auth-access-control`: Keycloak-based login and role separation (company, partner); protected views not reachable by direct URL; inactive-partner blocking via DB flag.
- `partner-management`: company CRUD of partners incl. `commissionPercent` + `active` flag; partner limited self-edit of contact data.
- `product-management`: company CRUD of articles (name, price, description, availability, optional category/image); central price/description control; unavailable handling.
- `data-import`: Excel/CSV import of partners and articles with column mapping, duplicate detection, and accepted/rejected validation report.
- `product-enablement`: company enables specific products for specific partners; partner sees/orders only enabled products; no-enabled-products handling.
- `customer-management`: reusable end-customer (Kunde) records; partner selects existing or creates new; mandatory last name + address.
- `order-management`: partner-driven order creation (multiple article lines + quantities), auto-assignment to partner, order/OrderItem storage, visibility rules, status workflow, on-screen + simulated e-mail confirmation.
- `commission`: per-order commission using `Partner.commissionPercent` on total order value, stored as CommissionRecord.
- `partner-dashboard`: compact partner landing dashboard (commission total, revenue, order count, enabled-product count, latest orders).

### Modified Capabilities
<!-- None. No pre-existing specs in openspec/specs/. -->

## Impact

- New Quarkus (Java) backend + PostgreSQL schema; entities: Partner (incl. commissionPercent + active), Product, ProductEnablement, Kunde, Order, OrderItem, CommissionRecord.
- New frontend built from native Web Components / Custom Elements (HTML/CSS), no framework.
- New Keycloak realm with company/partner roles; backend authorization on all protected endpoints; business active-state held in DB, not Keycloak.
- External dependency: an Excel/CSV parsing library on the JVM (e.g. Apache POI / opencsv) for `data-import`.
- No external e-mail infrastructure (confirmations simulated).
