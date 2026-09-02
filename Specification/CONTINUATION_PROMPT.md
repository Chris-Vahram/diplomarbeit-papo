# Continuation Prompt — PAPO Partnerportal

**Dokumentstand:** 01.09.2026

**Zweck:** vollständiger operativer Handoff für eine neue Claude-/Codex-Session

**Repository:** `/Users/mitgl/Desktop/Schule/4CHITM/Diplomarbeit/diplomarbeit-papo`

Diese Datei ist so geschrieben, dass ein neues Modell nach vollständigem Lesen das Projekt, den implementierten Stand, die fachlichen Grenzen und die nächste Reihenfolge versteht. Flüchtige Angaben zu Git, Tests, Infrastruktur und OpenSpec müssen trotzdem vor jeder Arbeit neu verifiziert werden.

````text
Du setzt die Arbeit am PAPO-Partnerportal fort. Lies diesen Continuation Prompt vollständig, bevor du Änderungen vorschlägst oder ausführst. Übernimm keine alten Erinnerungen ungeprüft.

# 1. Pflichtlektüre und Quellenhierarchie

Lies und analysiere in einer frischen Session mindestens diese Dateien, bevor du tief am Projekt arbeitest:

1. `AGENTS.md` — Arbeits-, Sicherheits-, Git- und Verifikationsregeln für Codex.
2. `CLAUDE.md` — stabiler Produktkontext, Stack, Rollen und Scope.
3. `Specification/CONTINUATION_PROMPT.md` — diese vollständige operative Übergabe.
4. `Documentation/Besprechungsprotokolle/Besprechungsprotokoll31_08_2026.md` — neueste fachliche Entscheidungen.
5. `Documentation/Besprechungsprotokolle/Besprechungsprotokoll25_08_2026.md` — vorherige Produkt-/Präsentationsentscheidungen.
6. `Specification/functional_specification_document_partnerportal_final.md` — FSD v2 mit FR-01..FR-25 als implementierte Baseline.
7. `Specification/phase2_backlog.md` — B-01..B-08 und Entscheidungshistorie; spätere Themen nicht still in die aktuelle Stufe ziehen.
8. `Specification/ROADMAP_2026-08-25.md` — gespeicherter Zukunftsplan, aktualisiert durch die Entscheidungen vom 31.08.2026.
9. `Specification/IMPLEMENTATION_PLAN.md` — ursprüngliche Arbeitspakete und Risiken.
10. `docs/CONVENTIONS.md`, `docs/SETUP.md`, `docs/DATA_MODEL.md` — Code-, Runtime- und Datenmodellregeln.
11. `openspec list --json`, danach Proposal/Design/Specs/Tasks jedes für die Aufgabe relevanten Changes.

Nur bei Arbeit am späteren Gesamtprodukt zusätzlich `Specification/functional_specification_document_full_vision.md` lesen. Dieses ältere Visionsdokument darf neuere Besprechungen oder die Roadmap nicht überstimmen.

Quellenrang bei Konflikten:

1. explizite aktuelle Benutzerentscheidung;
2. neueres Besprechungsprotokoll;
3. genehmigter aktiver OpenSpec-Change für die konkrete Änderung;
4. FSD und kanonische OpenSpec-Specs;
5. Phase-2-Backlog und Roadmap;
6. ältere Pläne, Präsentationen und Agentenerinnerungen.

Die neueste Entscheidung ist nicht automatisch implementiert. Code/FSD bleiben bis zu einem passenden OpenSpec-/Dokumentationschange im bisherigen Zustand.

# 2. Projekt, Ziel und Forschungsfrage

PAPO ist der Projekt- und Produktname der Diplomarbeit, nicht der Name des Referenzunternehmens. Das reale Unternehmen bleibt in öffentlicher Dokumentation ungenannt.

Team:

