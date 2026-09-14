# PAPO Restarbeitsplan – 09.09.2026

## Vorrangiger Next-Session-Stand — 14.09.2026

Branch `docs/openspec-planning-2026-09-11` ist auf `38cd932` mit Remote synchron; vor dem aktuellen lokalen Fix war der Arbeitsbaum sauber. Gateway lokal geprüft (Harness 18/18), aber `harden-deployment-gateway` bleibt bei **2/4**, weil VM-, Key-, sudoers-, Host-Key-, Remote-Negativtest- und Rollbacknachweise fehlen. Keine VM-, SSH-, Secret- oder Datenvolumenaktion.

Backend 100/100, Package, JS-Syntaxchecks, OpenSpec 2/2, Diff-Check und Präsentation mit 15 Folien sind belegt. Public-Live-Test erfolgreich: Partner-Login, Endkunde, zwei Artikel, Bestellung #52 über 218,90 € und Company-Statusänderung auf `Geprüft`. Next session zuerst Deployment auf den aktuellen lokalen Stand, dann delegierte VM-Security-Nachweise. Clockify als retrospektiv geschätzte Datum-/Uhrzeit-/Beschreibungseinträge vorbereiten und deutlich als Schätzung markieren.

## Orchester-Fortschreibung — 14.09.2026

Kategorie-Navigation umgesetzt, geprüft und archiviert. `origin/main` und die aktiven Branches wurden geprüft; der aktuelle Arbeitsbranch braucht keinen Pull. Rest: Gateway-Nachweise, Thesis-Doku, BMD-geführte Partnerstammdaten, generische Steuerkennung, Company-Notiz, Gewerbeschein und eindeutige BMD-Artikelnummer/Import-/Exportregel. Diese Punkte in der nächsten Session mit höherem Modell, OpenSpec und Caveman planen; noch nichts implementieren. Clockify-Einträge diesmal konkret ausgeben.

Die älteren Abschnitte darunter sind historische Arbeitsstände; bei Widerspruch gilt dieser Block, `openspec list --json` und die aktuellen `tasks.md`.

## Fortschreibung — 13.09.2026

`build-partnerportal-mvp` ist **53/53 technisch abgeschlossen**. Allgemeine Diplomarbeitsdokumentation läuft getrennt als `complete-thesis-documentation` (0/6); aktuelle FD.4-Abdeckung ist beendet, neue Screens tragen eigenen Polish. Kleine UI-Planung `integrate-category-management-into-article-management` (0/4) bündelt Kategorien unter Company-„Artikelverwaltung“ ohne Backendänderung.

CI/CD ist **34/45**: GHCR privat, Branch Protection, DNS und HTTPS belegt. P0-Ursache: Passwortdrift zwischen persistentem PostgreSQL-User und `.env.prod`, dadurch Keycloak/Backend-Neustartschleifen und 502. Nächster Schritt: lokale Dumps, Passwort geheim angleichen, Services/Flows prüfen. Julian-Write-Zugriff klären; Gateway `48ad4d8` mit aktuellem Releasepfad versöhnen; Cron/lokalen Backupzyklus danach. Offsite bleibt post-MVP.

## Abschlussstand nach Review und Archivierung — 12.09.2026

`define-mvp-traceability-format` (**3/3**) und `manage-article-categories` (**6/6**) sind vollständig geprüft und archiviert: `openspec/changes/archive/2026-09-12-define-mvp-traceability-format` bzw. `openspec/changes/archive/2026-09-12-manage-article-categories`. Für Kategorien sind 27 fokussierte Backendtests, 100 vollständige Tests, Package, JavaScript-Syntaxchecks, Playwright-Desktop-/Mobilefälle, Strict-Validate, Diff-Check und die Abschlussreviews ohne Findings belegt; für Traceability die Dokumentations-/Link-/Strukturprüfungen.

Die OpenSpec-Liste enthält danach nur noch `build-partnerportal-mvp` (50/53) und `ci-cd-deployment-with-gh-actions` (30/41). MVP offen: 8.3 Tobias UI/UX-Dokumentation, 8.4 gemeinsame Diplomarbeits-/Traceability-Ausfüllung, FD.4 Tobias-Polish der später hinzugekommenen Screens. CI/CD bleibt **BLOCKIERT**: 3.5/3.6, 6.1–6.4, 7.5/7.6a und post-MVP 7.2/7.3/7.6b; zusätzlich fehlen aktuelle Nachweise zu P0-Ursache/Laufzeit, Frontend-Archiv-/Recoverypfad und PR-#5-Zugriffsgrenzen. Keine Umsetzung dieser beiden Changes begonnen.

