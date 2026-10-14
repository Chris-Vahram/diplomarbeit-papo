## ADDED Requirements

### Requirement: Partner can edit own end customer
A partner SHALL be able to edit the last name, first name, address, e-mail and phone of an end customer they own from the "Meine Endkunden" list. The same mandatory-field validation as creation SHALL apply. The update SHALL be resolved against the authenticated partner on the server; it SHALL NOT be possible to update another partner's customer. Changes SHALL apply to the customer record itself and are therefore visible in all orders that reference it.

#### Scenario: Partner corrects a customer
- **WHEN** a partner opens "Bearbeiten" for one of their customers, changes the address and saves
- **THEN** the customer is updated, a German success message is shown and the list shows the new address

#### Scenario: Mandatory field removed
- **WHEN** a partner clears the last name or address and saves
- **THEN** the update is rejected with a German validation message and the stored customer stays unchanged

#### Scenario: Foreign or unknown customer
- **WHEN** a partner sends an update for a customer owned by another partner or for an unknown id
- **THEN** the system responds with 404 „Kunde nicht gefunden.“ and no customer is changed

#### Scenario: Inactive partner
- **WHEN** an inactive partner tries to update a customer
- **THEN** the request is rejected server-side and no customer is changed

#### Scenario: Company role
- **WHEN** a company user calls the partner customer update endpoint
- **THEN** the request is rejected with 403

#### Scenario: Existing order shows corrected data
- **WHEN** a customer with existing orders is edited
- **THEN** those orders, including their detail view and printed document, show the updated customer data

#### Scenario: Double submission
- **WHEN** the partner clicks save repeatedly while the update is pending
- **THEN** only one update request is sent

## MODIFIED Requirements

### Requirement: Customer scoping per partner
Each Kunde record SHALL be owned by the partner who created it. A partner SHALL only see, select and edit Kunde records they themselves created; partners SHALL NOT see, select or edit another partner's customers.

#### Scenario: Partner selects from own customer list only
- **WHEN** a partner opens the "select existing customer" list while creating an order
- **THEN** only Kunde records created by that partner are shown

#### Scenario: Company views a customer via an order
- **WHEN** the company views an order
- **THEN** the linked Kunde's data is visible as part of that order's detail (company sees all orders, and therefore all linked Kunden through them)
