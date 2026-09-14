## Why

Four partner-area UI/UX rough edges were reported after using the live flow: dashboard trend arrows never render, the Endkunde search on "Neue Bestellung" gives no visual feedback, the article-step catalog/cart columns are sized backwards, and the wizard's navigation buttons carry unclear labels.

## What Changes

- Dashboard: the three trailing metric cards (Provision, Umsatz, Abgeschlossene Bestellungen) currently render an empty circular badge whenever no explicit period is picked, because no previous-period comparison exists without one. Add a fixed trailing-30-days-vs-prior-30-days comparison that always drives the trend arrow, independent of the picker. The big headline numbers stay the backend's all-time-exact totals (unchanged) — only the arrow/badge gets a comparison basis.
- Order wizard, "Endkunde wählen": replace the filtered `<select>` dropdown with a visible, click-to-select list of own Endkunden. The search field above it filters that list instantly as the partner types. Existing search-matching rules (name/address, case-insensitive, no auto-selection, "Keine Endkunden gefunden" empty state, keyboard operability) carry over unchanged — this is a presentation rebuild, not a new search behavior.
- Order wizard, "Artikel wählen": swap the catalog/cart-preview column-width split so the article catalog gets the majority of the row width and the cart preview gets the minority (currently the reverse: catalog fixed at 21rem, cart preview takes the rest).
- Order wizard buttons: rename "Vorheriger Schritt" → "Zurück" and "Nächster Schritt" → "Weiter" on the step-navigation buttons (kunde, neuerKunde, produkte steps). The "Übersicht" step's final action stays "Bestellung aufgeben" (unchanged) and its "Vorheriger Schritt" button also becomes "Zurück".
- Settings ("Mein Profil"): rename the read-only field label "Partnernummer" → "Benutzername".

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `partner-dashboard`: metric cards without an explicit period now show a trend arrow derived from a fixed trailing-30-days-vs-prior-30-days comparison, instead of no trend at all. Headline totals remain all-time-exact and unaffected.

## Impact

- Frontend only: `frontend/src/pages/partner-dashboard-page.js`, `frontend/src/lib/dashboard-data.js`, `frontend/src/pages/partner-order-new-page.js`, `frontend/src/styles/base.css`, `frontend/src/pages/partner-profile-page.js`.
- No backend, database or API changes. No changes to `partner-order-wizard` or `partner-order-cart` spec requirements — the search rebuild and column-ratio swap keep existing locked requirements (searchable-selection scenarios, "Catalog precedes order panel") intact; only presentation changes.
- Out of scope: company-side dashboard, any other wizard step content, real trailing-period totals shown elsewhere.
