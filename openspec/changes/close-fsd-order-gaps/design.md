## Context

Two FSD gaps in shipped code. The order confirmation on submission already exists (`lib/order-form.js` `renderOrderConfirmationHtml`) and the simulated-email presentation is reusable. The partner self-service order path already blocks inactive partners via `CurrentPartner.resolve()`; only the company manual-order path (`OrderService.createForPartnerId`) is missing the guard. No schema or dependency changes.

## Goals / Non-Goals

**Goals:**
- Render a simulated shipping confirmation when an order becomes `fulfilled`.
- Block new-order creation for inactive partners on the company manual-order path.
- Remove the order-management spec/code divergence.

**Non-Goals:**
- No real e-mail sending (remains deferred, B-04).
- No redesign of the confirmation visuals in this change (Direction B "Auftragsbeleg" styling is a separate design change).
- No change to the status workflow itself (submitted→reviewed→fulfilled stays forward-only).

## Decisions

- **Reuse the existing confirmation/simulated-email presentation** for the shipping confirmation rather than a new component. Rationale: FR-17 asks for a simulated shipping confirmation "e-mail view"; the order-confirmation view already models this. The shipping confirmation is triggered from the status change to `fulfilled` in `company-order-list-page.js`. Alternative considered: a brand-new shipping component — deferred to the later design change so this change stays a focused gap-fill.
- **Guard in `OrderService.createForPartnerId`** by checking `partner.active` before creating the order, throwing the project's `ApiException` with a German message (HTTP 400/409). Rationale: the check belongs server-side on the service path that bypasses `CurrentPartner`; mirrors the existing partner-path enforcement.
- **Spec sync via ADDED deltas** under `order-management`. Rationale: the authoritative order-management spec is not yet in `openspec/specs/` (its `build-partnerportal-mvp` change is unarchived), so the two behaviors are added as delta requirements that merge on archive.

## Risks / Trade-offs

- [Shipping confirmation reusing the order-confirmation view could confuse the two] → Mitigation: label it explicitly as a shipping confirmation ("Versandbestätigung", simulated) distinct from the order confirmation.
- [Blocking inactive-partner manual orders could surprise a company user mid-flow] → Mitigation: clear German error; the partner can be reactivated first. This matches FR-23 intent.
- [Delta-vs-build-change duplication of order-management] → Both use ADDED requirements; OpenSpec merges deltas at archive. Acceptable.

## Migration Plan

Code-only, backward compatible. No data migration. Existing fulfilled orders are unaffected (the confirmation is shown at the moment of the status change going forward). Rollback = revert the change.
