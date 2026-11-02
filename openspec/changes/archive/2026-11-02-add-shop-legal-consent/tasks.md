## 1. Backend (essential)

- [x] 1.1 Legal text fields on `CompanyProfile`, validation, missing-text indicator
- [x] 1.2 Public legal endpoint per shop; placeholders when empty
- [x] 1.3 `papo.environment` flag; release gate and production offline rule; tests

## 2. Frontend (essential)

- [x] 2.1 Company profile section "Rechtstexte für Webshops"
- [x] 2.2 Safe restricted-Markdown renderer; legal system pages; placeholder marking
- [x] 2.3 Fixed shop footer
- [x] 2.4 Checkout: AGB checkbox, withdrawal and privacy notices
- [x] 2.5 Consent banner, settings dialog, revocation, `hasConsent` API

## 3. Verification

- [x] 3.1 Tests + package; `git diff --check`
- [x] 3.2 Browser: decline consent → no optional storage; keyboard operation of banner
- [x] 3.3 Update traceability
