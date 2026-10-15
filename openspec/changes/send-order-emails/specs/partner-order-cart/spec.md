## MODIFIED Requirements

### Requirement: Submit creates the order unchanged

Placing the order SHALL send the existing request shape (`{ kundeId | newKunde, items: [{ productId, quantity }] }`) to the partner order endpoint and SHALL show the on-screen confirmation on success. The submit SHALL be guarded against double submission. The request payload SHALL stay unchanged; validation changes only in that a malformed new-customer e-mail is rejected with a German message. The response additionally carries the customer e-mail notice.

#### Scenario: Place the order
- **WHEN** the partner has selected or created an Endkunde, has a non-empty cart, and places the order
- **THEN** the order is created and the on-screen confirmation (with the e-mail notice) is shown, and a rapid second click does not create a duplicate order
