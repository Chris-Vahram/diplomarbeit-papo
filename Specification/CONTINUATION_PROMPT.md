# PAPO – Next-Session-Context

**Stand:** 06.10.2026 (lokales Datum geprüft). Operative Übergabe; volatile Angaben beim Start prüfen.
Stabile Regeln stehen in `AGENTS.md` und `CLAUDE.md`. Der beschlossene/noch zu
beschließende Ausbau ist in [PRODUCT_COMPLETION_PLAN.md](PRODUCT_COMPLETION_PLAN.md)
geordnet. Dieses Dokument bleibt der einzige operative Session-Handoff.

## Aktiver Gesprächsfaden — Teamprovision für Partner-Mitarbeiter

Chris hat ein gemeinsames Provisionsbudget bestätigt: Verkäufer, Team und Bonus
teilen höchstens die bestehende Partnerprovision. Standard bei Verkäuferzuordnung
ist 100 % Verkäufer; zunächst nur interne Zuordnung/Anzeige, keine beschlossenen
Mitarbeiteransprüche oder Auszahlungen. Team besteht aus Mitarbeitern derselben
Partnerfirma, ohne untergeordnete Partnerfirmen oder Schulungen. Zahlung bleibt
getrennt von Erfüllung, Regeln/Zuordnungen auditiert, Historie unverändert.

Chris akzeptiert die Empfehlungen als **Planungsrichtung**, nicht als
Firmenfreigaben. Vorschlag für erste Version: Zuordnung/Geldwerte und getrennte
Zahlungserfassung; Punkte/Karriere nach eigenständigem Nutzen, Bonus nach Regeln.
Ein Team, Gleichverteilung und Regelstand bei Erstellung sind akzeptierte Vorschläge;
Empfänger, Stichtag, konkrete Rechte, Geldbasis und Abrechnung bleiben Firmenfragen.

**Nächster Gesprächsschritt:** Am **07.10.2026** möchte Chris mit dem Lehrer einen
Firmentermin vereinbaren, nicht bereits die Firmenbesprechung durchführen.
[TEAM_COMMISSION_DISCOVERY.md](TEAM_COMMISSION_DISCOVERY.md) enthält bestätigte
Nutzerentscheidungen, akzeptierte Planungsempfehlungen, acht verbleibende
Firmenfragen, unverbindliches Rechenbeispiel und Risiken. Bereits beantwortete
Nutzerfragen nicht erneut stellen. B-09 bleibt nicht implementierungsreif;
kein neuer OpenSpec-Change und keine Anwendung implementiert.

**Technischer Ist-Stand:** Aktueller Code kennt weder persönliche Partner-Mitarbeiter
noch Zahlungsstatus oder Shop-Affiliate-Zuordnung. Provision wird bei
Bestellerstellung als Partner-Prozentsatz gespeichert. Diese Grenze steht samt
Risiken und Abhängigkeiten im Discovery-Dokument.

## Allgemeiner Stand und Präsentationsvorbereitung

- Prüfung vor Veröffentlichung am 06.10.2026: Branch
  `docs/team-commission-discovery-context`, HEAD `25ce0ee`; Main enthält bereits
  PR #33 (Gateway-Hardening abgeschlossen). Neuester geprüfter Main-Workflow
  `36433087198`: erfolgreich, SHA `25ce0ee`. Historische Live-Nachweise unten sind
  keine neue VM-/Browserabnahme.
- Aktive Changes unverändert: Thesis 4/6, Login-Entkopplung 0/6, BMD-Artikelnummer 0/6.
- Chris autorisiert Commit/Push/Merge des geprüften Dokumentationsstands.
  Bestehende Graphify-Vorarbeiten bleiben unverändert lokal; kein pauschales
  Publizieren fremder Änderungen. Ein Main-Merge löst bestehende Demo-CI/CD aus.
- Präsentationsinhalte für den Lehrertermin sind in
  [PROJEKTSTAND_2026-10-06.md](../presentation/PROJEKTSTAND_2026-10-06.md)
  vorbereitet: umgesetzt / Produktrichtung / Firmenfragen getrennt. Das bestehende
  reveal.js-Deck und Tobias' Design sind noch nicht verändert.
