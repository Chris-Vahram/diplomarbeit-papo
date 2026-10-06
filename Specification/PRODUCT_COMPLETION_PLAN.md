# PAPO – Plan zur funktionalen Fertigstellung

Stand: 07.10.2026. Dieser Plan ist eine Arbeitsgrundlage, keine Freigabe durch Schule
oder Referenzunternehmen. Die aktuelle FSD beschreibt das interne MVP; die gewünschte
Erweiterung wird in kleinen OpenSpec-Changes spezifiziert und einzeln umgesetzt.
`Specification/CONTINUATION_PROMPT.md` bleibt der einzige operative Session-Handoff.

Die Lehrerpräsentation ist in einen allgemeinen Projektüberblick (13 Folien) und
ein Sprint-Update (8 Folien) geteilt; Einstieg und Inhaltsbelege stehen in
[presentation/README.md](../presentation/README.md). Der Inhaltsstand ist
06.10.2026. Noch kein neuer Firmentermin, kein neues Protokoll und keine neue
Firmenfreigabe bestätigt. Veröffentlichungsauftrag vom 07.10.2026 betrifft
Dokumentation/Präsentationen, nicht Implementierung oder neue OpenSpec-Artefakte.

## Bestätigte Zielrichtung durch Chris

- Ergebnis bis zu den Herbstferien: sicher deploybare Staging-Version der erweiterten
  Produktfunktionen; schriftliche Diplomarbeit kann danach weitergeführt werden.
- Phase 2 soll nicht beim MVP enden. B-06 (direkte BMD-Anbindung) bleibt zur
  Entscheidung geöffnet. Für B-07 bestätigt Chris die Reihenfolge: zuerst
  prüfbare Abrechnung/Freigabe/Export umsetzen; echte Banküberweisungen danach
  separat spezifizieren und planen, später implementieren. Firmen-/Schulregeln
  und Freigaben sind dadurch noch nicht ersetzt.
- Zwei Testunternehmen dürfen manuell angelegt werden. Die Trennung ihrer Daten und
  Zugriffe ist zwingend. Subdomains sind noch nicht bestätigt.
- Persönliche Logins auf Firmen- und Partnerseite: je Organisation Administratoren
  (Konten anlegen, sperren, verwalten) und Mitarbeiter. Wichtige Änderungen zeigen
  Person und Zeitpunkt in einem Audit-Log; keine Leistungsbewertung.
- Öffentlicher Partnershop: einmalige Gastbestellung oder optionales Kundenkonto zum
  Speichern eigener Daten. Bestellung ist kostenpflichtig, Zahlung/Erfüllung erfolgen
  später; kein Online-Bezahlvorgang bestätigt.
- Der Deploy-Gateway, Health-Check und automatische Anwendungs-Rollback sind
  implementiert, auf der Demo-VM verifiziert und archiviert. Letzter geprüfter
  Main-Workflow am 07.10.2026: `37385508517`, SHA `7eddc12`, erfolgreich;
  den neuesten Workflow beim Sessionstart prüfen. Keine neue Login-Abnahme.
  Kein Deployment-Task blockiert die Produktimplementierung.
- Chris bestätigt für die aktuelle VM ausschließlich Demo-Daten und aktive,
  bekannte Demo-Logins. Sie ist keine sichere Umgebung für echte Kundendaten;
  Demo-Logins vor echtem Onboarding deaktivieren und gesondert prüfen.

## Herbstferien-Ziel und Machbarkeit

Chris wünscht die **gesamte Phase 2 plus Mandantenfähigkeit und persönliche Konten**
funktional fertig, nicht nur das MVP. Das ist der gewünschte Umfang, aber noch keine
von Firma/Schule bestätigte Abnahme oder belastbare Terminzusage. Zwischen diesem
Stand vom 07.10.2026 und dem 27.10.2026 liegen 20 Tage; B-03, B-06 und B-07 haben wesentliche
fehlende Regeln/Zugänge. Ohne deren rechtzeitige Klärung kann „alles fertig“ nicht
seriös zugesagt werden. Chris entscheidet nach dem nächsten Termin ausdrücklich,
welche Funktionen zwingend bis Herbstferien in Staging abgenommen werden und welche
als späterer Teil des vollständigen Produktumfangs gelten. Empfehlung für den ersten
prüfbaren Meilenstein: getrenntes Staging, zwei isolierte Testunternehmen,
persönliche Konten/Audit, B-08 und B-04. Das ist **keine stillschweigende Kürzung**
des Gesamtziels; B-01 bis B-09 bleiben im Plan und brauchen eigene Entscheidungen.

