# PAPO-Planungs- und Umsetzungshandoff – 11.09.2026

## Fortschreibung — 11.09.2026

Maßgeblich: [vollständiger Ergebnisreport](../openspec/PLANNING_REVIEW_2026-09-11.md). Der genehmigte Plan wurde in sechs getrennten neuen OpenSpec-Changes umgesetzt; elf aktive Changes insgesamt. Der Endkundenplan erhält einen präzisen UI-Submit-/HTTP-401-Retry-Vertrag. Vorherige Kandidaten-/READY-Tabellen unten sind historisch.

Bestätigt: bestehender Partnername als Firmenname; synthetische Huemer GmbH; verwaltete Kategorien mit kontrolliertem Import; Kundensuche/Neuanlagebutton; Kundenbezug und links/rechts-Anordnung. Keycloak-ID statt Nummer als künftige Loginbindung; vorhandene Partnernummern bleiben. Kundenbearbeitung nur nach Schutz historischer Kundendaten.

Kein Produktcode dieser Sitzung. Neue Changes besitzen Proposal, Design, Delta-Spec und Tasks mit Akzeptanz, Daten/Security, Tests, DoD und Stop. Finale Status im Ergebnisreport; kein bedingtes READY. MVP-Abschluss und CI/CD bleiben mit konkreten fremden Nachweisen offen.

### Nächste Sitzung

1. Branch/Worktree prüfen und vorhandene Änderungen bewahren. Aktueller Planungsbranch `docs/openspec-planning-2026-09-11`; kein automatischer Stash/Branchwechsel/Pull.
2. Ergebnisreport und seitdem geänderte Quellen lesen; neuestes Protokoll ist `Besprechungsprotokoll_10.9.md`.
3. Nur beauftragten READY-Change umsetzen. Empfohlene Reihenfolge im Report; eine Capability je Change.
4. Snapshot: Chris entscheidet Altbestandsquelle oder Umstellungsstand. Login: verifiziertes issuer/sub-Mapping und Bindungsprozess fehlen. Ohne Antworten keine Migration beginnen.
5. Neue Verifikation exakt dokumentieren; historisches 11/11-Validierungsergebnis oder alte Browserchecks sind keine neue Produktregression.

### Terra-medium-Goal zur taskweisen Umsetzung

```text
/goal Implementiere die bestätigten PAPO-OpenSpec-Changes taskweise mit Terra medium. Lies zuerst vollständig AGENTS.md, CLAUDE.md, Specification/CONTINUATION_PROMPT.md, Specification/NEXT_SESSION_PLAN.md und openspec/PLANNING_REVIEW_2026-09-11.md. Prüfe Branch, git status und openspec list; bewahre alle fremden und uncommitteten Änderungen. Kein Fetch/Pull bei Dirty Worktree. Kein Commit, Push, Merge, Deployment, VM-, Secret-, SSH-, Backup- oder GitHub-Secrets-Zugriff. Nutze für jeden Change einzeln openspec-apply-change: Change ankündigen; openspec status --change <change> --json und openspec instructions apply --change <change> --json ausführen; alle dort genannten Context-Files vollständig lesen; dann offene Tasks der Reihe nach umsetzen und jede Checkbox unmittelbar nach erfüllten Akzeptanzkriterien, Tests und DoD setzen. Bei Stopbedingung, unklarer Anforderung oder Designfehler nicht raten: genauen Blocker im Change dokumentieren und den Change pausieren. Reihenfolge: align-visible-article-terminology, fix-partner-create-commission-default, add-standalone-customer-creation, show-partner-company-name, use-huemer-demo-partner, search-order-customers, clarify-order-customer-context, manage-article-categories, define-mvp-traceability-format. Pro Change nur dessen Scope; keine Mandantenfähigkeit, Subdomains, öffentlichen Partnershop, direkte BMD-Schnittstelle, Fachbereiche, Produktgruppen, Paketlogik oder KI-Features ergänzen. Nach jedem Task passende fokussierte Tests ausführen. Nach jedem fertigen Change openspec validate <change> --strict, relevante Backend-Gesamttests und package bzw. Desktop-/Mobile-Browserprüfung ausführen. Danach partnerportal-quality, unabhängigen Funktions-/Test-/Sinnhaftigkeitsreview und bei Daten-, Auth-, Rollen- oder Eingabeänderungen unabhängigen Securityreview durchführen; Findings beheben, erneut testen und validieren. Einen Change erst als fertig melden, wenn alle Tasks, Akzeptanzkriterien, Daten-/Securityfolgen, Tests und DoD belegt sind. Nicht automatisch archivieren; fertigen Change samt exakten Prüfergebnissen und nächstem Change melden und weiter iterieren. Am Ende CONTINUATION_PROMPT.md, NEXT_SESSION_PLAN.md, RESTARBEITSPLAN_2026-09-09.md und den Planning-Report mit dem tatsächlichen Stand aktualisieren, KI-Nachweis synchronisieren/anreichern und sichere, kurze Commit-Vorschläge mit expliziten git-add-Pfaden liefern. Keine Prüfung erfinden.
```

