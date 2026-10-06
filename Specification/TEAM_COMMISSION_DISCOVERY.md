# Teamprovision, Punkte und Karrierestufen — Entscheidungsstand

**Stand:** 07.10.2026 (Kontextabgleich; fachliche Entscheidungen vom 06.10.2026 unverändert)

**Status:** Produktrichtung und Planungsempfehlungen durch Chris bestätigt; Firmenregeln offen. Noch nicht implementierungsreif; kein OpenSpec-Change angelegt.
**Zweck:** Verlässlicher Wiedereinstieg in die Featureplanung ohne erneute Befragung zu bereits beantworteten Punkten.

## Fachlicher Fortsetzungsprompt

Die operative Session-Übergabe steht ausschließlich in
[CONTINUATION_PROMPT.md](CONTINUATION_PROMPT.md). Dieser Abschnitt beschreibt
nur die Fortsetzung der B-09-Erkundung, keinen separaten Git-/Arbeitsauftrag.

Lies zuerst Specification/CONTINUATION_PROMPT.md und dieses Dokument vollständig,
danach Specification/PRODUCT_COMPLETION_PLAN.md, Specification/phase2_backlog.md,
relevante aktive OpenSpec-Changes sowie den aktuellen Code zu Aufträgen, Provision,
Authentifizierung und Dashboard.

Setze die Erkundung am Entscheidungsstand vom 06.10.2026 fort. Chris hat die
unten genannten Empfehlungen als Planungsrichtung akzeptiert; das ersetzt keine
Firmenfreigabe für Geldansprüche, Verteilung, Rechte oder Abrechnung. Bereits
beantwortete Nutzerfragen nicht erneut stellen. Die acht verbleibenden
Firmenfragen unten sind die Gesprächsagenda. Am 07.10.2026 möchte Chris mit dem
Lehrer einen Firmentermin vereinbaren; der Firmentermin selbst steht noch nicht
fest. Keine Geschäftsregel aus fehlenden Firmenantworten ableiten.

Ein erfolgtes Lehrer-/Firmengespräch ist am 07.10.2026 noch nicht bestätigt.
Die acht Firmenfragegruppen stehen kompakt auf Sprintfolien 6/7; vollständige
Unterfragen bleiben hier. Decks und Belege: [Präsentationen](../presentation/README.md).
Kontext und Decks werden auf ausdrücklichen Auftrag veröffentlicht; daraus
folgt keine Implementierungsfreigabe und keine zusätzliche Firmenentscheidung.

Ziel ist ein implementierungsreifer OpenSpec-Change mit Proposal, Design,
Capability-Specs, Abnahmekriterien, Daten-/Migrationsplan und abhängigen
Umsetzungsschritten. Noch keine Anwendungsimplementierung beginnen. B-03
(automatische Änderung des Partnerfirmen-Provisionssatzes) nicht mit der neuen
Mitarbeiter-Provisionslogik vermischen. Erst nach Klärung der Geschäftsregeln
OpenSpec-Artefakte schreiben.

## Aktueller Produkt- und Codekontext

- Der Nutzer möchte das im Figma-Screenshot gezeigte Provisions-/Team-Dashboard als spätere PAPO-Funktion verfolgen.
- Im Ist-Code meldet sich eine Partnerfirma über einen gemeinsamen Zugang an. Persönliche Mitarbeiterkonten, Teamzuordnung und Teamleiterrechte sind noch nicht implementiert.
- Partner.commissionPercent ist derzeit ein Prozentsatz pro Partnerfirma. Bei Bestellerstellung erzeugt CommissionService einen CommissionRecord mit Bestellwert, verwendetem Prozentsatz und Betrag als Snapshot.
- Auftragsstatus sind derzeit SUBMITTED, REVIEWED und FULFILLED. Es gibt keinen Zahlungsstatus, Zahlungseingang und keine Teilzahlungs-/Erstattungsbuchung.
- B-01, der öffentliche Endkundenshop, ist noch nicht implementiert. Es gibt daher auch keine Affiliate-Link-Zuordnung.
- Bestehende OIDC-Identitätsbindung, Mehrmandantenfähigkeit, persönliche Mitarbeiterkonten und Audit brauchen eigene vorgelagerte Changes gemäß Produktplan.