- Chris: Implementierung, Backend und funktionales Frontend.
- Tobias: visuelles Frontenddesign; Präsentationen und Project-Award-Analyse.
- Julian ist nicht mehr im Team.
- Zielwert für Chris und Tobias zusammen: rund 400 nachvollziehbar in Clockify dokumentierte Arbeitsstunden. Auch Planung, Meetings, Dokumentation, Tests, Deployment und Präsentation zählen.

Fachliche Kette:

```text
Unternehmen -> Partnerfirma -> Endkunde
```

Problem des Referenzfalls: Partnerbestellungen kommen derzeit manuell per E-Mail oder Telefon. Das erste funktionsfähige Produktstadium ersetzt diesen Ablauf durch strukturierte Bestellungen im internen Portal.

Aktueller Kernprozess:

```text
Partnerfirma meldet sich an
  -> sieht nur freigeschaltete Artikel
  -> wählt oder erstellt einen Endkunden
  -> legt mehrere Artikel und Mengen in die Bestellung
  -> sendet die Bestellung ab
  -> Bestellung wird automatisch dem Partner zugeordnet
Unternehmen sieht und bearbeitet die Bestellung
  -> submitted -> reviewed -> fulfilled
```

Die aktuelle Demo enthält nur ein Unternehmen. Die Diplomarbeit ist aber kein dauerhaftes „Ein-Unternehmen-MVP“. Das Ergebnis soll ein funktionierendes und weiter ausbaufähiges Produkt sein. Die Ein-Unternehmen-Demo ist der gegenwärtige Implementierungsstand, nicht die endgültige Produktgrenze.

Bestätigte Forschungsfrage (Wortlaut nicht still ändern):

> Wie kann ein webbasiertes Partnerportal gestaltet werden, das Unternehmen, Vertriebspartner und Endkunden in einem partnerbasierten Vertriebsmodell schrittweise verbindet und dabei Partnerverwaltung, Produktfreigabe, Bestellabwicklung, Provisionszuordnung und Auswertungen in einer benutzerfreundlichen und erweiterbaren Plattform zusammenführt?

Die neuere fachliche Terminologie verwendet „Artikel“. Ob in der bereits bestätigten Forschungsfrage „Produktfreigabe“ rein sprachlich zu „Artikelfreigabe“ geändert wird, muss transparent als Terminologieänderung behandelt werden; nicht unbemerkt umschreiben.

# 3. Produktstufen und klare Scope-Grenzen

## 3.1 Aktuelle funktionsfähige Stufe

- interner B2B-Bestellprozess;
- ein gemeinsamer organisatorischer Zugang je Partnerfirma;
- Partner- und Artikelverwaltung durch das Unternehmen;
- Excel/CSV-Import mit Spaltenzuordnung, Dublettenerkennung und Validierungsbericht;
- Artikelfreischaltung je Partner;
- wiederverwendbare Endkundenverwaltung;
- Bestellung mit mehreren Artikeln und Mengen;
- Preis-Snapshot je OrderItem;
- Provision je Bestellung;
- Company- und Partner-Dashboards;
- Bestellstatus `submitted -> reviewed -> fulfilled`;
- Bestätigung in der Anwendung und druckbarer Beleg;
- E-Mail derzeit simuliert, nicht real versendet.

## 3.2 Spätere Mandantenarchitektur

Sobald ein zweites Unternehmen aufgenommen wird, ist eine gemeinsame sicher mandantenfähige Plattform das richtige Ziel:

```text
firma-a.paportal.at -> Daten von Unternehmen A
firma-b.paportal.at -> Daten von Unternehmen B
```

Dann gelten als Ziel:

- eine gemeinsame PAPO-Anwendung;
- klare `Company`-/Tenant-Entität;
- `company_id` auf allen unternehmensbezogenen Datensätzen;
- zentrale Tenant-Auflösung;
- strikte Cross-Tenant-Negativtests;
- voraussichtlich ein Keycloak-Realm mit Organization je Unternehmen;
- Hostname, Organization-Claim und Datenbankmandant müssen zusammenpassen;
- lokale sichtbare Partnernummern wie `P-0001` dürfen pro Unternehmen vorkommen, intern wird eine globale technische ID verwendet.

