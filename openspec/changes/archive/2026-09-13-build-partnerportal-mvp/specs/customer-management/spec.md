## ADDED Requirements

### Requirement: Reusable end-customer records
The system SHALL store end customers (Kunde) as reusable records. A partner SHALL be able to select an existing end customer or create a new one when placing an order.

#### Scenario: Create new customer during order
- **WHEN** a partner creates a new end customer with last name and address
- **THEN** the system stores the customer and makes it selectable for future orders

#### Scenario: Reuse existing customer
- **WHEN** a partner selects a previously created end customer for a new order
- **THEN** the system links the new order to that existing customer without re-entering data

### Requirement: Mandatory customer fields
End-customer records SHALL require at least a last name (Nachname) and an address. Other contact fields (first name, e-mail, phone) are optional.

#### Scenario: Missing mandatory field
- **WHEN** a partner tries to save an end customer without last name or address
- **THEN** the system rejects the save and shows an understandable validation message

### Requirement: Customer scoping per partner
Each Kunde record SHALL be owned by the partner who created it. A partner SHALL only see and select Kunde records they themselves created; partners SHALL NOT see or select another partner's customers.

#### Scenario: Partner selects from own customer list only
- **WHEN** a partner opens the "select existing customer" list while creating an order
- **THEN** only Kunde records created by that partner are shown

#### Scenario: Company views a customer via an order
- **WHEN** the company views an order
- **THEN** the linked Kunde's data is visible as part of that order's detail (company sees all orders, and therefore all linked Kunden through them)
