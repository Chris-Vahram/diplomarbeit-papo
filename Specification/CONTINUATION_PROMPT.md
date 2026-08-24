# Continuation Prompt — Partnerportal (Stand 2026-08-24)

Diese Datei ist der operative Einstiegspunkt für eine neue Claude-/Codex-Session. Zuerst `CLAUDE.md` lesen, dann diese Datei vollständig lesen. Flüchtige Angaben anschließend immer mit `git status`, `git log` und `openspec list` verifizieren.

```text
Du setzt die Arbeit am PAPO-Partnerportal fort. Arbeite im Repository
/Users/mitgl/Desktop/Schule/4CHITM/Diplomarbeit/diplomarbeit-papo.

Lies zuerst CLAUDE.md und danach diese Datei vollständig. Behandle die stabilen Produktregeln in CLAUDE.md, das FSD, den Phase-2-Backlog und neuere Besprechungsprotokolle als Source of Truth. Verifiziere Git-, Branch-, Test- und OpenSpec-Status, weil diese Angaben nach dem 24.08.2026 weiterlaufen können.

# Projekt und Zuständigkeiten

- PAPO ist der Projekt-/Produktname, nicht der Name des realen Kunden.
- Reales internes B2B-Partnerportal: Company -> Partner -> Endkunde.
- Chris verantwortet Backend und funktionales Frontend. Tobias verantwortet visuelles Frontenddesign sowie seinen CI/CD-/Deployment-Change.
- Chris führt Commit, Push und Merge selbst aus. Ohne explizite Freigabe nichts committen, pushen, mergen oder deployen.
- Für nicht-triviale Änderungen OpenSpec verwenden: propose -> review -> apply -> validate -> separate regression review -> archive/spec sync.
- Keine Frameworks oder Node-Toolchain im Frontend: HTML/CSS/JavaScript, native Web Components. Backend: Java 21, Quarkus, Panache, PostgreSQL, Keycloak.
- Die aktuelle Forschungsfrage ist vom Betreuer bestätigt; exakter Wortlaut in FSD §13.

# Unmittelbar wichtiger Workspace-Stand

Stand beim Schreiben:

- Branch: `main`; lokale Commits vor dem Weiterarbeiten mit `origin/main` abgleichen.
- Batch-A-Fix-Commit: `e3baab4`
- Batch-B-Fix-Commit: `540d6fa`
- Batch-B-Regression-Review-Commit: `3b24ffc`
- Batch-B-Archiv-/Spec-Sync-Commit: `b38dfe5`
- Audit Fix Batches A und B sind gemergt und erfolgreich verifiziert.
- Batch B ist nach `openspec/changes/archive/2026-07-19-fix-pre-termin-order-and-dashboard/` archiviert; die Haupt-Specs `company-dashboard`, `order-management` und `partner-dashboard` sind synchronisiert.
- Aktueller verifizierter Backend-Teststand: 77 Tests, 0 Failures, 0 Errors, 0 Skipped.
- B-08 ist als STUB dokumentiert: client-seitiger Vorperiodenvergleich im Partner-Dashboard vorhanden; vollständige unternehmensweite Trendanalyse bleibt Phase 2.

# Letzte Kundenbesprechung und daraus entstandene Arbeit

Letzte dokumentierte Kundenbesprechung: 10.07.2026, 11:00 Uhr (`Documentation/Besprechungsprotokolle/Besprechungsprotokoll10_07_2026.txt`). Das MVP und die Live-Demo kamen gut an. Kundenprioritäten: einfache Benutzerführung, gute mobile Bedienung, sichtbar länger inaktive Endkunden, bessere Produktauswahl, Bestellfilter sowie häufigeres reales Durchtesten. Importdaten/Vorgehen sollten innerhalb von 1–2 Wochen nachgereicht werden. Hetzner-Hosting wurde angeboten.

Seit dieser Besprechung wurden folgende Arbeiten umgesetzt:

## Bestellvorgang

- Durchsuchbare Produktauswahl im gemeinsamen Bestellformular.
- Live-Gesamtwert und Provisionsvorschau mit Integer-Cent-Berechnung.
- Partner-Bestellseite als durchsuch-/filterbarer Produktkatalog mit Produktkarten, Bildern, Warenkorb, Mengensteuerung und Live-Summen.
- Doppelte Partner-Bestellmodale aus Dashboard und Bestellliste entfernt; beide navigieren zur kanonischen Bestellseite.
- Mengenwerte auf ganze Zahlen 1..10000 begrenzt.
- Double-Submit-Schutz auch bei Warenkorb-Re-Render während eines laufenden Requests.
- Bestehende Endkunden werden nicht mehr still vorgewählt. Der Nutzer muss ausdrücklich wählen; fehlende Auswahl zeigt `Bitte Endkunden wählen.`. Der gemeinsame Flow ist für Partner- und Company-Bestellungen geprüft.
- Partner-Bestellhistorie liefert neueste Bestellung zuerst (`id desc`) bei unverändertem Partner-Scoping.

## Listen, Endkunden und Partnerverwaltung

- Company-Bestellliste: Partner- und Endkundenfilter plus gemeinsamer Filter-Reset.
- Partner-Bestellliste: Endkundenfilter plus Filter-Reset.
- Shared `data-table` unterstützt Textfilter.
- Partnerseite `Meine Endkunden` mit `lastOrderDate`, älteste zuerst, Inaktiv-Badge und Inaktivfilter; Arbeitswert 60 Tage. Backend als gruppierte Left-Join-Abfrage ohne N+1, Grenzen 59/60/61 Tage getestet.
- Separates Feld `keycloakUsername` vollständig entfernt. `Partner.partnerNumber` ist die Login-Zuordnung, wird per `Locale.ROOT` kanonisch großgeschrieben und bei manueller Anlage, Änderung und CSV-Import gleich behandelt.

## Auth, Darstellung und Beleg

- Keycloak Refresh-Token-Flow hält Company-/Partner-Sessions aktiv.
- Plus Jakarta Sans und Fira Code werden lokal ausgeliefert; kein Google-Fonts-CDN zur Laufzeit.
- Druckbarer Auftragsbeleg für Bestellbestätigung und Detaildialog. Native Browser-Druckfunktion, sauberer `#print-root`, lange mehrseitige Bestellung geprüft; Chromium und Firefox verifiziert.

