# Präsentationsvorbereitung — Projektstand und Ideen

**Stand:** 06.10.2026. Inhalte für Chris/Tobias und das Gespräch mit dem Lehrer.
Am 07.10.2026 möchte Chris mit dem Lehrer einen Firmentermin vereinbaren;
die Firmenbesprechung selbst hat noch keinen bestätigten Termin.
Dieses Dokument ist Folieninhalt, kein zweiter operativer Handoff und keine
Firmen-/Schulfreigabe. Bestehendes Deck/Design bleibt zunächst unverändert.

## Folie 1 — Funktionsfähiger aktueller Stand

- Internes Partnerportal: Unternehmen verwaltet Partner/Artikel; Partner bestellt
  für Endkunden; Freischaltung, Bestellung, Status und Provisionssnapshot vorhanden.
- Excel/CSV-Import, Kategorien, Firmenprofil/Steuerkennung und privater aktueller
  Partnernachweis vorhanden. E-Mails weiterhin simuliert.
- Eine Portal-Firma, gemeinsame Partnerlogins. Keine persönlichen Mitarbeiterkonten,
  Teamprovision, Zahlungserfassung, öffentliche Shops oder direkte BMD-Anbindung.
- Demo-Deployment mit Health-Check und Anwendungs-Rollback abgeschlossen;
  aktuelle VM nur für Demo-Daten, nicht für echten Firmenbetrieb freigegeben.

## Folie 2 — Bestätigte Produktrichtung, noch nicht umgesetzt

- Zwei isolierte Testunternehmen, persönliche Firmen-/Partnerkonten und Audit.
- Öffentlicher Partnershop mit Gastbestellung und optionalen Kundenkonten;
  keine bestätigte Online-Zahlung.
- Teamprovision innerhalb einer Partnerfirma: Verkäufer, Team und Bonus teilen
  vorhandene Partnerprovision; keine zusätzliche Auszahlungssumme.
- Erstumfang als akzeptierter Vorschlag: Zuordnung/Geldwerte und Zahlungserfassung.
  Punkte/Karriere nach Nutzen, Bonus nach Firmenregeln; keine Schulungen.
- B-03 ist automatische Partnerfirmen-Provisionsstufe; B-09 ist Mitarbeiter-
  Teamverteilung. Nicht vermischen, keine erfundenen Schwellen zeigen.

## Folie 3 — Was mit der Firma geklärt werden muss

- Provisionsbasis und Systemgrenze: interne Anzeige oder individuelle Ansprüche?
- Empfänger, Stichtag, Gleichverteilung/Gewichtung, Rundung und Restbeträge.
- Zahlungsnachweis, Teilzahlung/Storno/Rückgabe, Abrechnungsperiode und Freigabe.
- Regelversionen, Teamleiterrechte, Affiliate-Zuordnung und Korrekturfristen.
- Nutzen/Erstumfang von Punkten/Karriere sowie Bonusregeln und Budgetobergrenze.
- Zusätzlich: BMD-Beispielexporte/-Vertrag, B-03-Schwellen, Staging,
  Account-Provisionierung/Wiederherstellung und verbindlicher Schul-/Firmenumfang.

Vollständige acht B-09-Firmenfragen und unverbindliches Rechenbeispiel:
[Teamprovisions-Erkundung](../Specification/TEAM_COMMISSION_DISCOVERY.md).
70/25/5 und Beispielschwellen niemals als bereits bestätigte Regeln präsentieren.

## Folie 4 — Realistischer nächster Schritt

- Lehrertermin: Firmenbesprechung vereinbaren und Abnahme-/Prioritätsliste klären;
  keine Zusage „alles fertig bis Herbstferien“ ohne Regeln und Freigaben.
- Parallel planbar: OIDC-Identity-Grundlage separat von BMD-Nummern; danach
  Mandantengrenzen, persönliche Konten/Audit und Staging.
- B-09 nach Voraussetzungen/Firmenregeln; B-06/B-07 ohne Vertrag/Zahlungsregeln blockiert.
- Aktive Changes: Thesis-Dokumentation 4/6 (menschliche Abnahmen offen),
  Login-Entkopplung 0/6, BMD-Artikelnummer 0/6 (Beispieldaten fehlen).

## Abgleich mit bestehendem Deck

- `mvp-deck/index.html`: Folien „Out of Scope / Phase-2-Backlog“ und „Nächste Schritte“
  später aktualisieren. B-06/B-07 sind zur Entscheidung geöffnet, nicht dauerhaft
  ausgeschlossen; B-09 ergänzen. Neue Ideen klar als geplant/firmenabhängig markieren.
- Keine Zahlen zu Punkten, Schwellen, Karriere, Boni oder Einsatzzeiten erfinden.
  Screenshot-/Clockify-/UI-Abnahme bleibt Chris/Tobias vorbehalten.
- Inhaltsänderungen am Deck separat abstimmen; Tobias' Theme/Design nicht ersetzen.

## Quellen und Nachweisgrenze

- [Operativer Handoff](../Specification/CONTINUATION_PROMPT.md)
- [Produktplan](../Specification/PRODUCT_COMPLETION_PLAN.md)
- [Phase-2-Backlog](../Specification/phase2_backlog.md)
- [Neueste Besprechungsnotizen](../Documentation/Besprechungsprotokolle/Besprechungsprotokoll_14.9.md)

Git/OpenSpec vor Veröffentlichung geprüft: HEAD 25ce0ee, Main-Workflow 36433087198
erfolgreich. Keine neue Live-Abnahme, Backend- oder Browserprüfung in dieser
Dokumentationsarbeit. Veröffentlichung ersetzt keine fachliche Abnahme.
