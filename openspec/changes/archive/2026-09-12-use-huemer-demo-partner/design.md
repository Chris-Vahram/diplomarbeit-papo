## Context

`DemoDataSeeder.seedPartners()` erzeugt in dev/test `PARTNER-DEMO`; in NORMAL läuft kein Seed. Vorhandene Daten werden bereits durch den Seed-Guard erhalten.

## Goals / Non-Goals

Reproduzierbarer synthetischer Firmenname bei frischem lokalem Start. Keine zweite Partnerfirma oder Company anlegen.

## Decisions

- Nur den Namen beim bestehenden `PARTNER-DEMO`-Seed auf „Huemer GmbH“ ändern. `PARTNER-DEMO` bleibt unverändert, damit aktuelle Loginbindung und Beziehungen intakt bleiben.
- Alle anderen Seedwerte, Anzahl der Partner und Verknüpfungen erhalten. Keine Zugangsdaten-/Realmdateien lesen oder ändern.
- Bestehende Tests nach dem alten Firmennamen durchsuchen; nur synthetische Erwartungswerte anpassen. In der vorhandenen Partner-Testklasse eine Abfrage nach stabiler Partnernummer ergänzt die Namensassertion. Test darf nicht von Reihenfolge wechselnder IDs abhängen.
- Ein früherer NORMAL-/Demobestand wird nicht automatisch umbenannt. Das ist keine fehlende Seedfunktion und erfordert gegebenenfalls einen getrennten autorisierten Datenauftrag.

## Risks / Trade-offs

Tests können Namen statt Kennungen zur Auswahl verwenden. Relevante Assertions prüfen, ohne Tests pauschal zu ersetzen.

## Migration Plan

Keine Bestandsmigration. Frischer isolierter Testdatenbestand verwendet neuen Namen; bestehende Daten werden nicht zurückgesetzt. Kein produktiver Seed.