- Keine neue B-09-Implementierungsfreigabe. Als nächster unabhängiger technischer
  Plan eignet sich die OIDC-Identity-Grundlage; vor Code eigener geprüfter Change.

## Wichtiger Wiedereinstieg — Deployment abgeschlossen

PR #31 (`6853e78`, Health-Check und Rollback) und PR #32 (`711d8f2`, Spec-Sync
und Archivierung) sind gemergt. Der Health/Rollback-Change
`verify-deploy-health-and-rollback` liegt in
`openspec/changes/archive/2026-09-28-verify-deploy-health-and-rollback/`.
Der Gateway-Hardening-Change wurde in dieser Session entsprechend Chris'
Scope-Entscheidung abgeschlossen und unter
`openspec/changes/archive/2026-09-28-harden-deployment-gateway/` archiviert;
die `deployment-pipeline`-Capability-Spec ist synchronisiert. Er verlangt keine
weitere Gateway-Prüfung.

Der lokale Gateway-Harness besteht mit **30/30** Prüfungen; Gateway und Harness
bestehen `sh -n`. Der Rollback stellt Backend und Frontend wieder her, erhält den
Recovery-Zustand bis zur bestätigten Bereitschaft und bricht bei wiederholten
TERM-Signalen nicht ab. Health verlangt stabile HTTPS-401-Antworten derselben
Backend-Container-ID, desselben Images und Restart-Zählers. Das Datenbankschema
und Daten werden von diesem Rollback nicht zurückgesetzt.

Die Code-Version aus PR #32 wurde am 28.09.2026 mit Main-Run `36426325791`
(Tests 2:21, Image-Build/Push 1:06, VM-Deploy 0:41) verifiziert. Dabei lief Backend-Image
`ghcr.io/chris-vahram/diplomarbeit-papo-backend:711d8f2a47ddf185423dccb5d363022581ed5a0f`
mit `RestartCount 0`; PostgreSQL, Keycloak und Caddy liefen ebenfalls ohne
Restarts. VM-lokale HTTPS-Prüfung: Frontend `200`, geschützte API `401`, OIDC
Discovery `200`. Deployment und Rollback sind damit für diesen Change erledigt.

## Konkreter nächster Schritt

**Aktive Changes und offene Aufgaben:**

- `complete-thesis-documentation` **4/6**: Tobias prüft Screenshots und Texte
  (2.2); Chris und Tobias bestätigen gemeinsam fachliche Vollständigkeit (3.1).
- `decouple-partner-number-from-login` **0/6**: OIDC-Subject-Bindung und company-
  only Compare-and-Set (1.2/1.3) sind als Identity-Grundlage vom BMD-Beispiel
  trennbar. BMD-Partnernummern und produktive Identity-Migration bleiben separat.
- `add-unique-bmd-article-number` **0/6**: beginnt nach anonymisiertem BMD-Export
  oder verbindlichen Feld-/Normalisierungsregeln.

**Nächste technische Arbeit:** den Identity-Grundlagen-Teil aus
`decouple-partner-number-from-login` in einen eigenen OpenSpec-Change aufteilen.
Zuerst den sicheren Plan für OIDC-`sub`, lokale dev/test-Migration und Tests
festlegen, dann umsetzen. Keine produktive Identitätsmigration ohne geprüften
Daten-/Rollbackplan.

### Einstieg für den nächsten Agenten

Zuerst diesen Prompt vollständig lesen, dann `AGENTS.md`, `CLAUDE.md`, das neueste
Besprechungsprotokoll, FSD, `phase2_backlog.md`, `PRODUCT_COMPLETION_PLAN.md`, die
aktiven OpenSpec-Tasks sowie `docs/CONVENTIONS.md`/`docs/DATA_MODEL.md` prüfen.
Danach `git status --short --branch`, `git log -5 --oneline --decorate`,
`openspec list --json` und den neuesten Main-Workflow via
`gh run list --branch main -L 1 --json databaseId,status,conclusion` verifizieren.
Anschließend den aktuellen Auftrag beachten: Firmen-/Präsentationsvorbereitung
fortsetzen oder die Identity-Grundlage als separaten OpenSpec-Change planen.
Keine automatische Implementierung allein wegen dieses Handoffs. Vor dem ersten Edit die drei
aktiven Changes samt noch offenen Aufgaben, menschlichen/data gates und Themen
für höheres Reasoning kurz priorisieren; nicht mit einem allgemeinen
„weiter implementieren?“ stoppen.

