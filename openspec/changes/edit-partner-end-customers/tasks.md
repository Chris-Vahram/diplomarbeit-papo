## 1. Backend — essential

- [ ] 1.1 Write failing `KundeResourceTest` cases first: owner updates all fields (umlauts, optional fields cleared to null), missing last name/address → German 400 with unchanged data, foreign partner and unknown id → 404 „Kunde nicht gefunden.“, inactive partner rejected, company role → 403, unauthenticated → 401, updated data visible in an existing order's DTO.
- [ ] 1.2 Add `KundeService.update(partner, id, request)` using `findOwnedOrThrow` and `PUT /api/partner/customers/{id}` returning `KundeDto` with `lastOrderDate`; make the tests from 1.1 pass.

## 2. Frontend — essential

- [ ] 2.1 Add an actions column with „Bearbeiten“ (`data-edit-id`) to `partner-customer-list-page.js`; reuse the create dialog in edit mode (title „Endkunde bearbeiten“, prefilled via DOM properties, `readAddress`/`addressFieldsHtml`), existing pending/feedback/refresh logic and escaping; verify edit, cancel, validation error, double click, desktop 1280 px and mobile 390 px.

## 3. Completion

- [ ] 3.1 Run focused tests, full `./mvnw test`, `./mvnw package`, `node --check`, `openspec validate edit-partner-end-customers --strict`, `git diff --check`, `partnerportal-quality` and an independent security/Java review; fix findings and update traceability/handoff.