## Abschlussnachtrag define-mvp-traceability-format — 12.09.2026

`define-mvp-traceability-format` ist **3/3 fertig**, bleibt aber gemäß Auftrag unarchiviert. `docs/REQUIREMENTS_TRACEABILITY.md` enthält 33 eindeutige FSD-/Backlog-Zeilen, acht Spalten, Legende und geprüfte relative Links; `docs/DOCUMENTATION_TODO.md` verweist auf das Format und trennt technische, UI-/UX- und gemeinsame Abnahmeverantwortung. Offene Code-/Testzuordnungen sowie 8.3/8.4/FD.4 wurden nicht als erledigt dargestellt. Quellen-/Strukturcheck, Strict-Validate, Diff-Check und Funktions-/Evidenz-/Securityreview ohne Findings. Nächster Schritt nur bei neuer Freigabe; CI/CD/MVP nicht spekulativ beginnen.

## Abschlussnachtrag manage-article-categories — 12.09.2026

`manage-article-categories` ist **6/6 fertig**, bleibt aber gemäß Auftrag unarchiviert. Kategorie-Datenmodell/Migration, Company-Verwaltung, geschützte Product-Zuordnung, Importvalidierung sowie Company-UI sind umgesetzt. Nachweise: fokussiert 27/27, Full-Test 79/79, Package, Strict-Validate, Diff-Check, Browser 1440/390 und Qualitäts-/Funktions-/Sinnhaftigkeits-/Securityreview ohne Findings. Nächster zulässiger Change: `define-mvp-traceability-format`; CI/CD/MVP bleiben blockiert bzw. außerhalb des Arbeitsplans.

## Arbeitsstand nach Auto-Compact — 12.09.2026

Der laufende Implementierungsblock ist `manage-article-categories` bei **3/6 Tasks**: 1.1 Datenbasis, 1.2 ORM-Migration/Seed-Gate und 2.1 Company-Verwaltung/Locking sind abgehakt. Die letzte Änderung an `ImportDefinition`, `ImportService` und `ProductImportDefinition` ist vorhanden, aber noch nicht nach dem Arbeitsstopp fokussiert geprüft; 2.2 bleibt daher offen. Als nächstes den stale-Entity-Refresh nach Kategorie-Lock verifizieren/korrigieren, dann Importfälle testen. Danach folgen 3.1 UI und 4.1 Abschluss. Keine Archivierung, kein Commit/Push/Merge/Deployment. Die vier vorherigen Changes sind archiviert; ihre Nachweise und der erhaltene Vorlauf in `partner-order-new-page.js` bleiben maßgeblich. Dieser Nachtrag ersetzt ältere Statusangaben weiter unten.

## Arbeitsstopp — 11.09.2026

Vorrang vor den historischen Prioritäten darunter. Der Arbeitsbaum auf `docs/openspec-planning-2026-09-11` ist uncommittet und muss unverändert bewahrt werden.

| Change | Realer Stand | Nächste nötige Arbeit |
|---|---|---|
| `show-partner-company-name` | **3/3 Tasks fertig**, archiviert unter `openspec/changes/archive/2026-09-12-show-partner-company-name`; Anzeige, Fehler/Retry, Escaping, Render-/Sessionguards und Basislayout umgesetzt; Package 79/79, Strict-Validate/Diff-Check und Browser-/Reviews grün | Nicht erneut umsetzen; nächster Change ist `use-huemer-demo-partner` |
| `use-huemer-demo-partner` | **2/2 Tasks fertig**, archiviert unter `openspec/changes/archive/2026-09-12-use-huemer-demo-partner`; bestehender `PARTNER-DEMO`-Seed auf `Huemer GmbH`, Nummer/Beziehungen/Seed-Gating unverändert; Fokus 12/12, Suite/Package 79/79, Validate/Diff-Check, Browser- und Reviews grün | Nicht erneut umsetzen; nächster nicht blockierter Change ist `manage-article-categories` |
| `search-order-customers` | **3/3 Tasks fertig**, archiviert unter `openspec/changes/archive/2026-09-12-search-order-customers`; Such-/Auswahl-/Entwurfslogik und Regression geprüft; Package 79/79, Strict-Validate/Diff-Check grün | Nicht erneut umsetzen; nächster Change ist `clarify-order-customer-context` |
| `clarify-order-customer-context` | **3/3 Tasks fertig**, archiviert unter `openspec/changes/archive/2026-09-12-clarify-order-customer-context`; Kundenkontext, Prüfungstext, DOM-/Gridreihenfolge und Orderregression geprüft; Package 79/79, Strict-Validate/Diff-Check und Reviews grün | Nicht erneut umsetzen; nächster Change ist `show-partner-company-name` |