## Sequenz für den nächsten Implementierungsagenten

Jeder Schritt erhält **einen eigenen** OpenSpec-Change mit Proposal, Design,
Capability-Specs und überprüfbaren Tasks. Erst Change validieren und freigeben,
dann implementieren, testen, reviewen und archivieren. Keine Sammel-Implementierung
von B-01 bis B-09. Vor jedem Change aktuelle Protokolle und Git-Stand prüfen.

## Aktive Feature-Erkundung: Teamprovision und Mitarbeiterziele

Am 01.10.2026 hat Chris Teamprovision für Mitarbeiter innerhalb einer Partnerfirma
als spätere Produktfunktion konkretisiert. Der Scope umfasst zunächst persönliche
Verkäufer-/Teamleiterkonten, nachvollziehbare Verkaufszuordnung, Eigen-/Teamwerte,
Punkte, Karrierestufen, Team-Boni und Teamverwaltung. Untergeordnete Partnerfirmen
sowie Seminare/Schulungen sind ausgeschlossen. Entscheidungen und offene Fragen
stehen in [TEAM_COMMISSION_DISCOVERY.md](TEAM_COMMISSION_DISCOVERY.md).

Am 06.10.2026 bestätigt Chris ein einziges Bestell-Provisionsbudget, standardmäßig
100 % Verkäufer bei Zuordnung und zunächst interne Anzeige ohne beschlossene
Mitarbeiteransprüche/Auszahlungen. Verkäufer, Team und Bonus teilen höchstens den
bestehenden Betrag. Zahlung und Erfüllung werden getrennt betrachtet; historische
Regeln/Zuordnungen und freigegebene Abrechnungen dürfen sich nicht still verändern.

Chris akzeptiert die Empfehlungen als Planungsrichtung: zunächst Zuordnung/Geldwerte
und Zahlungserfassung, ein Team, Gleichverteilung, Regelstand bei Bestellung und
begrenzte Bonusreserve. Punkte/Karriere nach eigenständigem Nutzen, Bonus nach
bestätigten Regeln. Das Gesamtziel bleibt erhalten; Firmenfreigaben fehlen weiterhin.
Offen sind Geldbasis, berechtigte Empfänger/Stichtag, Gewichtung/Rundung, Zahlungs-
und Korrekturregeln, Rechte, Regelautorität, Affiliate sowie Punkte-/Bonusschwellen.
Das Feature bleibt nicht implementierungsreif; kein B-09-Change angelegt.

Am 07.10.2026 möchte Chris mit dem Lehrer einen Firmentermin vereinbaren;
die Firmenbesprechung selbst ist nicht terminiert. Die acht Firmenfragen stehen
im Discovery-Dokument. Bereits beantwortete Nutzerfragen nicht erneut stellen.

**Empfohlene Reihenfolge:** zuerst die bestehende Identity-Grundlage auf OIDC-sub
absichern; danach Mandanten-/Organisationsgrenzen und persönliche Konten samt
Adminrechten/Audit spezifizieren. Vor der Team-Provisionsberechnung außerdem einen
getrennten Zahlungsstatus mit nachvollziehbarem Zahlungseingang benötigen. Danach
Teamprovisions- und Zielregeln als eigenen OpenSpec-Change umsetzen. Affiliate-
Zuordnung hängt vom B-01-Endkundenshop ab und ist als Shop-Integration einzuplanen.
B-03 bleibt automatische Provisionsstufe der Partnerfirma und wird nicht mit
Mitarbeiter-Karrierestufen gleichgesetzt.

## Nächster konkreter Arbeitsblock

Beginne mit dem technischen Identity-Grundlagen-Teil aus
`decouple-partner-number-from-login`: eine stabile OIDC-`sub`-Bindung statt der
Kopplung von Loginname und Partnernummer. Dieser Teil darf laut aktivem Change
unabhängig von BMD-Partnernummernregeln umgesetzt werden. Teile ihn daher in einen
eigenen OpenSpec-Change; lasse BMD-Feldregeln und spätere Partnernummer-Änderungen
im bestehenden BMD-Change, bis anonymisierte Beispieldaten vorliegen. Beschränke
die erste Umsetzung auf lokale dev/test-Realm und sichere Migration; keine
produktive Identitätsänderung oder Demo-VM-Deployment ohne separaten geprüften
Rollout-/Rollbackplan.

