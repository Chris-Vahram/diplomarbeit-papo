## MODIFIED Requirements

### Requirement: Order visibility
The system SHALL show the company all orders of its own partners and only its own orders to each partner. Orders of another company's partners SHALL be invisible and unchangeable for the company.

#### Scenario: Company sees all orders
- **WHEN** the company opens the order list
- **THEN** orders from all of its own partners are shown with their assigned partner, and no order of another company

#### Scenario: Partner sees only own orders
- **WHEN** a partner opens the order list
- **THEN** only orders assigned to that partner are shown

#### Scenario: Foreign order by id
- **WHEN** a company user changes the status of, marks as seen, or lists mails of an order of another company
- **THEN** the request returns 404 and the order is unchanged

#### Scenario: Manual order for foreign partner
- **WHEN** a company user creates a manual order for a partner of another company
- **THEN** the request returns 404 and no order is created