Diese Punkte sind Ist-Zustand, keine bereits gelösten Anforderungen.

## Historische Entscheidungen des Nutzers vom 01.10.2026

Der folgende Block bleibt als Verlauf erhalten. Der Entscheidungsstand vom
06.10.2026 darunter geht ihm vor, insbesondere beim Erstumfang und Provisionsbudget.

1. **Team:** Mitarbeiter derselben Partnerfirma bilden das Team, insbesondere Verkäufer und Teamleiter. Partnerfirma samt Mitarbeitern bildet das betrachtete Netzwerk. Untergeordnete Partnerfirmen gehören vorerst nicht dazu.
2. **Provision:** Regeln sind pro Partnerfirma konfigurierbar. Grundsätzlich erhält der Mitarbeiter die vollständige vorgesehene Provision. Eine Partnerfirma kann einen Teamanteil festlegen und einen höheren Anteil für Teamleiter aktivieren. Konkrete Werte und Verteilung fehlen.
3. **Verkaufszuordnung:** Ersteller einer internen Bestellung gilt zunächst als Verkäufer. Ein Admin kann die Zuordnung ändern. Partner-Admins dürfen nachträglich zuweisen oder im Namen eines Mitarbeiters bestellen. Ursprüngliche und spätere Zuordnungen müssen mit Zeit und verantwortlicher Person auditiert werden. Shopbestellungen sollen per Affiliate-Link zugeordnet werden; ohne Link kommen sie in den Team-Topf.
4. **Berechnungsgrundlage:** In erster Version zählen nur erfüllte und bezahlte Bestellungen. Eigenumsatz, Teamumsatz und Teamprovision sollen getrennt sichtbar sein. Eine ungefähre Aufteilung von zwei Dritteln Eigenprovision und einem Drittel Teamanteil ist nur ein Beispiel, keine festgelegte Regel. Der Rest des Teamanteils kann nach einer noch festzulegenden Regel verteilt werden.
5. **Punkte/Karriere:** Punkte können je Produkt und Bestellung variieren, etwa nach Produktpreis. Mitarbeiterpunkte können gegebenenfalls in einen gemeinsamen Team-Topf fließen und nach konfigurierbaren Regeln verteilt werden, wenn dies fair ist. Zeiträume, Verteilung und Stufen sind offen. Fairnessrisiken eines gemeinsamen Punktetopfs sollen ausdrücklich geprüft werden.
6. **Meilensteine/Boni:** Schwellen und/oder Zeiträume, Höhe, Häufigkeit, Berechnungsbasis und Empfänger sollen konfigurierbar sein. Das Modell muss wirtschaftlich tragfähig bleiben.
7. **Historie:** Jede Bestellung soll die damals geltenden Regeln und daraus berechneten Ergebnisse dauerhaft nachvollziehbar speichern. Konfigurationsänderungen dürfen vergangene Abrechnungen nicht unbemerkt verändern.
8. **Erste Funktionsversion:** Eigen-/Teamprovision, Eigen-/Teamumsatz, Punkte, Karrierestufe, Bonusfortschritt und eine eigene Teamverwaltungsseite. Teamleiter bekommen die dafür nötigen Rechte. Seminare und Schulungen sind vorerst ausgeschlossen.

## Aktueller Entscheidungsstand vom 06.10.2026

### Bestätigte Nutzerentscheidungen

- Ein Provisionsbetrag je Bestellung: `Partner.commissionPercent` berechnet die
  bestehende Partnerprovision. Verkäuferanteil, Teamanteil und Geldbonus teilen
  höchstens diesen Betrag auf; kein zusätzlicher Mitarbeiter-Topf.
- Zunächst interne Zuordnung und Anzeige. Individuelle Mitarbeiteransprüche,
  Mitarbeiterabrechnungen und Auszahlungen bleiben Firmenentscheidung.
- Bei Verkäuferzuordnung gelten standardmäßig 100 % Verkäufer, solange keine
  andere Verteilung ausdrücklich aktiviert ist. Ein positiver Teamanteil oder
  Bonus innerhalb dieses Budgets muss andere Anteile entsprechend verringern.
