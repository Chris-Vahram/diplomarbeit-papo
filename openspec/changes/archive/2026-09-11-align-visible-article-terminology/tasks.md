# Tasks

Alle Aufgaben sind spätere Umsetzung durch Chris; in dieser Planung bleiben Checkboxen offen. Keine Commit-/Push-/Merge-/Deploymentaktion.

## 1. Textabgleich (erforderlich)

- [x] 1.1 **Ziel/Komponenten:** Bestehenden Frontenddiff bewahren und verbleibende sichtbare Strings in Frontend/Backendfehlern gezielt auf Artikel/Artikelkategorie bringen. **Voraussetzung:** Vorhandene Benutzeränderungen und exakte aktive Stringpfade prüfen. **Akzeptanz:** Navigation, Listen, Formulare, Fehler konsistent; keine sichtbare zusätzliche Artikel-ID. **Daten/Security:** Technische Keys/IDs/API/Headeraliases und Forschungsfrage unverändert. **Tests:** rg-Treffer einzeln klassifizieren; geänderte Backend-Stringassertionen fokussiert mit jeweiliger Testklasse ausführen. **DoD:** Nur sichtbare Texte und passende Erwartungswerte geändert; vorhandene Arbeit zugeordnet. **Stop:** Technische Umbenennung, Mapping-/Verhaltensänderung oder nicht trennbare fremde Arbeit.

## 2. Regression (erforderlich)

- [x] 2.1 **Ziel/Komponenten:** Company-/Partner-Artikelflows, Import und Bestellung prüfen. **Voraussetzung:** 1.1. **Akzeptanz:** Artikel-/Kategoriebegriffe sichtbar; alte Importheader weiterhin akzeptiert; IDs weiterhin nutzbar. **Daten/Security:** Keine Datenmigration, synthetische Browserdaten. **Tests:** Browser 1440/390 einschließlich Fehler; cd backend && ./mvnw test bei Backendtextänderung; cd backend && ./mvnw package; openspec validate align-visible-article-terminology --strict; git diff --check. **DoD:** Exakte Checks sowie partnerportal-quality und unabhängiger Korrektheits-/Securityreview ohne Blocker. **Stop:** Test-/Browserfehler oder API-/Importkompatibilität verletzt.

## Testinfrastruktur gelöst — 11.09.2026

Der lokale Keycloak-Dev-Service-Fehler wurde ohne Produktänderung behoben: Docker Desktop präsentierte Hostzugriffe mit einer öffentlichen Quell-IP, wodurch Keycloak für den Admin-Token HTTPS verlangte. Ein sicherer Frontend-Hostname mit dynamischem Backchannel, passendem Test-Issuer und 30-Sekunden-Webclient-Timeout hält den Realm-Import erreichbar. `JAVA_HOME=.../temurin-21.0.9/... ./mvnw test -Dtest=EnablementResourceTest,ProductResourceTest,OrderResourceTest` bestand mit 26/26 Tests. Task 1.1 bleibt bis zu ihren übrigen Akzeptanzkriterien und DoD offen.
