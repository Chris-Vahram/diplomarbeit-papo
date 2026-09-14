## Why

Company users need operational context about a partner, but that text must never appear in partner-visible APIs. A bounded plain-text field covers the need without a document system or note history.

## What Changes

- Add one optional internal partner note with a maximum length of 4,000 characters.
- Allow only company-role users to read and change it.
- Use plain text in the existing company partner form.
- Exclude the note from partner self-service, imports, exports, orders and other responses.
- Do not add rich text, multiple notes, history, attachments or notifications.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `partner-management`: Company partner management includes a 4,000-character internal note with a strict response boundary.

## Impact

Affected areas are the Partner entity, separate company/partner DTO mapping, company request validation, partner management UI, tests and data documentation. No new dependency is required.
