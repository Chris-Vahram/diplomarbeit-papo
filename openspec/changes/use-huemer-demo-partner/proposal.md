## Why

Das Originalprotokoll vom 10.09.2026 benennt „Huemer GmbH“ als synthetische Testfirma. Der lokale dev/test-Seed verwendet für den Demo-Partner noch „Elektro Huber GmbH“.

## What Changes

- Bei frischem dev/test-Seed erhält der Partner `PARTNER-DEMO` den Namen „Huemer GmbH“.
- Bestehende Kennung, Aktivstatus, Provision und Beziehungen bleiben erhalten.
- Keine automatische Änderung vorhandener Demo-/Produktivdaten.

## Capabilities

### New Capabilities

- `demo-partner-data`: reproduzierbarer synthetischer Firmenname für den lokalen Demo-Partner.

### Modified Capabilities

Keine.

## Impact

`backend/src/main/java/at/papo/partnerportal/seed/DemoDataSeeder.java` und gezielte Testassertion. Kein Realm-, Zugangsdaten-, Schema- oder Produktionszugriff.

## Non-goals

Neue Company-Entität, neue Partnerkonten, Authmigration, Präsentationsgestaltung und Änderung vorhandener Datenbanken.
