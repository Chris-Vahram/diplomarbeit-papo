# Besprechungsprotokoll vom 31.08.2026

## Anlass und Ziel

In der Betreuungsbesprechung wurde die zukünftige Struktur von PAPO gegenüber dem tatsächlich erforderlichen MVP abgegrenzt. Außerdem wurden das Zugangsmodell für Partnerfirmen, die Artikelterminologie, die Rolle von BMD, die Zeiterfassung und der Umgang mit KI-Nachweisen besprochen.

Der Name des Referenzunternehmens wird in der Projektdokumentation nicht ausgeschrieben. Es dient als erster fachlicher Testfall, begrenzt PAPO aber nicht auf ein einzelnes Unternehmen.

## Zentrales Ergebnis

Das Ziel des MVP bleibt eindeutig:

> Bestellungen sollen nicht länger per E-Mail oder Telefon entgegengenommen werden, sondern strukturiert über das Partnerportal erfolgen.

Die erste Version ist daher **kein öffentlicher Endkunden-Partnershop**. Im MVP meldet sich die Partnerfirma im internen Portal an, sieht die für sie freigeschalteten Artikel, wählt Artikel und Mengen aus und gibt die Bestellung über PAPO auf. Der bereits umgesetzte partnergetriebene Bestellprozess entspricht damit weiterhin der richtigen fachlichen Richtung.

Die aktuelle Demo verwendet zwar nur ein Unternehmen, die Diplomarbeit soll aber nicht als dauerhaftes „Ein-Unternehmen-MVP“ verstanden werden. Das Ergebnis soll ein funktionsfähiges und weiter ausbaufähiges Produkt sein. Die Beschränkung auf ein Referenzunternehmen beschreibt den gegenwärtigen Implementierungsstand, nicht die endgültige Produktgrenze.

## Langfristige Plattformstruktur

PAPO soll bei mehreren angeschlossenen Unternehmen mandantenfähig aufgebaut werden. Jedes Unternehmen erhält einen getrennten Bereich, perspektivisch beispielsweise unter:

```text
firma-x.paportal.at
```

Das erste Referenzunternehmen hat rund 100 Partner. Langfristig soll jeder dieser Partner einen eigenen Partnershop erhalten können. Diese öffentlichen Shops gehören jedoch nicht zur ersten MVP-Version. Zuerst wird der interne Bestellweg zwischen Partner und Unternehmen digitalisiert.

Damit sind zwei Ausbaustufen klar zu unterscheiden:

1. **MVP:** Partnerfirma meldet sich intern an und erfasst Bestellungen über das Portal.
2. **Spätere Ausbaustufe:** Endkunden bestellen über einen individuellen öffentlichen Shop ihres Partners.

Die Mandantenfähigkeit ist als richtiges strukturelles Ziel bestätigt, sobald ein zweites Unternehmen aufgenommen wird. Sie ist ausdrücklich **nicht der nächste Implementierungsschritt**. Noch nicht festgelegt wurde, bis wann beziehungsweise in welchem Umfang sie innerhalb der Diplomarbeit umgesetzt werden soll.

## Partnerzugänge

Für das MVP erhält jede Partnerfirma genau **einen gemeinsamen Partnerzugang**. Der Partner ist im B2B-Modell selbst ein Unternehmen; mehrere Mitarbeiter dieser Partnerfirma arbeiten daher zunächst mit demselben organisatorischen Zugang.

Persönliche Benutzerkonten je Mitarbeiter sind kein Bestandteil des MVP. Sie können später ergänzt werden, falls individuelle Sperren, personenbezogene Nachvollziehbarkeit oder unterschiedliche Berechtigungen tatsächlich benötigt werden.

Noch festzulegen sind nur die operativen Details des gemeinsamen Zugangs, insbesondere die verwendete zentrale E-Mail-Adresse, der Passwort-Zurücksetzen-Prozess und die Übergabe des Zugangs bei einer Änderung der zuständigen Mitarbeiter.

## Artikel statt Produkte

Das Referenzunternehmen unterscheidet fachlich derzeit nicht zwischen „Artikel“ und „Produkt“. In der Benutzeroberfläche und in der fachlichen Dokumentation soll deshalb einheitlich der Begriff **Artikel** verwendet werden.

Eine interne Umbenennung aller technischen Klassen, Tabellen und API-Felder ist daraus nicht automatisch erforderlich. Entscheidend ist zunächst eine konsistente fachliche Sprache für die Benutzer.

### Erkenntnisse aus der Firmenmail zu den Artikelfeldern

