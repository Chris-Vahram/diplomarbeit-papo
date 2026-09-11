## Why

Das Originalprotokoll vom 10.09.2026 fordert sichtbaren Endkundenbezug und eine klare Bestellprüfung. Aktuell bleiben Hauptüberschrift und „Ihr Warenkorb“ generisch; die Artikelvorschau steht vor dem Katalog.

## What Changes

- „Neue Bestellung für <Kunde>“ nach bestätigter Auswahl.
- Katalog links, rechts „Bestellung für <Kunde>“; mobil Katalog vor Bestellübersicht.
- Übersichtstext exakt „Bitte Bestellung überprüfen und kostenpflichtig aufgeben“.
- Bestehender finaler Bestellbutton und Bestellvertrag bleiben erhalten.

## Capabilities

### New Capabilities

Keine.

### Modified Capabilities

- `partner-order-wizard`: kundenbezogene Überschriften, Anordnung und Prüfungstext.

## Impact

`frontend/src/pages/partner-order-new-page.js` und seitenspezifisches Layout in `frontend/src/styles/base.css`. Keine API, Datenbank, Zahlung oder Provisionsänderung.

## Non-goals

Payment, Bestellkorrektur, echte E-Mail, Kundenpersistierung vor Abschluss und Neugestaltung gemeinsamer Komponenten.