Wichtig: Mandantenfähigkeit ist ausdrücklich **nicht der nächste Implementierungsschritt**. Bis wann und wie weit sie Teil der Diplomarbeit wird, ist noch nicht festgelegt. Sie wird erst gestartet, wenn ein zweites Unternehmen aufgenommen oder ein verbindlicher Diplomarbeitstermin dafür gesetzt wird. Dann zuerst eigener Architektur-/Migrationsspike, nicht sofort alle Tenant-Funktionen bauen.

## 3.3 Öffentlicher Partnershop

Langfristig kann jeder Partner einen öffentlichen Shop erhalten, über den Endkunden selbst bestellen. Das ist nicht dasselbe wie Mandantenfähigkeit und nicht Teil der aktuellen Kernstufe. B-01/B-02 bleiben später. Keine Shopimplementierung ohne eigene fachliche Spezifikation des Referenzunternehmens.

# 4. Rollen und Zugänge

## Company

- verwaltet Partner und Artikel;
- importiert Stammdaten;
- schaltet Artikel partnerbezogen frei;
- sieht alle Bestellungen und Provisionen;
- ändert Bestellstatus;
- gibt Provisionsprozentsatz vor.

Das spätere Modell für mehrere Company-Mitarbeiterzugänge ist noch offen. Nicht spekulativ implementieren.

## Partner

- B2B-Partner ist selbst eine Firma;
- aktuelle Stufe: genau ein gemeinsamer organisatorischer Zugang je Partnerfirma;
- keine persönlichen Partnermitarbeiterkonten erforderlich;
- sieht nur eigene Endkunden, Bestellungen, Provisionen und freigeschaltete Artikel;
- darf zentrale Artikel-/Provisionsdaten nicht verändern;
- inaktive Partner werden nach Authentifizierung serverseitig vor Schreibvorgängen blockiert.

Operativ noch festzulegen: zentrale Login-/Reset-E-Mail und geregelte Übergabe des gemeinsamen Zugangs bei Mitarbeiterwechsel. Die Endkunden-E-Mail bleibt optional.

## Endkunde

- besitzt in der aktuellen Stufe keinen Login;
- wird vom Partner angelegt oder ausgewählt;
- Bestellung wird vom Partner auf dessen behalf erfasst;
- Nachname und Adresse sind Pflicht; Vorname, E-Mail und Telefon optional;
- sieht derzeit Bestätigung über den Partner beziehungsweise den gedruckten/simulierten Beleg.

# 5. Fachliche Terminologie und Artikelstammdaten

In UI und fachlicher Dokumentation einheitlich **Artikel** verwenden. Das Referenzunternehmen unterscheidet derzeit nicht zwischen Produkt und Artikel. Interne technische Namen (`Product`, Product-API, Tabellen-/Paketnamen) müssen nicht ohne technischen Nutzen umbenannt werden.

Aus der Firmenmail und Besprechung vom 31.08. folgt:

- BMD ist das führende System für Artikelstammdaten.
- Die eindeutige BMD-Artikelnummer ist die sichtbare fachliche Kennung.
- PAPO besitzt intern bereits eine technische Datenbank-ID; keine zweite sichtbare „Artikel-ID“ hinzufügen.
- `Preislistenbereich` wird für PAPO nicht benötigt und entfällt.
- Artikelkategorie ist ein eigenes relevantes Merkmal.
- Kategorie kann später kaufmännische Provisions-/Rabattregeln beeinflussen; solche Regeln sind jetzt nicht beauftragt.
- Fachbereich ist eine spätere Grundlage für Schulungs-/Einschulungsberechtigungen.
- Produktgruppe A/B/C/D ist ein separates späteres Merkmal für PP-/Margenlogik.
- Artikelpakete/Stücklisten und eine übergeordnete Produktebene für Varianten sind spätere Möglichkeiten.

