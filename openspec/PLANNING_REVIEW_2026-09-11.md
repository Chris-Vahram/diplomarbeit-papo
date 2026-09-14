# OpenSpec-Planungsreview — 11.09.2026

## Neuer Next-Session-Plan — Besprechungsprotokoll 14.9

Das neue Protokoll `Documentation/Besprechungsprotokolle/Besprechungsprotokoll_14.9.md` ist gelesen. Die nächste Session priorisiert die Präsentation: `presentation/mvp-deck/index.html` sprachlich anpassen, digitale Souveränität mit 2–3 Folien erklären, GitHub-Stars und zwei echte Screenshots ergänzen, Clockify-Gesamtzeit darstellen und ein kurzes Probe-Gallopp-Drehbuch erstellen. Danach Desktop/Mobile und Offline-Assets prüfen und über PR mergen.

Der Company-Notiz-Change ist archiviert und nicht erneut zu starten. Der nächste technische Andockpunkt nach der Präsentation bleibt `harden-deployment-gateway` 3/4 mit fehlender externer SSH-Negativmatrix; BMD-Nummern bleiben bis Beispieldaten/Vertrag begrenzt.

## Aktueller Abschlussnachtrag — 14.09.2026

`add-company-internal-partner-note` ist **4/4 erledigt** und über PR #18 (`e061ab8c1e3cc41cae57b7fbe17a8f9e65664b5d`) gemergt. Die finale Request-Semantik bewahrt Notizen bei ausgelassenem Feld und löscht bei explizitem `null`, Leertext oder leerem Formular. Die Antwortgrenzen bleiben Company-only; relevante Partner-, Order-, Dashboard-, Produkt- und Exportpfade sind negativ geprüft. Backend 13/13 fokussiert und 101/101 vollständig, Package, JS-Syntax, Strict-Validate, Diff-Check, Browser Desktop/Mobile sowie PAPO-/Java-/Securityreview sind grün.
- CI/CD `34857012639` ist mit Test, Image-Build und VM-Deployment erfolgreich; Live-Smoke: Root 200, unauthentifizierte Company-API 401.

**Nächster Andockpunkt:** PR/CI/live verifizieren und anschließend VM-Security Task 2.2 mit autorisiertem Deploy-Key um die externe SSH-Negativmatrix ergänzen. Danach einzeln mit den vier freigegebenen Stammdaten-/BMD-Changes fortfahren; BMD-Nummern bleiben bis Beispieldaten/Vertrag begrenzt.

## Tatsächliche Fortschreibung — 14.09.2026

PR #16 mit `add-company-internal-partner-note` und den drei UI-Korrekturen ist nach `main` gemergt und über CI/CD-Lauf `34853223915` deployed. Der Change bleibt formal offen (0/4): zwei Review-Findings, Browserabnahme und vollständige Antwortgrenzen fehlen. PR #17 (`ae749ea`) dokumentiert den KI-Nachweis und wartet auf CI.

Erledigt: Gateway-Streamingbug, Push/Merge/Deployment, Live-HTTPS/API-Smoke, Clockify und VM-Security-Task 2.1. Teilweise: VM-Security-Task 2.2 sowie Live-/Rollback-Abnahme. Offen: externe SSH-Negativmatrix, formaler Company-Notizabschluss und vier weitere Stammdaten-/BMD-Changes. BMD-Nummern bleiben bis Beispieldaten/Vertrag gestoppt; keine direkte BMD-API.

## Next-Session-Audit — 14.09.2026

- Branch/Remote sauber synchron auf `38cd932`; lokaler Wortlaut-/Deck-Fix ist danach noch nicht committed oder gepusht. `origin/main` ist `05e0bdd`; lokales `main` liegt vier Commits zurück.
- `harden-deployment-gateway` **2/4**: lokale Umsetzung und Harness 18/18 belegt. Next session zuerst Live-Deployment auf den aktuellen lokalen Stand; anschließend autorisierter VM-/Git-/Security-Subagent für Tasks 2.1/2.2.
- Verifiziert: Backend 100/100, Package, JS-Syntaxchecks, OpenSpec Strict 2/2, `git diff --check`, Präsentation 15 Folien mit echtem Screenshot. Keine VM-, SSH-, Secret- oder Datenvolumenaktion.
- Präsentationsquelle ist auf **Artikel** vereinheitlicht; Folie 8/13 und technische Kommentare/Dokumentation wurden lokal korrigiert. `complete-thesis-documentation` bleibt 0/6.
- Public-Live-Test am 14.09. erfolgreich: Partner-Login → Endkunde → zwei Artikel → Bestellung #52 (218,90 €) → Company-Status `Geprüft`. Die Live-VM zeigt trotzdem noch alte „Produkte“-Labels, weil kein Deployment ausgeführt wurde.
- Wirklich offen sind: VM-Deployment/Nachweise, Thesis-Doku, BMD-geführte Partnerstammdaten, generische UID-/Steuerkennung, Company-Notiz, Gewerbeschein und eindeutige BMD-Artikelnummer mit Import-/Exportregel. Diese Punkte sollen in der nächsten Session mit höherem Modell, OpenSpec und Caveman geplant werden; noch keine Implementierung.
- Clockify ist als eigener nächster Arbeitspunkt verpflichtend: retrospektiv geschätzte, klar markierte Einträge mit Datum, Uhrzeit und Beschreibung erstellen.