### Planung mit höherem Reasoning

- **Jetzt erforderlich:** OIDC-Subjektbindung und sichere Migration bestehender
  Demo-/Partner-Identitäten. Vor Code die Vertrauensgrenze, parallele/duplizierte
  Bindungen, Datenmigration und Rollback planen; danach unabhängiger Security- und
  Persistence-Review.
- **Vor Mehrmandantenbetrieb:** Tenant-Grenze über alle Unternehmensdaten,
  tenant-abgeleitete Autorisierung, Cross-Tenant-Negativtests, Datenmigration,
  Admin-Provisionierung und Wiederherstellung zusammen planen.
- **Vor echtem externem Betrieb:** getrennte Staging-Umgebung, Demo-Zugangsdaten,
  Backups und Abnahme-/Rolloutgrenzen festlegen.
- **Erst nach Firmen-/Schulregeln:** B-06 BMD-Synchronisation und B-07 echte
  Banküberweisungen. B-07s erster bestätigter Schritt bleibt Abrechnung/Freigabe/
  Export; Geldbewegungen benötigen einen eigenen Plan samt Autorisierung und
  Fehler-/Abgleichsablauf.

Routineaufgaben wie Screenshotabnahme und BMD-Feldregeln brauchen keine höhere
Reasoning-Stufe, sondern Tobias-/Firmeninput.

**Nicht entschieden:** verbindliche Herbstferien-Abnahmeliste; Firmen-/Schulfreigabe
für B-06 und B-07; BMD-Produkt/API und Export-/Rückkanal; Provisionsformel/
Auszahlungsart; B-03-Schwellen; Teamprovisionsregeln aus
[TEAM_COMMISSION_DISCOVERY.md](TEAM_COMMISSION_DISCOVERY.md); Subdomains;
Shop-Layouts/Anfragefelder; Staging-Infrastruktur. Der Plan dokumentiert
Empfehlungen, nicht stillschweigende Entscheidungen. Herr Hammer ist Verbindung
zur Firma; nach dem nächsten Termin liefert Chris ein neues Besprechungsprotokoll.
Die öffentliche Website ersetzt keinen technischen BMD-Vertrag.

## Git/Deployment-Grenze

`main` enthält PR #27 (`5fff3b0`), Migrationsfix aus PR #28 (`b5ab7fb`),
Post-Deploy-Handoff aus PR #29 (`19fa02b`), Präsentationsänderung aus PR #30
(`075bd93`), Deploy-Health/Rollback aus PR #31 (`6853e78`) und OpenSpec-/Doku-
Abschluss aus PR #32 (`711d8f2`) sowie Gateway-Abschluss aus PR #33 (`25ce0ee`). Die archivierte Deployment-Spezifikation ist
mit dem Code synchronisiert. Archivdateien
und neuere Spezifikationen aus dem früheren lokalen `main` blieben erhalten.
Der erste Deploy von `5fff3b0` startete das Backend nicht: Die VM hatte den
historischen Demo-Namen „Elektro Huber GmbH“, die Migration erkannte nur
„Huemer GmbH“. Die VM wurde ohne Datenbank-/Volume-Löschung auf das vorige
Image `e63a81d` zurückgestellt. Der enge Fix akzeptiert beide bekannten
Demo-Namen, unbekannte Daten weiter nicht. Der Regressionstest reproduzierte
den Fehler zuerst; danach `./mvnw test` und `./mvnw package` jeweils
**130/130** grün, unabhängiger Java-Review ohne Blocker. `main`-Run
`36115368910` bestand. Live nach Deploy von `b5ab7fb`: Backend `running`,
RestartCount 0, Frontend-Link auf denselben SHA, 3/3 Partner mit Steuerkennung,
1 Firmenprofil; Frontend HTTP 200, geschützte API ohne Login HTTP 401, OIDC
HTTP 200. Noch keine angemeldete End-to-End-Browserabnahme.

