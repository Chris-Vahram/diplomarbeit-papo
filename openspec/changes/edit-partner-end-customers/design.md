## Context

`Kunde` is owned by the creating partner (`ownerPartner`). `KundeService.findOwnedOrThrow(partner, id)` already enforces ownership with a German 404, and `CurrentPartner.resolve()` blocks inactive partners. `Order.kunde` is a live `@ManyToOne` reference; orders store no customer snapshot. The customer list page already has a create dialog with double-submit protection, distinct save/refresh feedback and the shared address helpers (`lib/address-fields.js`).

## Goals / Non-Goals

**Goals:** Partners correct their own customers' data from the list with the same validation as creation and the same safe-submission behaviour.

**Non-Goals:** Deleting customers, company editing, change history/audit, customer snapshots on orders, optimistic locking.

## Decisions

- **API:** `PUT /api/partner/customers/{id}` with `@Valid KundeRequest`, class-level `@RolesAllowed("partner")`. Full replacement of the editable fields: optional fields sent empty become null, mirroring creation.
- **Ownership:** load via `findOwnedOrThrow(currentPartner.resolve(), id)`; never accept a partner id from the client. Foreign and unknown ids both return 404 so existence is not revealed.
- **Response:** return `KundeDto` including `lastOrderDate`, so the list row can update consistently; the list is reloaded after saving anyway.
- **Live effect:** accepted by Chris on 14.10.2026. A corrected address or name shows in existing and already fulfilled orders and their printed documents. A per-order snapshot would need schema, migration and DTO changes and stays a possible later change.
- **UI:** "Bearbeiten" button per row (`data-edit-id`, as in `company-product-list-page.js`). The existing dialog switches title ("Endkunde bearbeiten"), prefills via DOM properties, and reuses the pending/feedback/refresh logic. All rendered customer values stay escaped.
- **Concurrency:** last write wins. Acceptable while one shared login per partner company exists; revisit with personal accounts.

## Risks / Trade-offs

- [Risk] Editing changes historical order documents. Mitigation: documented decision; spec scenario makes it explicit.
- [Risk] Cross-partner update via guessed id. Mitigation: ownership lookup plus negative test.
- [Trade-off] No history of customer changes. Accepted for the current stage; audit belongs to the personal-accounts change.

## Migration Plan

No schema change. Deploy backend and frontend together; rollback removes the endpoint and button without data impact.