Der Betreuer bewertete den Gesamtumfang der ersten Firmenmail als zu groß. Eine vereinfachte Folgemail ist angekündigt. Vorher keinen Sammelchange für Artikelnummer, Kategorie, Fachbereich, Produktgruppe, Pakete und Provisionslogik anlegen. Die frühere Mail ist Architektur-/Zukunftskontext, keine vollständige aktuelle Anforderung.

Aktueller Codezustand vor einem neuen Artikelchange:

- `Product` hat technische Panache-ID, Name, Preis, Beschreibung, Verfügbarkeit, einfache Kategorie und Bild.
- Es gibt noch kein eigenes Feld für die fachliche Artikelnummer.
- Import-Dublettenerkennung verwendet derzeit den Namen, weil noch kein fachlicher Identifier existiert.
- Finales Mapping und Dedupe dürfen erst anhand bestätigter BMD-/Echtdaten geändert werden.

Direkte BMD-API/Schnittstelle bleibt nicht beschlossen. Kleinster aktueller Austausch:

```text
BMD -> Excel/CSV-Export -> PAPO-Import
```

# 6. Fester technischer Stack

- Frontend: statisches HTML/CSS/JavaScript, native Web Components/Custom Elements, Light DOM.
- Kein React/Vue/Angular, kein Node/npm, kein Bundler, keine CDN-Laufzeitabhängigkeit, kein globaler State Store.
- Backend: Java 21, Quarkus REST, Hibernate ORM Panache, OIDC.
- Datenbank: PostgreSQL.
- Auth: Keycloak Realm `partnerportal`, Rollen `company` und `partner`.
- Import: Apache POI für xlsx, opencsv für csv.
- Geld Backend: `BigDecimal`, PostgreSQL `numeric`.
- Frontend-Summen: Integer-Cents, nicht Float-Akkumulation.
- Schema aktuell über Hibernate-Generation; kein Flyway/manuelles DDL ohne bewusste Änderung der Konvention.
- dev/test: drop-and-create plus programmatic seed; demo/prod: update.

# 7. Aktuelles Datenmodell und harte Domänenregeln

Aktuelle Entitäten:

- `Partner`: u. a. partnerNumber, commissionPercent, active;
- `Product`: technischer Name, fachlich Artikel;
- `ProductImage`;
- `ProductEnablement`: Partner × Artikel;
- `Kunde`: ownerPartner-scoped;
- `Order` in Tabelle `orders`;
- `OrderItem`: Menge und unit-price snapshot;
- `CommissionRecord`.

Harte Regeln:

- `Partner.commissionPercent`: Presets 5/10/15 plus optionaler individueller Wert.
- Neue Partner sollen in der späteren Stammdatenerweiterung bei 5 % starten.
- Provision ist Prozentsatz des Bestellgesamtwerts und wird als eigener Record gespeichert.
- Kein `CommissionLevel`-Entity; automatischer Aufstieg bleibt später.
- Company allein ändert Bestellstatus.
- Partneridentität nie aus Client-Partner-ID vertrauen; immer `security/CurrentPartner.resolve()`.
- Kunden, Orders, Provisionen und freigeschaltete Artikel bleiben partner-scoped.
- Partner-Dashboard-Kennzahlen zählen nur eigene `fulfilled` Orders; Neueste-Orders-Liste zeigt alle Status.
- Inaktive Partner werden serverseitig blockiert; Keycloak hält diesen Businessstatus nicht.
- `Partner.partnerNumber` wird im Ist-Stand kanonisch großgeschrieben und noch mit dem Keycloak-Username-Claim verbunden. Kein separates `keycloakUsername`-Feld.
- Import erzeugt keine Keycloak-Benutzer.
- HTML aus Benutzer-/Importdaten mit `escapeHtml` ausgeben.
- Alle geschützten REST-Methoden mit korrektem `@RolesAllowed`.
- Requestdaten mit Jakarta Bean Validation und deutschen `ApiException`-Meldungen validieren.
- ORM/parameterisierte Panache-Abfragen verwenden; keine Secrets oder Stacktraces ausgeben.

# 8. Belegter Implementierungs- und Qualitätsstand

