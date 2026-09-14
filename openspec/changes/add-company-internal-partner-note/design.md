## Context

The same `PartnerDto` is currently reused by company and partner-self resources. Adding the field there would leak company-only text.

## Goals / Non-Goals

**Goals:** One bounded operational note, server-enforced company visibility, existing form reuse.

**Non-Goals:** Rich text, audit history, attachments, import/export or notifications.

## Decisions

- Add nullable Partner text field with an explicit 4,000-character limit and request validation.
- Split response mapping so company representation contains `internalNote` and partner-self representation never has the property. Do not rely on frontend hiding or role-sensitive entity serialization.
- Add native `<textarea maxlength="4000">` to the existing company form and assign values through DOM properties. No editor dependency.
- Treat blank input as null. Full company updates must round-trip the loaded note so unrelated edits do not erase it.

## Risks / Trade-offs

- [Risk] DTO reuse leaks note. Mitigation: separate response types and negative JSON assertions.
- [Risk] Full-update clients erase unknown fields. Mitigation: update all company callers in same change and regression-test edits.

## Migration Plan

Add nullable column, then API and UI. Existing partners need no backfill. Rollback removes UI/API exposure while nullable data remains harmless.
