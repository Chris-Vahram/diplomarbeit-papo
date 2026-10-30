# PAPO – Next-Session-Context

**Stand:** 29.10.2026. Operative Übergabe; volatile Angaben beim Start prüfen.
Stabile Regeln stehen in `AGENTS.md` und `CLAUDE.md`. Der beschlossene/noch zu
beschließende Ausbau ist in [PRODUCT_COMPLETION_PLAN.md](PRODUCT_COMPLETION_PLAN.md)
geordnet. Dieses Dokument bleibt der einzige operative Session-Handoff.

## Stand 30.10.2026 (Nachmittag) — hier einsteigen (Mandantenfähigkeit komplett, archiviert)

**Erledigt:** Release B (PR #71, contract-Migration, `singletonKey` und `PAPO_MAIL_COMPANY_RECIPIENT`
entfernt) live; Firma 2 „Testfirma Zwei GmbH“ mit `company-zwei`/`partner-zwei` (Zufallspasswörter →
Vaultwarden) angelegt, Partner `P-1002` in Firma 2 an `partner-zwei` gebunden, Trennung live mit echten
Tokens geprüft. `add-company-tenancy` archiviert (`openspec/changes/archive/2026-10-30-add-company-tenancy/`).

**Als Nächstes:** 1. Backup-Cron auf der VM einrichten (`backup.sh` liegt jetzt in der gefixten Fassung
dort; Zeile aus docs/DEPLOYMENT.md „Backups“). 2. `send-order-emails` 4.2: Firmen-E-Mail im Firmenprofil
eintragen, Testbestellung, dann archivieren. 3. Vor echten Daten: Demo-Logins deaktivieren, Hinweis
„Passwort = Benutzername“ auf der Login-Seite entfernen, Passwörter von `company-zwei`/`partner-zwei`
rotieren (standen im Chatprotokoll).

## Stand 30.10.2026 (Vormittag) — Release A live, company-demo gebunden

**Erledigt 30.10.:** VM-Backup manuell erstellt (`deploy/backup/*-20261030-013703.dump`), PR #68
(Backup-Fix) und PR #66 (Release A) gemergt und deployt, `company-demo` (Keycloak-ID `3755bf72-…`)
an Firma 1 „Huemer GmbH“ gebunden (Provisionierungsskript, COMMIT). Firmenpostfach existiert noch
nicht → „E-Mail für Bestellhinweise“ im Firmenprofil leer; Chris trägt es später im Portal ein
(nicht mehr in `.env.prod`). Punkte 1–3 unten sind damit erledigt bis auf Backup-Skript auf die VM
kopieren + Cron einrichten.

**Ursprüngliche Reihenfolge (Stand 29.10.):**
1. **Backup auf der VM** (Chris). `backup.sh` brach ab (`.env.prod` Zeile 11 mit Sonderzeichen,
   Skript las die ganze Datei per `.`). Fix = PR #68 (`fix/backup-env-parsing`, liest nur `DB_USER`).
   Es gibt **kein** `backup.log` → der nächtliche Cron war nie eingerichtet; es existieren keine
   regelmäßigen Backups. Nach Merge #68: Skript per scp auf die VM, Cron laut docs/DEPLOYMENT.md
   „Backups“ einrichten.
2. **PR #66 mergen** = Release A (expand) von `add-company-tenancy` (Branch
   `feat/company-tenancy-release-a`, main inkl. Tobias' PR #67 eingemergt, 263/263 Tests grün).
   Merge deployt sofort; danach bekommen Firmenbenutzer 403 bis zur Bindung.
3. **Direkt danach `company-demo` binden** (DEPLOYMENT.md „Mandantenfähigkeit“, Schritt 3):
   Keycloak-ID von `company-demo` kopieren, `deploy/tenancy/provision-company.sql` per scp auf die VM,
   im Postgres-Container mit `subject`, `company_id` (aus `select id, businessname from companyprofile`)
   und `operator` ausführen, als `company-demo` anmelden und prüfen.