Alle Abschnitte mit älterem Datum darunter sind historische Reviews und nicht der aktuelle Aufgabenstand.

## Orchester-Fortschreibung — 14.09.2026

Nächster Abschlusslauf: 5.9 zuerst mit echten Tobias-Nachweisen auditieren. Bei ausreichender Freigabe darf der Agent VM-/Git-/Deploymentarbeit übernehmen und unabhängige Subagents für Remote-Security-, Browser-, Live- und Rollbacktests einsetzen. Danach Präsentation anhand der neuesten Lehreranforderungen und OpenSpec-Archive finalisieren; Clockify verteilt über belegte Arbeitstage aus Protokollen/Archiven ableiten, ohne erfundene Tätigkeiten oder Zeiten.

- `origin/main` `05e0bdd` kontrolliert integriert. Tobias' Basis-CI/CD- und Demo-Seeding-Archive bleiben erhalten; offene Gateway-Sicherheitsarbeit ist im aktiven `harden-deployment-gateway` (**2/4**) nachvollziehbar.
- Seeder-Konflikt kombiniert: Opt-in-Normal-Seeding plus weiterhin unabhängige Kategorie-Migration. Flag nicht aktiviert, keine VM-/Produktionsdaten geändert.
- Integration geprüft: Seeder 1/1, vollständige Suite 100/100 und Package 100/100 erfolgreich.

- Kategorien-Change: **4/4, archiviert** am 14.09.
- P0-Recovery laut Betriebsnachweis erfolgreich; CI-Nachweise/Tasks noch synchronisieren.
- Nächste Entscheidung: Remote-Installation erst nach Tobias-Nachweisen; danach Security-/Live-Checks, Thesis-Doku und Präsentation.

## Fortschreibung — 13.09.2026

| Change | Stand / Entscheidung |
|---|---|
| `build-partnerportal-mvp` | **53/53, technisch abgeschlossen**, nicht archiviert. Allgemeine Doku ausgelagert; FD.4 aktueller Stand fertig. |
| `complete-thesis-documentation` | **0/6, READY.** Allgemeine UI/UX-, Technik- und Traceability-Dokumentation; keine Produktänderung. |
| `integrate-category-management-into-article-management` | **0/4, READY.** Company-„Artikelverwaltung“, Kategorien über bestehende Unterseite, Partner bleibt „Artikel“. |
| `ci-cd-deployment-with-gh-actions` | **34/45, teilweise READY.** P0-Ursache bekannt; Recovery benötigt DB-Dumps und geheime Passwortangleichung. Gateway/Liveflows/Cron bleiben offen. |

Aktuelle Belege: GHCR privat; `main` mit PR-, Up-to-date- und `Test backend`-Pflicht ohne Bypass; DNS auf `178.104.109.45`; Root über gültiges Let's-Encrypt-HTTPS 200. Julian hat laut Screenshot noch geerbten Write-Zugriff und muss entfernt oder neu autorisiert werden. PostgreSQL ist gesund; Keycloak/Backend scheitern ausschließlich an `FATAL: password authentication failed for user "partnerportal"`. Frontend-404 gilt als behoben. Gateway aus `48ad4d8` bleibt verwertbare Quelle, muss aber auf `1513f9a`-Releasepfad angepasst werden.

## Abschlussstand nach Review und Archivierung — 12.09.2026

`define-mvp-traceability-format` ist **3/3 fertig und archiviert** unter `openspec/changes/archive/2026-09-12-define-mvp-traceability-format`. `manage-article-categories` ist **6/6 fertig und archiviert** unter `openspec/changes/archive/2026-09-12-manage-article-categories`. Die dokumentierten Nachweise und Reviews sind auf den echten Stand nachgezogen; die aktive OpenSpec-Liste enthält nur noch die beiden folgenden Changes.

