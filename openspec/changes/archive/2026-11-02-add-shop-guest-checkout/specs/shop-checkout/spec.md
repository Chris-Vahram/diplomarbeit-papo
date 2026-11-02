## ADDED Requirements

### Requirement: Shop cart
Visitors SHALL add articles of the public catalogue to a cart, change quantities and remove lines. The cart SHALL be stored in the browser per shop and show gross line totals, the gross total and the contained USt per rate.

#### Scenario: Cart survives reload
- **WHEN** a visitor adds an article and reloads the page
- **THEN** the cart still contains the article

#### Scenario: Article no longer available
- **WHEN** an article in the cart becomes unavailable or is no longer enabled
- **THEN** the cart marks the line "Nicht mehr verfügbar" and excludes it from checkout

### Requirement: Checkout form
The checkout SHALL require Nachname, street, postal code, city and e-mail, and SHALL offer first name and phone as optional fields. It SHALL show the order summary, the payment note "Die Rechnung erhalten Sie mit der Lieferung." and a submit button labelled "Zahlungspflichtig bestellen".

#### Scenario: Missing e-mail
- **WHEN** a visitor submits without e-mail
- **THEN** the form shows "Bitte eine E-Mail-Adresse angeben." and nothing is sent

### Requirement: Server-side order resolution
The public order endpoint SHALL resolve the partner only from the shop identifier, SHALL accept only article ids and quantities from the client, and SHALL re-read gross price, tax rate, enablement and availability on the server. Any client-supplied price SHALL be ignored. Orders for unreachable shops SHALL be rejected with the uniform 404.

#### Scenario: Manipulated price
- **WHEN** a request contains a price field lower than the article price
- **THEN** the order is stored with the server price

#### Scenario: Foreign article
- **WHEN** a request contains an article not enabled for the shop's partner
- **THEN** the request is rejected and no order is created

### Requirement: Abuse protection
The order endpoint SHALL be rate-limited, SHALL reject requests whose hidden honeypot field is filled, SHALL apply the existing quantity upper bound and SHALL use a client-generated idempotency key so that repeated submissions within 24 hours create only one order.

#### Scenario: Double submit
- **WHEN** the same checkout is submitted twice with the same idempotency key
- **THEN** one order exists and both responses return the same order number

#### Scenario: Honeypot filled
- **WHEN** the hidden field contains a value
- **THEN** no order is created and the response looks like a generic error

### Requirement: Order confirmation in the shop
After a successful order the shop SHALL show an order number, the summary with gross totals and USt, the partner as contact, and a printable receipt. It SHALL state that a confirmation e-mail is not yet sent if B-04 is not active.

#### Scenario: Print receipt
- **WHEN** the visitor clicks "Beleg drucken"
- **THEN** a print-optimised receipt with order number, items, totals and seller data opens