## Audit-Fixes

- UX-001: Reload/Deep-Link-Race behoben; Router wartet vor dem ersten Render auf Custom-Element-Registrierung.
- UX-002: Warenkorb-Produktnamen kollidieren nicht mehr mit dem Preis; Desktop und Mobile geprüft.
- UX-003: bewusste Endkundenauswahl mit deutscher Pflichtfeldvalidierung.
- UX-004: neue Partnerbestellung erscheint oben.
- TECH-001: Dashboard-Datumsgrenzen verwenden lokale Kalenderbestandteile statt UTC-`toISOString`; Partner- und Company-Dashboard in Europe/Vienna geprüft.
- UX-001..004 und TECH-001 stehen nach separaten Regression Reviews auf `Verified`.

## Audit, Specs und Dokumentation

- Besprechungsprotokoll, FSD, Full-Vision-Dokument und Phase-2-Backlog wurden nach dem Meeting aktualisiert.
- Vollständige Audit-Suite erstellt: Projektüberblick, Scope Review, Technical Review, UX Review, Findings Register und Final Action Plan.
- Browser-Audit beider Rollen bei Desktop und Mobile inklusive Druckpfad; 29 Evidence-Screenshots.
- Batch A wurde implementiert, separat regression-reviewed, archiviert und in die Haupt-Specs synchronisiert.
- Batch B wurde implementiert, separat regression-reviewed, archiviert und in die Haupt-Specs synchronisiert (`b38dfe5`).
- Produktbild-Change und drei ältere Hardening-Changes wurden archiviert; mehrere weitere fertige Changes sind noch aktiv und Teil von HYG-001.
- MVP-Präsentation, QA-/Scope-Audit-Prompt, Codex-Projektregeln und projektspezifischer `partnerportal-quality`-Review-Skill wurden ergänzt.

