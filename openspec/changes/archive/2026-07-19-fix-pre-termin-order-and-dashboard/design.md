## Context

Batch B addresses three independent audit findings that share a pre-termin priority but touch only established order and dashboard flows. The shared `kundePickerHtml` currently renders the first existing customer as selected; `OrderService.listForPartner` orders by ascending id; and both dashboard pages convert locally constructed dates with `toISOString()`, shifting Austrian calendar boundaries to the previous UTC day.

The implementation must remain within the existing static Web Component frontend and Quarkus backend. No dependency, API, schema, or broad dashboard/order refactor is needed.

## Goals / Non-Goals

**Goals:**

- Require a deliberate existing Endkunde selection in both consumers of the shared picker.
- Show a partner's newest order first and cover that backend ordering contract.
- Preserve exact local calendar dates in both dashboard range filters.
- Keep all existing validation, partner scoping, fulfilled-only totals, money handling, and company-order behavior unchanged.

**Non-Goals:**

- No customer-picker redesign, searchable customer dropdown, or customer data-model change.
- No pagination or general order-list sorting framework.
- No timezone library, UTC migration, backend dashboard filter API, or new frontend test framework.
- No work on findings outside UX-003, UX-004, and TECH-001.

## Decisions

1. **Fix customer choice once in the shared picker.** Add a disabled, selected empty option and `required` to the existing-customer `<select>` produced by `kundePickerHtml`. Wire a German custom-validity message and clear it after a real choice. This covers partner checkout and company manual ordering without duplicate page changes. The new-customer mode remains unchanged because its existing fields provide the alternative payload.

2. **Sort partner orders in the backend query.** Change only `OrderService.listForPartner` to `order by o.id desc` and assert returned ids are descending in `OrderResourceTest`. This makes the API contract correct for every partner consumer and avoids client-only re-sorting. `listAll` stays unchanged because company UI ordering is outside UX-004 and already handled separately.

3. **Centralize local calendar serialization in the existing dashboard helper.** Export one `toLocalIsoDate(date)` function from `dashboard-data.js`, built from `getFullYear()`, `getMonth() + 1`, and `getDate()` with zero-padding. Both dashboard pages import it and remove their duplicate UTC-based helpers. A timezone dependency is unnecessary for plain local calendar fields.

4. **Use existing verification surfaces.** Add the backend ordering assertion to the matching Quarkus test and verify frontend behavior with the real browser at Europe/Vienna dates, including summer time, because the repository intentionally has no frontend build or test runner.

## Risks / Trade-offs

- **Native form validation can vary by browser language** → set the established German message explicitly on the shared select.
- **Changing backend order direction affects every partner order-list consumer** → scope the query change to `listForPartner` and verify list rendering plus fresh-order visibility.
- **Local date serialization intentionally depends on the browser calendar zone** → this matches `<input type="date">` semantics; verify both winter and summer dates without adding timezone machinery.