- Ohne gültigen Affiliate-Link bleibt eine Shopbestellung ohne persönlichen
  Verkäufer und gehört zur Partnerfirma/Team-Topf, nicht automatisch zum Teamleiter.
- Für die gewünschte Umsatz-, Provisions- und Bonusauswertung zählen nur erfüllte
  und bezahlte Bestellungen. Zahlung und Bestellstatus bleiben getrennte Fakten;
  das aktuelle Dashboard prüft weiterhin nur Erfüllung.
- Interner Erfasser ist zunächst Verkäufer; Partner-Admin darf für Mitarbeiter
  bestellen und Zuordnung korrigieren. Ursprüngliche und aktuelle Zuordnung sowie
  Änderungen mit Akteur und Zeitpunkt bleiben nachvollziehbar.
- Historische Provisionsdatensätze und freigegebene Abrechnungen ändern sich nicht
  still. Spätere Änderungen benötigen referenzierte Korrekturen.
- Mitarbeiter derselben Partnerfirma bilden das Team. Untergeordnete Partnerfirmen,
  Seminare und Schulungen bleiben ausgeschlossen. Geldprovision und Punkte sind
  getrennt; Karrierestufen ändern zunächst keine Geldprovision. B-03 bleibt separat.

### Von Chris akzeptierte Planungsempfehlungen, Firmenbestätigung ausstehend

Chris erklärte am 06.10.2026, die Empfehlungen zu den offenen Fragen passen.
Das bestätigt folgende Gesprächs-/Planungsrichtung, nicht konkrete Geld-, Rechte-
oder Buchhaltungsregeln:

- Erstumfang vorschlagen: Verkäufer-/Teamzuordnung und Geldwerte; getrennte
  Zahlungserfassung als Voraussetzung. Punkte/Karriere zunächst zurückstellen,
  sofern die Firma keinen eigenständigen Nutzen bestätigt; Bonus erst mit Regeln.
  Gewünschte spätere Funktionen sind dadurch nicht gestrichen.
- Zunächst ein Team je Partnerfirma, Team-Topf gleichmäßig auf bestätigte berechtigte
  Empfänger verteilen. Teamleitergewichtung, Empfänger und Stichtag bleiben offen.
- Regeln versionieren und verwendeten Regelstand bei Bestellung speichern.
  Gültigkeitszeitpunkt, Ausnahmen und Änderungs-/Freigaberechte bestätigt die Firma.
- Zahlungseingang separat mit Betrag, Datum, Referenz/Nachweis und Akteur erfassen.
  Wer erfassen darf und wie Korrekturen wirken, ist noch keine beschlossene Regel.
- Zustände vorschlagen: **vorgemerkt** (berechnet, Voraussetzungen fehlen),
  **abrechnungsfähig** (bestätigte Zahlungs-/Erfüllungs-/Verteilungsregeln erfüllt),
  **abgerechnet** (in freigegebener Abrechnung enthalten). Abgerechnet bedeutet
  nicht ausgezahlt; Anzeige begründet keinen bestätigten individuellen Anspruch.
- Bestellbezogene Affiliate-Zuordnung ohne dauerhaftes Besucherprofil vorschlagen.
  Letzter gültiger Link im aktuellen Bestellvorgang ist eine Empfehlung, keine
  bestätigte Mehrlink-Regel. Vor Freigabe auditierte Korrektur, danach Gegenbuchung.
- Teamleiterrechte eng begrenzen; keine automatische Sicht auf persönliche Geldwerte,
  Regeländerung oder Abrechnungsfreigabe. Verbindliche Rechte-Matrix fehlt.
- Persönliche Punkte nicht pauschal verteilen. Falls benötigt, bestätigte
  Artikel-/Mengenwerte als Snapshot; Teamfortschritt getrennt aggregieren.
- Geldbonus aus begrenzter Reserve innerhalb der vorhandenen Provision vorschlagen.
  Zusätzliche Finanzierung aus Firmenmarge braucht eine eigene Firmenentscheidung.
- Rundung reproduzierbar und summenerhaltend vorsehen; Empfänger von Restcents,
  ungenutzte Reserven und Beträge ohne berechtigte Empfänger sind Firmenregeln.
  Vorschlag: unverteilt/reserviert sichtbar lassen, nicht automatisch umverteilen.