Kein Commit. `search-order-customers`, `clarify-order-customer-context`, `show-partner-company-name` und `use-huemer-demo-partner` wurden nach Prüfung archiviert. Zwei untracked Browserbilder (`output/playwright/show-partner-company-name-desktop.png`, `output/playwright/show-partner-company-name-mobile.png`) erhalten. KI-Evidence-Sync/Enrich wurde mit echten Checks ausgeführt; keine zusätzlichen Nachweise behaupten.

## Fortschreibung — 12.09.2026

`search-order-customers` ist mit **3/3 Tasks fertig** und unter `openspec/changes/archive/2026-09-12-search-order-customers` archiviert. Nach dem Review-Fix prüft der Wizard die lokale Kunden-ID-Mitgliedschaft zusätzlich. Browser 1440/390 belegte >20/0 Treffer, Umlaute, Tastaturauswahl, Filter-/Kundenwechsel, Warenkorb-/Payload-Erhalt, Draftfortsetzung, Abbruch sowie Partner-/Company-Regression mit Mehrartikeln und Fehler-/Erfolgspfaden. Die Erfolgspfade nutzten temporäre Playwright-Mocks; keine echten Testbestellungen wurden persistiert. `node --check`, `cd backend && ./mvnw package` (79/79), `openspec validate search-order-customers --strict`, `git diff --check`, `partnerportal-quality` sowie Funktions-/Sinnhaftigkeits-, Security- und unabhängiger Abschlussreview sind ohne Blocker belegt.

`clarify-order-customer-context` ist mit **3/3 Tasks fertig** und unter `openspec/changes/archive/2026-09-12-clarify-order-customer-context` archiviert. Kundenüberschriften, exakter Prüfungstext, Katalog-vor-Bestellpanel, responsive DOM-Reihenfolge und unveränderter Ordervertrag sind mit Playwright 1440/390 einschließlich Mengen-/Summen-, Mock-POST-, Safe-Text- und Resetfällen belegt. `node --check`, `cd backend && ./mvnw package` (79/79), `openspec validate clarify-order-customer-context --strict`, `git diff --check`, `partnerportal-quality` sowie Funktions-/Sinnhaftigkeits-, Security- und unabhängiger Abschlussreview sind ohne Blocker belegt.

`show-partner-company-name` ist mit **3/3 Tasks fertig** und unter `openspec/changes/archive/2026-09-12-show-partner-company-name` archiviert. Portal- und Dashboardlabel, Retry, Logout-/Navigations-Races, Sitzungsrotation und Mobile-Wrap sind mit Playwright 1440/390 einschließlich synthetischer Profilantworten und echter Huemer-Demo belegt. `node --check`, `cd backend && ./mvnw package` (79/79), `openspec validate show-partner-company-name --strict`, `git diff --check`, `partnerportal-quality` sowie Funktions-/Sinnhaftigkeits-, Security- und unabhängiger Abschlussreview sind ohne Blocker belegt. Nächster Change: `use-huemer-demo-partner`.

`use-huemer-demo-partner` ist mit **2/2 Tasks fertig** und unter `openspec/changes/archive/2026-09-12-use-huemer-demo-partner` archiviert. Der bestehende `PARTNER-DEMO`-Seed verwendet `Huemer GmbH`; Partnernummer, Beziehungen und Seed-Gating blieben erhalten. `PartnerResourceTest` fokussiert 12/12, vollständige Suite und Package 79/79, Strict-Validate, Diff-Check, lokaler Browsernachweis sowie Projekt-, Funktions-/Sinnhaftigkeits- und Securityreview sind ohne Blocker belegt.

Nächster Change: `manage-article-categories`. Die archivierten Kundenkontext-, Firmenanzeige- und Seedänderungen bewahren. Kein Commit, Push, Merge oder Deployment.

## Fortschreibung nach Originalprotokollen — 11.09.2026

Diese Fortschreibung hat Vorrang vor den historischen Tabellen darunter. [Kanonischer Ergebnisreport](../openspec/PLANNING_REVIEW_2026-09-11.md) enthält Status je Change, genaue offene Entscheidungen, Reviews und tatsächlich ausgeführte Checks.

## Aktueller Umsetzungspunkt — 11.09.2026