Der alte nächste Astra-Goal darunter ist durch diese abgeschlossene Planung ersetzt; nicht dieselben Entwürfe erneut von null erstellen.

> **Fortsetzung nach OpenSpec-Planung, Stand 10.09.2026:** Dieser Abschnitt ersetzt die darunterliegende ursprüngliche Startlage. Nicht wieder bei null beginnen. Drei Changes sind geplant und streng validiert: `add-standalone-customer-creation`, `fix-partner-create-commission-default`, `define-mvp-traceability-format`. Detailstatus: `openspec/PLANNING_REVIEW_2026-09-10.md`. CI/CD und MVP-Abschluss bleiben blockiert. Die ältere Kandidatenliste darunter bleibt als Herleitung erhalten.

## Nächster Astra-Goal

```text
/goal Setze die PAPO-OpenSpec-Planung am dokumentierten Stand vom 10.09.2026 fort, ohne Produktimplementierung, Commit, Push, Merge, Deployment, VM-, Secret-, SSH-, Backup- oder GitHub-Secrets-Zugriff. Arbeite knapp und quellenbezogen: lies jede relevante Datei einmal, nutze rg und gezielte Ausschnitte statt wiederholter Vollreads. Prüfe zuerst den realen Workspace; bewahre alle uncommitteten Änderungen. Wenn der Arbeitsbaum sicher sauber bzw. durch den Benutzer gesichert ist, führe git fetch --prune und git pull --ff-only auf dem aktuellen Zielbranch aus; bei Dirty Worktree, Divergenz oder Konfliktgefahr nicht pullen, sondern genauen Blocker melden. Ermittle danach das neueste Besprechungsprotokoll in Documentation/Besprechungsprotokolle/, lies und analysiere es vollständig und vergleiche seine Entscheidungen mit AGENTS.md, Specification/CONTINUATION_PROMPT.md, Specification/NEXT_SESSION_PLAN.md, docs/RESTARBEITSPLAN_2026-09-09.md, FSD, Roadmap, Phase-2-Backlog, kanonischen Specs, openspec list --json sowie allen aktiven Proposals/Designs/Specs/Tasks/Reviews. Übernimm neue bestätigte Anforderungen ausschließlich als kleine getrennte OpenSpec-Changes; ändere oder blockiere bestehende Changes, wenn das neueste Protokoll Scope, Daten, Security, UI, Tests oder Abhängigkeiten ändert. Prüfe zuerst die drei als READY dokumentierten Changes add-standalone-customer-creation, fix-partner-create-commission-default und define-mvp-traceability-format gegen den dann aktuellen Git-/Besprechungsstand; keine Umsetzung. Danach prüfe jeden übrigen Chris-Kandidaten einzeln. Lege nur für fachlich bestätigte, nicht extern blockierte Arbeit vollständige proposal.md, design.md, Delta-Spec und tasks.md an. Jede Task braucht Voraussetzungen, Komponenten, Akzeptanzkriterien, Daten-/Securityfolgen, Tests, DoD und Stopbedingung. Extern oder fachlich offene Kandidaten erhalten nur die genaue fehlende Entscheidung, nie Terra-READY. Mandantenfähigkeit, Subdomains, öffentlicher Partnershop, direkte BMD-Schnittstelle, Fachbereiche, Produktgruppen, Paketlogik und KI-Feature nicht einplanen. CI/CD ausschließlich als Tobias-Blocker reviewen; P0-Ausfall, Frontendpfad und PR #5 getrennt halten. Abschließend jeden aktiven/relevanten Change mit openspec validate --strict, unabhängigem Securityreview und unabhängigem Funktions-/Test-/Sinnhaftigkeitsreview prüfen. Zielzustand: jeder bestätigte Chris-Task besitzt einen entscheidungskompletten kleinen OpenSpec-Change und Status READY für Terra medium; jeder andere Kandidat besitzt eine konkrete offene Entscheidung oder Evidenz und Status BLOCKIERT. Aktualisiere danach Continuation Prompt, Next Session Plan, Restarbeitsplan und openspec/PLANNING_REVIEW_2026-09-10.md mit tatsächlichen Ergebnissen. Keine Prüfungen erfinden.
```

## Startreihenfolge nächste Session

