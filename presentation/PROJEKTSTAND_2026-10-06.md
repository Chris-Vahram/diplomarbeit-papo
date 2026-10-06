# Präsentationsstand — Projektstand und Ideen

**Stand:** 06.10.2026. Inhalte für Chris/Tobias und das Gespräch mit dem Lehrer.
Am 07.10.2026 möchte Chris mit dem Lehrer einen Firmentermin vereinbaren;
die Firmenbesprechung selbst hat noch keinen bestätigten Termin.
Dieses Dokument ist Folieninhalt, kein zweiter operativer Handoff und keine
Firmen-/Schulfreigabe. Auf Chris' Wunsch ist die Präsentation jetzt gekürzt und
getrennt: [Projektüberblick](mvp-deck/index.html) mit 13 Folien und
[Sprint-Update](sprint-deck/index.html) mit 8 Folien. Die acht B-09-Fragegruppen
stehen auf Sprintfolien 6/7, weitere Firmenfragen auf Folie 8. Keine Zeiterfassung
in beiden Decks. Bestehendes Theme und Browseraufnahmen bleiben unverändert.

## Neue Umsetzung gegenüber dem Protokollstand 14.09.2026

Die Uhrzeit der Besprechung ist nicht dokumentiert. Ergänzungen vom selben Tag
werden ausdrücklich mitgezählt, nicht sicher als nach dem Gespräch bezeichnet.
Git-Merges belegen den Integrationszeitpunkt; Archivierungsdaten sind keine
eigenständigen Nachweise für den Live-Rollout.

| Datum | Umgesetzt | Grenze / Quelle |
| --- | --- | --- |
| 14.09.2026 | Firmeninterne Partnernotiz bis 4.000 Zeichen; nur Company liest/ändert sie. | Keine Notizhistorie und keine Partner-Ausgabe. [Archivierter Change](../openspec/changes/archive/2026-09-14-add-company-internal-partner-note/proposal.md), PR #16–#24. |
| 14.09.2026 | Firmenprofil und Steuerkennung mit Land/Typ/Wert; konsistente API-/Importvalidierung. | Ein aktuelles Portal-Unternehmen, keine Mandantenfähigkeit oder Behördenabfrage. [Change](../openspec/changes/archive/2026-09-15-add-business-tax-identifiers/proposal.md), PR #25/#26. |
| 25.09.2026 | Registerkennung und privater aktueller Partnernachweis mit Upload, Ersatz und Firmenprüfung. | Annahme/Ablehnung ändert Partneraktivierung nicht automatisch. [Capability](../openspec/specs/partner-trade-proof/spec.md), Commit `7b2d4b7`, PR #27. |
| 25.09.2026 | Bestandsmigration/Demo-Initialisierung abgesichert; alter VM-Demoname als Regression behoben. | Keine erfundenen Kennungen für unbekannte echte Daten. [Rollout-Change](../openspec/changes/archive/2026-09-16-harden-tax-identifier-rollout/proposal.md), PR #28. |
| 28.09.2026 | Exakter Main-Release, Archivprüfung, Health-Check und Backend-/Frontend-Rollback. | Kein Rücksetzen von DB-Schema/Daten. [Health-Change](../openspec/changes/archive/2026-09-28-verify-deploy-health-and-rollback/proposal.md), [Gateway](../openspec/changes/archive/2026-09-28-harden-deployment-gateway/proposal.md), PR #31–#33. |

Zusätzlich wurde das Präsentationsfeedback umgesetzt: digitale Souveränität,
datierte GitHub-Stars, echte Aufnahmen, historischer Clockify-Auszug und Demo-Drehbuch.
Technik-/UI-Dokumentation und Anforderungsmatrix sind vorhanden; die menschlichen
Abnahmen des Thesis-Changes bleiben bei 4/6 offen. Das sind Dokumentationsarbeiten,
keine neuen Laufzeitfunktionen. Teamprovision bleibt reine Planung.

Letzter bestätigter Veröffentlichungsstand: PR #34, Main-Merge `7eddc12`;
CI/CD-Run `37385508517` am 06.10.2026 erfolgreich (Tests, Paket/Image, Deployment).
PR-Testlauf: 130 ohne Fehler. Die Präsentationsänderungen dieser Folgearbeit sind
damit nicht als bereits gepusht oder deployt nachgewiesen.

## Inhaltsgrundlage — Funktionsfähiger aktueller Stand

- Internes Partnerportal: Unternehmen verwaltet Partner/Artikel; Partner bestellt
  für Endkunden; Freischaltung, Bestellung, Status und Provisionssnapshot vorhanden.