# Verifizierter Qualitätsstand

Letzte separate Batch-B-Regression Review:

- fokussierter Test `OrderResourceTest#partnerSeesNewestOwnOrdersFirst`: 1/1 grün;
- kompletter Backend-Testlauf über den Compose-Keycloak-Pfad: 77 Tests, 0 Failures, 0 Errors, 0 Skipped;
- Browserflows mit realem lokalem Dev-Stack für `partner` und `company` erfolgreich;
- Datum 11.02.2026 wurde in beiden Dashboards exakt und ohne UTC-Verschiebung ausgewertet;
- OpenSpec-Validierung und `git diff --check` erfolgreich.

Audit-Berichte:

- `docs/partner_portal_audit_suite/Audit/05_FINAL_ACTION_PLAN.md`
- `docs/partner_portal_audit_suite/Audit/06_FINDINGS_REGISTER.md`
- `docs/partner_portal_audit_suite/Audit/HISTORY/2026-07-18_REGRESSION_REVIEW_UX_RELOAD_CART.md`
- `docs/partner_portal_audit_suite/Audit/HISTORY/2026-07-19_REGRESSION_REVIEW_PRE_TERMIN_ORDER_DASHBOARD.md`

# Offene Arbeit in sinnvoller Reihenfolge

## 1. Lokale Commits veröffentlichen

Vor einem neuen Feature-Branch prüfen, ob `b38dfe5` und nachfolgende Dokumentations-Commits bereits nach `origin/main` gepusht wurden. Nicht erneut archivieren.

## 2. Kundendaten prüfen / Batch C

Prüfen, ob echte Partner-/Produkt-Importdateien oder verbindliche Spaltenformate inzwischen eingetroffen sind. Falls ja, einen kleinen OpenSpec-Change für Batch C erstellen:

- finales Spalten-Mapping gegen Echtdaten;
- UX-005: Bestätigen bei fehlender Pflichtzuordnung deaktivieren und `0 importiert` nicht als grünen Erfolg melden;
- TECH-002: Import-Dedupe überall mit `Locale.ROOT`.

Ohne Echtdaten Batch C nicht spekulativ finalisieren.

## 3. Batch D / OpenSpec-Hygiene

`build-partnerportal-mvp` hat 50/53 Tasks. Offen sind:

- 8.3: UI/UX-Dokumentation der Screens, Shared Components und Flows — Tobias;
- 8.4: Diplomarbeits-Traceability FSD -> Implementierung -> Tests und dokumentierte B-01..B-08-Deferrals — Chris + Tobias;
- FD.4: visueller Polish der später ergänzten Screens (Produktbilder, Bestellfilter, Partnerkatalog/Warenkorb, Meine Endkunden, Auftragsbeleg) — Tobias.

Erst nach Klärung dieser Aufgaben `build-partnerportal-mvp` archivieren. Danach die übrigen abgeschlossenen Changes mit jeweiliger Delta-Spec-Prüfung archivieren, insbesondere `order-correctness-hardening`, `add-order-document-print`, `remove-keycloak-username-field`, `partner-order-cart`, `add-dormant-kunde-visibility`, `add-order-list-filters`, `improve-order-form`, `add-keycloak-session-refresh` und `self-host-fonts`.

Zusätzlich in Batch D:

- `docs/DATA_MODEL.md` bezüglich ProductImage prüfen/synchronisieren;
- SCOPE-001 ist erledigt: B-08 ist als STUB dokumentiert; der vorhandene Partner-Dashboard-Trend bleibt bestehen;
- INFRA-001: den funktionierenden lokalen Keycloak-Testpfad in `docs/SETUP.md` festhalten.