| Change | Stand / Entscheidung |
|---|---|
| `build-partnerportal-mvp` | **50/53, nicht abgeschlossen.** Offen: 8.3 Tobias UI/UX-Doku, 8.4 gemeinsame Diplomarbeits-/Traceability-Ausfüllung, FD.4 Tobias-Polish später ergänzter Screens. Technisch weitgehend umgesetzt, aber ohne die zuständigen Inputs nicht selbstständig abschließbar; keine neue Planungsphase begonnen. |
| `ci-cd-deployment-with-gh-actions` | **30/41, BLOCKIERT.** Offen: 3.5/3.6, 6.1–6.4, 7.5/7.6a und post-MVP 7.2/7.3/7.6b. Zusätzlich fehlen aktuelle Nachweise zu P0-Ursache/Laufzeit, Frontend-Archiv-/Index-/Recoverypfad und PR-#5-Gateway-/Schlüsselgrenzen. Nicht Terra-ready; vor Umsetzung braucht es neue Nachweise/Entscheidungen. |

Keine Implementierung, VM-/Secret-/SSH-/Backup-/GitHub-Secrets-, Deployment-, Commit- oder Pushaktion für diese beiden Changes.

## Abschlussnachtrag define-mvp-traceability-format — 12.09.2026

Der Change `define-mvp-traceability-format` ist **3/3 fertig**, nicht archiviert. Die Matrix umfasst exakt 33 eindeutige IDs, acht spezifizierte Spalten, Legende, relative FSD-/Backlog-Links und transparente offene Zuordnungen; das TODO benennt Zuständigkeiten und lässt MVP 8.3/8.4/FD.4 offen. Quellen-/Link-/Status-/Strukturcheck, `openspec validate define-mvp-traceability-format --strict`, `git diff --check` sowie Funktions-/Evidenz-/Securityreview ohne Findings. Keine Backendprüfung erforderlich und keine privaten oder externen Nachweise verwendet. Nächster zulässiger Arbeitsschritt nur nach neuer Freigabe; keine Archivierung, kein Commit/Push/Merge/Deployment.

## Abschlussnachtrag manage-article-categories — 12.09.2026

Der Change `manage-article-categories` ist **6/6 fertig**, nicht archiviert. Die Kategorieverwaltung ist additiv umgesetzt: normalisierte eindeutige Namen, idempotente Legacy-Übernahme, Company-Rollen/Locking, optionale Product-Zuordnung, aktive Importauflösung und native Company-Oberfläche. Fokussiert 27/27, Full-Test 79/79, Package, Strict-Validate, Diff-Check, Browser 1440/390 sowie `partnerportal-quality`, Funktions-/Test-/Sinnhaftigkeits- und Securityreview waren ohne Findings. Nächster zulässiger Change: `define-mvp-traceability-format`. Keine Produktabnahme oder Evidenz für blockierte MVP-/CI/CD-Themen daraus ableiten.

## Arbeitsstopp nach Auto-Compact — 12.09.2026

`manage-article-categories` ist aktuell **3/6**: Tasks 1.1, 1.2 und 2.1 sind nach fokussierten Backendläufen abgehakt. Die Import-Pipeline wurde um vorbereitendes Kategorie-Locking und finale Zeilenvalidierung erweitert; diese letzte Änderung ist real im Worktree, aber noch ohne anschließenden fokussierten Importtest. Task 2.2 ist deshalb offen. Vor der Fortsetzung den letzten Diff prüfen und den dokumentierten Refresh-/Race-Punkt für manuelle Product-Zuordnung verifizieren. Danach Import, UI und Abschluss strikt sequenziell erledigen; keine Nachweise vorwegnehmen.

`search-order-customers`, `clarify-order-customer-context`, `show-partner-company-name` und `use-huemer-demo-partner` sind fertig geprüft und unter `openspec/changes/archive/2026-09-12-*` archiviert. Branch `docs/openspec-planning-2026-09-11` bleibt dirty; kein Fetch/Pull, Commit, Push, Merge, Deployment oder Archivieren im nächsten Arbeitsblock. Screenshots unter `output/playwright/` bewahren.

## Implementierungsnachtrag — Arbeitsstand 12.09.2026

Dieser Nachtrag ersetzt für `search-order-customers`, `clarify-order-customer-context`, `show-partner-company-name` und `use-huemer-demo-partner` die ältere Arbeitsstopp-Tabelle; die ursprüngliche Planungsbewertung und die übrigen historischen Statusangaben bleiben erhalten.