Stand der letzten verifizierten Regression (volatile Angaben neu prüfen):

- Audit Fix Batches A und B implementiert, reviewed und archiviert/synchronisiert.
- Batch-B-Archiv-/Spec-Sync im lokalen Verlauf: `94050cb`.
- Nachfolgender Dokumentationscommit: `d0ab7d5`.
- Historisch verifizierter Backend-Lauf: 77 Tests, 0 Failures, 0 Errors, 0 Skipped.
- UX-001..004 und TECH-001 stehen auf `Verified`.
- Browserflows für Company und Partner, Desktop/Mobile sowie mehrseitiger Druckpfad wurden geprüft.
- B-08 ist STUB: client-seitiger Vorperiodenvergleich im Partner-Dashboard vorhanden; vollständige/unternehmensweite Trendanalyse bleibt später.

Bereits umgesetzt:

- Rollen-/Partner-Scoping und inaktive Partnerprüfung;
- Partner-/Artikelverwaltung und Importgrundlage;
- partnerbezogene Artikelfreischaltung;
- Dashboards und Datumsfilter;
- Suche/Pagination/Filter in relevanten Listen;
- Bestellwizard und kanonische Partner-Bestellseite;
- Produkt-/Artikelkatalog mit Bildern, Warenkorb, Mengen und Live-Summen;
- mehrere Artikel je Bestellung;
- bewusste Endkundenauswahl oder Neuanlage;
- unabhängige Seite `Meine Endkunden` mit Dormanzanzeige;
- Bestellfilter nach Partner/Endkunde;
- neueste Partnerbestellung zuerst;
- Refresh-Token-Flow;
- lokal ausgelieferte Fonts;
- druckbarer Auftragsbeleg;
- Audit-Hardening und Datumskorrekturen.

# 9. Git- und Workspace-Snapshot vom 01.09.2026

Vor jeder Arbeit neu ausführen:

```bash
git status --short --branch
git log -10 --oneline --decorate
openspec list --json
```

Snapshot beim Schreiben:

- Branch `main`, zwei Commits vor `origin/main` (`94050cb`, `d0ab7d5`).
- Dokumentationsänderungen/-neuanlagen sind noch nicht committed.
- Untracked `tmp/` enthält gerenderte PDF-Arbeitsdateien und darf nicht mit `git add .` übernommen werden.
- Chris führt Commit/Push/Merge selbst aus; ohne explizite Freigabe nichts davon ausführen.
- Vor neuem Feature-Branch zuerst Dokumentationsstand und lokale Commits nachvollziehbar veröffentlichen.

Empfohlene getrennte Dokumentationscommits nach Review:

- `docs: record August project decisions`
- `docs(openspec): plan AI evidence automation`

Keine Claude-/Codex-Attribution in Commitmessages.

# 10. Aktive OpenSpec-Changes — priorisierte Reihenfolge

Taskzahlen sind Snapshotwerte und mit `openspec list --json` zu verifizieren.

## Rang 1 — `place-order-redesign` (36/37)

Nur Task 9.3 offen: manueller Smoke-Test mit bestehendem/neuem Endkunden, Rücknavigation ohne Datenverlust, leerem Warenkorb, fehlgeschlagener Übermittlung und `prefers-reduced-motion`. Danach validieren und archivieren. Keine neuen Features hineinziehen.

## Rang 2 — `automate-ai-evidence-log` (0/18)

Vollständig geplant und validiert, noch nicht implementiert. Früh umsetzen, damit weitere KI-unterstützte Arbeit automatisch nachweisbar wird:

- Codex-/Claude-Code-JSONL projektgenau erkennen;
- unveränderte private Belege und SHA-256-Manifest außerhalb Git;
- normalisierte menschliche Prompts/sichtbare Antworten;
- datensparsames Register plus `AUSSTEHEND`-Status;
- rückwirkender lokaler Import;
- optional bereitgestellte Webexporte;
- Hintergrund-LaunchAgent nur nach ausdrücklicher Freigabe außerhalb des Repositories installieren.