1. `git status --short --branch`, `git log -10 --oneline --decorate`, `openspec list --json`.
2. Nur bei sicher sauberem/gesichertem Arbeitsbaum: `git fetch --prune`, dann `git pull --ff-only`. Nie lokale Änderungen überschreiben oder stashen ohne Auftrag.
3. Neuestes Besprechungsprotokoll per Dateiliste bestimmen, vollständig lesen, Änderungen zum Stand 10.09 herausarbeiten.
4. Pflichtkontext und relevante OpenSpec-Artefakte gezielt lesen; keine wiederholten Vollreads ohne neue Revision.
5. Drei READY-Changes zuerst auf Drift prüfen; danach blockierte Kandidaten und neue Besprechungsentscheidungen.
6. Alle Changes streng validieren und unabhängig reviewen; Kontextdateien zuletzt synchronisieren.

## Aktueller Abschlussstatus

- READY: `add-standalone-customer-creation` (0/6), `fix-partner-create-commission-default` (0/2), `define-mvp-traceability-format` (0/3).
- BLOCKIERT: `build-partnerportal-mvp` (50/53: 8.3, 8.4, FD.4), `ci-cd-deployment-with-gh-actions` (30/41 plus aktuelle Tobias-Nachweise).
- Nicht geplant wegen offener Entscheidungen: Partneridentität/Stammdaten außer 5 %, Partnernachweis, Artikel-Minimalmodell, Kategorien, Personalisierung, reale E-Mails, In-App-Benachrichtigungen.
- Aktiver Workspace war bei Abschluss schmutzig; deshalb kein Pull. Branch zum letzten Check: `main`, Tracking `origin/main`; Remote-Inhalt nicht neu abgerufen.

## Ziel

Prüfe zuerst vorhandene, uncommittete Planentwürfe. Plane danach verbleibende bestätigte PAPO-Arbeit einzeln mit OpenSpec. Nach diesem Block ist jeder nicht extern blockierte Chris-Task vollständig für `GPT-5.6 Terra medium` implementierbar. Nicht lösbare externe Abhängigkeiten werden als präzise Entscheidung dokumentiert, nie als fertige Terra-Task ausgegeben.

## Pflichtlektüre

1. `AGENTS.md`
2. `Specification/CONTINUATION_PROMPT.md`
3. dieses Dokument
4. `docs/AI_WORKFLOW_EFFICIENCY.md`
5. Besprechungsprotokolle 25.08. und 31.08.2026
6. `Specification/ROADMAP_2026-08-25.md`, FSD und Phase-2-Backlog
7. `openspec list --json`, dann vollständige Artefakte der aktiven Changes
8. Code und Tests der jeweils betroffenen Capability

## Arbeitsauftrag für Astra

```text
Arbeite ausschließlich als Planungs-, OpenSpec- und Review-Session für PAPO. Lies AGENTS.md, CONTINUATION_PROMPT.md, NEXT_SESSION_PLAN.md und docs/AI_WORKFLOW_EFFICIENCY.md vollständig. Prüfe Git/OpenSpec-Stand. Keine Produktimplementierung, kein Commit, Push, Merge, Deployment, VM-, Secret-, SSH-, Backup- oder GitHub-Secrets-Zugriff.

Prüfe zuerst den uncommitteten Entwurf `openspec/changes/add-standalone-customer-creation/`. Er wurde vor dieser Astra-Session erstellt und gilt nicht als freigegeben oder Terra-ready. Prüfe fachliche Grundlage, Scope, Partnerisolation, Tests, Tasks, UI-Verhalten und Abhängigkeiten unabhängig. Korrigiere oder verwerfe ihn, wenn er nicht belastbar ist. Erst nach diesem Review darfst du seinen Status als READY oder BLOCKIERT bewerten.

Gehe Kandidat für Kandidat in dieser Reihenfolge vor. Prüfe jeweils aktuelle Quellen, Code und Tests; kläre Widersprüche als Dokumentationslücke. Lege pro bestätigter Capability genau einen kleinen OpenSpec-Change mit proposal.md, design.md wenn Entscheidungen nötig sind, Delta-Spec und tasks.md an. Jede Task braucht konkrete Akzeptanzkriterien, Sicherheits-/Datenfolgen, Tests, DoD und Stopbedingung. Kein Terra-Ready, solange eine fachliche, externe oder Sicherheitsentscheidung fehlt.

1. Unabhängige Endkundenanlage: Roadmap behauptet sie sei vorhanden, `partner-customer-list-page.js` zeigt nur Liste. Beweise Ist-Zustand und plane nur bei echter Lücke.
2. Partneridentität und Stammdaten: automatische P-Nummer, externe Referenz, Firma/Ansprechpartner, Notiz, Steuerkennung, 5-%-Standard.
3. Partnernachweis: erst planen, wenn Speicher-, Zugriffs-, Lösch- und Nachweisregeln belegbar entschieden sind; sonst exakte externe Fragen dokumentieren.
4. Artikel-Minimalmodell: Artikelnummer, BMD-Import und Dedupe nur nach vereinfachter Firmenmail bzw. Beispieldaten; Fachbereich, Gruppen und Pakete ausschließen.
5. Kategorienverwaltung: globale Kategorieverwaltung ohne Tenant-Vorgriff; Abhängigkeit zu Artikel-Minimalmodell begründen.
6. Kleine Unternehmenspersonalisierung: Name, Logo, Begrüßung; keine Theme-Engine.
7. Reale Bestell-E-Mails: nur mit geklärtem SMTP-/No-reply-Stand, persistierter Bestellung vor Versand, Ereignis-ID, Status und Retry.
8. Wesentliche In-App-Benachrichtigungen: nach E-Mail-Entscheidung; Glocke, Ungelesen-Zähler, letzte 20, Link, gelesen; keine WebSockets.
9. MVP-Dokumentation: 8.4 nur als gemeinsames Traceability-Format planen. 8.3 und FD.4 sind Tobias-Aufgaben, nicht übernehmen.
10. CI/CD: nur Planreview für Tobias. Aktuelle P0-Störung, Frontend-Path-Fehler und Closed PR #5 getrennt bewerten; keine Infrastrukturänderung planen, solange VM-Diagnose fehlt.

Mandantenfähigkeit, Subdomains, öffentlicher Partnershop, direkte BMD-Schnittstelle, echte Produktgruppen/Fachbereiche/Paketlogik und KI-Feature nicht als aktive MVP-Changes planen.

Zum Schluss: jeden Change mit openspec validate prüfen; unabhängigen Security- und Funktions-/Testreview durchführen. Prüfe Scope, Rollen-/Partnerisolation, Datenmodell, Migration, Uploads, Fehlerpfade, Tests, Verifikation und Terra-Readiness. Liefere pro Change READY oder die genaue fehlende Entscheidung. Kein Implementieren.
```