| Change | Verifizierter Implementierungsstand | Offen / keine behauptete Abnahme |
|---|---|---|
| `search-order-customers` | **3/3 Tasks fertig und archiviert** unter `openspec/changes/archive/2026-09-12-search-order-customers`: lokale Suche/Select, explizite Neuanlage, Draft-/Weitergate, lokale Kunden-ID-Prüfung sowie Partner-/Company-Regression. Browser 1440/390: >20/0 Treffer, Umlaute, Tastaturauswahl, Filter-/Kundenwechsel, Warenkorb/Payload, Draft, Abbruch, Mehrartikel und Fehler/Erfolg. `node --check`, Java-21-`./mvnw package` 79/79, Strict-Validate, Diff-Check, `partnerportal-quality`, Funktions-/Sinnhaftigkeits-, Securityreview und unabhängiger Abschlussreview ohne Blocker. | Erfolgspfade waren temporäre Playwright-Mocks; keine echten Testbestellungen persistiert. Keine weitere offene Task in diesem Change. |
| `clarify-order-customer-context` | **3/3 Tasks fertig und archiviert** unter `openspec/changes/archive/2026-09-12-clarify-order-customer-context`: Kundenüberschriften, exakter Prüfhint, Katalog-vor-Bestellpanel, responsive DOM-Reihenfolge und unveränderter Ordervertrag. Browser 1440/390: Mengen/Summen, Safe-Text, Mock-POST, Erfolg und Reset. `node --check`, Java-21-`./mvnw package` 79/79, Strict-Validate, Diff-Check, `partnerportal-quality`, Funktions-/Sinnhaftigkeits-, Securityreview und unabhängiger Abschlussreview ohne Blocker. | Order-Erfolgspfad war ein temporärer Playwright-Mock; keine echte Testbestellung persistiert. Keine weitere offene Task in diesem Change. |
| `show-partner-company-name` | **3/3 Tasks fertig und archiviert** unter `openspec/changes/archive/2026-09-12-show-partner-company-name`: Portal-/Dashboardname, Retry, Rendergeneration, Logout-/Navigations-Races, Sitzungsrotation und Mobile-Wrap. Browser 1440/390: echter Huemer-Name, Profilnavigation, Fehler-/Retrypfad und synthetische Raceantworten. `node --check`, Java-21-`./mvnw package` 79/79, Strict-Validate, Diff-Check, `partnerportal-quality`, Funktions-/Sinnhaftigkeits-, Securityreview und unabhängiger Abschlussreview ohne Blocker. | Profilantworten der Racefälle waren temporäre Playwright-Mocks; keine Authänderung oder persistente Profildaten. Keine weitere offene Task in diesem Change. |
| `use-huemer-demo-partner` | **2/2 Tasks fertig und archiviert** unter `openspec/changes/archive/2026-09-12-use-huemer-demo-partner`: bestehender `PARTNER-DEMO`-Seed auf `Huemer GmbH`, unveränderte Partnernummer/Beziehungen/Seed-Gating und aktualisierte Namensassertionen. `PartnerResourceTest` 12/12, Java-21-`./mvnw test` und `./mvnw package` 79/79, Strict-Validate, Diff-Check, lokaler Browsernachweis sowie `partnerportal-quality`, Funktions-/Sinnhaftigkeits- und Securityreview ohne Blocker. | Keine weitere offene Task in diesem Change. |

Nächster Change: `manage-article-categories`. Kein Commit, Push, Merge oder Deployment.

## Implementierungsnachtrag — Arbeitsstopp 11.09.2026

Dieser Nachtrag ersetzt ausschließlich Statusangaben zu den vier unten genannten Changes; die ursprüngliche Planungsbewertung bleibt historische Grundlage.

| Change | Verifizierter Implementierungsstand | Offen / keine behauptete Abnahme |
|---|---|---|
| `show-partner-company-name` | Firmenlabel im Router/Dashboard, neutraler Fehlerzustand, Retry, Escaping und Basis-Browserprüfungen; Java-21-Package 79/79, Strict-Validate, Diff-Check | Verzögerte P1→Logout→P2/Company- und parallele Refresh-Races nicht abschließend belegt; Tasks 1.2/2.1 offen; keine formalen Abschlussreviews dokumentiert |
| `use-huemer-demo-partner` | `PARTNER-DEMO` heißt `Huemer GmbH`; Namensassertions angepasst; Fokus 18/18, späteres Package 79/79, Strict-Validate, Diff-Check, sichtbare lokale Demo | Task 2.1 offen, da formale Korrektheits-/Securityreviews nicht dokumentiert sind |
| `search-order-customers` | Native Suche/Select, explizite Neuanlage, lokaler Entwurf und Weitergate im Partnerwizard; Teil-Browserfälle; Package 79/79, Strict-Validate, Diff-Check | Alle Tasks offen: vollständige Such-, Tastatur-, Wechsel-/Payload-, Company-, Mobil- und Review-DoDs fehlen |
| `clarify-order-customer-context` | Uncommitteter Vorlauf für dynamische Kundenüberschriften, Prüfhint sowie Katalog-vor-Bestellung; `node --check`, Strict-Validate, Diff-Check | Alle Tasks offen, keine Browser- oder Abschlussprüfung |

Reihenfolge der Wiederaufnahme: erst `search-order-customers` ehrlich abschließen, dann `clarify-order-customer-context`; die überlappende Datei nicht zurücksetzen. Branch `docs/openspec-planning-2026-09-11` ist schmutzig. Kein Commit/Push/Merge/Archivieren/Deployment. Die zwei untracked Screenshots unter `output/playwright/` bewahren. KI-Evidence-Sync/Enrich wurde nicht ausgeführt; keine Evidenz erfinden.

