# Tasks

Alle Aufgaben sind spätere Umsetzung durch Chris; in dieser Planung bleiben Checkboxen offen. Keine Commit-/Push-/Merge-/Deploymentaktion.

## 1. Seed (erforderlich)

- [x] 1.1 **Ziel/Komponenten:** DemoDataSeeder.seedPartners und passende Partner-Testassertion auf synthetischen Namen ausrichten. **Voraussetzung:** Bestehenden PARTNER-DEMO-Datensatz und Namensreferenzen prüfen. **Akzeptanz:** Frischer dev/test-Seed nennt genau diesen Partner Huemer GmbH; Nummer/Beziehungen und Seed-Guard bleiben. **Daten/Security:** Keine Realmdatei, Zugangsdaten, Bestands- oder Schemapflege. **Tests:** cd backend && ./mvnw -Dtest=PartnerResourceTest test; Assertion nach stabiler Partnernummer. **DoD:** Namensassertion erfolgreich; kein zusätzlicher Partner angelegt. **Stop:** Bestehende Daten müssten gelöscht/umbenannt oder Login geändert werden.

## 2. Regression (erforderlich)

- [x] 2.1 **Ziel/Komponenten:** Seed-Gating und betroffene Namensassertionen abschließend prüfen. **Voraussetzung:** 1.1. **Akzeptanz:** NORMAL seedet nicht; bestehende Datensätze bleiben unverändert; lokale Demo zeigt korrekten Namen. **Daten/Security:** Synthetische Tests, kein externer Zugriff. **Tests:** cd backend && ./mvnw test; cd backend && ./mvnw package; openspec validate use-huemer-demo-partner --strict; git diff --check. **DoD:** Ergebnisse und Korrektheits-/Securityreview dokumentiert. **Stop:** Testfehler, Änderung an Auth-/Produktivdaten nötig.