Für diesen Auth-/Persistenzplan ist **höheres Reasoning** sinnvoll: Vertrauensgrenze
des OIDC-Subjects, eindeutige Bindung, bestehende Demo-Identitäten, Migration,
Rollback und Autorisierung müssen zusammenpassen. Danach unabhängiger Security-
und Datenbank-Review.

Höheres Reasoning für spätere Planung wird außerdem benötigt für:

- Mehrmandanten-Grenzen und Cross-Tenant-Isolation über alle Daten/Dateien,
  Autorisierungswege und Migrationen hinweg.
- Persönliche Firmen-/Partnerkonten mit Admin-Provisionierung, Kontosperre,
  letztem Admin und Wiederherstellung.
- Öffentlichen Partnershop/Gastbestellung, wenn dieser freigegeben wird:
  Partnerzuordnung, Preis-Snapshot, Missbrauchsschutz und Datenschutz.
- B-06 BMD-Synchronisation und echte B-07-Zahlungen erst nach verbindlichen
  Firmen-/Schulregeln, technischem Zugang und Freigaben.

Screenshotabnahme, BMD-Beispiellieferung und fachliche Textfreigabe sind konkrete
menschliche Aufgaben; sie erfordern keine zusätzliche Architekturplanung.

1. **Staging-Grenze klären und herstellen.** Bachl um eine getrennte Testumgebung
   oder freigegebenes gleichwertiges Staging fragen. Getrennte Daten, Zugangsdaten,
   SMTP-Ziele und Backups; kein echter Kunde und keine echte Bankbewegung. Solange
   `main` direkt auf die heutige VM deployt, große Änderungen zuerst lokal/auf
   Feature-Branch/PR prüfen. Eine bloß andere URL bei derselben DB ist keine Trennung.
2. **Mandantenfundament – zwei Testunternehmen.** Bestehendes Singleton-Firmenprofil
   und sämtliche Partner-, Kunden-, Artikel-, Kategorie-, Bild-, Proof-, Freischaltungs-,
   Bestell-, Provisions-, Import-/Export- und späteren Benachrichtigungs-/Auditzugriffe
   inventarisieren. Datenmigration mit Backup/Rollback spezifizieren.
   Tenant-Kontext aus vertrauenswürdiger Identität ableiten, nie aus frei gewählter
   Request-ID. Zwei Unternehmen manuell provisionieren. Cross-Tenant-Tests für
   Listen/Details, direkte Fremd-IDs, Exporte/Downloads und widersprüchliche
   Host-/Organisationszuordnung. Subdomains erst nach eigener Entscheidung.
3. **Identitätszuordnung, dann persönliche Konten und Audit.** Vor mehreren
   Personen je Partner muss die heutige Kopplung `Keycloak username == Partnernummer`
   durch eine unveränderliche Benutzer-ID mit geprüfter Organisations-/Partner-
   Mitgliedschaft ersetzt werden. Diesen Teil des aktiven
   `decouple-partner-number-from-login`-Changes separat neu spezifizieren;
   Legacy-/BMD-Nummernregeln dürfen weiter auf Beispieldaten warten. Danach erhalten
   Firmen- und Partnerorganisationen
   Admin-/Mitarbeiterrollen; Admins verwalten nur ihre eigene Organisation. Migration
   der bisherigen gemeinsamen Logins ohne Verlust bestehender Daten. Vorab festlegen:
   wer den ersten Admin erstellt, wer Partner-Admins einlädt, wie der letzte Admin
   geschützt und ein verlorener Admin-Zugang wiederhergestellt wird. Mindest-Audit:
   Kontoanlage/-sperre/Rollenwechsel, Profil-/Artikelpreis-/Freischaltungsänderung,
   Bestellstatus, Proof-Prüfung und spätere Abrechnungsfreigabe; unveränderliche
   Akteur-ID, UTC-Zeit, Objekt und Aktion. Zugriff/Aufbewahrung festlegen; keine
   Passwörter, Tokens oder unnötigen Kundendaten im Log. Tests für Sperre, Rollen,
   Fremdorganisation und unveränderliche Zuordnung.
4. **B-08 und B-04 als getrennte, kleinere Produkt-Changes.** B-08 ergänzt fehlende
   Firmen-/Periodenvergleiche mit definierter Behandlung von Vorperiode 0 und
   historischem Testdatensatz. B-04 ersetzt simulierte Bestellmails durch echten
   Versand an die bestätigten Empfänger; Zustellstatus, Idempotenz, Retry, Ausfall
   und datensparsame Logs spezifizieren. SMTP-Zugang über die vorhandenen
   Deployment-Secrets, nie aus Chat oder Git. Versandbestätigung nur nach expliziter
   fachlicher Freigabe ergänzen.
