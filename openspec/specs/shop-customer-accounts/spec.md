# shop-customer-accounts Specification

## Purpose
TBD - created by archiving change add-shop-customer-accounts. Update Purpose after archive.
## Requirements
### Requirement: Separate customer realm
End-customer identities SHALL live in a separate Keycloak realm `papo-shop`. Tokens from this realm SHALL only be accepted on the customer API namespace `/api/shop-customer/*`; all company and partner endpoints SHALL reject them. Customer accounts SHALL NOT be able to obtain company or partner roles.

#### Scenario: Customer token on partner API
- **WHEN** a request to `/api/partner/orders` carries a valid `papo-shop` token
- **THEN** the system responds 401

### Requirement: Registration and login
Visitors SHALL be able to register with e-mail and password, SHALL verify their e-mail before the account becomes usable, and SHALL be able to log in and reset their password using Keycloak's standard redirect flow. Guest checkout SHALL remain available without an account.

#### Scenario: Unverified account
- **WHEN** a visitor registers but has not confirmed the e-mail
- **THEN** login is refused with a hint to confirm the e-mail address

#### Scenario: Guest still possible
- **WHEN** a visitor proceeds to checkout without logging in
- **THEN** the guest checkout is offered as before

### Requirement: Customer profile
A logged-in customer SHALL maintain a profile with Nachname, first name, address, e-mail and phone, which prefills the checkout.

#### Scenario: Prefilled checkout
- **WHEN** a logged-in customer opens the checkout
- **THEN** the form is prefilled from the profile and editable

### Requirement: Own orders view
A logged-in customer SHALL see the orders placed with the account in the current shop, with order number, date, items, gross total and status. Customers SHALL NOT see orders of other customers or orders placed as guests.

#### Scenario: Other customer's order
- **WHEN** a customer requests an order id not linked to the account
- **THEN** the response is 404

### Requirement: Data export and account deletion
A logged-in customer SHALL download the account's stored personal data and orders as a file, and SHALL delete the account. Deletion SHALL remove the login and profile; order records SHALL remain as business records of the company and partner with the customer data entered at order time.

#### Scenario: Delete account
- **WHEN** a customer deletes the account and confirms
- **THEN** the login no longer works, the profile is removed, and existing orders remain visible to partner and company