4. **Erst danach Release B** (Task 3.4 in `openspec/changes/add-company-tenancy/tasks.md`): eigener
   Branch/PR — `contract()` (NOT NULL, alte Unique-Constraints `partner.partnernumber`,
   `article_categories.normalized_name`, `companyprofile.singletonkey` per `pg_constraint` droppen,
   `singletonkey` nullable), Entity ohne `singletonKey`, `papo.mail.company-recipient` entfernen,
   Kompatibilitätscheck A-Image auf B-Schema (gleiches Vorgehen wie bei A: `git archive` + beide JARs
   gegen Scratch-DB). Dann 7.4b (VM, Firma 2 anlegen), 7.5 (Purpose `company-profile`), Archiv.

**Was Release A enthält:** `CompanyLogin` + `security/CurrentCompany`, alle Firmen-Services filtern
nach Firma (fremde IDs 404), `company_id` an Partner/Product/Category/ProductImage,
`CompanyTenancyMigrationService.expand()`, gemeinsame Sperre `LoginSubjectLock`, Mail-Absender/
Empfänger je Firma (`CompanyProfile.orderNoticeEmail`, „“ = bewusst geleert, null = nie gesetzt),
Bilder `private, no-store` + Frontend `cache: "no-store"` + Generationszähler, Testfirma 2
(`company-zwei`/`partner-zwei`, feste Realm-IDs `c0a1d2e3-0000-4000-8000-00000000000{1,2,3}`).
Tests: `business/CompanyTenancyResourceTest`, `CompanyTenancyMigrationTest`, `support/TestCompanies`.
Zwei Codex-Adversarial-Reviews (Spec + Diff) eingearbeitet. Details: tasks.md „Implementation notes“.

**Sonst offen:** `send-order-emails` 4.2 — SMTP auf VM aktiv (Image läuft mit mailer/scheduler),
Testbestellung + Firmen-Empfänger (kommt nach Release A übers Firmenprofil) noch zu prüfen, dann
archivieren. Leitkennzahl Firma = Umsatz bestätigen lassen. Drei GitHub-Tokens im KI-Roharchiv
widerrufen (seit 16.10. offen). Tobias' Branch `docs/impeccable-product-context` kann weg.

**Lokale Umgebung:** Tests mit `./mvnw -o test -Dquarkus.http.test-port=8091` (Dev Services gehen).
Lokaler Keycloak nach `--force-recreate` braucht `kcadm … update realms/partnerportal -s sslRequired=NONE`
(Docker-Desktop-Netz). Ausgabewunsch Chris: kurz, ein Befehl pro Codeblock, Schritte nummeriert
(Plugin `i-have-adhd` installiert, mit `/i-have-adhd` aktivieren).

## Stand 22.10.2026

