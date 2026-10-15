## 1. Backend (essential)

- [ ] 1.1 Legal text fields on `CompanyProfile`, validation, missing-text indicator
- [ ] 1.2 Public legal endpoint per shop; placeholders when empty
- [ ] 1.3 `papo.environment` flag; release gate and production offline rule; tests

## 2. Frontend (essential)

- [ ] 2.1 Company profile section "Rechtstexte für Webshops"
- [ ] 2.2 Safe restricted-Markdown renderer; legal system pages; placeholder marking
- [ ] 2.3 Fixed shop footer
- [ ] 2.4 Checkout: AGB checkbox, withdrawal and privacy notices
- [ ] 2.5 Consent banner, settings dialog, revocation, `hasConsent` API

## 3. Verification

- [ ] 3.1 Tests + package; `git diff --check`
- [ ] 3.2 Browser: decline consent → no optional storage; keyboard operation of banner
- [ ] 3.3 Update traceability