- Excel/CSV-Import, Kategorien, Firmenprofil/Steuerkennung und privater aktueller
  Partnernachweis vorhanden. E-Mails weiterhin simuliert.
- Eine Portal-Firma, gemeinsame Partnerlogins. Keine persönlichen Mitarbeiterkonten,
  Teamprovision, Zahlungserfassung, öffentliche Shops oder direkte BMD-Anbindung.
- Demo-Deployment mit Health-Check und Anwendungs-Rollback abgeschlossen;
  aktuelle VM nur für Demo-Daten, nicht für echten Firmenbetrieb freigegeben.

## Inhaltsgrundlage — Bestätigte Produktrichtung, noch nicht umgesetzt

- Zwei isolierte Testunternehmen, persönliche Firmen-/Partnerkonten und Audit.
- Öffentlicher Partnershop mit Gastbestellung und optionalen Kundenkonten;
  keine bestätigte Online-Zahlung.
- Teamprovision innerhalb einer Partnerfirma: Verkäufer, Team und Bonus teilen
  vorhandene Partnerprovision; keine zusätzliche Auszahlungssumme.
- Erstumfang als akzeptierter Vorschlag: Zuordnung/Geldwerte und Zahlungserfassung.
  Punkte/Karriere nach Nutzen, Bonus nach Firmenregeln; keine Schulungen.
- B-03 ist automatische Partnerfirmen-Provisionsstufe; B-09 ist Mitarbeiter-
  Teamverteilung. Nicht vermischen, keine erfundenen Schwellen zeigen.

## Inhaltsgrundlage — Was mit der Firma geklärt werden muss

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

## Inhaltsgrundlage — Realistischer nächster Schritt

- Lehrertermin: Firmenbesprechung vereinbaren und Abnahme-/Prioritätsliste klären;
  keine Zusage „alles fertig bis Herbstferien“ ohne Regeln und Freigaben.
- Parallel planbar: OIDC-Identity-Grundlage separat von BMD-Nummern; danach
  Mandantengrenzen, persönliche Konten/Audit und Staging.
- B-09 nach Voraussetzungen/Firmenregeln; B-06/B-07 ohne Vertrag/Zahlungsregeln blockiert.
- Aktive Changes: Thesis-Dokumentation 4/6 (menschliche Abnahmen offen),
  Login-Entkopplung 0/6, BMD-Artikelnummer 0/6 (Beispieldaten fehlen).

## Abgleich mit den getrennten Decks

- `mvp-deck/index.html`: allgemeine Beschreibung, Nutzen, zwei Souveränitätsfolien,
  Stack, Architektur, Browserbeispiele und Demo. Keine Sprint- oder Zeitfolien.
- `sprint-deck/index.html`: Stand, Umsetzung, Ausbau, nächste Schritte und Fragen.
  B-06/B-07 sind zur Entscheidung geöffnet, nicht dauerhaft ausgeschlossen;
  B-09 mit allen acht Firmenfragegruppen enthalten. Details bleiben in diesem
  Dokument und in der Erkundung, statt jede Unterfrage auf eine Folie zu drängen.
- Keine Zahlen zu Punkten, Schwellen, Karriere, Boni oder Einsatzzeiten erfinden.
  Screenshot-/Clockify-/UI-Abnahme bleibt Chris/Tobias vorbehalten.
- Chris hat die Inhaltsänderung beauftragt; Tobias' Theme/Design bleibt erhalten.

## Quellen und Nachweisgrenze

- [Operativer Handoff](../Specification/CONTINUATION_PROMPT.md)
- [Produktplan](../Specification/PRODUCT_COMPLETION_PLAN.md)
- [Phase-2-Backlog](../Specification/phase2_backlog.md)
- [Neueste Besprechungsnotizen](../Documentation/Besprechungsprotokolle/Besprechungsprotokoll_14.9.md)

Historische Prüfung vor der Dokumentationsveröffentlichung: HEAD 25ce0ee,
Main-Workflow 36433087198 erfolgreich. PR #34 und der danach erfolgreiche
Main-Run sind oben gesondert dokumentiert. Browserprüfungen der Präsentationen
sind keine neue angemeldete Anwendungsabnahme; Veröffentlichung ersetzt keine
fachliche Abnahme. Am 07.10.2026 hat Chris Veröffentlichung der getrennten Decks
und aktualisierten Kontextdateien beauftragt. PR #35 (Inhaltscommit `67a5acf`) trägt den Titel
`docs: publish split presentations and session context`; seinen Merge-/CI-Status
beim Wiedereinstieg prüfen. Der Folien-Inhaltsstand vom 06.10.2026 bleibt erhalten;
ein neuer Lehrer-/Firmentermin oder neue Geschäftsregeln sind nicht bestätigt.