5. **B-01-Shop in aufeinander aufbauenden Changes.** Zuerst öffentlicher,
   partnergebundener, nur lesender Katalog freigeschalteter Artikel. Danach
   Gastbestellung mit Preis-Snapshot und serverseitiger Partnerzuordnung. Danach
   optionales Endkundenkonto (Registrierung, Verifizierung, Login, Reset,
   Datenzugriff/-löschung). Ob Katalog, Bestellung und Konto je eigene Herbstferien-
   Abnahmeschritte sind, muss Chris priorisieren. Der Shop darf keine interne
   Firmen-/Partner-API öffnen.
   Negative Tests für fremde Partner, manipulierte Preise/Mengen und doppelte
   Übermittlung; Desktop- und Mobile-Browserabnahme.
6. **B-02 und B-05 separat.** B-02: Tobias entwirft zwei konkrete Layoutvarianten,
   Chris bestätigt. Änderbar sind nur Shopname, Logo und definierte Farben/Layout;
   zentrale Artikelpreise/-texte bleiben gesperrt. B-05: Anfrage aus dem Shop wird
   dem aus der Shoproute ermittelten Partner zugeordnet; Spam-, Datenschutz- und
   Benachrichtigungsregeln vor Implementierung festlegen.
7. **B-03 erst nach Schwellenentscheidung.** Definieren, welcher Umsatz in welchem
   Zeitraum zählt, wie Rückgaben/Stornos behandelt werden, wann Aufstieg greift und
   ob Abstieg möglich ist. Bestehende manuelle Provisionssätze dürfen nicht
   stillschweigend überschrieben werden. Dann separater Change mit Grenzwerttests.
8. **BMD-Nummern, B-06 und B-07 nach Firmen-/Schulantwort.** Die fachliche
   Partnernummer/Legacy-Referenz und `add-unique-bmd-article-number` brauchen
   anonymisierte Exportzeilen und Feldregeln. B-06 braucht Produkt/Version,
   freigegebene Schnittstelle,
   Synchronisationsrichtung, Fehler-/Konfliktregeln und Testzugang. B-07 beginnt
   nach Chris' Entscheidung mit prüfbarer Abrechnung/Freigabe/Export. **Dieser
   erste Schritt ist nicht das volle B-07.** Echte Banküberweisungen danach als
   eigenen Sicherheits- und Rechts-Change spezifizieren und planen, später
   implementieren. Die Firma muss Zahlungsregeln, Bankzugang und Freigaben
   bestätigen; keine Produktionsbuchung oder Auszahlung aus Testcode.

Unabhängig vom Umsetzungstermin: Bundles/Stücklisten, Schulungsberechtigungen und
PP-/Margen-Provisionen sind **keine automatisch beschlossenen Phase-2-Aufgaben**.
Die Firmenmail beschreibt künftige Möglichkeiten, nicht vollständige Regeln.

## Offene Entscheidungen, jeweils mit Empfehlung

1. **Staging:** Kann Bachl eine getrennte VM/DB/Keycloak/Domain bereitstellen?
   Empfehlung: ja; bei fehlender Ressource nur lokale Docker-Tests und PR-Checks,
   kein echter Kundentest auf der Produktions-VM.
2. **Shop B-02/B-05:** Welche zwei Layouts und welche Anfragefelder sind gewünscht?
   Empfehlung: Kachel- und kompakte Listenansicht; Anfrage mit Name,
   Rückkontakt, Nachricht und Einwilligung, Partner aus Shoproute.
3. **B-03:** Umsatzschwellen, Zeitraum, Storno- und manuelle Override-Regel?
   Empfehlung: Firma bestätigt eine schriftliche Beispieltabelle vor dem Change.
4. **B-04:** Die Empfängergruppen für Bestellmails stehen fest: Kunde (falls Mail),
   zuständige Partnerbenutzer und Firma. Welche konkreten Adressen, Absenderdomain,
   Testpostfächer und zusätzlichen Ereignisse (z. B. Versandbestätigung) gelten?
   Empfehlung: nur bestätigte Bestellereignisse zuerst, Testpostfächer in Staging.
5. **BMD-Stammdaten/B-06:** Welches BMD-Produkt und welche Version? Welche
   Artikel-/Partnerfelder und Formate, führende Nullen, Eindeutigkeit,
   Änderungs-/Deaktivierungsregeln? Nur Import nach PAPO oder auch Bestellungen/
   Provisionen zurück? Welche Test-API und welcher BMD-Kontakt? Empfehlung:
   Herr Hammer fragt die Firma nach anonymisiertem CSV/XLSX-Export, Feldregelblatt,
   Schnittstellendokumentation und freigegebenem Testzugang.
