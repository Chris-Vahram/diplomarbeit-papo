# Firmen-Rückmeldung zum ersten Test (03.11.2026) — Auswertung

**Eingang:** über Prof. Hammer, bei Chris am 22.11.2026. Auswertung am 22./23.11.2026 gegen den
Code-Stand `main` (`e6d356e`). Originale (Test- und Änderungsliste, Gesamtpflichtenheft V1.2.SH vom
07.08.2026, Mail) liegen **nur lokal** unter `private/firmenfeedback-2026-11-03/` (gitignored — das Repo
ist öffentlich; Firmenname, Personen und Provisionssätze nicht committen).

Die Firma trennt ausdrücklich: **A** = steht schon im Pflichtenheft, **B** = Testbeobachtung/Frage,
**C** = neue Anforderung (für V1.3.SH vorgemerkt). Keine Aufforderung, alles sofort umzusetzen.
**Prioritäten der Firma jetzt:** DOI-Verfahren, vollständige Artikelstammdaten, Grundlagen der
Provisionsberechnung; dazu Partner-Stammdatenfelder + optionale UID; Partnerwebsite-Umfang früh abstimmen.

## Bedeutung

1. Das Pflichtenheft V1.2.SH ist deutlich größer als unsere FSD (PP/QP, Karrierestufen, Einschulungen,
   Leihvorgänge, Gutscheine/Aktionen, Statusmodell ENTWURF/DOI_WARTET/…/STORNIERT). Die Firma will vor
   allem, dass das **Datenmodell** diese Grundlagen schon berücksichtigt.
2. Zwei gesperrte Entscheidungen aus `CLAUDE.md` sind damit fachlich überholt und mit Chris neu zu
   beschließen: **Provision = ein `commissionPercent` je Partner** (Punkt 8) und **Kunde mit Pflicht nur
   Nachname + Adresse** (Pflichtenheft §8.3: Anrede, Vor-/Nachname, E-Mail, Telefon, Land, Sprache, DOI).
3. **Partnerwebsite (Punkt 14):** frei gestaltbarer Shop-Baukasten wird von der Firma nicht gewollt.
   Ziel: fertige Seite, Partner pflegt nur Profilbild, Kontaktdaten, kurzen Begrüßungstext; jede Änderung
   braucht Freigabe durch die Firma, bis dahin bleibt der alte Stand online. Betrifft Tobias' Shop-Arbeit
   → Tobias zeitnah informieren.
4. Scope-Frage für Team + Prof. Hammer: was gehört noch in die Diplomarbeit, was ist Produktausbau danach.

## Ranking (Priorität × Aufwand)

### Sofort umsetzbar (klein, klar)

| Punkt | Thema | Aufwand | Code-Befund |
|---|---|---|---|
| 2 | Dialog „Neuer Endkunde“ durchsichtig | 1 Zeile | `.customer-create-dialog` nutzt `var(--surface-primary)` (`frontend/src/styles/base.css` ~2208), Token existiert nicht → transparent. Fix: `var(--bg-surface)` |
| 10 | UID bei Partneranlage optional | winzig | Backend: `PartnerRequest.taxIdentifier` ohne `@NotNull` → schon optional. Formular prüfen |
| 7 | Zeitraum bei „Top-Endkunden“ unklar | klein | Zeitraumwahl existiert oben im Partner-Dashboard; Karte zeigt nicht, welcher Zeitraum gilt |

### Jetzt planen (je eigener OpenSpec-Change, Firmen-Priorität)

| Rang | Punkt | Thema | Aufwand | Code-Befund |
|---|---|---|---|---|
| 1 | 3 | DOI + Ermächtigungs-Checkbox bei Kundenanlage (DOI-Mail mit Datenschutzinfo/Link, 14 Tage Frist, Löschung bei Ablauf, Partner-Info-Mail, Bestellsperre bis DOI) | groß | nichts vorhanden; Mail-Outbox aus `send-order-emails` wiederverwendbar |
| 2 | 6 | Bestell-Bestätigung: Pflicht-Checkbox(en) vor Absenden, protokolliert mit Versionen (§7.2.1: fremde Adresse, Widerruf, AGB); AGB als PDF in Kundenbestätigung | mittel | Bestell-Mails an Kunde/Partner/Firma vorhanden; Checkboxen + AGB-Anhang fehlen |
| 3 | 5 | Artikelstamm: BMD-Artikelnummer, PP, QP, Preiszonen AT/DE, MwSt je Land, Nettopreise | mittel–groß | `taxRatePercent` nur 20/13/10/0 (`ArticleTax.ALLOWED_RATES`), **DE 19/7 und Preiszonen fehlen**; Artikelnummer = offener Change `add-unique-bmd-article-number` |
| 4 | 9 | Partner-Stammdaten: Anrede, Titel vorne/hinten, Vor-/Nachname getrennt, Straße+Hausnummer in einem Feld | mittel | `Partner.name` ist ein Feld → Migration nötig |
| 5 | 8 | Provisionsgrundlage: PP-basiert, Karrierestufen (konfigurierbar), Produktart, Einschulungsbonus an ggf. anderen Partner | groß | heute `commissionPercent` × Bestellwert; erst gemeinsamer Termin mit der Firma, dann Datenmodell |

### Vorbereiten (danach)

| Punkt | Thema | Aufwand | Code-Befund |
|---|---|---|---|
| 4 | Firmen-Übersicht aller Endkunden (suchen, ansehen, bearbeiten, DOI-Status, Partnerzuordnung) | mittel | nur `/api/partner/customers`, kein Firmen-Endpunkt |
| 12 | Firma lädt Gewerbeschein/Nachweise selbst hoch | klein–mittel | `CompanyPartnerTradeProofResource` hat nur Status/Download/Review |
| 13 | Import-Musterdatei + Datenexport | klein–mittel | keine Vorlage; Export nur Provisionen (`export/CsvExportService`) |
| 16 | Kundentyp Privat/Firma, Firmenname, Übernahme in Bestellung/Rechnungsadresse | mittel | `Kunde` ohne Typ/Firmenname |
| 14/15 | vereinfachte Partnerwebsite mit Freigabe; verständliche Fehler, kein Datenverlust | groß | Tobias' Bereich; heutiger Baukasten muss nicht überarbeitet werden |

### Zukunft (nichts tun)

- Punkt 1: Eigenbestellung des Partners + Eigenrabatt — hängt an Punkt 8.
- Punkt 11: UID-Bestätigungs-Checkbox nur bei späterer Selbstregistrierung.
- Punkt 17 (nur in der Liste): Endkunde wird Partneranwärter/Partner, ohne Doppelanlage.

## Antworten an die Firma (Entwurf-Stichpunkte)

- „Inaktiv“ bei Endkunden = länger keine Bestellung (Ruhe-Schwelle 60 Tage), **nicht** DOI. Wording klären.
- DOI ist noch **nicht** programmiert (nicht nur deaktiviert); wird als nächstes geplant.
- Status „abgeschlossen“: Endkunde bekommt eine neutrale Abschluss-Mail; echter Versand wartet auf
  echte Daten (Umleitung aktiv).
- Provision bewusst vereinfacht; gemeinsamer Termin zur Provisionslogik vorschlagen.
- Zeitraum für „Top-Endkunden“ = Zeitraumwahl oben im Dashboard (wird sichtbarer gemacht).
