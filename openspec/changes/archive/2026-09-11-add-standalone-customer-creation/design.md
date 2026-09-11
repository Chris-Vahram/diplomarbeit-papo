## Context

The customer API already provides partner-scoped list and create operations. The order wizard already contains the canonical end-customer form and uses the shared address-field helper. The overview currently only renders the list.

## Goals / Non-Goals

**Goals:**

- Reuse the existing request shape and partner-scoped API.
- Keep one shared address-form source for the wizard and overview.
- Show the created customer immediately through the overview's existing load path.

**Non-Goals:**

- No customer edit, delete, company-facing customer management, duplicate heuristic, new endpoint, or data-model change.
- No change to order creation or customer-scoping rules.

## Decisions

### Reuse the existing customer endpoint

The overview sends the existing `KundeRequest` shape to `POST /api/partner/customers`. The server already resolves `CurrentPartner`, validates mandatory fields and persists ownership. A new endpoint or client-supplied partner identifier would duplicate behavior and weaken the existing security boundary.

### Reuse the address helper, keep form local to the overview

The overview imports `addressFieldsHtml` and `readAddress`, mirroring the wizard's inputs and request shape. It owns modal state and submit handling because no reusable customer-form component exists and adding one for two callers is unnecessary. Use a local native `<dialog>` with `showModal()`: the existing custom modal has no focus containment or pending-close veto. Reuse its presentation classes, with only page-scoped CSS for native border/padding and `::backdrop`; do not refactor shared components or replace Tobias's styles. Give the dialog an `aria-labelledby` heading, focus the last-name input on opening, and return focus to the opener on closing. No backdrop dismissal. Escape and explicit close/cancel work only when no POST is pending.

### No duplicate detection

Names and addresses are not stable business identifiers for end customers. The existing API permits repeated values, and rejecting a plausible duplicate could prevent legitimate household or branch records. Terra must preserve that behavior.

### Refresh after successful create

After a successful POST response (existing status 200), close the dialog, report success and call the page's `load(createdId)`. Preserve the customer ID in row mapping. Keep the existing oldest-first order, but put this created ID first among customers with null last-order dates for this success refresh. Assign rows (which resets page 1), then trigger the existing `[data-table-reset]` button to clear search/activity filters. Do not access private table fields or add a table abstraction. The created ID is retained for GET retries, so more than 20 never-ordered customers cannot hide the new row. Normal page entry retains the canonical existing ordering.

Separate POST failure from refresh failure: after a successful POST and failed GET, show a persistent page message, “Endkunde angelegt. Liste konnte nicht aktualisiert werden.” with “Liste erneut laden”. That action performs only GET with the same created ID. Do not re-open the submitted form. Disable a second create until this refresh is resolved; navigation away remains possible. Keep a page-local load generation so an older initial GET cannot overwrite a newer refresh, and ignore UI updates when the page is disconnected. No new state store.

### Pending and failure behavior

Read the complete payload before disabling controls. Use the existing shape `{lastName, address: {street, houseNumber, postalCode, city, country}, firstName, email, phone}`; empty optional values become null as in the wizard. The form's e-mail input uses `type=email`, without adding a new server validation rule. Set a synchronous pending guard, disable controls and all local close actions, and prevent the native `cancel` event while pending. A second click or Enter must not start another apiFetch call. The shared API helper's existing single HTTP-401 retry after token refresh remains unchanged; count UI calls separately from HTTP attempts. Do not add transport retries or alter shared authentication.

On any failed `apiFetch` creation call keep entered values, restore controls and show the established German error feedback. The helper throws plain errors without preserving HTTP status, so the page must not infer a confirmed rejection from error text. Conservatively also show “Speicherstatus unklar. Bitte zuerst die Endkundenliste prüfen.” for every such failure. Native required-field validation still prevents invalid submissions locally. No shared API change or error-string heuristic is needed.

The “Liste prüfen” action closes the dialog without resetting its fields and performs GET, making the list accessible rather than updating an inert background. A page-local “Eingabe fortsetzen” action reopens these retained values after a failed/unconfirmed save; a fresh “Neuer Endkunde” action resets them. No browser storage of customer data. GET failure keeps a visible retry action. Do not automatically resubmit or claim exactly-once semantics. Existing authenticated-fetch behavior remains unchanged. After navigation, a late response must not touch the new page or its focus. Cancelling before submit creates nothing; opening a fresh dialog clears previously cancelled values.

## Risks / Trade-offs

- **[Risk]** Double-click can create two records because the API has no idempotency key. **Mitigation:** disable the submit button while the request is pending and restore it only on failure.
- **[Risk]** A failed request could lose entered values. **Mitigation:** do not reset or close the form until the API response succeeds; show the established German toast error.
- **[Risk]** Client-side role checks are not a security boundary. **Mitigation:** reuse the existing partner-only route and server-side `CurrentPartner.resolve()` endpoint.

## Migration Plan

No migration. Rollback removes only the overview action; existing customer records and API behavior remain valid.

## Independent Review

Historical draft assessment (superseded by the independent review of 10.09.2026): the prior draft claimed that a second login in the shared test/dev realm was required and declared `READY`. That declaration is withdrawn. Existing DB fixtures can establish the required ownership/query isolation without modifying the realm; formal artifact presence alone was insufficient for readiness.

Functional review confirms that the change adds the missing overview entry point only. It reuses the existing request validation, partner resolution, ownership query, list ordering and escaped rendering; no new order, authentication, data-model or external-shop behavior is introduced.

### Review corrections, 10.09.2026

- Missing create entry point is confirmed against the meeting and actual page.
- Filter reset alone is insufficient; created-ID priority and retained ID on refresh retries cover pagination.
- Native dialog and synchronous pending guard close the keyboard/focus/close gaps locally.
- POST success and failed refresh must not be represented as a failed save.
- Payload is read before disabling controls; optional-field behavior remains compatible.
- Test strategy: create a customer fixture for seeded P-1002 using `QuarkusTransaction`; the real P1 GET must exclude that ID. After a real P1 POST, inspect the persisted owner and verify the P2 service list excludes the resulting ID. Preserve the existing two-sided `KundeServiceTest`. This is real P1 HTTP plus service/DB isolation, not a claimed authenticated P2 HTTP test.
- Add anonymous/company GET and POST denial, inactive POST without persistence (restore active state in `finally`), required address validation and escaped output checks. Fixtures are identified by IDs/unique test markers and cleaned up without affecting other tests.
- No API, authentication, dependency, upload, external integration or schema change is required. Backend hardening beyond this contract is not silently added.

Final artifact readiness and actual validation evidence are recorded in `review.md` after the artifact review. Product implementation remains unstarted.