Die in der Besprechung gezeigte Firmenmail wurde nachträglich ausgewertet. Sie beschreibt bewusst auch spätere Entwicklungsmöglichkeiten. Laut Rückmeldung des Betreuers ist dieser Gesamtumfang für den aktuellen Stand zu groß; eine vereinfachte Folgemail des Unternehmens ist angekündigt. Die folgenden Punkte sind daher als fachliche Richtung zu verstehen und nicht vollständig als sofort umzusetzende Anforderung.

#### Artikelnummer und technische ID

Die **Artikelnummer** ist die eindeutige fachliche Kennung, über die das Unternehmen seine Artikel in BMD erkennt und verwaltet. Eine zusätzliche fachliche „Artikel-ID“ wurde nur vorsorglich angenommen und darf entfallen, wenn PAPO intern bereits eine technische Datenbank-ID verwendet. Für PAPO ergibt sich daher voraussichtlich:

- interne technische Datenbank-ID für Beziehungen innerhalb der Anwendung;
- eindeutige, aus BMD stammende Artikelnummer als sichtbare fachliche Kennung;
- keine zweite sichtbare Artikel-ID ohne nachgewiesenen Bedarf.

#### Artikel und übergeordnetes Produkt

Aktuell benötigt das Unternehmen keine eigene Produktebene oberhalb der Artikel. Der Artikel ist die relevante Einheit. Eine spätere Produktebene könnte Varianten mehrerer Artikel zusammenfassen, wird für den derzeitigen Umfang aber nicht benötigt.

Die Mail nennt außerdem beispielhaft zusammengesetzte Artikel bzw. Pakete, bei denen ein Artikel aus mehreren Stück anderer Artikel besteht. Eine solche Stücklisten- oder Paketlogik ist eine mögliche spätere Erweiterung und keine aktuelle MVP-Anforderung.

#### Preislistenbereich

Der Preislistenbereich dient dem Unternehmen nur zur Orientierung in bestehenden Preislisten. Für die weitere Verarbeitung in PAPO wird er nicht benötigt und kann weggelassen werden.

#### Artikelkategorie

Die in der Mail als „Produktkategorie“ bezeichnete Kategorie ordnet einen Artikel beispielsweise Bereichen wie Vitalstoff, Physiotron, Seminare, Drucksorten oder Leistungen zu. Sie wird perspektivisch für die kaufmännische Behandlung sowie unterschiedliche Provisions- und Rabattregeln verwendet.

Für den aktuellen Stand genügt es, die Kategorie als eigenes fachliches Merkmal vorzusehen. Neue Provisions- oder Rabattlogik entsteht daraus noch nicht automatisch.

#### Fachbereich

Der Fachbereich beschreibt eine fachliche Zuordnung wie Magnetfeld, Radionik, Schlafplatz oder Vitalstoffe. Er kann später für Schulungs- und Einschulungsberechtigungen verwendet werden. Diese Berechtigungslogik gehört nicht zum aktuellen MVP.

#### Produktgruppe A/B/C/D

Die Produktgruppe ist ein drittes, von Kategorie und Fachbereich getrenntes Merkmal. Sie wird im Unternehmen für eine spätere Margen- bzw. PP-Logik verwendet, auf deren Basis wiederum Provisionen berechnet werden können. Diese Logik unterscheidet sich vom derzeitigen einfachen Provisionsprozentsatz je Partner und ist nicht als aktueller Implementierungsauftrag bestätigt.

Kategorie, Fachbereich und Produktgruppe dürfen nicht zu einem einzigen Feld vermischt werden, falls sie später übernommen werden. Ob Fachbereich und Produktgruppe bereits jetzt als einfache Stammdatenfelder vorbereitet werden sollen, wird erst anhand der vereinfachten Folgemail festgelegt.

Die zugrunde liegenden Bilder werden wegen sichtbarer Namen und E-Mail-Adressen nicht in das öffentliche Repository übernommen. Ihre fachlichen Aussagen sind in diesem Abschnitt zusammengefasst.

## Rolle von BMD

BMD soll das **führende System für Artikelstammdaten** bleiben. PAPO verwendet diese Daten für Freischaltung, Darstellung und Bestellung, soll aber keine konkurrierende unabhängige Artikelstammdatenquelle werden.

