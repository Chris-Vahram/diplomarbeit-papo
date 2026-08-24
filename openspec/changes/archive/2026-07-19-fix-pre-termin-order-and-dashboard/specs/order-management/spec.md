## ADDED Requirements

### Requirement: Existing Endkunde selection is deliberate
The shared order form SHALL require the user to choose an existing Endkunde explicitly and SHALL NOT silently preselect the first customer. This behavior SHALL apply to partner order creation and company manual-order creation.

#### Scenario: Existing-customer mode opens
- **WHEN** an order form opens in existing-customer mode
- **THEN** the customer field shows a disabled German prompt and no Endkunde id is selected

#### Scenario: Submission without an existing customer
- **WHEN** the user submits in existing-customer mode without choosing an Endkunde
- **THEN** the browser blocks submission and shows a German validation message asking for an Endkunde

#### Scenario: Explicit customer selection
- **WHEN** the user chooses an existing Endkunde and submits an otherwise valid order
- **THEN** the selected Endkunde id is included and order creation proceeds normally

#### Scenario: New-customer mode remains available
- **WHEN** the user switches to new-customer mode and supplies valid customer data
- **THEN** no existing Endkunde selection is required

### Requirement: Partner order history is newest first
The partner order-list API SHALL return the authenticated partner's orders in descending order so the newest order appears first while partner scoping remains unchanged.

#### Scenario: Partner has multiple orders
- **WHEN** the partner loads `Meine Bestellungen`
- **THEN** orders are returned newest first by descending order id

#### Scenario: Fresh order is visible first
- **WHEN** a partner completes a new order and returns to `Meine Bestellungen`
- **THEN** the new order appears as the first order in the list

#### Scenario: Partner isolation remains unchanged
- **WHEN** a partner loads their ordered history
- **THEN** no order belonging to another partner is returned