### Unverbindliches Rechenbeispiel, keine beschlossenen Werte

Fiktiver Bestellwert 1.000,30 EUR, Partnersatz 10 %, Provisionsbetrag 100,03 EUR.
Keine Aussage zu netto/brutto, Steuer, Versand oder Mitarbeiteransprüchen.
Nur für dieses Beispiel: 70 % Verkäufer, 25 % Team, 5 % Bonus; Anna ist Verkäuferin,
Anna/Ben/Cem sind gleichberechtigte Teamempfänger, Ben erhält hypothetisch den Bonus.

Exakte Budgetanteile: 70,021 / 25,0075 / 5,0015 EUR. Beispielverfahren: auf Cent
abrunden, verbleibenden Cent dem größten Rest zuweisen; ergibt 70,02 / 25,01 / 5,00 EUR.
Teamtopf 25,01 EUR: dreimal 8,33 EUR plus zwei Restcents, im Beispiel Anna und Ben.
Diese Restcent-Zuordnung ist keine freigegebene Rundungsregel.

| Empfänger | Verkäuferanteil | Teamanteil | Bonus | Interne Zuordnung |
|---|---:|---:|---:|---:|
| Anna | 70,02 EUR | 8,34 EUR | 0,00 EUR | 78,36 EUR |
| Ben | 0,00 EUR | 8,34 EUR | 5,00 EUR | 13,34 EUR |
| Cem | 0,00 EUR | 8,33 EUR | 0,00 EUR | 8,33 EUR |
| Gesamt | 70,02 EUR | 25,01 EUR | 5,00 EUR | 100,03 EUR |

## Historische Prüfung und Empfehlungen vom 01.10.2026

Die folgenden Hinweise bleiben fachlicher Kontext. Aktuelle Bestätigung und offene
Firmenfreigaben stehen im Block vom 06.10.2026; insbesondere ist „verdient“ keine
geklärte Aussage über einen individuellen Mitarbeiteranspruch.

- **„Volle Provision“ und Teamanteil sind verschiedene Konfigurationen.** Als Standard ist volle Verkäuferprovision genannt; zugleich steht eine Zwei-Drittel-zu-ein-Drittel-Aufteilung im Raum.
  **Empfehlung:** Standard bleibt 100 % Verkäufer / 0 % Team, bis eine Partnerfirma ausdrücklich Teamverteilung aktiviert. Jede Aufteilung muss innerhalb eines festgelegten Provisionsbudgets bleiben. Eine beispielhafte Regel wie 70 % Verkäufer, 25 % Team und höchstens 5 % Bonusreserve ist nur ein Diskussionsbeispiel, keine Nutzerentscheidung.
- **„Firma zahlt Provision“ kann zwei Geldflüsse meinen.** PAPO könnte nur die Zahlung des Unternehmens an die Partnerfirma ausweisen oder echte Ansprüche einzelner Mitarbeiter verwalten. Das ändert Abrechnung, Datenschutz und Systemgrenze.
  **Empfehlung:** Bestehende Provision zunächst als Betrag der Partnerfirma behandeln; Mitarbeiteraufteilung als nachvollziehbare interne Verteilung darstellen. Direkte Auszahlung an Mitarbeiter erst spezifizieren, wenn die Firma deren Vertrags- und Abrechnungsmodell bestätigt hat.
- **„Bezahlt und erfüllt“ braucht zwei getrennte Fakten.** Erfüllung bedeutet im aktuellen Modell abgeschlossene Lieferung; Zahlungseingang wird nicht erfasst. Zahlung kann früher/später eintreffen oder teilweise erfolgen.
  **Empfehlung:** Zahlungsstatus/-buchung getrennt vom Auftragsstatus führen. Erfüllung und Zahlungseingang mit Person/Zeit dokumentieren; Provision zunächst nur bei vollständig bezahlt und erfüllt als verdient anzeigen. Teilzahlungen, Erstattungen und Stornos benötigen bestätigte Regeln.
