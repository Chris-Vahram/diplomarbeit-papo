## Why

The 2026-07-09 review found two FSD requirements only partially implemented in the shipped MVP, with the spec and code diverged (order-management task 4.4 is checked off although the behavior is incomplete):

- **FR-17** requires both a simulated order confirmation *and* a simulated shipping confirmation. Only the order confirmation exists; setting an order to `fulfilled` shows just a toast, with no shipping-confirmation view anywhere in the code.
- **FR-23** requires the system to prevent inactive partners from receiving new orders. The partner self-service path is guarded (via `CurrentPartner`), but the company manual-order path (`OrderService.createForPartnerId`) never checks `partner.active`, so the company can create orders and commission records for deactivated partners.

## What Changes

- Add a simulated **shipping confirmation** rendered in-app when the company advances an order to `fulfilled` (no real e-mail send, consistent with the simulated order confirmation). Confirmed decision: build this.
- Enforce `partner.active` on the **company manual-order path** so new orders (and their commission records) cannot be created for an inactive partner. Confirmed decision: treat the current behavior as a bug and block it, returning a German validation error.
- Bring the `order-management` spec in sync: split the confirmation requirement into an explicit order-confirmation-on-submission and a shipping-confirmation-on-fulfilment, and state the inactive-partner rule for both order-creation paths.

No new dependencies. All user-facing text stays German with correct umlauts.

## Capabilities

### New Capabilities
- `order-management`: (not yet present in `openspec/specs/`, currently only inside the unarchived `build-partnerportal-mvp` change) — this change adds the shipping-confirmation-on-fulfilment requirement and the inactive-partner order-creation guard as delta requirements for that capability.

## Impact

- Frontend: shipping-confirmation view rendered from the status-change flow (`company-order-list-page.js`), reusing the existing confirmation/simulated-email presentation.
- Backend: `OrderService.createForPartnerId` gains an `active` check; a German error is returned when the target partner is inactive.
- Spec: `order-management` requirements updated to remove the divergence noted in the review.
- No schema changes, no new dependencies.
