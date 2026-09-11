# Tasks

Alle Aufgaben sind spätere Umsetzung durch Chris; in dieser Planung bleiben Checkboxen offen. Keine Commit-/Push-/Merge-/Deploymentaktion.

## 1. Textabgleich (erforderlich)

- [ ] 1.1 **Ziel/Komponenten:** Bestehenden Frontenddiff bewahren und verbleibende sichtbare Strings in Frontend/Backendfehlern gezielt auf Artikel/Artikelkategorie bringen. **Voraussetzung:** Vorhandene Benutzeränderungen und exakte aktive Stringpfade prüfen. **Akzeptanz:** Navigation, Listen, Formulare, Fehler konsistent; keine sichtbare zusätzliche Artikel-ID. **Daten/Security:** Technische Keys/IDs/API/Headeraliases und Forschungsfrage unverändert. **Tests:** rg-Treffer einzeln klassifizieren; geänderte Backend-Stringassertionen fokussiert mit jeweiliger Testklasse ausführen. **DoD:** Nur sichtbare Texte und passende Erwartungswerte geändert; vorhandene Arbeit zugeordnet. **Stop:** Technische Umbenennung, Mapping-/Verhaltensänderung oder nicht trennbare fremde Arbeit.

## 2. Regression (erforderlich)

- [ ] 2.1 **Ziel/Komponenten:** Company-/Partner-Artikelflows, Import und Bestellung prüfen. **Voraussetzung:** 1.1. **Akzeptanz:** Artikel-/Kategoriebegriffe sichtbar; alte Importheader weiterhin akzeptiert; IDs weiterhin nutzbar. **Daten/Security:** Keine Datenmigration, synthetische Browserdaten. **Tests:** Browser 1440/390 einschließlich Fehler; cd backend && ./mvnw test bei Backendtextänderung; cd backend && ./mvnw package; openspec validate align-visible-article-terminology --strict; git diff --check. **DoD:** Exakte Checks sowie partnerportal-quality und unabhängiger Korrektheits-/Securityreview ohne Blocker. **Stop:** Test-/Browserfehler oder API-/Importkompatibilität verletzt.
