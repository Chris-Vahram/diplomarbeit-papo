## Why

In the meeting on 12.10.2026 the partner side asked for end customers to be editable directly in the "Meine Endkunden" list. Today a partner can only create and select customers; a typo in a name or a changed address cannot be corrected, because the API has no update operation (`KundeResource` offers only GET and POST).

## What Changes

- Add `PUT /api/partner/customers/{id}` for the partner role. It accepts the same payload and validation as creation (last name and address mandatory; first name, e-mail and phone optional).
- Only the owning partner may update a customer; another partner's or an unknown customer returns 404 with a German message. Inactive partners stay blocked server-side.
- Add a "Bearbeiten" action per row in the partner customer list, reusing the existing create dialog in an edit mode with prefilled values.
- Changes apply live: existing orders reference the customer record, so corrected data also appears in existing orders and order documents. No customer snapshot on orders.
- No delete, no company-side customer editing, no change history.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `customer-management`: partners can edit their own end customers; scoping covers write access.

## Impact

Backend `customer/KundeResource.java`, `customer/KundeService.java`, `KundeResourceTest`; frontend `pages/partner-customer-list-page.js`. No schema change, no new dependency.
