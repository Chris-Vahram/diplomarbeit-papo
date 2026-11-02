## Why

The company is the legal seller in every partner shop (decision 2026-10-15, `Specification/ENDKUNDENSHOP_DECISIONS.md` §2, §5). A public B2C shop in Austria needs an imprint, terms, privacy information, withdrawal information, gross prices, a clearly labelled order button and a real consent mechanism for anything beyond technically necessary storage. The team decided these legal parts must be implemented and must be in place before any shop is released in production.

## What Changes

- Company maintains legal texts (Impressum, AGB, Datenschutzerklärung, Widerrufsbelehrung incl. Muster-Widerrufsformular) in the company profile; shown on the fixed system pages of all its partners' shops.
- Fixed shop footer: links to the legal pages, "Verkauf durch <Unternehmen>", partner as contact, "Cookie-Einstellungen".
- Checkout: AGB acceptance checkbox with links, notice on withdrawal right and privacy information next to "Zahlungspflichtig bestellen".
- Consent banner with equally prominent "Alle akzeptieren" and "Nur notwendige", category details, revocation at any time via the footer; nothing non-essential is stored or sent before consent; consent decision stored locally with version and date.
- Placeholder legal texts are marked visibly; in deployments flagged as production a shop SHALL NOT be released while any legal text is missing or still a placeholder.
- Not included: drafting the actual legal texts (company delivers them), legal review.

## Capabilities

### New Capabilities

- `shop-legal`: Legal system pages, footer, checkout legal elements, release gate.
- `shop-consent`: Consent banner, categories, storage, revocation.

### Modified Capabilities

- `company-profile`: Company profile holds the shop legal texts.

## Impact

Backend: legal text fields on `CompanyProfile` (plain text with limited formatting), public legal endpoint, release check in the shop release action, deployment flag `papo.environment` (staging/production). Frontend: company profile editor section, shop system pages, footer, checkout additions, consent banner component. Depends on `add-partner-shop-foundation`, `add-shop-guest-checkout`. Required before `add-shop-statistics` (consent).