- **Teamumsatz und Eigenumsatz können doppelt wirken.** Ist Teamumsatz eine Summe inklusive Eigenumsatz oder nur Umsatz der übrigen Mitarbeiter?
  **Empfehlung:** Kennzahlen eindeutig benennen: persönlicher Eigenumsatz plus Teamgesamtumsatz inklusive Eigenumsatz; falls beides addiert werden soll, stattdessen „Eigenumsatz“ plus „Umsatz übriges Team“ anzeigen.
- **Teamleiterbonus kann Budget sprengen.** Ein höherer Anteil zusätzlich zum Teamtopf erhöht Gesamtauszahlung, außer er wird aus einem bestehenden Anteil finanziert.
  **Empfehlung:** Teamleiteranteil als Anteil innerhalb des Teamtopfs behandeln. Gesamtverteilung darf den zugelassenen Provisionsbetrag nicht überschreiten; Boni benötigen eine konfigurierte Obergrenze.
- **Nicht zugeordnete Shopbestellungen haben keine Verkäuferperson.** Automatische Zuweisung an den Teamleiter wäre nicht neutral.
  **Empfehlung:** Bestellung bleibt zunächst dem Team-Topf zugeordnet. Eine bestätigte Verteilungsregel verteilt diesen Topf; ohne qualifizierte Empfänger bleibt er unverändert im Partnerfirmenkonto bzw. wird nicht als individuelle Mitarbeiterauszahlung ausgewiesen.
- **Punkteteilung kann Leistung verschleiern.** Ein gemeinsamer Topf kann Mitarbeitenden gleiche Punkte geben, obwohl Beiträge stark abweichen; Karrierestufen werden dadurch wenig aussagekräftig.
  **Empfehlung:** Persönliche Punkte zunächst aus eigenen, qualifizierten Verkäufen bilden; Teamfortschritt separat als Summe ausweisen. Gemeinsame Punktverteilung erst nach festgelegtem Fairnessmodell.
- **Historische Aufträge brauchen alte und neue Verkäuferzuordnung.** Nur den aktuellen Verkäufer zu speichern würde die ursprüngliche Attribution überschreiben.
  **Empfehlung:** Ursprünglichen Verkäufer am Auftrag unveränderlich speichern; spätere Zuweisungen als Audit-/Änderungsereignisse führen. Regelversion und einzelne Rechenergebnisse als unveränderliche Snapshots bzw. Korrekturbuchungen speichern.
- **Karrierestufe ist nicht automatisch Provisionssatz.** Bestehende B-03 betrifft die automatische Änderung des Prozentsatzes einer Partnerfirma. Die neue Mitarbeiterkarriere könnte sonst versehentlich bestehende Geschäftswerte ändern.
  **Empfehlung:** In der ersten Spezifikation Karrierestufen getrennt vom Geld-Provisionssatz behandeln, bis der Nutzer ausdrücklich bestätigt, dass eine Stufe Geldbeträge verändert.