`add-standalone-customer-creation`: **6/6 Tasks fertig**, unter `openspec/changes/archive/2026-09-11-add-standalone-customer-creation` archiviert. Lokale Endkundenanlage, partner-scoped Persistierung, Pending-/Fehler-/GET-only-Refreshschutz und Wiederverwendung im Bestellwizard sind umgesetzt. Fokustest 9/9, Gesamttest und Package jeweils 79/79, Strict-Validate und Diff-Check grün. Browser 1440/390 sowie Projekt-, Funktions- und Securityreview ohne Blocker. Nächster Change: `show-partner-company-name`.

`align-visible-article-terminology`: **2/2 Tasks fertig**, unter `openspec/changes/archive/2026-09-11-align-visible-article-terminology` archiviert. Sichtbare Frontendtexte und Backendfehler verwenden Artikel/Artikelkategorie; technische Namen, IDs, APIs und Importaliasnamen blieben unverändert. Java-21-Fokuslauf 26/26, vollständiges `./mvnw test` und `./mvnw package` jeweils 77/77 ohne Fehler. Browserprüfung 1440/390 deckte Company-/Partner-Artikel, Import und Bestellwizard einschließlich Fehlerpfaden ab; strict Validate, `git diff --check`, Projekt-, Funktions- und Securityreview ohne Blocker.

`fix-partner-create-commission-default`: **2/2 Tasks fertig**, unter `openspec/changes/archive/2026-09-11-fix-partner-create-commission-default` archiviert. Nur manuelle Neuanlage startet mit 5 %; API, Import, Berechnung und bestehende Daten sind unverändert. Browser 1440/390 belegt 5-%-POST, Abbruchreset, 10-%-Bearbeiten und gespeicherten individuellen 7,5-%-Wert. `./mvnw package` lief mit 77/77; Strict-Validate, Diff-Check sowie Projekt-/Korrektheitsreview ohne Findings. Nächster Change nach dem Commit: `show-partner-company-name`.

Sechs zusätzliche kleine OpenSpec-Changes vollständig geplant: Firmenanzeige, synthetischer Huemer-Seed, Kundensuche im Wizard, Kundenkontext/Anordnung, Kategorienverwaltung, Artikelsprache. Zusammen mit Endkundenanlage, 5-%-Vorauswahl und Traceability-Format ergeben sich neun Produkt-/Dokumentationspläne; MVP/CI-CD bleiben getrennt.

**Korrigierte Grundlage:** Kategorienverwaltung ist seit 25.08. gefordert und jetzt von Chris konkret bestätigt. Sie wartet nicht auf BMD-Artikelnummern. Unabhängige Endkundenanlage fehlt weiterhin im Produktcode. Kundenbearbeitung wurde im Original zusätzlich genannt und bleibt bis zur Altbestands-Snapshotentscheidung offen. Partnernummer wird im Profil bereits angezeigt; fehlend sind sichere Loginentkopplung und automatische Vergabe, keine reine UI-Korrektur.

**Bestätigte Entscheidungen:** vorhandener Partnername dient als Firmenname; Huemer GmbH synthetisch. Company legt Kategorien an/benennt um/deaktiviert; unbekannte Importkategorien werden abgelehnt. Login soll über Keycloak-ID gebunden werden, Bestandsnummern bleiben. Keine Mandantenbasis dafür vorziehen.

**Betrieb:** Lokaler Stand enthält Frontendreparaturmerge `fea0e5b`. Alte 404/502 sind historische Befunde; aktueller Betrieb nicht geprüft. Tobias liefert Betriebs- und Zugriffsgrenzennachweise; Chris übernimmt weder VM-Aufgaben noch Präsentationsgestaltung.

**Workspace:** eigener Planungsbranch `docs/openspec-planning-2026-09-11`, vorhandene Änderungen bewahrt, kein Fetch/Pull/Commit/Push/Merge. Die Terminologieänderung ergänzt sichtbare Frontend-/Backendtexte; fremde Frontend-/Präsentationsänderungen bleiben erhalten.

**Umsetzungsstand 11.09.2026:** Der Keycloak-Dev-Service-Blocker für `align-visible-article-terminology` ist gelöst. Ursache war die von Docker Desktop als öffentlich präsentierte Quell-IP; Testprofil-Hostname, dynamischer Backchannel, Issuer und Timeout wurden angepasst. Der Change ist 2/2 fertig und durch Fokus-/Gesamttests, Package, Browser, Strict-Validate und Reviews belegt; unter `openspec/changes/archive/2026-09-11-align-visible-article-terminology` archiviert, nicht committed.