## Rang 3 — fertige, aber noch nicht archivierte Changes

Jeweils Delta-Spec prüfen, einzeln validieren und archivieren, ohne erneut zu implementieren:

- `add-order-document-print` 10/10;
- `remove-keycloak-username-field` 25/25;
- `partner-order-cart` 21/21;
- `add-dormant-kunde-visibility` 18/18;
- `add-order-list-filters` 15/15;
- `improve-order-form` 21/21;
- `add-keycloak-session-refresh` 14/14;
- `self-host-fonts` 4/4;
- `order-correctness-hardening` 19/19.

Nicht erneut archivieren, was bereits unter `openspec/changes/archive/` liegt.

## Rang 4 — `build-partnerportal-mvp` (50/53)

Offen:

- 8.3 UI/UX-Dokumentation der Screens, Komponenten und Flows;
- 8.4 Traceability FSD -> Implementierung -> Tests plus B-01..B-08;
- FD.4 visueller Polish der später ergänzten Screens.

Außerdem `docs/DATA_MODEL.md` bezüglich ProductImage synchronisieren und den funktionierenden Keycloak-Testpfad in `docs/SETUP.md` festhalten. Erst nach den echten offenen Aufgaben archivieren.

## Rang 5 — `ci-cd-deployment-with-gh-actions` (25/41)

Nicht blind fortsetzen: Der Change enthält ältere Oracle-/ARM-Annahmen. Tatsächlich gilt:

- Ziel: Hetzner x86_64 VM mit Docker/Compose;
- Domain `paportal.at` ist eingerichtet;
- offen: dedizierter Deploy-Key, GitHub-Secrets, GHCR-Login, Produktionsdateien, DNS/HTTPS, Keycloak-Proxy-Login, persistenter Redeploy, Backup und Offsite-S3;
- Oracle ist veraltet;
- Wildcard-Subdomains sind spätere Tenant-Arbeit und kein aktueller Deployment-Blocker;
- SMTP/no-reply und angebotenen S3-Zugang mit aktuellem externen Stand verifizieren, keine Zugangsdaten dokumentieren.

# 11. Reihenfolge zukünftiger Produkt-Changes

Der dauerhafte Plan steht in `Specification/ROADMAP_2026-08-25.md`. Diese Reihenfolge nicht aus alten Chats rekonstruieren.

Nach Rang 1–5 und jeweils nur als eigener OpenSpec-Change:

1. **Partneridentität und Stammdaten**
   - automatisch erzeugte interne Nummer `P-####`;
   - importierte bestehende Nummer als externe Referenz;
   - Firmenname und Ansprechpartner getrennt;
   - internes Notizfeld;
   - allgemeine Steuerkennung mit Land/Typ/Wert statt nur österreichischer UID;
   - neue Partner standardmäßig 5 % Provision.

2. **Partnernachweis**
   - aktueller Gewerbeschein oder gleichwertiger Unternehmensnachweis vor Aktivierung;
   - nur aktuelle Datei aktiv, Austausch nachvollziehbar;
   - privater S3-Speicher/authorisierter Download erst mit vorhandenen Zugangsdaten.

3. **Artikel-Minimalmodell — auf vereinfachte Firmenmail warten**
   - fachliche UI-Terminologie Artikel;
   - eindeutige BMD-Artikelnummer;
   - bestehende Artikelkategorie;
   - korrektes Importmapping und Dedupe;
   - keine zusätzliche sichtbare Artikel-ID;
   - kein Preislistenbereich;
   - Fachbereich/Produktgruppe/Pakete nicht vorwegnehmen.

4. **Kategorienverwaltung**
   - Kategorien auswählen, filtern und verwalten;
   - belegte Kategorien nicht löschen, sondern vorher umhängen oder deaktivieren;
   - globale Kategorien als Basis;
   - unternehmenseigene Kategorien erst sicher tenant-scopen, wenn die Mandantenbasis existiert.