6. **B-07:** Welche Provisionsformel, Abrechnungsperiode, Korrekturen und
   Freigabeperson? Welche Exportformate? Welcher Bank-/Zahlungsweg und welche
   organisatorischen/rechtlichen Freigaben gelten für die spätere echte
   Überweisung? Entscheidung von Chris: Abrechnung zuerst; Bankanbindung
   anschließend separat spezifizieren, planen und später implementieren.
7. **Mehrmandant/Accounts:** Sollen Subdomains schon im Staging gelten? Wer legt
   ersten Firmen-/Partner-Admin an, wer rettet gesperrten letzten Admin und wer
   liest Audit-Ereignisse? Empfehlung: erst tokenbasierte Tenant-Grenze ohne
   Subdomains; Firmen-Admin lädt Partner-Admins ein, Partner-Admin nur eigene
   Mitarbeiter; Audit-Lesezugriff nur für berechtigte Admins.
8. **Abnahme/Schule:** Welche Funktionen müssen bis Herbstferien nachweislich
   funktionieren, wer zeichnet die Testfälle ab und zählt B-06/B-07 zum
   Diplomarbeitsumfang? Empfehlung: Chris und Tobias legen je Feature einen
   Staging-Demoablauf und Testbeleg vor; Herr Hammer klärt Schule/Firma beim
   nächsten Termin. Ohne diese Antwort kein seriöses Versprechen „alles fertig“.
9. **Teamprovision B-09:** Wer erhält Provision, wie wird sie aus einem begrenzten
   Budget verteilt, wann gilt ein Auftrag als bezahlt, wie werden Punkte/Stufen
   berechnet und welche Bonusobergrenze gilt? Chris' Budget- und Produktrichtung
   sowie Planungsempfehlungen sind dokumentiert. Die acht verbleibenden Firmenfragen in
   [TEAM_COMMISSION_DISCOVERY.md](TEAM_COMMISSION_DISCOVERY.md) beantworten und
   mit Beispielrechnungen von der Firma bestätigen lassen, bevor OpenSpec als
   implementierungsreif gilt.

## Begriffsklärung

- **Bundle/Stückliste:** Ein verkaufbarer Artikel besteht aus anderen Artikeln,
  z. B. der in der Firmenmail genannte Artikel 1051 aus 1 × 1053 und 7 × 1052.
  Ob daraus Lager-, Preis- oder Provisionslogik folgt, ist offen.
- **Schulungsberechtigung:** Eine Person/Partnerorganisation darf bestimmte
  Fachbereiche nur nach bestätigter Schulung verkaufen oder einschulen. Wer
  Berechtigung erteilt und wie lange sie gilt, ist offen.
- **PP-/Margen-Provision:** Produktgruppe A/B/C/D liefert einen PP-/Margenwert als
  Berechnungsbasis statt des aktuellen einfachen Prozentsatzes vom Bestellwert.
  Formel, Rundung, Rückgaben und Auszahlungsregeln fehlen.
- **Prüfbare Abrechnung:** Pro Zeitraum nachvollziehbare Provisionen,
  Korrekturen, Freigabe und Export, aber **keine** Überweisung. Bankzahlung braucht
  Empfängerbankdaten, Zahlungsdienst/Bank-API, starke Freigabe, Fehlerbehandlung,
  Abgleich und rechtliche Klärung; technisch möglich, aber deutlich größer.

Die Mail von Sabrina Höhn vom 25.08.2026 ist ohne Kontaktdaten bereits in
`Documentation/Besprechungsprotokolle/Besprechungsprotokoll31_08_2026.md`
fachlich zusammengefasst. Die öffentliche Website des Referenzunternehmens zeigt
Produktkategorien, aber weder BMD-Schnittstellenvertrag noch Provisionsregeln.
Originalmail und Zugangsdaten nicht in Git übernehmen.

## Definition of Done je Change

Fachliche Entscheidung dokumentiert; OpenSpec validiert; Daten-/Rollen-Grenzen
getestet; relevante Backend-Tests und Package grün; Browser Desktop/Mobile bei UI;
Staging-Migration und Rollback getestet; kein Secret im Git-Diff; unabhängiger
Review ohne Blocker; echte Abnahme dokumentiert. Nach jedem Change aktualisieren:
`tasks.md`, Traceability, Phase-2-Status und `CONTINUATION_PROMPT.md`.
