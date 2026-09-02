# partner-order-cart Specification

## Purpose
TBD - created by archiving change partner-order-cart. Update Purpose after archive.
## Requirements
### Requirement: Browsable, searchable product catalog

The partner order page SHALL show the partner's enabled products as a catalog the partner can both browse and narrow. Each product SHALL display its thumbnail, full name, category, and unit price, and SHALL indicate availability. The catalog SHALL be narrowable by a search field matching name and category case-insensitively as a substring (umlaut-correct, no ASCII folding) and by a category filter. All product-derived text SHALL be HTML-escaped. Unavailable products SHALL be shown disabled and SHALL NOT be addable to the cart.

#### Scenario: Browse the full catalog
- **WHEN** a partner opens the order page
- **THEN** all their enabled products are shown as catalog entries with thumbnail, full name (not truncated), category, and price, without the partner having to search first

#### Scenario: Search narrows the catalog
- **WHEN** the partner types "müll" into the catalog search
- **THEN** only enabled products whose name or category contains "müll" case-insensitively are shown, with the umlaut matched correctly (and "muller" does not match "Müller")

#### Scenario: Category filter narrows the catalog
- **WHEN** the partner selects a category in the category filter
- **THEN** only products in that category are shown; a neutral option imposes no restriction

#### Scenario: Unavailable product cannot be added
- **WHEN** an unavailable product appears in the catalog
- **THEN** it is shown disabled ("nicht verfügbar") and cannot be added to the cart

#### Scenario: Escaped product text
- **WHEN** a product name or category contains HTML-special characters
- **THEN** the catalog renders them as literal text, not markup

### Requirement: Cart with live totals and commission preview

The partner order page SHALL provide a cart to which products are added. The cart SHALL show one line per product with its quantity (adjustable, minimum 1), unit price, and line total, and SHALL let the partner change a quantity or remove a line. The cart SHALL show the running order total (Gesamtwert) and a commission preview, both computed in integer cents; the commission preview SHALL reproduce the backend HALF_UP arithmetic so it equals the stored commission to the cent. An empty cart SHALL show a German empty-state message.

#### Scenario: Add products and see the total
- **WHEN** the partner adds two products to the cart and sets quantities
- **THEN** each line shows its line total and the cart shows the summed Gesamtwert and a commission preview, updating immediately as quantities change

#### Scenario: Remove a line
- **WHEN** the partner removes a cart line
- **THEN** the Gesamtwert and commission preview drop by that line's contribution immediately

#### Scenario: Commission preview matches the backend
- **WHEN** the cart total and the partner's commission percentage would round at the cent boundary
- **THEN** the previewed commission equals the commission that is stored on the created order

#### Scenario: Empty cart
- **WHEN** the cart contains no products
- **THEN** a German empty-state message ("Warenkorb ist leer") is shown and the order cannot be submitted

### Requirement: Add-to-cart and direct single-item ordering

The partner SHALL be able to build a multi-item order by adding several products to the cart, and SHALL be able to order a single product directly (a one-item cart). Both paths SHALL require the mandatory Endkunde and SHALL submit through the same order request; there is no separate direct-order payload.

#### Scenario: Multi-item order
- **WHEN** the partner adds several products and places the order
- **THEN** one order is created containing all cart items

#### Scenario: Single-item order
- **WHEN** the partner adds one product and places the order
- **THEN** one order is created containing that single item, using the same submit as a multi-item order

### Requirement: Submit creates the order unchanged

Placing the order SHALL send the existing request shape (`{ kundeId | newKunde, items: [{ productId, quantity }] }`) to the partner order endpoint and SHALL show the on-screen confirmation on success. The submit SHALL be guarded against double submission. No backend behavior, validation, or payload SHALL change relative to the previous order form.

#### Scenario: Place the order
- **WHEN** the partner has selected or created an Endkunde, has a non-empty cart, and places the order
- **THEN** the order is created and the on-screen confirmation (with the simulated e-mail) is shown, and a rapid second click does not create a duplicate order

### Requirement: Single canonical partner order surface

Partner order creation SHALL happen on one canonical page (route `/partner/orders/new`). The "Neue Bestellung" actions on the partner dashboard and the partner order list SHALL navigate to that page. No partner order-creation modal SHALL remain. The page SHALL offer a way back to "Meine Bestellungen".

#### Scenario: Dashboard and order-list buttons navigate to the page
- **WHEN** a partner clicks "Neue Bestellung" on the dashboard or the order list
- **THEN** the app navigates to `/partner/orders/new` and no order-creation modal is shown

#### Scenario: Return to the order list shows the new order
- **WHEN** the partner places an order and returns to "Meine Bestellungen" via the page's back affordance
- **THEN** the freshly loaded order list includes the newly created order

#### Scenario: No products enabled
- **WHEN** a partner with no enabled products opens the page
- **THEN** the German "keine Produkte freigeschaltet" message is shown instead of the catalog and cart