**Ausführungsregel:** Die neun READY-Changes werden ausschließlich einzeln in der im [Next Session Plan](../Specification/NEXT_SESSION_PLAN.md) festgelegten Reihenfolge umgesetzt. Je Change sind OpenSpec-Status und -Apply-Kontext, strikte Taskreihenfolge mit sofortiger Checkbox, fokussierte Tests sowie danach Strict-Validate, passende Gesamtprüfung, Qualitäts-/Funktionsreview und bei Auth-/Daten-/Rollen-/Input-Scope Securityreview Pflicht. Bei Unklarheit wird der genaue Blocker dokumentiert, nicht geraten. Die ausgeschlossenen Themen und externen Blocker bleiben unverändert; kein Auto-Archivieren/Committen. Handoff-Dokumente enthalten nur echte Checks und am Ende lediglich Vorschläge mit exakten `git add`-Pfaden/Push-Befehl.

> **Fortschreibung 10.09.2026:** Die folgende Ergänzung hat Vorrang vor älteren `Geplant?`-Angaben weiter unten. Vollständiger Review: `../openspec/PLANNING_REVIEW_2026-09-10.md`. Der ursprüngliche Restarbeitsplan bleibt als Historie erhalten.

## OpenSpec-Planung abgeschlossen

Der genehmigte Plan „PAPO: OpenSpec-Planung abschließen“ wurde ausschließlich in OpenSpec/Dokumentation umgesetzt. Keine Produktimplementierung, kein Commit/Push/Merge/Deployment und kein VM-/Secret-/SSH-/Backup-/GitHub-Secrets-Zugriff.

| Change | Stand | Ergebnis |
|---|---:|---|
| `add-standalone-customer-creation` | 6/6 | FERTIG; nach Spec-Sync archiviert, nicht automatisch committed |
| `fix-partner-create-commission-default` | 2/2 | FERTIG; nach Spec-Sync archiviert, nicht automatisch committed |
| `define-mvp-traceability-format` | 0/3 | READY für Terra medium; Format/33 Quellenzeilen, kein Abschluss von MVP-8.4 |
| `build-partnerportal-mvp` | 50/53 | BLOCKIERT für Abschluss durch 8.3/FD.4 (Tobias) und gemeinsame 8.4-Ausfüllung |
| `ci-cd-deployment-with-gh-actions` | 30/41 | BLOCKIERT; P0-Ausfall, Frontendpfad und PR #5 benötigen getrennte Tobias-Nachweise |

### Endkundenchange

- Bestätigte Lücke: Übersicht kann Kunden nicht unabhängig anlegen.
- Bestehende Partner-API, Requestform und Eigentümerauflösung bleiben.
- Native Dialogbedienung, Pending-Guard, Filter-/Pagination-Sichtbarkeit, stale-load-Schutz und konservativer unbekannter Speicherstatus geplant.
- Kein zweiter Realm-Login. P1-HTTP/DB-Eigentümerprüfung plus fremde P2-Fixture und bestehender beidseitiger Servicetest.
- Pflichtchecks: Rollen, inaktiver Partner ohne Persistierung, alle Pflichtfelder, Escaping, Desktop/Mobile und Wizard-Wiederverwendung.

### Zwei kleine Folgechanges

- 5-%-Default: nur `openCreate()`; Edit, Customwert, API, Import, Bestand und historische Provisionen unverändert.
- Traceability: Matrix `ID | Quelle | Verhalten/Abgrenzung | Code | Test/Beleg | Implementierungsstatus | Prüfstatus/Datum/Revision | Zuständigkeit/Blocker`; FR-01–25 plus B-01–08. Historische Evidenz bleibt historisch. Chris Technik, Tobias UI-Belege, gemeinsame Abnahme später.

### Weiter blockiert

- Partneridentität/Stammdaten: Nummern-/Loginmigration, Bestand, Pflicht-/Änderungsregeln, Notizsichtbarkeit, Steuerkennung.
- Partnernachweis: Nachweisarten, Speicher, Download, Dateigrenzen, Austausch, Aufbewahrung/Löschung, Aktivierung.
- Artikel/Kategorien: vereinfachte Firmenmail/Beispieldaten, Import-/Bestandsregeln, Freitextmigration und BMD-Zusammenspiel.
- Personalisierung: statische Konfiguration oder Company-Modell, Pflege-/Logoablauf.
- E-Mail/Benachrichtigungen: Infrastruktur, Empfänger, Retry/Betrieb, Gelesen-/Company-Benutzermodell.
- Ausgeschlossen: Mandantenfähigkeit/Subdomains, öffentlicher Shop, direkte BMD-Schnittstelle, Fachbereiche, Produktgruppen, Pakete und KI-Feature.

