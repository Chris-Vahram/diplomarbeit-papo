# PAPO – Plan zur funktionalen Fertigstellung

Stand: 25.09.2026. Dieser Plan ist eine Arbeitsgrundlage, keine Freigabe durch Schule
oder Referenzunternehmen. Die aktuelle FSD beschreibt das interne MVP; die gewünschte
Erweiterung wird in kleinen OpenSpec-Changes spezifiziert und einzeln umgesetzt.
`Specification/CONTINUATION_PROMPT.md` bleibt der einzige operative Session-Handoff.

## Bestätigte Zielrichtung durch Chris

- Ergebnis bis zu den Herbstferien: sicher deploybare Staging-Version der erweiterten
  Produktfunktionen; schriftliche Diplomarbeit kann danach weitergeführt werden.
- Phase 2 soll nicht beim MVP enden. B-06 (direkte BMD-Anbindung) und B-07
  (Provisionsabrechnung/Auszahlung) werden zur Entscheidung wieder geöffnet, aber
  nicht ohne Firmen-/Schulabstimmung als fertig spezifizierte Funktionen ausgegeben.
- Zwei Testunternehmen dürfen manuell angelegt werden. Die Trennung ihrer Daten und
  Zugriffe ist zwingend. Subdomains sind noch nicht bestätigt.
- Persönliche Logins auf Firmen- und Partnerseite: je Organisation Administratoren
  (Konten anlegen, sperren, verwalten) und Mitarbeiter. Wichtige Änderungen zeigen
  Person und Zeitpunkt in einem Audit-Log; keine Leistungsbewertung.
- Öffentlicher Partnershop: einmalige Gastbestellung oder optionales Kundenkonto zum
  Speichern eigener Daten. Bestellung ist kostenpflichtig, Zahlung/Erfüllung erfolgen
  später; kein Online-Bezahlvorgang bestätigt.
- Chris und Tobias können die aktuelle VM administrieren. Der private Schlüssel für
  den eingeschränkten GitHub-Deploy-Zugang ist lokal noch nicht gefunden; das
  GitHub-Secret ist vorhanden, aber sein Wert ist nicht rücklesbar.

## Herbstferien-Ziel und Machbarkeit

Chris wünscht die **gesamte Phase 2 plus Mandantenfähigkeit und persönliche Konten**
funktional fertig, nicht nur das MVP. Das ist der gewünschte Umfang, aber noch keine
von Firma/Schule bestätigte Abnahme oder belastbare Terminzusage. Zwischen diesem
Stand und dem 27.10. liegen rund 32 Tage; B-03, B-06 und B-07 haben wesentliche
fehlende Regeln/Zugänge. Ohne deren rechtzeitige Klärung kann „alles fertig“ nicht
seriös zugesagt werden. Chris entscheidet nach dem nächsten Termin ausdrücklich,
welche Funktionen zwingend bis Herbstferien in Staging abgenommen werden und welche
als späterer Teil des vollständigen Produktumfangs gelten. Empfehlung für den ersten
prüfbaren Meilenstein: getrenntes Staging, zwei isolierte Testunternehmen,
persönliche Konten/Audit, B-08 und B-04. Das ist **keine stillschweigende Kürzung**
des Gesamtziels; B-01 bis B-07 bleiben im Plan und brauchen eigene Entscheidungen.

## Sequenz für den nächsten Implementierungsagenten

Jeder Schritt erhält **einen eigenen** OpenSpec-Change mit Proposal, Design,
Capability-Specs und überprüfbaren Tasks. Erst Change validieren und freigeben,
dann implementieren, testen, reviewen und archivieren. Keine Sammel-Implementierung
von B-01 bis B-08. Vor jedem Change aktuelle Protokolle und Git-Stand prüfen.

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
   empfohlen mit prüfbarer Abrechnung/Freigabe/Export. **Dieser erste Schritt ist
   nicht das volle B-07**, wenn tatsächliche Auszahlung gefordert wird. Echte
   Banküberweisungen sind ein eigener Sicherheits- und Rechts-Change; ob sie zum
   beauftragten Umfang gehören, entscheiden Firma/Schule/Chris. Keine
   Produktionsbuchung oder Auszahlung aus Testcode.

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
   Freigabeperson? Welche Exportformate? Sind echte Banküberweisungen überhaupt
   beauftragt und rechtlich/organisatorisch freigegeben? Empfehlung: zuerst
   prüfbare Abrechnung ohne Geldtransfer, Bankanbindung nur als eigener Change.
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
