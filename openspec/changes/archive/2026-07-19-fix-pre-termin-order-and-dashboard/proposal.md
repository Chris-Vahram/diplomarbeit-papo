## Why

Three confirmed P1 findings still make the pre-termin order flow misleading: an order silently defaults to the first Endkunde, new partner orders appear last, and explicit dashboard date ranges shift by one day in Austria because local dates are converted through UTC. Fixing them removes avoidable wrong selections and incorrect period figures before the next customer demonstration.

## What Changes

- Require an explicit Endkunde selection in the shared existing-customer picker used by partner and company manual-order flows.
- Return the partner's order list newest first so a freshly submitted order is immediately visible.
- Convert dashboard period boundaries from local calendar fields without UTC conversion in both dashboards.
- Add focused regression coverage for partner-order ordering and browser verification for both shared picker paths and dashboard boundary dates.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `order-management`: Orders require a deliberate existing-customer choice, and partner order history is ordered newest first.
- `company-dashboard`: Explicit date ranges preserve the selected local calendar days exactly.
- `partner-dashboard`: Explicit date ranges preserve the selected local calendar days exactly.

## Impact

- Frontend: shared customer picker/order form, both dashboard pages, and the existing dashboard-data helper.
- Backend: partner order-list query and its matching Quarkus test.
- No API shape, database schema, dependency, authentication, authorization, money, commission, or deferred-scope change.
