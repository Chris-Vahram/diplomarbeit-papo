## Why

Partner können Endkunden im Bestellwizard anlegen, aber nicht auf der eigenen Seite „Meine Endkunden“. Das widerspricht dem ausdrücklichen Wunsch aus `Documentation/Besprechungsprotokolle/Besprechungsprotokoll25_08_2026.md`, Abschnitt „Fachliches Feedback zur Partner-Sicht“, und ergänzt FR-10. Die gegenteilige Erledigt-Aussage in der Roadmap beschreibt nur die vorhandene Übersicht, nicht deren fehlende Anlagefunktion.

## What Changes

- Die Partnerseite „Meine Endkunden“ erhält einen Dialog zum Anlegen eines Endkunden.
- Der Dialog verwendet den bestehenden partner-scoped `POST /api/partner/customers`-Endpunkt; es entstehen keine neuen Backend-Endpunkte oder Datenfelder.
- Nach erfolgreichem Speichern schließt der Dialog, setzt Suche und Filter zurück und zeigt den angelegten Endkunden auf Seite 1 mit „Noch keine Bestellung“. Dies wurde von Chris in der Planungssitzung ausdrücklich bestätigt.
- Speichern, Listenaktualisierung und unklarer Transportausgang erhalten getrennte Rückmeldungen. Ein erneuter Listenabruf darf keine weitere Anlage auslösen.
- Pflichtfelder bleiben Nachname und vollständige Adresse; Vorname, E-Mail und Telefon bleiben optional.

## Capabilities

### New Capabilities

Keine.

### Modified Capabilities

- `customer-management`: Die Partner-Endkundenübersicht ermöglicht zusätzlich die unabhängige Anlage eines eigenen Endkunden.

## Impact

- Frontend: `frontend/src/pages/partner-customer-list-page.js`; bestehende Formular-, Adress-, Tabellen- und API-Helfer wiederverwenden. Nur notwendige, seitenspezifische Dialogregeln in `frontend/src/styles/base.css` ergänzen; bestehende Designregeln erhalten.
- Bestehende API: `POST /api/partner/customers`; keine Änderung an Rollen, Datenmodell, URLs oder Backend-Verhalten.
- Tests: `KundeResourceTest` erweitern und `KundeServiceTest` als Regressionsbasis nutzen. Kein Realm-, Login-, Secret- oder Infrastrukturumbau.