## Umsetzungsnachtrag — 11.09.2026

`add-standalone-customer-creation` ist **6/6 implementiert** und unter `openspec/changes/archive/2026-09-11-add-standalone-customer-creation` archiviert. Die Umsetzung verwendet den bestehenden partner-scoped Kundenendpunkt und die gemeinsamen Adressfelder; kein Schema-, Realm- oder API-Umbau. Belegt sind fokussierte Kunden-Tests (9/9), vollständige Backendtests und Package (je 79/79), Strict-Validate und Diff-Check. Browserprüfungen 1440/390 decken Anlage, Filterreset, Escaping, Pending-/Fehler-/Refreshpfade, Wizard-Wiederverwendung und anonyme/falsche Rollenrouten ab. Partnerportal-Quality, Korrektheits- und Securityreview: keine offenen Findings.

`align-visible-article-terminology` ist bei **2/2 Tasks fertig** und unter `openspec/changes/archive/2026-09-11-align-visible-article-terminology` archiviert. Der vorhandene Frontenddiff blieb erhalten; Navigation, Listen, Formulare, Bestellschritte und sechs sichtbare Backendfehler verwenden „Artikel“ beziehungsweise „Artikelkategorie“. Technische Names/URLs/Keys, `productId` und Importaliasnamen einschließlich `produktname` blieben unverändert; keine sichtbare Artikel-ID ergänzt. Der lokale Keycloak-Dev-Service-Fehler wurde auf Docker Desktops öffentliche NAT-Quell-IP zurückgeführt und ausschließlich im `%test`-Profil behoben (sicherer Frontend-Hostname, dynamischer Backchannel, erwarteter Issuer, 30-Sekunden-Timeout).

Belegt unter Java 21: Fokuslauf `EnablementResourceTest,ProductResourceTest,OrderResourceTest` 26/26; vollständiges `./mvnw test` und `./mvnw package` jeweils 77/77 ohne Fehler. Browserprüfung 1440/390 deckte Company-/Partner-Artikel, Import und Bestellwizard einschließlich Fehlerpfaden ab. `openspec validate align-visible-article-terminology --strict` und `git diff --check` erfolgreich; partnerportal-quality, unabhängiger Funktions-/Test-/Sinnhaftigkeitsreview und Securityreview ohne Blocker. Kein Commit/Push/Merge/Deployment.

`fix-partner-create-commission-default` ist ebenfalls bei **2/2 Tasks fertig** und unter `openspec/changes/archive/2026-09-11-fix-partner-create-commission-default` archiviert. Die einzige eigene Codeänderung setzt die manuelle `openCreate()`-Vorauswahl auf 5 %. API, Import, bestehende Partner, historische Provisionen, Berechnung, Login und Datenmodell blieben unverändert. Browser 1440/390 belegt 5-%-POST, Abbruchreset, Bearbeiten eines 10-%-Partners, bewusste 15-%-Wahl und gespeicherten individuellen Wert 7,5 %. `./mvnw package` bestand unter Java 21 mit 77/77; `openspec validate fix-partner-create-commission-default --strict` und `git diff --check` erfolgreich. partnerportal-quality und unabhängiger Korrektheitsreview ohne Findings.

## Auftrag und geprüfte Grundlage

Freigegeben wurde die Umsetzung des Plans **ausschließlich in OpenSpec und Übergabedokumentation**. Kein Produktcode wurde in diesem Arbeitsblock geändert; vorhandene uncommittete Benutzeränderungen bleiben bestehen. Keine Commit-/Push-/Merge-/Deploymentaktion, kein VM-, SSH-, Secret-, Realm-, Backup- oder GitHub-Secrets-Zugriff.

Vollständig gelesen: AGENTS, Continuation Prompt, Next Session Plan, AI-Workflow; CLAUDE und aktive OpenSpec-Artefakte; Originalprotokolle [25.08.](../Documentation/Besprechungsprotokolle/Besprechungsprotokoll25_08_2026_Original.md), [31.08.](../Documentation/Besprechungsprotokolle/Besprechungsprotokoll31_08_2026_Original.md), [10.09.](../Documentation/Besprechungsprotokolle/Besprechungsprotokoll_10.9.md). Gegen FSD, Backlog, Roadmap, kanonische Specs und betroffene Code-/Testpfade abgeglichen. AGENTS, CLAUDE und Roadmap erhielten nur aktuelle Quellen-/Handoffhinweise; historische Inhalte blieben erhalten.

Start: main bei lokalem `fea0e5b`, identisch zum lokal gespeicherten origin/main; zahlreiche Änderungen. Deshalb kein Fetch/Pull, kein Stash. Planungsbranch: `docs/openspec-planning-2026-09-11`. Remote nicht neu geprüft.

## Neue Entscheidungen und Quellenabweichungen