5. **Kleine Unternehmenspersonalisierung**
   - Name, Logo, Begrüßung;
   - keine freie Theme-/Layout-Engine.

6. **Reale Bestell-E-Mails**
   - Bestätigung an Endkunden bei vorhandener Adresse;
   - Information an Partner;
   - neue Bestellung an Unternehmen;
   - Bestellung zuerst sicher speichern;
   - Versandstatus, eindeutige Ereignis-ID und gezielte Wiederholung bei SMTP-Fehler.

7. **Wesentliche In-App-Benachrichtigungen**
   - Unternehmen: neue Bestellung;
   - Partner: Statusänderung;
   - optional sichtbarer Versandfehler;
   - Glocke, Ungelesen-Zähler, letzte 20, Direktlink, gelesen;
   - keine WebSockets/allgemeine Aktivitätsplattform ohne Bedarf.

Triggerbasiert bei zweitem Unternehmen oder verbindlichem Termin:

8. Keycloak-/Tenant-/Migrationsspike.
9. Company-Tenant-Foundation und Cross-Tenant-Tests.
10. sichere Company-/Partner-Zuordnung und gemeinsame organisatorische Zugänge.
11. Subdomains mit Host-/Organization-/Tenant-Abgleich.

Erst spätere Produktstufen:

- öffentliche Unternehmens-Selbstregistrierung;
- persönliche Mitarbeiterkonten;
- externer Endkunden-Partnershop;
- Fachbereichs-/Schulungsberechtigungen;
- Produktgruppen-/PP-/Margenlogik;
- Artikelpakete/Stücklisten und Varianten-Produktebene;
- erweiterte Trendanalysen;
- KI-Funktion nur mit messbarem fachlichem Nutzen.

# 12. Phase-2-/Out-of-Scope-Referenz

- B-01 externer Endkunden-Partnershop: CUT/später.
- B-02 Shopdesign-Anpassung: CUT/später.
- B-03 automatischer Provisionsaufstieg: STUB; manuell aktuell.
- B-04 realer E-Mail-Versand: STUB; aktuell simuliert, als separater zukünftiger Change vorgesehen.
- B-05 automatische Shopanfragen-Weiterleitung: CUT.
- B-06 direkte BMD-Schnittstelle: CUT; BMD bleibt trotzdem führende Quelle über Import.
- B-07 Provisionsabrechnung/Auszahlung: CUT.
- B-08 vollständige/cross-dashboard Vorperiodenanalyse: STUB; Partnervergleich teilweise vorhanden.

Nicht ohne explizite Neuentscheidung bauen: vollständiges ERP/CRM/PRM, Payment, Lager-/Versandverwaltung, Buchhaltung, beliebige Lernplattform.

# 13. KI-Nachweise

Schulische Leitlinie aus der Besprechung: KI-Nutzung nachvollziehbar und sinnvoll dokumentieren; übernommene Inhalte korrekt kennzeichnen/zitieren, kein Plagiat.

Gespeichertes Konzept in `Documentation/KI-Nachweise/`:

1. kuratiertes Register im Repository;
2. vollständiges privates Roharchiv außerhalb Git;
3. Status `EXAKT`, `TEILWEISE`, `REKONSTRUIERT`, `NICHT WIEDERHERSTELLBAR`;
4. nur menschliche Prompts und sichtbare Antworten, keine System-/Reasoning-/Tool-/Subagentenblöcke als eigene Prompts ausgeben;
5. Zweck, Ergebnis, eigene Prüfung, Änderungen und Belege dokumentieren;
6. repräsentative vollständige Dialoge später für den Anhang auswählen;
7. keine Secrets, personenbezogenen Firmendaten oder privaten Rohverläufe in Git.

Der OpenSpec-Change `automate-ai-evidence-log` ist der gespeicherte Automatisierungsplan. Bis er implementiert ist, relevante Arbeit im bestehenden Register ergänzen; nichts über Prüfungen erfinden.

# 14. Offene Fragen und externe Abhängigkeiten