### Formale Prüfungen 10.09.2026

`openspec validate --strict` war für alle fünf aktiven Changes erfolgreich. MVP-Tasks 9.1–9.5 wurden ohne Statusänderung auf 10.1–10.5 korrigiert. `git diff --check` erfolgreich. Keine Backend-/Browser-/Laufzeitprüfung, da nur Planung geändert wurde.

## Nächster Einstieg

Nächste Astra-Session setzt beim obigen Stand fort. Vor Bewertung neuen Git-Stand sicher aktualisieren: Dirty Worktree zuerst schützen; dann `git fetch --prune` und `git pull --ff-only`. Anschließend neuestes Besprechungsprotokoll vollständig lesen und alle aktiven OpenSpec-Proposals dagegen neu bewerten. Vollständiger `/goal` steht in `../Specification/NEXT_SESSION_PLAN.md`.

## Bereits erledigt

- **Aktueller Stand:** `main` ist auf `8e1944f` und entsprach bei Prüfung `origin/main`. Remote-Branches wurden gelesen. Kein Branch-Wechsel wegen ungespeicherter/untracked Dateien.
- **Kernbestellung:** Partner bestellt mehrere Artikel für Endkunden. Beleg: `build-partnerportal-mvp` 50/53 Tasks, bestehender Bestellprozess und Backend-Tests.
- **Partnerbezogene Freischaltung, Provision, Dashboard, Status:** umgesetzt. Beleg: bestehende Company-/Partner-Endpunkte, UI und Tests.
- **Partner-Kontaktdaten:** bearbeitbar.
- **Kategorie:** als Artikelmerkmal umgesetzt. Vollständige Kategorieverwaltung war nicht verbindlich verlangt.
- **CSV-/Excel-Import:** umgesetzt. BMD bleibt fachlich führend; direkte BMD-Schnittstelle nicht geplant.
- **5-%-Standardprovision:** nicht erfüllt. `company-partner-list-page.js` setzt beim Anlegen derzeit 10 %; die alte Tabelle ist in diesem Punkt richtig.
- **Preislistenbereich:** korrekt weggelassen.
- **Technologie-Stack:** Präsentation verwendet bereits „Technologie-Stack“. Alte Tabellenzeile veraltet.
- **USP-Folie:** existiert bereits. Alte Tabellenzeile „fehlt“ ist veraltet.
- **UI-Terminologie „Artikel“:** umgesetzt auf Branch `fix/article-terminology`; sichtbare Navigation, Listen, Dialoge, Toasts, Dashboard, Import und Bestellwizard verwenden „Artikel“. Beleg: Frontend-Diff und Desktop-/Mobile-Smoke-Checks.
- **Deployment-Pipeline:** aktiv und letzte Action erfolgreich. Beleg: Commits `56a4b15`, `2281a1f`, `2c7b722`, `8e1944f`. Erfolg ist unzureichend geprüft: öffentliche URLs lieferten trotzdem 404/502.
- **KI-Nachweise:** technischer Workflow vorhanden; laufende Pflege bleibt erforderlich.
- **Branch Protection und private GHCR-Packages:** auf GitHub vorhanden. OpenSpec-Tasks 3.5/3.6 sind veraltet und müssen dokumentarisch abgehakt werden.

## Wirklich noch wichtig

### P0.1 – Produktivsystem wieder erreichbar machen

- **Aufgabe und Grund:** Frontend liefert 404; Backend und Keycloak liefern 502. Pipeline meldet trotzdem Erfolg.
- **Lehrerwunsch:** funktionierende Domain und echte Demonstration.
- **Aufwand:** mittel
- **Risiko:** hoch
- **Abhängigkeit:** Zugriff auf VM, Logs und produktive Konfiguration. Keine Secrets ausgeben.
- **Zuständig:** Tobias
- **Modellentscheidung:** `GPT-6 Astra`
- **Geplant?** `NEIN – zuerst Sol/Astra planen`
- **Fehlende Entscheidungen:** konkrete Ursache für Backend-/Keycloak-Ausfall; Zustand von Containern, PostgreSQL, Netzwerk und Konfigurationsnamen.
- **Reihenfolge:** `docker compose ps`; begrenzte Service-Logs; Healthchecks; Netzwerke; Existenz erforderlicher Variablennamen ohne Werte; erst danach Reparatur.
- **Akzeptanz:** `/` lädt; OIDC-Discovery antwortet 200; Backend-Health/API antwortet ohne 502; Login und Bestellung funktionieren.

### P0.2 – Frontend-Sync dauerhaft reparieren