- 10.09: eigene **Partnerfirma**, nicht betreibende Company, prominent anzeigen. Chris bestätigt bestehendes `Partner.name` als Quelle; keine Firmen-/Ansprechpartnermigration hierfür.
- „Huemer GmbH“ ist synthetische Testpartnerfirma. Kein reales Referenzunternehmen als Testname übernehmen.
- 25.08 fordert Kategorien verwalten/hinzufügen; 10.09 bestätigt Erweiterbarkeit. Chris bestätigt kontrollierte Company-Anlage/Umbenennung/Deaktivierung; Import unbekannter Kategorien wird abgelehnt. Die frühere pauschale Blockade durch BMD-Artikelnummer ist aufgehoben.
- 10.09 konkretisiert Kundensuche, Neuanlagebutton, Kundenüberschrift, Artikel links/Bestellung rechts und Prüfungstext. Bestehender Kern mit mehreren Artikeln bleibt.
- 25.08 nennt auch **Bearbeiten** eigener Endkunden. Chris bestätigt separate Planung unter Schutz historischer Kundendaten. Das ist nicht Bestandteil der unabhängigen Anlage.
- Chris entscheidet Loginbindung künftig über Keycloak-ID; vorhandene sichtbare Partnernummern behalten, neue automatisch vergeben. Ziel bestätigt, sichere Bestands-/Betriebszuordnung noch offen.
- Artikel-ID vom 31.08 wird durch 10.09 für die sichtbare Oberfläche aufgehoben; technische DB-ID bleibt. BMD bleibt führend. Direkte Schnittstelle und ausgenommene Felder bleiben ausgeschlossen.
- Original 25.08 nennt Bestell-E-Mail und Präsentationsformulierung ohne „simuliert“. Dies ist gewünschtes Ziel, kein Nachweis vorhandener SMTP-Funktion. Umsetzung bleibt wegen dokumentierter externer Entscheidungen offen.
- Präsentation mit Screenshots, Vorbereitung und nachvollziehbare Clockify-/KI-Nachweise bestätigt. Keine erfundenen Stunden, Screenshots oder Laufzeitergebnisse.

## Ergebnis pro aktivem Change

| Change | Umsetzungstasks | Ergebnis |
|---|---:|---|
| add-standalone-customer-creation | 6/6 | FERTIG, archiviert unter `openspec/changes/archive/2026-09-11-add-standalone-customer-creation` |
| fix-partner-create-commission-default | 2/2 | FERTIG, archiviert unter `openspec/changes/archive/2026-09-11-fix-partner-create-commission-default` |
| define-mvp-traceability-format | 0/3 | READY für Terra medium |
| show-partner-company-name | 0/3 | READY für Terra medium |
| use-huemer-demo-partner | 0/2 | READY für Terra medium |
| search-order-customers | 0/3 | READY für Terra medium |
| clarify-order-customer-context | 0/3 | READY für Terra medium |
| manage-article-categories | 0/6 | READY für Terra medium |
| align-visible-article-terminology | 2/2 | FERTIG, archiviert unter `openspec/changes/archive/2026-09-11-align-visible-article-terminology`: sichtbare Artikel-/Artikelkategoriesprache; Java-21-Fokuslauf 26/26, Gesamttest/Package jeweils 77/77, Browser 1440/390, strict Validate und Reviews ohne Blocker. |
| fix-partner-create-commission-default | 2/2 | FERTIG, archiviert unter `openspec/changes/archive/2026-09-11-fix-partner-create-commission-default`: manuelle 5-%-Vorauswahl; Browser 1440/390 und Package 77/77, strict Validate und Reviews ohne Findings. |
| build-partnerportal-mvp | 50/53 | Offen: Tobias liefert 8.3/FD.4; gemeinsame 8.4-Ausfüllung/Abnahme fehlt |
| ci-cd-deployment-with-gh-actions | 30/41 | Offen: Tobias liefert datierte Betriebs-, Frontend-Reparatur- und Zugriffsgrenzennachweise |

Die sechs neuen Changes besitzen je proposal.md, design.md, mindestens eine Delta-Spec und tasks.md. Jede Task enthält Voraussetzung, Komponenten, Akzeptanz, Daten/Security, Tests, DoD und Stop. Checkboxen bezeichnen spätere Umsetzung, nicht erfolgreiche Planung. READY ist Planreife, keine Produkt- oder Betriebsabnahme.

Empfohlene Apply-Reihenfolge: Artikelsprache; 5-%-Vorauswahl; unabhängige Kundenanlage; Firmenanzeige und Huemer-Seed; Kundensuche; Kundenkontext; Kategorien. Traceability-Format unabhängig. Gemeinsame Dateiberührung erfordert Integration bestehender Änderungen, keinen pauschalen Reset.