## Aktuelle Planungskandidaten

| Rang | Kandidat | Status für Planung |
|---|---|---|
| P0 | Live-System, Frontend-Sync, Forced-Command-Deploy | Tobias; VM-Diagnose und Freigabe fehlen |
| P0 | MVP 8.3, 8.4, FD.4 | 8.3/FD.4 Tobias; 8.4 gemeinsam |
| P1 | unabhängige Endkundenanlage | unreviewter Entwurf `add-standalone-customer-creation`; Astra prüft ihn zuerst unabhängig |
| P1 | Partneridentität und Stammdaten | Blockiert: aktuelle Partnernummer ist Keycloak-Login; Zielnummer/Legacy-Referenz und Migrations-/Loginablauf müssen zusammen entschieden werden |
| P1 | Partnernachweis | Blockiert: Speicher, autorisierter Download, Lösch-/Aufbewahrung und Nachweisprozess fehlen |
| P1 | Artikel-Minimalmodell | Blockiert: vereinfachte Firmenmail und BMD-Beispieldaten fehlen |
| P2 | Kategorienverwaltung | Blockiert durch Artikel-Minimalmodell; bestehendes Freitextfeld darf nicht ohne bestätigte Migrationsregel ersetzt werden |
| P2 | Unternehmenspersonalisierung | Blockiert: kein `Company`-Datenmodell in Ein-Unternehmen-Architektur; Entscheidung zwischen statischer Deploy-Konfiguration und späterer Tenant-Entität fehlt |
| P2 | reale E-Mails | Blockiert: SMTP-/No-reply-Zugang, Absender, Empfänger- und Retry-Betriebsregeln fehlen |
| P2 | In-App-Benachrichtigungen | Teilweise vorhanden für neue Company-Bestellungen; Erweiterung blockiert durch Company-Mitarbeiter-/Empfängermodell und E-Mail-Entscheidung |

## Bekannte Dokumentationslücken

- `build-partnerportal-mvp` ist 50/53 offen: 8.3, 8.4, FD.4.
- `ci-cd-deployment-with-gh-actions` ist 30/41 offen; einzelne Tasks 3.5/3.6 und Domain-/HTTPS-Angaben sind laut überprüftem GitHub-/Live-Stand veraltet. Nicht still abhaken.
- Roadmap nennt unabhängige Endkundenverwaltung erledigt; aktuelle Listen-Seite besitzt keinen Erstellen-Dialog. Code schlägt hier Dokumentationsbehauptung.
- Der 5-%-Standard ist im aktuellen Formular nicht umgesetzt: `openCreate()` setzt 10 %.
- `add-standalone-customer-creation` ist ein uncommitteter Vorentwurf. Erst Astra-Review entscheidet READY oder BLOCKIERT.
