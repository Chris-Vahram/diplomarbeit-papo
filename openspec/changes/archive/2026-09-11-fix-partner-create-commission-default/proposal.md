## Why

Die Besprechung vom 25.08.2026 verlangt 5 % als Standard für neue Partner. `frontend/src/pages/company-partner-list-page.js` setzt in `openCreate()` weiterhin ausdrücklich 10 %. Chris hat die getrennte Planung dieser kleinen Korrektur bestätigt; sie benötigt keine Partnernummer-/Loginmigration.

## What Changes

- Das Formular „Neuer Partner“ wählt bei jedem Öffnen 5 % voraus.
- Bewusste Auswahl von 5/10/15 % oder individuellem Wert bleibt möglich.
- Bearbeiten zeigt weiterhin den gespeicherten Wert. API, Import, bestehende Partner und historische Provisionen erhalten keinen neuen Default.

## Capabilities

### New Capabilities

Keine.

### Modified Capabilities

- `partner-management`: Standardvorauswahl bei manueller Partneranlage.

## Impact

Eine Produktzeile in `frontend/src/pages/company-partner-list-page.js` bei späterer Umsetzung. Keine neuen Endpunkte, DTOs, Dependencies, Schema- oder Datenänderungen. Prüfung über bestehende Browserwerkzeuge; keine neue Testbibliothek.