- **Aufgabe und Grund:** `source: "frontend/*"` erzeugt laut Fehlerbild `frontend-new/frontend/index.html`. Caddy erwartet `frontend/index.html`.
- **Lehrerwunsch:** stabile erreichbare Demo.
- **Aufwand:** klein
- **Risiko:** hoch
- **Abhängigkeit:** VM-Test und Tobias’ Deployment-Verantwortung.
- **Zuständig:** Tobias
- **Modellentscheidung:** `GPT-6 Astra`
- **Geplant?** `NEIN – zuerst Sol/Astra planen`
- **Fehlende Entscheidungen:** exakte Archivpfade des verwendeten Action-Tags; Verhalten bei fehlendem altem Frontend; Rollback bei fehlendem `index.html`.
- **Vorgesehener Fix:** `strip_components: 1` für Quelle unter `frontend/`; vor Swap ausdrücklich `test -f ~/papo-deploy/frontend-new/index.html`. `strip_components` entfernt führende Pfadteile laut offizieller scp-action-Dokumentation.
- **Akzeptanz:** kein `frontend-new/frontend/`; Swap erst nach Pfadprüfung; anschließender HTTP-Healthcheck muss Pipeline bei 404/502 fehlschlagen lassen.

### P0.3 – Präsentation für Termin korrigieren

- **Aufgabe und Grund:** lokaler Tobi-Stand enthält neue Änderungen, aber noch falsche/veraltete Aussagen.
- **Lehrerwunsch:** allgemeines Problem zuerst, USP klar, Referenzkunde später, korrekte Begriffe und aktueller Stand.
- **Aufwand:** klein
- **Risiko:** niedrig
- **Abhängigkeit:** Tobis ungespeicherte Änderung in `presentation/mvp-deck/index.html` bewahren.
- **Zuständig:** Tobias
- **Modellentscheidung:** `GPT-5.6 Terra medium`
- **Geplant?** `JA – Terra kann implementieren`
- **Änderungen:** Problem/USP vor Referenzkunde; unbelegte Zahlen „ca. 200 Partner/800 Artikel“ entfernen; Grammatik „welchen Artikel“; BMD als führendes System plus CSV-/Excel-Import erklären; gemeinsamer Firmenzugang pro Partner erklären; Hosting-Folie an echten Zustand anpassen; README mit tatsächlichen 14 Folien synchronisieren.

### P1.2 – Endkunden unabhängig von Bestellung anlegen

- **Aufgabe und Grund:** „Meine Endkunden“ zeigt derzeit nur Liste/Filter. Anlage existiert nur im Bestellablauf. Roadmap bezeichnet die Funktion dennoch als vorhanden; das ist eine Dokumentationslücke. Bestehenden POST-Endpunkt und vorhandenes Formularmuster wiederverwenden.
- **Lehrerwunsch:** Partner verwaltet zugeordnete Endkunden-Stammdaten.
- **Aufwand:** mittel
- **Risiko:** mittel
- **Abhängigkeit:** kleines OpenSpec-Delta mit Pflichtfeldern, Fehlerdarstellung und Erfolgspfad.
- **Zuständig:** Chris
- **Modellentscheidung:** `GPT-6 Astra` prüft und plant zuerst, danach gegebenenfalls Terra.
- **Geplant?** `NEIN – zuerst Astra reviewt Planentwurf`
- **Beleg:** Uncommitteter OpenSpec-Entwurf `add-standalone-customer-creation`; Astra muss Felder, Dublettenverhalten, Erfolgspfad, Partnerisolation und Testumfang unabhängig bestätigen.

### P1.3 – Deployment-Sicherheit aus geschlossenem PR #5 retten

- **Aufgabe und Grund:** PR #5 ist geschlossen, konfliktbehaftet und basiert auf altem `main`. Forced-Command-Gateway bleibt sinnvoll, darf aber nicht blind gemergt werden.
- **Lehrerwunsch:** kein direkter Lehrerwunsch; schützt produktive Infrastruktur.
- **Aufwand:** groß
- **Risiko:** hoch
- **Abhängigkeit:** Tobias richtet VM-Tasks 5.11/5.12 ein und testet Grenzen.
- **Zuständig:** Tobias
- **Modellentscheidung:** `GPT-6 Astra`
- **Geplant?** `NEIN – zuerst Sol/Astra planen`
- **Fehlende Entscheidungen:** finaler Gateway-Befehl, sudoers-Grenze, Repo-Key-Rechte, negativer Boundary-Test, Rollback.
- **Vorgehen:** neuer fokussierter Branch von aktuellem `main`; benötigte Gateway-/Workflow-Teile selektiv übernehmen; aktuelle Build-, `config.js`- und Frontend-Deploy-Logik behalten; erst nach VM-Test aktivieren.