Aus dieser Aussage folgt noch keine beschlossene direkte BMD-Schnittstelle. Bis eine andere Übertragungsart ausdrücklich gefordert wird, bleibt der bereits vorgesehene Excel-/CSV-Import der kleinste passende Datenaustausch. Noch zu klären sind Exportformat, Spalten, Aktualisierungsrhythmus, Dublettenregeln und der Umgang mit in BMD geänderten oder deaktivierten Artikeln.

## Projektorganisation und Aufwand

Für Chris und Tobias zusammen sind rund **400 in Clockify dokumentierte Arbeitsstunden** das Ziel. Die Zeiterfassung soll laufend, nachvollziehbar und nach sinnvollen Arbeitspaketen erfolgen. Präsentation, Dokumentation, Besprechungen, Planung, Tests, Deployment und Implementierung gehören dabei ebenso zur Diplomarbeit wie das eigentliche Schreiben von Code.

Eine mögliche Verbesserung der GitHub-Spezifikation wurde nur als unverbindliche Idee erwähnt. Ohne konkretes Ziel oder festgestellten Mangel entsteht daraus noch keine eigene Aufgabe.

## KI-Dokumentation

Die KI-Nutzung soll nachvollziehbar und sinnvoll dokumentiert werden. Übernommene Inhalte sind zulässig, müssen aber korrekt als KI-Unterstützung gekennzeichnet bzw. entsprechend den schulischen Vorgaben zitiert werden. Ungekennzeichnetes Übernehmen wäre ein Plagiatsproblem.

Das bereits geplante zweistufige Vorgehen bleibt passend:

1. kuratiertes KI-Nachweisregister mit Zweck, Promptbeleg, verwendetem Ergebnis, eigener Prüfung und zugehörigem Arbeitsergebnis;
2. vollständiges privates Roharchiv als Beleg, aus dem repräsentative Dialoge für die Diplomarbeit ausgewählt werden.

Nicht erforderlich ist ein unkommentierter Abdruck jedes vollständigen Chats. Entscheidend sind Nachvollziehbarkeit, korrekte Kennzeichnung und die Darstellung der eigenen Prüfung und Weiterverarbeitung. Die genaue Zitierform und Anzahl vollständiger Beispieldialoge bleibt vor der Endfassung der Diplomarbeit festzulegen.

## Entscheidungsstand nach der Besprechung

| Thema | Stand | Folge |
|---|---|---|
| Ziel des MVP | geklärt | Interne Partnerbestellungen ersetzen E-Mail und Telefon |
| Öffentlicher Partnershop | geklärt | Spätere Ausbaustufe, nicht der nächste MVP-Schritt |
| Mehrere Unternehmen | Zielbild geklärt | Gemeinsame mandantenfähige Plattform mit getrennten Unternehmensbereichen |
| Nächster Schritt Mandantenfähigkeit | geklärt | Jetzt nicht umsetzen; erst erforderlich, sobald ein zweites Unternehmen aufgenommen wird |
| Umfang der Mandantenfähigkeit in der Diplomarbeit | teilweise geklärt | Technischer Umfang und Fertigstellungstermin noch festlegen |
| Unternehmenssubdomains | Zielbild geklärt | Perspektivisch `firma-x.paportal.at`; noch keine sofortige Implementierungsfreigabe |
| Partnerzugang | für MVP geklärt | Ein gemeinsamer organisatorischer Zugang je Partnerfirma |
| Persönliche Partnermitarbeiterkonten | geklärt | Nicht im MVP |
| Zugänge für Mitarbeiter des betreibenden Unternehmens | offen | Aus der Aussage zum Partnerzugang nicht ableitbar |
| Fachbegriff | geklärt | Benutzerseitig und fachlich einheitlich „Artikel“ verwenden |
| Artikelnummer | grundsätzlich geklärt | Eindeutige fachliche Kennung aus BMD; genaue Importregeln folgen mit Echtdaten |
| Zusätzliche Artikel-ID | geklärt | Nicht benötigt; PAPO verwendet intern seine technische Datenbank-ID |
| Preislistenbereich | geklärt | Für PAPO nicht erforderlich und weglassen |
| Artikelkategorie | Richtung geklärt | Eigenes Merkmal; spätere kaufmännische Regeln, aber noch keine neue Provisionslogik |
| Fachbereich | nur Zukunftsrichtung | Später für Schulungs-/Einschulungslogik; aktueller Bedarf folgt aus der vereinfachten Firmenmail |
| Produktgruppe A/B/C/D | nur Zukunftsrichtung | Getrenntes Merkmal für spätere PP-/Margenlogik, derzeit kein Implementierungsauftrag |
| Artikelpakete/Stücklisten | nur Zukunftsrichtung | Zusammengesetzte Artikel später möglich, nicht Teil des aktuellen MVP |
| BMD | teilweise geklärt | Führendes Artikelstammsystem; Übertragungsweg noch nicht festgelegt |
| Clockify | geklärt | Rund 400 Stunden für das gesamte Zweierteam dokumentieren |
| KI-Dokumentation | grundsätzlich geklärt | Nachvollziehbar dokumentieren und korrekt zitieren; endgültige Anhangsform noch offen |
| GitHub-Spezifikation | nicht beschlossen | Nur bei einem konkreten Verbesserungsbedarf weiterverfolgen |