- `refine-portal-frontend` gemergt (PR #59), live, von Tobias abgenommen und archiviert
  (`openspec/changes/archive/2026-10-22-refine-portal-frontend/`, Spec `portal-visual-design`).
  Designentscheidungen 1B 2C 3C 4B 5C 6C + Desktop-Listen stehen in `DESIGN.md`.
- PRODUCT.md aus Chris' und Tobias' Fassung zusammengeführt (PR #60, von Tobias bestätigt);
  Tobias' Branch `docs/impeccable-product-context` ist damit erledigt und kann gelöscht werden.
- Leitkennzahl Firmen-Dashboard = Umsatz (Vorschlag, Chris bestätigen lassen).
- Nächster Kandidat: `add-article-tax-rate` (0/17, spezifiziert von Tobias, kein Code). Chris will
  ihn übernehmen; Tobias' Rückmeldung, ob er schon angefangen hat, steht aus. Er ist Voraussetzung
  für `add-shop-guest-checkout`. Übrige offene Changes warten auf Externe (SMTP, BMD-Daten,
  Thesis-Abnahme) oder gehören Tobias (7 Shop-Changes).

## Stand 16.10.2026 — vorheriger Stand

**Live auf paportal.at (main, alle Deploys grün, live geprüft):**
- B-08 Firmen-Trendpfeile (archiviert), Zeitraum-Presets/Partner-Navigation/Endkunden bearbeiten (12.10.-Feedback).
- B-04 `send-order-emails` 5/6: Outbox, Retries, Firmen-Statusansicht. Auf der VM **aus**
  (`PAPO_MAIL_ENABLED=false` in `~/papo-deploy/.env.prod`, Compose schon aktualisiert).
  Offen Task 4.2: SMTP-Daten neu von Herrn Bachl (Vaultwarden-Links waren abgelaufen) +
  Testpostfach (z. B. test@paportal.at) → Schritte in `docs/DEPLOYMENT.md` „Bestell-E-Mails“;
  immer mit `PAPO_MAIL_REDIRECT_TO`, Absender `no-reply@paportal.at` (SPF/DMARC gesetzt).
- OIDC-Login-Bindung Release A+B (archiviert): Partner nur noch über `PartnerLogin`
  (Firmen-Dialog „Logins“); `partner-demo` ist auf der VM verknüpft.
- Kategorie nur noch als Katalog-Referenz (archiviert, Migration und Alttext entfernt).
- Ponytail-Audit (−770 Zeilen, Plan: `openspec/PONYTAIL_AUDIT_2026-10-15.md`).
- KI-Register repariert (PR #48): 74 Zeilen; Tool behält archivierte Sitzungen.

**Nächster Arbeitsblock (Chris):** `refine-portal-frontend` (impeccable, „Verfeinerung +
Akzente“, Tobias hat zugestimmt). Start: impeccable `init` → PRODUCT.md, `document` → DESIGN.md
aus `Design/` (Tobias' System), beides Tobias zeigen, dann Tasks top-down; Shop-Dateien nicht anfassen.

**Tobias' Changes (nicht anfassen):** add-partner-shop-foundation, add-shop-page-renderer und add-shop-builder-editor (alle archiviert),
add-shop-templates, add-shop-design-assistant, add-shop-inquiries,
add-article-tax-rate, add-shop-guest-checkout, add-shop-legal-consent, add-shop-statistics,
add-shop-customer-accounts. Entscheidungen: `Specification/ENDKUNDENSHOP_DECISIONS.md`.

**Wartet auf Externe:** `decouple-partner-number-from-login` 0/4 und
`add-unique-bmd-article-number` (BMD-Beispieldaten), `complete-thesis-documentation` 4/6
(Abnahme Tobias/gemeinsam), B-07/B-09/B-03 (Firmenregeln).

**Offene Hinweise für Chris:**
- Drei GitHub-Tokens stehen in lokalen Sitzungsprotokollen des privaten KI-Roharchivs →
  widerrufen. Roharchiv **nicht** in Git (README-Regel, Secrets, 396 MB); Alternative
  verschlüsseltes Backup ist angeboten, Entscheidung offen.
- Tobias soll das KI-Tool selbst einrichten (`PAPO_KI_PERSON=Tobias`, `install`, `sync`).
- Lokal: `my_postgres` (anderes Projekt) belegt 5432, Case-Claused belegt 8080 → Tests mit
  `-Dquarkus.http.test-port=8091`, Dev-Backend z. B. auf 8090 + Wegwerf-DB 5433.

**Arbeitsregeln (CLAUDE.md „Git workflow“):** Feature-Branch, lokale Commits je Schritt ohne
Rückfrage; Push/Merge/Deploy nur mit Chris' OK. Keine Worktrees ohne guten Grund. Nach jeder
Arbeit WorkAddict-Einträge (Repo `diplomarbeit-papo/time-data`) und KI-Nachweis
(`tools/ai_evidence_log.py sync` + `enrich`). Ponytail-Stil.

## Stand 14.10.2026 — Besprechung 12.10.2026 umgesetzt

Neuestes Protokoll: `Documentation/Besprechungsprotokolle/Besprechungsprotokoll_12.10.md`
(Prof. Hammer, Chris, Tobias). Präsentationen abgenommen. Daraus am 14.10.2026:

- Umgesetzt und gemergt: PR #37 `apply-dashboard-navigation-feedback`
  (Zeitraum-Presets Woche/Monat/Jahr, Standard Monat, „Gesamter Zeitraum“;
  Partner-Dashboard mit „Letzte Bestellungen“ oben und größerem „Neue Bestellung“;
  Sidebar „Meine Endkunden“/„Meine Artikel“) und PR #38 `edit-partner-end-customers`
  (`PUT /api/partner/customers/{id}`, Bearbeiten-Dialog; Änderungen wirken live
  auch in bestehenden Bestellungen). Beide Changes archiviert unter
  `openspec/changes/archive/2026-10-14-*`. Prüfung: 134/134 Backend-Tests,
  Package, Playwright Desktop/Mobil, `openspec validate --strict`.
- Doku-Sync: `sync-specs-after-meeting-2026-10-12` (Full-Vision-FSD, MVP-FSD-Hinweis,
  Backlog, Handoff, Agentendateien).
- Shop: partnerseitiger Homepage-Baukasten „Mein Webshop“ (Pfad oder Subdomain
  offen). Tobias erstellt Empfehlungen zu den offenen Shop-Fragen und danach die
  Spezifikation; die Fragen gehen per Mail über Prof. Hammer an die Firma. Kein
  Dummy-Shop.
- Datenimport bleibt Dummy bis Firmendaten vorliegen. Firmentermin noch nicht
  vereinbart. „KI einbauen?“ nur Idee ohne Scope. „Adressleiste sichtbar“ ist kein
  Arbeitspunkt (Prof. Hammer wollte nur die URL sehen).
- Zeiterfassung: WorkAddict (Daten im Repo `diplomarbeit-papo/time-data`);
  Chris' Einträge bis 14.10.2026 nachgetragen, Uhrzeit des Meetings 12.10. geschätzt.
- 15.10.2026: OIDC-Identity-Grundlage als eigener Change
  `bind-partner-login-to-oidc-subject` spezifiziert (Tabelle `PartnerLogin`,
  Subject-Auflösung, Firmen-Dialog „Logins“, Release A mit Übergangs-Fallback,
  VM-Bindung, Release B ohne Fallback). `decouple-partner-number-from-login`
  enthält nur noch den BMD-Nummern-Teil und wartet auf BMD-Beispieldaten.
- Nächster technischer Block für Chris: `bind-partner-login-to-oidc-subject`
  Release A umsetzen (Tasks 1–3), danach VM-Bindung und Release B.
- 15.10.2026 umgesetzt (lokal, noch nicht committet): B-08 `add-company-dashboard-trends`
  (archiviert) und B-04 `send-order-emails` 5/6 (Outbox, Retries, Firmen-Statusansicht,
  Versand standardmäßig aus, Umleitung erzwungen; Mailpit im dev). Prüfung: 153/153
  Backend-Tests, Package, Playwright Desktop/Mobil, Mailpit mit allen vier Mail-Typen,
  zwei Codex-Reviews eingearbeitet. Offen (Task 4.2): SMTP-Daten von Herrn Bachl (per
  Vaultwarden-Links an Chris, nicht im Repo) auf der VM eintragen, mit Testpostfach-
  Umleitung prüfen; Chris entscheidet über Abschluss-Mail bei „Abgeschlossen“ und
  Firmen-Empfängeradresse; Aufbewahrungsregel vor echten Kundendaten.
  Update 15.10. nachmittags: Entscheidungen übernommen (neutrale Abschlussmail aktiv,
  Funktionspostfach, 90-Tage-Adresslöschung bei fehlgeschlagenen Mails, Absender
  no-reply@paportal.at; SPF/DMARC für paportal.at sind gesetzt). Die Vaultwarden-Links waren
  abgelaufen → SMTP-Daten neu anfordern. VM: `docker-compose.prod.yml` aktualisiert
  (Backup `.bak-20261015-1558`), Mail-Variablen in `.env.prod` mit `PAPO_MAIL_ENABLED=false`.
- `bind-partner-login-to-oidc-subject` Release A umgesetzt (6/8) auf Branch
  `feat/bind-partner-login-to-oidc-subject` im Worktree `../papo-oidc`: 144/144 Tests,
  Upgrade-Check mit schema update, Browser-Check, Codex-Review eingearbeitet. Offen: Deploy,
  VM-Bindung (docs/DEPLOYMENT.md „Partner-Login binden“), danach Release B.

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
Ein tatsächlich vereinbarter Termin oder ein neues Gesprächsprotokoll ist noch
nicht bestätigt (Stand 12.10.2026: Firmentermin weiterhin offen).
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

- Git-Prüfung am 07.10.2026 vor diesem Folgecommit: Branch
  `docs/team-commission-discovery-context`, auf `origin/main` fast-forwarded;
  HEAD `7eddc127f31b004051794e752612b1115ebb86fe`. PR #34 ist gemergt.
  Neuester geprüfter Main-Workflow `37385508517`: erfolgreich, SHA `7eddc12`
  (Tests, Paket/Image und Demo-Deployment). Das ist keine neue angemeldete
  VM-/Browserabnahme. Nach Veröffentlichung dieses Updates den neuen PR/Merge
  und dessen Main-Workflow prüfen, nicht diesen Snapshot als aktuell übernehmen.
- Aktive Changes unverändert: Thesis 4/6, Login-Entkopplung 0/6, BMD-Artikelnummer 0/6.
- Chris autorisiert am 07.10.2026 ausdrücklich Commit/Push/Merge der aktualisierten
  Kontextdateien und beiden Präsentationen.
  Bestehende Graphify-Vorarbeiten bleiben unverändert lokal; kein pauschales
  Publizieren fremder Änderungen oder temporärer Testbilder. Ein Main-Merge löst
  bestehende Demo-CI/CD aus; keine Freigabe für echte Firmendaten.
- Präsentationsinhalte für den Lehrertermin sind in
  [PROJEKTSTAND_2026-10-06.md](../presentation/PROJEKTSTAND_2026-10-06.md)
  dokumentiert: umgesetzt / Produktrichtung / Firmenfragen getrennt. Auf Chris'
  Folgeauftrag sind zwei gekürzte reveal.js-Decks entstanden:
  [Projektüberblick](../presentation/mvp-deck/index.html) mit 13 Folien und
  [Sprint-Update](../presentation/sprint-deck/index.html) mit 8 Folien statt
  zusammen 35. Keine Zeiterfassung; die acht B-09-Fragegruppen stehen auf den
  Sprintfolien 6/7. Tobias' Theme und bestehende Aufnahmen bleiben erhalten.
  Veröffentlichung dieser Folgearbeit über PR #35 mit dem Titel
  `docs: publish split presentations and session context`; PR #34 betrifft nur
  den zuvor gemergten Entscheidungsstand. Der Sprint-Inhaltsstand bleibt bewusst
  06.10.2026; die Übergabe ist vom 07.10.2026. Inhaltscommit: `67a5acf`.
  Merge-/CI-Status von PR #35 und den neuesten Main-Workflow beim Start prüfen.
- Prüfung der Decks am 06.10.2026: 13 + 8 Folien, acht B-09-Fragegruppen,
  Desktop 1280 × 720 und Mobil 390 × 844 ohne Überlauf, Navigation/Bilder und
  Inline-Skripte geprüft. Keine externen Requests oder JS-Laufzeitfehler.
  Direkte `file:`-Navigation war toolgesperrt; keine neue Live-Anwendungsabnahme.
- Keine neue B-09-Implementierungsfreigabe. Als nächster unabhängiger technischer
  Plan eignet sich die OIDC-Identity-Grundlage; vor Code eigener geprüfter Change.
  B-08 ist unabhängig planbar, B-04 braucht SMTP/Testempfänger. Die konkrete
  Account-Verwaltung braucht Erst-/Letzter-Admin, Recovery und Rechte-Matrix.
  BMD-Artikelnummern warten auf anonymisierten Export/Feldregeln. B-06/B-07
  brauchen Firmen-/Schulfreigaben; B-07 zuerst Abrechnung, Bank separat später.

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
Den Veröffentlichungs-PR dieses Updates über Branch/Titel suchen und dessen
Status prüfen; Git- und CI-Ergebnisse aus diesem Dokument sind datierte Belege.
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
Firmenbestätigung der Endkundenshop-Entscheidungen; Staging-Infrastruktur.
Endkundenshop (B-01/B-02/B-05): vorläufig am 15.10.2026 entschieden, inklusive
Baukasten „Mein Webshop“ (Protokoll 12.10.) und Changes-Schnitt, siehe
[ENDKUNDENSHOP_DECISIONS.md](ENDKUNDENSHOP_DECISIONS.md); Vorbehalt Firmenmeeting. Der Plan dokumentiert
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
gh pr list --state all --head docs/team-commission-discovery-context --json number,title,state,mergeCommit
gh run list --branch main -L 1 --json databaseId,status,conclusion,headSha
```

Bei Backendänderungen gemäß `AGENTS.md`:

```bash
cd backend
./mvnw test
./mvnw package
```