Für jeden Apply gilt: erst `openspec status --change <name> --json` und `openspec instructions apply --change <name> --json`, dann nur dessen vollständigen Kontext lesen und Tasks in Reihenfolge erfüllen. Checkboxen folgen unmittelbar auf erfüllte Akzeptanz, Daten-/Securityfolgen, Tests und DoD. Danach: fokussierte Tests je Task; pro Change Strict-Validate, passende Gesamtprüfung, `partnerportal-quality`, Funktions-/Test-/Sinnhaftigkeitsreview sowie bei Auth-/Daten-/Rollen-/Input-Scope Securityreview. Findings werden vor dem nächsten Change behoben und erneut geprüft. Bei ungeklärter Stopbedingung wird der konkrete Blocker dokumentiert und pausiert; kein Raten, Archivieren oder Committen. Handoffs dokumentieren ausschließlich echte Prüfungen.

## Präzise offene Entscheidungen — keine ausführbaren Terra-Changes

| Kandidat | Bestätigt / technische Richtung | Fehlende Entscheidung oder Evidenz; Stop |
|---|---|---|
| snapshot-order-customer-data | Unabhängige Kopie von Nach-/Vorname, Adresse, E-Mail/Telefon pro Bestellung; Kundenrelation/ID bleibt. OrderDto **und CSV** müssen Snapshot lesen; gemeinsamer Partner-/Company-Createpfad sowie Seed/Testfixtures befüllen. | Chris muss entscheiden: genügt für Altbestellungen der Kundenstand beim Umstellungszeitpunkt, oder gibt es eine maßgebliche historische Quelle? Ohne diese Entscheidung kein Backfillplan/READY. Keine ursprünglichen Werte erfinden. |
| edit-own-customers | Danach eigenes PUT /api/partner/customers/{id}, bestehende Pflichtfelder, serverseitiger Eigentümer, fremde/unbekannte ID 404. Keine Company-Kundenpflege/Löschung. | Snapshotstrategie und vollständiger Schutz aller alten Bestellungen zuerst. Atomare Änderungen/Snapshotkopie testen; alte Belege/CSV bleiben, neue Orders nutzen neue Stammdaten. Kein Live-Fallback. |
| Keycloak-ID-Bindung | Ein gemeinsamer Partnerzugang, künftig eindeutiges issuer/sub statt sichtbarer Nummer. Keine Tenant-/Organizationarbeit. | Verifiziertes Partner-ID↔issuer/sub-Mapping, Behandlung unzugeordneter/mehrdeutiger Konten; autorisierte Einrichtung/Neubindung/Entzug und sicherer Rückfall fehlen. Kein Username-Fallback, keine Partner-Selbstauskunft/Erstloginbindung. |
| Automatische Partnernummern | Bestandsnummern bleiben; neue P-Nummern oberhalb vorhandener numerischer P-Nummern, Lücken erlaubt, keine Wiederverwendung; parallele Anlage/Import zentral absichern. | Sichere Loginentkopplung sowie Importvertrag für bisherige externe Nummer/Quellenkennung und Dublettenschlüssel fehlen. Nicht als reine Profilanzeige planen: die Anzeige existiert bereits. |
| Ansprechpartner / weitere Stammdaten | Firmenanzeige nutzt vorerst vorhandenen Namen; Ansprechpartner bleibt separat. | Pflichtfelder, Company-/Partneränderungsrechte, Zuordnung bestehender Namen, Notizsichtbarkeit und unterstützte Steuerkennungsarten/Validierung fehlen. Je Anliegen später eigener kleiner Change, kein Sammelmodell. |
| Partnernachweis | Aktueller Unternehmensnachweis als eigenes sensibles Anliegen. | Nachweisarten, Dateigrenzen, Speicher, autorisierter Download, Austauschhistorie, Aufbewahrung/Löschung und Aktivierungsübergang fehlen. Keine Upload-/Storageplanung ohne Regeln. |
| BMD-Artikelnummer | Fachliche Artikelnummer; keine zweite sichtbare technische ID. | Verbindliches Export-/Spaltenformat, Pflicht/Änderbarkeit, Bestandszuordnung sowie Import-Aktualisierung/Dedupe fehlen. Kategorienverwaltung ist davon unabhängig. |
| Company-Logo/Begrüßung | Früherer Personalisierungswunsch bleibt eigenständig. | Pflegeort/-recht, Logoablauf und statische Konfiguration versus Company-Datenhaltung fehlen. Partnername löst diesen anderen Scope nicht. |
| Reale E-Mails | Drei Bestell-Empfänger als Ziel; Bestellung vor Versand sicher speichern. | Bestätigter SMTP-/Absenderstand, Empfängeradressen/-regeln, Datenschutz und Retry-/Betriebszuständigkeit fehlen. Keine Zugangsdaten anfordern/lesen. |
| In-App-Benachrichtigungen | Company-Neubestellungen teilweise bereits vorhanden. | Empfänger-/Gelesenmodell für Company, Statusereignisse und Abgrenzung von Versandfehlern fehlen. Keine pauschale E-Mail-Abhängigkeit für vorhandene Funktion erfinden. |
| MVP-Abschluss / Präsentation | Tobias: 8.3 und FD.4, Deck/Screenshots. Gemeinsam: 8.4. | Konkrete UI-Belege/Abnahme und gemeinsame technische Zuordnung fehlen. Format-Change schließt keine dieser Aufgaben. |
| CI/CD | Lokaler Merge fea0e5b enthält Frontendreparatur; kein aktueller Laufzeitbeleg. | Tobias: laufende Revision + HTTPS/OIDC/API/Login/Bestellung; Frontendpfad/Reparatur/Recovery; getrennt PR-5-Befehls-/Schlüsselgrenzen und negative Tests. Alte 404/502 nicht als aktuelle Diagnose behaupten. |