## Auswirkungen auf den aktuellen Projektumfang

Die Besprechung bestätigt den bestehenden Kernprozess und ändert ihn nicht grundlegend. Für die weitere Planung sind vor allem folgende Punkte zu präzisieren:

- durchgängige fachliche Umstellung der sichtbaren Bezeichnung von „Produkt“ auf „Artikel“;
- eindeutige Artikelnummer aus dem führenden BMD-System;
- Artikelkategorie als eigenes fachliches Merkmal;
- Fachbereich und Produktgruppe nur dann vorbereiten, wenn die vereinfachte Firmenrückmeldung dies für den aktuellen Schritt bestätigt;
- klare Abbildung von BMD als führender Quelle;
- gemeinsamer Zugang je Partnerfirma als ausdrückliche MVP-Regel;
- Mandantenfähigkeit als zukünftiges Architekturziel, aber nicht als nächster Change.

Nicht neu in den MVP aufgenommen wurden:

- der öffentliche Endkunden-Partnershop;
- persönliche Konten für Mitarbeiter einer Partnerfirma;
- eine direkte BMD-Schnittstelle;
- öffentliche Unternehmens-Selbstregistrierung;
- Artikelpakete/Stücklisten;
- Schulungs- und Einschulungsberechtigungen;
- PP-, Margen- oder kategoriebasierte Provisionslogik.

## Empfohlene nächste Schritte

1. Die angekündigte vereinfachte Folgemail des Unternehmens abwarten und nur deren bestätigten Minimalumfang als nächsten Artikel-Change verwenden.
2. Dieses Protokoll als neueste Besprechungsquelle in `CLAUDE.md` und `Specification/CONTINUATION_PROMPT.md` referenzieren.
3. FSD, Phase-2-Backlog und Roadmap synchronisieren:
   - „Artikel“ als fachlichen UI-Begriff festlegen;
   - BMD als führendes System dokumentieren, ohne eine direkte Schnittstelle vorwegzunehmen;
   - eindeutige Artikelnummer statt zusätzlicher fachlicher Artikel-ID vorsehen;
   - Preislistenbereich ausdrücklich weglassen;
   - Fachbereich, Produktgruppe, Paketlogik und davon abhängige Regeln als mögliche spätere Erweiterungen dokumentieren;
   - gemeinsamen Partnerzugang als MVP-Entscheidung markieren;
   - Partnershop weiterhin in Phase 2 belassen;
   - Mandanten-Zielbild bestätigen, es aber aus der unmittelbaren Umsetzungsreihenfolge herausnehmen;
   - die Diplomarbeit als funktionsfähiges, erweiterbares Produkt und nicht als dauerhaftes Ein-Unternehmen-System beschreiben.
4. Den aktuellen ungesicherten Dokumentationsstand prüfen, `tmp/` vom Commit fernhalten und die bestehenden lokalen Commits sowie den Dokumentations-Commit veröffentlichen.
5. Den offenen manuellen Smoke-Test des Bestellablaufs und die bestehende OpenSpec-Hygiene abschließen.
6. Den bereits festgelegten nächsten Implementierungsschritt nicht durch die umfangreiche Firmenmail ersetzen. Erst nach der vereinfachten Folgemail entscheiden, ob daraus ein kleiner eigener Artikelstammdaten-Change entsteht.
7. Mandantenfähigkeit jetzt nicht beginnen. Beim Onboarding eines zweiten Unternehmens wird dafür ein eigener Architektur- und Sicherheitschange benötigt; der genaue Zeitpunkt innerhalb der Diplomarbeit bleibt offen.
8. Clockify ab sofort konsequent führen und bisherige belegbare Tätigkeiten anhand von Commits, Besprechungen und Dokumenten nachtragen.
9. Die bereits geplante Automatisierung der KI-Nachweise umsetzen, damit kommende Arbeiten ohne manuelle Promptpflege erfasst werden.