## 4. Späterer Polish

- Accessibility: Tastaturbedienung klickbarer Tabellenzeilen, zugängliche Namen für Filter/Datumsfelder, Bildergalerie-Labels/Selected-State.
- Mobile: sichtbare Tabellen-Scroll-Affordanz und kleinere Touch-/Chart-Probleme.
- Design-System-Konsistenz und Session-Logout-Hinweis.
- SEC-003 und ausgewählte High-Value-Testlücken.
- SEC-001/SEC-002 bleiben für das MVP bewusst deferred.

# Tobias / Deployment

`ci-cd-deployment-with-gh-actions` ist Tobias zugeordnet. Der aktive Change zeigt im Repository 0/40 Tasks und enthält teils alte Oracle-/ARM-Annahmen. Tatsächliche Zielumgebung ist eine bereits bereitgestellte Hetzner-x86_64-VM mit Docker/Compose. Nicht ohne ausdrückliche Anweisung übernehmen oder neu planen; Status zuerst mit Tobias abgleichen. Produktionsdomain ist weiterhin zu klären.

# Offene Kundenentscheidungen

- Echte Importdateien und verbindliche Spaltenformate.
- Inaktivitätsgrenze für Endkunden (aktuell 60 Tage).
- Produktionsdomain bzw. finaler Deployment-Zeitpunkt.

# Wichtige technische Regeln und bekannte Stolperstellen

- Geschützte REST-Methoden brauchen `@RolesAllowed`; Partneridentität immer über `CurrentPartner.resolve()`, niemals aus einer Client-Partner-ID.
- Geld im Backend als `BigDecimal`, im Frontend als Integer-Cents; vorhandene Helpers wiederverwenden.
- Alle UI-/Validierungstexte in korrektem Deutsch mit Umlauten.
- Bei Frontend-Reloadtests kann Navigation zur gleichen Hash-URL bfcache-bedingt ein No-op sein: zuerst `about:blank`, dann Ziel-URL.
- Ein deaktivierter Submit-Button genügt nicht, wenn ein Re-Render ihn ersetzt; Guard-Zustand am Komponentenobjekt halten.
- Formfelder außerhalb des `<form>` benötigen eigene Validierung; HTML-Attribute allein reichen dort nicht.
- Lange Druckdokumente real als mehrseitiges PDF prüfen; der `#print-root`-Ansatz verhindert Abschneiden.
- Quarkus-Dev startet DB neu und seedet. Testprofil seedet keine historischen Orders; Tests müssen eigene Fixtures erzeugen.
- Lokaler Standard-Testlauf kann durch Keycloak/Docker-Desktop-Netzwerkverhalten flaken. Der zuletzt erfolgreiche Pfad war:

  cd backend
  ./mvnw test \
    -Dquarkus.http.test-port=8091 \
    -Dquarkus.keycloak.devservices.enabled=false \
    -Dquarkus.oidc.auth-server-url=http://localhost:8081/realms/partnerportal \
    -Dquarkus.oidc.client-id=partnerportal-frontend

- Lokale App starten: Docker Compose für PostgreSQL/Keycloak, `cd backend && ./mvnw quarkus:dev`, anschließend `cd frontend && python3 -m http.server 3000`. URL: `http://localhost:3000/`; Demo-Logins `company-demo` bzw. `partner-demo`, Passwort jeweils identisch.

# Startcheck für jede neue Session

1. `git status --short --branch`
2. `git log -8 --oneline --decorate`
3. `openspec list --json`
4. Prüfen, ob lokale Dokumentations-/Archivierungs-Commits bereits auf `origin/main` liegen.
5. Danach Chris fragen, ob Kundendaten eingetroffen sind oder Batch D fortgesetzt werden soll.
6. Keine Produktivcodeänderung, kein Commit/Push/Merge/Deployment ohne passenden Auftrag.
```