- **Mitarbeiterprovision kann arbeitsrechtliches Entgelt berühren.** WKO behandelt Provisionen als variables Entgelt; für Angestellte enthält § 10 AngG Regeln zu Anspruch und Abrechnung. Anwendbarkeit hängt vom tatsächlichen Beschäftigungs-/Vertragsverhältnis ab.
  **Empfehlung:** Vor echten individuellen Auszahlungen die Firma eine fachkundige Prüfung ihrer Mitarbeiter- und Partnerverträge durchführen lassen. PAPO-Spezifikation nicht als Rechtsberatung darstellen. Referenzen: [WKO – Variables Entgelt: Provision](https://www.wko.at/entlohnung/variables-entgelt-provision), [RIS – AngG § 10](https://ris.bka.gv.at/GeltendeFassung.wxe?Abfrage=Bundesnormen&Gesetzesnummer=10008069).

## Acht verbleibende Fragen an die Firma

Chris hat seine Produktrichtung beantwortet. Die folgenden Punkte erfordern
Firmenantworten; Planungszustimmung ersetzt diese nicht:

1. **Geldbasis/Systemgrenze:** Welche Bestellwertbasis gilt einschließlich Rabatten,
   Versand und Steuer? Artikelspezifische Geldsätze? Bleibt PAPO bei interner Anzeige
   oder verwaltet es individuelle Ansprüche/Abrechnungen? Empfehlung: bestehendes
   Partnerbudget und zunächst interne Zuordnung, keine Mitarbeiter-Bankauszahlung.
2. **Teamverteilung:** Welche Personen zählen, einschließlich Verkäufer, Teamleiter,
   Teilzeit, Abwesenheit und Austritt? Welcher Stichtag/Zeitraum? Gleichverteilung
   oder Gewichtung? Was passiert ohne Empfänger, mit Restcents und ungenutzter
   Reserve? Empfehlung: dokumentierte Berechtigung und gespeicherte Empfänger;
   Kontosperre nicht automatisch als Anspruchsverlust behandeln.
3. **Zahlung/Abrechnung/Korrekturen:** Welcher Zahlungseingang zählt, wer bestätigt
   welchen Nachweis? Teilzahlung, Storno, Rückgabe, Rückbuchung, Zahlung nach Austritt?
   Welche Abrechnungsperiode und Freigabeperson, wie wirken spätere Korrekturen?
   Empfehlung: separate Zahlungserfassung und referenzierte Korrekturpositionen.
4. **Regelversionen:** Wer darf je Partnerfirma konfigurieren/freigeben: Firmen-Admin
   oder Partner-Admin? Bindet der Regelstand bei Erstellung auch später bezahlte/
   erfüllte Bestellungen? Ausnahmen/Gültigkeitsbeginn? Empfehlung: unveränderliche
   verwendete Version bei Bestellung; keine rückwirkliche stille Neuberechnung.
5. **Team/Rechte:** Reicht ein Team, wie viele Teamleiter? Wer ändert Mitgliedschaft,
   Rollen und Bestellzuordnung? Welche Mitarbeiterdaten und persönlichen Geldwerte
   sehen Verkäufer/Teamleiter/Admins? Empfehlung: explizite, eng begrenzte Rechte-
   Matrix; keine automatische Regel-/Abrechnungsfreigabe für Teamleiter.
6. **Affiliate:** Gültigkeitsdauer und Bestellvorgang? Welcher Link bei mehreren?
   Austritt zwischen Aufruf und Bestellung? Wer korrigiert bis wann? Empfehlung:
   bestellbezogen ohne dauerhaftes Tracking; nach Freigabe nur Korrekturpositionen.
7. **Punkte/Karriere:** Gehören sie in Version 1, welchen eigenständigen Nutzen haben
   Punkte? Artikel-/Mengenwerte, Perioden, Schwellen, Verfall, Abstieg, Korrekturen?
   Verkäufe ohne Verkäufer? Empfehlung: persönliche Punkte nur mit fachlichem Zweck,
   keine pauschale Punkteteilung und zunächst keine Geldwirkung der Karriere.
8. **Bonus:** Perioden, Schwellen, Empfänger, Häufigkeit und Höchstbetrag? Welcher
   Anteil finanziert ihn, wann wird reserviert/freigegeben, wohin geht ungenutzte
   Reserve? Zusätzliche Firmenfinanzierung? Empfehlung: begrenzte Reserve innerhalb
   der Partnerprovision; zusätzliche Marge separat entscheiden.

## Pre-Mortem: Warnsignal und einfache Gegenmaßnahme

| Risiko | Frühes Warnsignal | Gegenmaßnahme |
|---|---|---|
| Budget überschritten | Bonus kommt zu 100 % Verkäuferanteil hinzu | Alle Anteile gegen Bestellbudget prüfen, Reserve sichtbar führen |
| Doppelte Abrechnung | Auftrag in mehreren freigegebenen Abrechnungen | Eindeutige Zuordnung; weitere Änderungen als referenzierte Korrektur |
| Empfänger rückwirkend geändert | Eintritt/Austritt verändert alte Verteilungen | Bestätigte Stichtagsregel und gespeicherte Empfängerliste |
| Unbelegte Zahlung | FULFILLED gilt automatisch als bezahlt | Getrennte autorisierte Zahlungsbestätigung mit Nachweis/Audit |
| Historie verändert | Regel-/Verkäuferänderung verändert Freigabesummen | Version/Zuordnung erhalten, Korrekturen separat |
| Fremde Affiliate-Zuordnung | Verweis auf Mitarbeiter anderer Partnerfirma | Serverseitige Organisations-/Berechtigungsprüfung |
| Zu breite Teamleiterrechte | Zugriff auf fremde Geldwerte oder eigene Freigabe | Rechte-Matrix, Organisationsgrenzen, getrennte Freigabe |
| Restbeträge verschwinden | Keine Empfänger oder ungenutzte Reserve | Unverteilt/reserviert sichtbar; Umverteilung erst nach Firmenregel |
| Unfaire Punkte | Gleiche Punkte ohne Beiträge oder künstliche Auftragsteilung | Zweck klären, persönliche Beiträge trennen, Mengenregel prüfen |
| Erfundene Altzuordnung | Gemeinsamer Login wird rückwirkend Person zugeordnet | Altdaten partnerfirmenbezogen erhalten, unbekannte Verkäufer nicht erfinden |

## Empfohlene Abhängigkeiten und nächste Schritte

1. Am 07.10.2026 mit dem Lehrer einen Firmentermin vereinbaren; danach die acht
   Firmenfragen klären. Keine erneute Befragung zu Chris' bestätigter Produktrichtung.
2. Antworten als bestätigt/offen kennzeichnen; Beispielrechnungen für Eigenverkauf, Shopverkauf ohne Affiliate, Teamleiteranteil, Zahlungskorrektur und Regeländerung erstellen.
3. Persönliche Mitarbeiterkonten benötigen zuerst stabile OIDC-Identität, sichere Organisations-/Partnerzuordnung und Audit. Mehrmandantenfähigkeit bleibt davor relevant, wenn Einstellungen wirklich je PAPO-Kundenunternehmen getrennt sein müssen.
4. Zahlungsstatus und manuelle Zahlungsbestätigung spezifizieren, bevor „verdiente“ Provision oder Bonusstand berechnet wird. Keine echte Auszahlung in diesen ersten Scope ziehen, sofern nicht separat entschieden.
5. Teamprovision und interne Attribution als eigenen OpenSpec-Change planen. Shop-Affiliate-Zuordnung hängt von B-01 ab oder wird als eigener späterer Shop-Teilchange ergänzt.
6. B-03 bleibt separat: automatische Änderung des Provisionssatzes einer Partnerfirma. B-09 beschreibt Mitarbeiter-Teamverteilung, Punkte, Karrierestufen und Team-Boni.
7. Nach akzeptiertem OpenSpec-Plan die Implementierung top-down gemäß Abhängigkeiten starten; keine offenen Geschäftsregeln stillschweigend als Defaults kodieren.

## Historische Erfassungsnachweise

Diese Nachweise betreffen die ursprüngliche Erkundung, nicht den aktuellen
Git-/Veröffentlichungsstand. Aktuelle Prüfung und nächste Arbeit stehen nur
in [CONTINUATION_PROMPT.md](CONTINUATION_PROMPT.md).

- Branch vor dieser Dokumentationsarbeit: main, Commit 25ce0ee (origin/main synchron).
- Vorbestehende lokale Änderungen des Nutzers betrafen Graphify (AGENTS.md, .gitignore, .codex/skills/graphify/, .graphifyignore) und Documentation/KI-Nachweise/prompt-register.md; diese sind zu erhalten.
- Für diese Dokumentationsarbeit wurde Branch docs/team-commission-discovery-context erstellt.
- Aktive OpenSpec-Changes laut Prüfung: complete-thesis-documentation 4/6, decouple-partner-number-from-login 0/6, add-unique-bmd-article-number 0/6. Beim Sessionstart neu prüfen.
- Neuester geprüfter Main-Workflow zum Zeitpunkt der Erkundung: Run 36433087198, SHA 25ce0ee, erfolgreich, 28.09.2026. Beim Sessionstart neu prüfen.
- Prüfung am 06.10.2026 vor Veröffentlichung: Branch docs/team-commission-discovery-context,
  HEAD 25ce0ee; aktive Tasks weiter 4/6, 0/6, 0/6; neuester Main-Workflow weiterhin
  36433087198 erfolgreich. Das ist keine neue Live-/Browserabnahme.
- Chris autorisiert am 06.10.2026 Commit/Push/Merge des geprüften Dokumentationsstands.
  Firmenfreigaben und Implementierungsauftrag entstehen dadurch nicht.