**Deploy-Risiko behoben:** Der Gateway meldete beim ersten Versuch „Release
aktiviert“, obwohl der Backend-Prozess danach abstürzte. PR #31 ergänzt
Post-Activation-Health-Check und automatischen Backend-/Frontend-Rollback; der
Live-Deploy von `6853e78` bestätigte den Ablauf.

Der bestehende Keycloak-Produktions-Template enthält **aktive Demo-Konten mit
bekannten Passwörtern**. `SEED_DEMO_DATA=false` deaktiviert sie nicht; eine bereits
importierte Realm wird durch bloßes Code-Deployment nicht zuverlässig aktualisiert.
Chris hat bestätigt: Auf der aktuellen VM liegen **nur Demo-Daten**, und die
Demo-Logins sind aktiv. Damit ist der ausdrücklich freigegebene `main`-Deploy ein
Demo-Deployment, **keine Freigabe für echte Firmen-/Kundendaten**. Vor echtem
Onboarding Demo-Logins deaktivieren, echte Zugänge provisionieren, Staging trennen
und erneut prüfen. Bei bestehenden Company-Profilen bleibt die neue echte
Registerkennung leer; ein Firmen-Admin muss sie vor Profiländerung ergänzen.

## Menschliche Abnahmen

Diese Punkte gehören zu Chris/Tobias bzw. autorisierter VM-Prüfung, **nicht** als
pauschales Aufgabenpaket zum nächsten Feature-Agenten:

1. Tobias liest `docs/UI_UX_DOCUMENTATION.md`, gleicht jeden Screenshot mit UI
   und Beschriftung ab, dokumentiert Abweichungen und bestätigt Task 2.2.
2. Chris + Tobias gehen FR-01–FR-25 und die ergänzenden Changes in
   `docs/REQUIREMENTS_TRACEABILITY.md` gegen FSD, Protokolle und B-01–B-08 durch,
   unterscheiden implementiert/simuliert/offen und bestätigen Task 3.1.
## Jev, Evidence und KI-Nachweis

`@wrtnlabs/evidence` ist nicht installiert: es benötigt Node/npm und Annotationen,
konfligiert mit dem festgelegten PAPO-Stack und ersetzt keine fachliche Prüfung.
OpenSpec/Traceability und `tools/ai_evidence_log.py` bleiben die passenden
bestehenden Mittel. Jev schreibt keinen Code, sondern trifft eng begrenzte
Entscheidungen. `TYPESAFE_API_KEY` war in dieser Session nicht gesetzt; ChatGPT
Plus liefert keinen TypeSafe-Schlüssel. Deshalb wurde kein funktionsloser Jev-
Wrapper eingebaut. Bei einem echten Routingbedarf und sicher bereitgestelltem
TypeSafe-Key kann ein optionaler lokaler Wrapper außerhalb der PAPO-Laufzeit
separat geplant werden; Tests/Git bleiben deterministische Evidenz.

## Verifikation beim nächsten Start

Prüfung dieser Dokumentationsarbeit: `git diff --check`, Markdown-Ziele und
Rechenbeispiel prüfen; die drei aktiven Changes gezielt strict validieren.
Die globale Prüfung `openspec validate --all --strict` meldete am 06.10.2026
11 gültige und 24 fehlgeschlagene Einträge wegen bereits bestehender Purpose-
Platzhalter in unveränderten Capability-Specs. Kein B-09-Spec-Fehler und keine
neue Implementierung; diese allgemeine Spec-Bereinigung bleibt separate Arbeit.

```bash
git status --short --branch
git log -5 --oneline --decorate
openspec list --json
gh run view 36426325791 --json status,conclusion
```

Bei Backendänderungen gemäß `AGENTS.md`:

```bash
cd backend
./mvnw test
./mvnw package
```