Nicht einplanen: Mandantenfähigkeit, Subdomains, öffentlicher Partnershop, direkte BMD-Schnittstelle, Fachbereiche, Produktgruppen, Paketlogik und KI-Feature.

## Unabhängige Reviews und eingearbeitete Korrekturen

- Securityreview durch `security_review`, Funktions-/Test-/Sinnhaftigkeitsreview durch `functional_review`: vollständige sechs neue Artefaktsätze, Endkundenkorrektur und bestehende kleine Changes geprüft.
- Endkundenanlage: UI-Submit von HTTP-Versuchen trennen; bestehender einmaliger 401-Refresh-Retry erhalten, kein zusätzlicher Transportretry.
- Kundensuche: bestehendes maxStepReached genügt nicht. Gemeinsames Kunden-Weitergate inklusive direkter bereits besuchter Checkpoints und geänderter Draft-Pflichtfelder festgelegt.
- Kategorien: einheitlich Kategorien-IDs vor Artikel-IDs, Refresh/Recheck und 409 bei Zuordnungsdrift; Rename entdeckt Artikel erst nach Kategorielock. Import sammelt/sperrt/refresht vor endgültiger Zeilenvalidierung.
- Unabhängiger Datenbankreview durch `category_plan_review`: der zuvor wegen Nutzungslimit abgebrochene Teilreview wurde in dieser Umsetzung fortgesetzt und abgeschlossen. Zusätzlich Legacy-null beim Entfernen, Unicode-Normalisierungslänge 510, LEFT JOIN FETCH und EnablementDto-Pfad aufgenommen.
- Projektcheck `partnerportal-quality`: ausschließlich Planungs-/Dokumentationsänderungen; deutsche Texte, Rollen, Partnerisolation, feste Stack-/Geld-/Schema-/Scopegrenzen geprüft. Keine fremden Produktänderungen als eigene bewertet.
- Abschließende Datenbank-, Security- und Funktionsreviews: alle Findings eingearbeitet; neun kleine Changes READY. MVP-/CI-CD-Blocker und offene Folgeentscheidungen bleiben.

## Tatsächliche Prüfungen und Grenzen

Abschließender Artefaktlauf am 11.09.2026: `openspec validate --changes --strict --json --no-interactive` — 11/11 gültig, keine Findings. `git diff --check` — erfolgreich. Der SHA-256 des bereits vorhandenen Produkt-/Präsentationsdiffs blieb vor und nach der Planungsarbeit identisch: `a5b58fafb581d0b8edfba9b7d5acfaec52179bdfeb399a66094ae272c4697f5b`.

Keine Backend-/Browser-/Produkt-/Datenbanktests ausgeführt: diese Sitzung setzt ausschließlich Planung um. Vorhandene Produkt-/Präsentationsdiffs werden vor/nach verglichen.

`python3 tools/ai_evidence_log.py sync` nicht ausgeführt: das Skript liest standardmäßig externe Sessionarchive und schreibt ins private Roharchiv (tools/ai_evidence_log.py:21–23,194–217). Diese Zugriffe sind im engen Auftrag nicht freigegeben. Auch keine fingierte Enrichment-Zeile; dieser Repo-Report hält Zweck, Ergebnis und echte Prüfungen fest.
# Fortschreibung — 14.09.2026 Präsentation

Die Präsentation wurde auf 22 Folien fertiggestellt vorbereitet. Sie verwendet „Artikel“, erklärt
Rollen und Bereitstellung verständlich für die Unternehmenszielgruppe und enthält GitHub-Stars,
digitale Souveränität, zwei zusätzliche echte Screenshots, eine Clockify-Übersicht für Chris und
Tobias sowie ein Demo-Drehbuch. Die fehlende Clockify-Istsumme bleibt als technischer Zugriffspunkt
dokumentiert; der Zielwert „ca. 400 h“ wird nicht als Ist-Zeit ausgegeben.