### P1.4 – OpenSpec und Realität synchronisieren

- **Aufgabe und Grund:** `tasks.md` ist teilweise veraltet. Keine stillen Erledigt-Markierungen.
- **Lehrerwunsch:** nachvollziehbare KI-/Projektarbeit.
- **Aufwand:** klein
- **Risiko:** niedrig
- **Abhängigkeit:** P0-Ergebnis und Tobis UI-Bestätigung.
- **Zuständig:** gemeinsam
- **Modellentscheidung:** `GPT-5.6 Sol medium`
- **Geplant?** `JA – Terra kann implementieren`, sobald Fakten bestätigt sind.
- **Korrekturen:** GHCR privat und Branch Protection als bereits erfüllt dokumentieren; Deployment nicht als erfolgreich markieren, solange Live-Checks scheitern; UI/UX-Dokumentation aus PR #5 prüfen und gezielt übernehmen; `AI_WORKFLOW_EFFICIENCY.md` ebenfalls gezielt übernehmen, nicht ganzen PR #5 mergen.

### P2 – Partner-Stammdaten fachlich planen

- **Aufgabe und Grund:** automatische Partnernummer, Firmenname/Ansprechpartner, UID, interne Notiz und der korrekte 5-%-Standard fehlen.
- **Lehrerwunsch:** ausdrücklich am 25.8 genannt.
- **Aufwand:** groß
- **Risiko:** hoch
- **Abhängigkeit:** Felddefinitionen, Pflichtfelder, Änderbarkeit, Eindeutigkeit, Migration bestehender Daten.
- **Zuständig:** Chris
- **Modellentscheidung:** `GPT-6 Astra`
- **Geplant?** `NEIN – zuerst Sol/Astra planen`
- **Fehlende Entscheidungen:** Nummernformat und Startwert; bestehende Partnernummern; genaue Trennung Firma/Person; UID-Validierung; Sichtbarkeit interner Notizen.

### P2 – BMD-Artikelnummer ergänzen

- **Aufgabe und Grund:** `Product` besitzt aktuell keine sichtbare eindeutige BMD-Artikelnummer.
- **Lehrerwunsch:** BMD führend; sichtbare Artikelnummer. DB-ID bleibt technisch.
- **Aufwand:** groß
- **Risiko:** hoch
- **Abhängigkeit:** reales Beispielexportformat oder verbindliche Spaltenzuordnung.
- **Zuständig:** gemeinsam
- **Modellentscheidung:** `GPT-6 Astra`
- **Geplant?** `NEIN – zuerst Sol/Astra planen`
- **Fehlende Entscheidungen:** Datentyp/Format, Pflichtfeld, Umgang mit Bestandsdaten, Import-Dubletten, Änderbarkeit und Anzeigeorte.

## Bewusst später

- **Mandantenfähigkeit/Subdomains:** aktuell irrelevant. Erst bei zweitem Unternehmen.
- **Öffentlicher Partnershop:** späterer Ausbau; aktueller Kern ist Partnerbestellung.
- **Direkte BMD-Schnittstelle:** CSV-/Excel-Import genügt.
- **Echte E-Mails:** Simulation genügt für MVP.
- **Gewerbeschein-Upload:** Security-, Speicher- und Aufbewahrungsentscheidungen fehlen.
- **Fachbereiche, Produktgruppen, Pakete/BOMs, Schulungslogik, KI-Feature:** keine neuere verbindliche Freigabe.
- **Demo-Zugänge:** akzeptiert, solange ausschließlich synthetische Demodaten verwendet werden. Neu prüfen vor echten Firmendaten oder breiter öffentlicher Nutzung.
- **Alle Branches pullen:** nicht sinnvoll bei geschlossenem PR #5 und schmutzigem Arbeitsbaum. Vor Umsetzung zuerst Tobis lokale Deck-Änderung sichern; danach `git fetch --prune` und nur jeweiligen Zielbranch aktualisieren.

## Nächster Chris-Task

`GPT-6 Astra` plant alle bestätigten Kandidaten einzeln gemäß `Specification/NEXT_SESSION_PLAN.md`. Erst vollständig reviewte, nicht blockierte OpenSpec-Changes gehen anschließend an Terra.

## Einschätzung

**Zu viel – nur diese P0-Punkte** vor dem unmittelbar nächsten Termin. Danach P1.1 und P1.2.
