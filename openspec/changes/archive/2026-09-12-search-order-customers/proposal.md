## Why

Das Originalprotokoll vom 10.09.2026 verlangt Kundensuche und einen klaren Neuanlagebutton im Bestellwizard. Aktuell erfolgt die Auswahl über Radiofelder und eine ungefilterte Select-Liste.

## What Changes

- Partnerwizard: lokale Suche über eigene Kunden, bewusste Auswahl und „Neuen Kunden anlegen“.
- Name und Adresse unterscheiden gleichnamige Kunden.
- Neuer Kunde bleibt Entwurf im vorhandenen Wizard bis zum Bestell-POST; bestehende Schritte und Warenkorb bleiben erhalten.

## Capabilities

### New Capabilities

Keine.

### Modified Capabilities

- `partner-order-wizard`: suchbare Endkundenauswahl mit eigenem Neuanlagebutton.

## Impact

`frontend/src/pages/partner-order-new-page.js`; bestehende Helfer in `frontend/src/lib/order-form.js` und Adressmodule werden weiterhin für gemeinsame Bestelldarstellungen genutzt. Bestehende Kunden-GET-/Bestell-POST-Verträge unverändert.

## Non-goals

Serverseitige Suche, CRM, Kundendublettenabgleich, vorzeitiger Kunden-POST, Company-Wizardumbau und Kundenbearbeitung.