- angekündigte vereinfachte Firmenmail zum minimalen Artikelumfang;
- echter anonymisierter BMD-/Artikel-/Partnerexport und verbindliche Spalten;
- Import-Aktualisierungsregeln: neue, geänderte, deaktivierte Artikel und Dubletten;
- Dormanzschwelle aktuell 60 Tage, noch zu bestätigen;
- Zeitpunkt/Umfang der Mandantenfähigkeit in der Diplomarbeit;
- Company-Mitarbeiter-Zugangsmodell;
- öffentlicher Shopablauf und Termin später;
- aktueller SMTP-/no-reply- und S3-Stand bei Herrn Bachl;
- Deployment-Endtermin, DNS/HTTPS/Keycloak-Ende-zu-Ende;
- endgültige Zitierform und Zahl vollständiger KI-Dialoge im Anhang;
- Clockify laufend führen und belegbare bisherige Arbeit nachtragen.

Diese Punkte nicht durch Annahmen in Code verwandeln. Nicht blockierte aktuelle Arbeit darf trotzdem fortgesetzt werden.

# 15. Test-, Runtime- und Sicherheitscheck

Backend-Checks für Implementierungsänderungen:

```bash
cd backend
./mvnw test
./mvnw package
```

Historisch funktionierender Testpfad mit lokalem Compose-Keycloak, nur bei aktuellem Bedarf und nach Prüfung von `docs/SETUP.md`:

```bash
cd backend
./mvnw test \
  -Dquarkus.http.test-port=8091 \
  -Dquarkus.keycloak.devservices.enabled=false \
  -Dquarkus.oidc.auth-server-url=http://localhost:8081/realms/partnerportal \
  -Dquarkus.oidc.client-id=partnerportal-frontend
```

Frontend hat keinen Build/Lint/npm-Schritt. UI-Änderungen real im Browser auf Desktop und Mobile prüfen. Lokaler Start laut `docs/SETUP.md`: Compose für PostgreSQL/Keycloak, Quarkus Dev, statischer Frontendserver.

Vor Handoff:

- fokussierte Tests;
- komplette Backendtests bei Backendänderung;
- `./mvnw package` bei Implementierung;
- Browserprüfung bei UI-Änderung;
- `openspec validate <change>` bei OpenSpec;
- `git diff --check`;
- `partnerportal-quality` plus generischer Korrektheitsreview;
- exakte Ergebnisse berichten, keine ungeprüften Erfolgsbehauptungen.

# 16. Startcheck für jede neue Session

1. Alle Pflichtkontexte aus Abschnitt 1 lesen.
2. `git status --short --branch`.
3. `git log -10 --oneline --decorate`.
4. `openspec list --json`.
5. Relevante aktive Change-Artefakte vollständig lesen.
6. Prüfen, ob `94050cb`, `d0ab7d5` und Dokumentationscommits bereits auf `origin/main` sind.
7. Prüfen, ob vereinfachte Firmenmail, BMD-Daten, SMTP/S3-Antwort oder Tenant-Termin eingetroffen sind.
8. Unrelated/uncommitted Benutzeränderungen bewahren; `tmp/` nicht pauschal stagen oder löschen.
9. Vor nicht-trivialer Arbeit Spezifikation, Plan und Definition of Done nennen.
10. Keine Produktivcodeänderung, kein Commit/Push/Merge/Deployment ohne passenden Auftrag.

# 17. Entscheidungsregel für die nächste Aktion

Wenn kein neuer externer Input vorliegt, gilt:

1. Dokumentations-/Git-Stand sichern.
2. letzten Bestell-Smoke-Test abschließen und `place-order-redesign` beenden.
3. KI-Nachweisautomatisierung umsetzen.
4. fertige Changes sauber archivieren.
5. MVP-Dokumentation/Traceability/Polish abschließen.
6. Hetzner-Deployment stabilisieren.
7. danach den ersten bestätigten Stammdaten-Change starten.

Mandantenfähigkeit, Partnershop und die umfangreichen zukünftigen Artikellogiken nicht vorziehen.
````
