# Besprechungsprotokoll – 25.08.2026

> **Nachtrag:** Die nach der Besprechung von Chris getroffenen Produktentscheidungen und die noch mit Prof. Hammer abzugrenzende Mehr-Unternehmen-Architektur sind am Ende dieses Protokolls dokumentiert. Sie konkretisieren bzw. ersetzen die dort ausdrücklich genannten offenen Punkte. Die vollständige Umsetzungsreihenfolge steht in `Specification/ROADMAP_2026-08-25.md`.

## Rahmendaten

- **Datum:** 25.08.2026
- **Teilnehmende:** Prof. Hammer, Chris Vahram, Tobias Huemer
- **Firma:** nicht anwesend
- **Nächster Termin:** Sonntag, 06.09.2026, 19:00 Uhr
- **Themen:** Produktausrichtung, Präsentation, Company- und Partner-Sicht, Stammdaten, Deployment und Diplomarbeitsdokumentation

## Kurzfazit

PAPO soll als **firmenunabhängige Plattform für partnerbasierte Vertriebsmodelle** entwickelt werden. Die bisher beteiligte Firma ist Referenz- und Testfall, aber nicht der einzige mögliche Kunde und nicht die fachliche Grenze des Produkts. Fehlende oder ungeeignete Firmendaten blockieren die weitere Arbeit daher nicht: Für Entwicklung und Präsentation werden eigenständige, datenschutzkonforme Testdaten verwendet.

Der Kern bleibt bewusst einfach:

> Ein Partner führt für einen Endkunden eine Bestellung durch.

PAPO soll manuelle Bestellungen per E-Mail oder Telefon durch einen zentralen, nachvollziehbaren Ablauf ersetzen. Als möglicher Präsentations-Kernsatz wurde festgehalten:

> PAPO soll zukünftig die zentrale Drehscheibe für den partnerbasierten Vertrieb eines Unternehmens bilden.

Die Vision eines individuellen Partnershops je Interessent bzw. Partner bleibt vorerst eine **Produktvision**. Sie ist noch keine automatisch beschlossene MVP-Anforderung. Vor einer Umsetzung muss geklärt werden, ob damit eine eigene Installation pro Unternehmen, echtes Multi-Tenancy oder ein Shop pro Vertriebspartner gemeint ist.

## Beschlüsse und Leitlinien

1. PAPO wird allgemein und nicht auf ein einzelnes Unternehmen zugeschnitten.
2. Die bisherige Firma dient als Referenz- und Testkunde.
3. Eigene realistische Testdaten ersetzen ungeeignete oder datenschutzkritische Firmendaten.
4. Der Kernprozess und die wesentlichen Ziele haben Vorrang vor zusätzlichen Ideen.
5. Bestehende Funktionen sollen zuerst vollständig abgerundet werden.
6. Die Abschlusspräsentation soll ab jetzt laufend mitentwickelt werden.
7. Reveal.js ist für die Diplomarbeitspräsentation geeignet.
8. Eine mögliche KI-Funktion wird erst nach Fertigstellung der Kernfunktionen bewertet.
9. Falls die Plattform für den Referenzkunden brauchbar ist, wurde eine finanzielle Anerkennung in Aussicht gestellt. Dies ist keine technische Anforderung und keine garantierte Vergütung.

## Präsentationsfeedback

### Folie 1 – Titel

- Die Folie passt in der aktuellen Form.

### Folie 2 – Problem und Referenzkunde

- Mit dem allgemeinen Problem beginnen: Partnerbestellungen laufen häufig per E-Mail oder Telefon.
- Den konkreten Test- bzw. MVP-Kunden und dessen Kennzahlen erst später als Referenzfall vorstellen.
- Den Nutzen klarer herausarbeiten: zentrale Verwaltung, weniger manuelle Arbeit und nachvollziehbare Zuordnung von Bestellung, Partner und Provision.
- Eine eigene **USP-/Problemfolie** ist sinnvoller als eine zu frühe Liste kundenspezifischer Zahlen.

### Folie 3 – Struktur und Akteure

#### Unternehmen

- verwaltet Partner und Produkte zentral;
- importiert Excel-/CSV-Daten;
- legt fest, welche Produkte je Partner verfügbar sind;
- sieht und bearbeitet alle Bestellungen;
- benötigt eine verständliche Übersicht über das eigene Partnergeschäft.

#### Partner

- sieht nur die für ihn freigeschalteten Produkte;
- führt Bestellungen für Endkunden durch;
- sieht eigene Bestellungen und Provisionen im Dashboard;
- bearbeitet eigene Stammdaten;
- verwaltet die ihm zugeordneten Endkunden.

#### Endkunde

- nutzt das Portal im aktuellen MVP nicht selbst;
- wird vom Partner angelegt;
- Bestellungen werden durch den Partner durchgeführt;
- erhält die Ware;
- erhält im MVP eine simulierte Bestätigung.

Die Beziehung muss korrekt formuliert werden:

> Ein Unternehmen verwaltet mehrere Partner. Ein Partner betreut mehrere Endkunden.

Die Aussagen „Produkte werden für alle Partner freigeschaltet“ und „Produkte werden je Partner freigeschaltet“ widersprechen einander. Der derzeitige und fachlich präzisere Stand lautet: **Produkte werden pro Partner freigeschaltet.**

### Folie 4 – Kernprozess

Der Bestellablauf soll klar und ohne technische Nebendetails dargestellt werden:

1. Endkunde auswählen oder anlegen.
2. Einen oder mehrere Artikel auswählen und Mengen festlegen.
3. Bestellung und Gesamtpreis kontrollieren.
4. Provision berechnen und anzeigen.
5. Bestellung aufgeben und Bestätigung auslösen.

Ob dabei künftig eine echte E-Mail gesendet wird, ist noch abzugrenzen. Im aktuellen MVP ist der Versand simuliert; realer E-Mail-Versand ist bisher Phase 2.

### Technologie-Stack

- Überschrift von „Fixer Technologie-Stack“ auf **„Technologie-Stack“** ändern.
- Stack verständlicher erklären und visuell stärker präsentieren.
- Bezug zum Unterricht herstellen, ohne die Folie mit Implementierungsdetails zu überladen.

### Abschlusspräsentation und Project Award

- Die Präsentation soll Produkt und Nutzen genauso stark vermitteln wie die technische Umsetzung.
- Gewinner-Videos früherer Project Awards analysieren: Aufbau, Verständlichkeit, Story, Demo und visuelle Qualität.
- Richtwert aus der Besprechung: ungefähr **50 % Produkt und 50 % Präsentation**.

## Fachliches Feedback zur Company-Sicht

### Partner-Stammdaten

Folgende Ergänzungen wurden genannt:

- Partnernummer automatisch vergeben und nach dem Anlegen deutlich anzeigen;
- internes Anmerkungsfeld für das Unternehmen;
- Firmenname und Ansprechpartner getrennt erfassen;
- UID-Nummer erfassen;
- Gewerbeberechtigung bzw. Gewerbeschein hochladen;
- Provisionsstufen 5 %, 10 %, 15 % bzw. individueller Wert anbieten;
- neue Partner standardmäßig mit 5 % Provision anlegen.

Vor der Umsetzung sind insbesondere Pflichtfelder, Dateitypen, maximale Dateigröße, Zugriffsrechte und Aufbewahrung des Gewerbescheins festzulegen. Der Upload enthält potenziell personenbezogene oder geschäftlich sensible Daten und benötigt daher eine bewusste Datenschutz- und Sicherheitsentscheidung.

### Produkte bzw. Artikel

Die aktuelle Begrifflichkeit ist nicht ausreichend konsistent:

- „Artikel-ID“ und „Artikelnummer“ müssen verständlich unterschieden werden. Die technische Datenbank-ID sollte normalerweise nicht als fachliches Eingabefeld erscheinen.
- Einheitlich festlegen, ob in der Oberfläche **Produkt** oder **Artikel** verwendet wird.
- Unterschied zwischen **Kategorie** und **Fachbereich** definieren oder nur einen der beiden Begriffe verwenden.
- Kategorien sollen auswählbar, filterbar und durch das Unternehmen verwaltbar sein.
- Vergleichbare Partnerportale sollen hinsichtlich ihrer Begriffe untersucht werden.

Die endgültige Taxonomie kann sich noch ändern. Daher zuerst Begriffe und fachliche Bedeutung entscheiden, danach Datenmodell und Oberfläche ändern.

### Bestellungen und Company-Dashboard

- Die Bestellübersicht wurde für eine erste Version positiv bewertet.
- Feinheiten und ein vollständig stabiler Ablauf sind wichtiger als weitere Zusatzfunktionen.
- „Ich will mein Unternehmen besser sehen“ ist noch keine umsetzbare Anforderung. Dafür müssen konkrete Kennzahlen, Filter und Entscheidungen definiert werden, die das Dashboard unterstützen soll.

## Fachliches Feedback zur Partner-Sicht

- Bestellungen: grundsätzlich gut.
- Produkte: grundsätzlich gut.
- Endkunden sollen auch unabhängig von einer Bestellung in der Endkundenverwaltung angelegt werden können.
- Die Verwaltung eigener Stammdaten und zugeordneter Endkunden soll klar erkennbar sein.
- Ein späterer individueller Partnershop gehört zur Produktvision und muss gegenüber dem internen MVP abgegrenzt bleiben.

## Datenstrategie

- Es werden eigene, realistische und datenschutzkonforme Testdaten erzeugt.
- Die bisher übermittelten Firmendaten sind für eine direkte Verwendung nicht ausreichend geeignet.
- Neue Artikeldaten sollen nachgereicht werden, sobald sie beim Ansprechpartner verfügbar sind.
- Firmendaten dürfen bei Präsentationen nicht ungeprüft verwendet werden.
- Import und Spalten-Mapping werden mit generischen Testdaten weiterentwickelt; kundenspezifische Mappings können später ergänzt werden.

## Domain, Hosting und E-Mail

Aus den Nachrichten von Christian Bachl vom 25.08.2026 ergibt sich:

- **`paportal.at` wurde eingerichtet.** DNS- und Erreichbarkeit müssen noch überprüft werden, da die Aktivierung etwas Zeit benötigen kann.
- E-Mail-Postfächer für ausgehende Benachrichtigungen können bei Bedarf eingerichtet werden.
- Bei Bedarf kann ein S3-kompatibler Speicher-Bucket bereitgestellt werden.

### Einordnung

- Die bisher offene Domainentscheidung ist damit grundsätzlich erledigt.
- Das Deployment ist noch nicht automatisch abgeschlossen. Offen bleiben DNS-Prüfung, Deployment-Schlüssel und Secrets, GHCR-Anmeldung auf der VM, Übertragung der Produktionsdateien sowie HTTPS- und Login-End-to-End-Test.
- Ein angebotenes E-Mail-Postfach erweitert nicht automatisch den MVP um echten Mailversand. Dafür ist eine bewusste Scope-Entscheidung erforderlich.
- Der angebotene S3-Speicher ist eine mögliche Grundlage für Offsite-Backups oder Dokument-Uploads. Ein Bucket wurde nur angeboten, nicht als bereits eingerichtet bestätigt.

## Diplomarbeits- und KI-Dokumentation

- Verwendete KI-Prompts nachvollziehbar aufheben.
- Für die Diplomarbeit repräsentative Prompts, Ergebnisse, Korrekturen und die eigene Überprüfung dokumentieren.
- Screenshots gezielt als Belege verwenden, nicht als bloße Seitenfüller.
- Quellen, übernommene Inhalte und KI-Unterstützung korrekt nach den schulischen Vorgaben kennzeichnen.
- Keine Geheimnisse, Zugangsdaten, personenbezogenen Firmendaten oder vertraulichen Inhalte in Prompt-Beispiele übernehmen.
- Einen priorisierten Backlog führen.

Eine reine Sammlung mehrerer Seiten mit Roh-Prompts wäre wenig aussagekräftig. Für die Bewertung ist hilfreicher zu zeigen, **wofür** KI verwendet wurde, **welches Ergebnis** entstand, **wie es geprüft wurde** und **welche Eigenleistung** danach notwendig war.

## Analyse: Auswirkungen auf den bisherigen Scope

| Thema | Einordnung | Konsequenz |
|---|---|---|
| Firmenunabhängige Plattform | strategische Produktausrichtung | Generische Begriffe und Testdaten verwenden; kein kundenspezifisches Datenmodell bauen |
| Individueller Shop | Phase-2-/Produktvision | Multi-Tenancy und Shopmodell zuerst klären; nicht still in den MVP ziehen |
| Eigene Testdaten | beschlossen | Referenzdaten generieren und Präsentation damit durchführen |
| Partner-Stammdaten | neue Anforderung | Als eigenen OpenSpec-Change spezifizieren und priorisieren |
| Gewerbeschein-Upload | neue, sensible Anforderung | Datenschutz, Berechtigungen und Speicher festlegen, bevor implementiert wird |
| Produktbegriffe und Kategorien | fachliche Klärung | Terminologie entscheiden, danach Oberfläche und Import angleichen |
| Endkunden unabhängig anlegen | sinnvolle MVP-Ergänzung | Als kleiner eigener OpenSpec-Change planen |
| Echter E-Mail-Versand | bisher Phase 2 | Nur nach ausdrücklicher Scope-Entscheidung umsetzen |
| KI-Funktion | optionale spätere Idee | Erst nach stabiler Kernfunktion und konkretem Nutzenfall bewerten |
| `paportal.at` | Domain eingerichtet | DNS prüfen und Deployment-Voraussetzungen fertigstellen |
| S3-Bucket | angeboten | Erst bei konkretem Backup- oder Uploadbedarf anfordern |

## Offene Entscheidungen

1. Bedeutet „einzigartiger Shop“ eine eigene Installation pro Unternehmen, echtes Multi-Tenancy oder einen Shop pro Partner?
2. Bleibt der Partnershop Phase 2 oder ändert sich der offizielle Diplomarbeitsumfang?
3. Welche Partner-Stammdaten sind Pflichtfelder und welche nur intern sichtbar?
4. Welche Anforderungen gelten für Gewerbeschein-Uploads und deren Aufbewahrung?
5. Wird fachlich einheitlich von „Produkten“ oder „Artikeln“ gesprochen?
6. Reicht eine Kategorie oder wird zusätzlich ein klar definierter Fachbereich benötigt?
7. Welche Kennzahlen benötigt das Unternehmen tatsächlich im Dashboard?
8. Soll echter E-Mail-Versand jetzt aktiviert werden oder weiterhin Phase 2 bleiben?
9. Welcher konkrete, prüfbare Nutzen würde eine KI-Funktion rechtfertigen?

## Priorisierter Plan bis zum 06.09.2026

### Priorität 1 – Bestehendes vorführbar abschließen

- Reveal-Präsentation anhand des Feedbacks überarbeiten.
- Allgemeines Problem und USP vor dem Referenzkunden erklären.
- Akteursfolie fachlich korrigieren und Kernprozess vereinfachen.
- Technologie-Folie visuell und sprachlich verbessern.
- Eigenständige Demo-Daten verwenden.
- Vorhandene Company-, Partner- und Bestellabläufe vollständig durchtesten und sichtbare Feinheiten abrunden.

### Priorität 2 – Deployment abschließen

- DNS für `paportal.at` prüfen und auf die Hetzner-VM zeigen lassen.
- Noch offene VM- und GitHub-Actions-Voraussetzungen erledigen.
- HTTPS, Keycloak-Login, Datenpersistenz und Deployment-End-to-End testen.
- Vor öffentlichem Betrieb Demo-Zugangsdaten aus der Login-Oberfläche entfernen oder auf Entwicklung beschränken.

### Priorität 3 – Neue Anforderungen spezifizieren

- OpenSpec-Change für erweiterte Partner-Stammdaten vorbereiten.
- Separaten kleinen Change für das unabhängige Anlegen von Endkunden planen.
- Produkt-/Artikelterminologie und Kategorie-Modell entscheiden, bevor Implementierung beginnt.
- Gewerbeschein-Upload erst nach Datenschutz- und Speicherentscheidung planen.

### Priorität 4 – Diplomarbeit und Abschlusspräsentation

- Präsentationsstruktur kontinuierlich weiterentwickeln.
- Anforderungen, Implementierung und Tests nachvollziehbar verknüpfen.
- Repräsentative KI-Prompts samt Ergebnis, Prüfung und Eigenleistung dokumentieren.
- Project-Award-Präsentationen analysieren und übertragbare Muster notieren.

### Bewusst später

- Multi-Tenant-Plattform bzw. individueller Partnershop;
- echter E-Mail-Versand, solange er nicht ausdrücklich in den MVP aufgenommen wird;
- KI-Funktion ohne konkreten und messbaren Anwendungsfall;
- weitere Zusatzfunktionen, bevor Kernprozess und Deployment stabil sind.

## Ziel für den nächsten Termin

Bis 06.09.2026 soll PAPO als generische Plattformidee verständlich präsentiert werden, der bestehende Kernprozess stabil vorführbar sein und `paportal.at` möglichst als funktionierende Deployment-Adresse bereitstehen. Für neue Stammdaten-, Shop- und KI-Ideen sollen klare Entscheidungen oder abgegrenzte OpenSpec-Vorschläge vorliegen, aber keine vorschnell erweiterten Implementierungen.

## Nachträgliche Entscheidungen und Konkretisierungen von Chris

Diese Punkte wurden nach der Besprechung festgelegt:

- PAPO soll langfristig mehrere Unternehmen in einer gemeinsamen, sicher getrennten Plattform bedienen. Eine eigene Installation je Unternehmen ist nicht das bevorzugte Zielbild.
- Unternehmen sollen perspektivisch eine Subdomain wie `firma.paportal.at` erhalten. Öffentliche Selbstregistrierung ist das Produktziel, ihr Umfang innerhalb der Diplomarbeit ist aber noch mit Prof. Hammer zu bestätigen.
- Keycloak Organizations wird als passendes Ziel für die Unternehmenszuordnung untersucht. Vor der Umsetzung sind ein Versions-/Integrationsspike und eine eigene OpenSpec-Spezifikation erforderlich.
- Die sichtbare Partnernummer wird automatisch als `P-0001` usw. vergeben und ist nur innerhalb des jeweiligen Unternehmens eindeutig. Eine globale technische UUID verhindert Verwechslungen.
- Eine importierte bestehende Partnernummer wird als externe Referenz gespeichert.
- Die Partnernummer soll im Zielbild nicht die globale technische Identität sein. Ob eine Partnerfirma einen gemeinsamen Zugang oder persönliche Mitarbeiterzugänge erhält, wird mit Prof. Hammer geklärt.
- Abhängig von diesem Zugangsmodell wird festgelegt, welche E-Mail-Adresse für Anmeldung, Passwort-Zurücksetzen und Partnerbenachrichtigungen erforderlich ist. Die Endkunden-E-Mail bleibt optional.
- Statt einer ausschließlich österreichischen UID wird eine allgemeine Steuerkennung mit Land, Typ und Wert vorgesehen.
- Globale Kategorien stehen allen Unternehmen zur Verfügung; jedes Unternehmen kann zusätzliche eigene Kategorien verwalten. Ein Produkt hat zunächst eine Hauptkategorie. Verwendete Kategorien dürfen nicht gelöscht, sondern nur nach Umzuordnung entfernt oder deaktiviert werden.
- Der aktuelle Gewerbeschein bzw. gleichwertige Unternehmensnachweis ist vor Partneraktivierung verpflichtend. Es wird nur die aktuelle Datei aktiv gehalten; Austauschvorgänge bleiben nachvollziehbar.
- Die erste Unternehmenspersonalisierung umfasst Name, Logo und Begrüßung. Freie Themes bleiben später.
- Reale Bestell-E-Mails sind vorgesehen: Bestätigung an den Endkunden bei vorhandener Adresse, Information an den Partner und Meldung an das Unternehmen.
- Ein kleines In-App-Benachrichtigungszentrum soll nur neue Bestellungen, Bestellstatusänderungen und gegebenenfalls relevante Versandfehler anzeigen.
- Für KI-Nachweise wird ein zweistufiges Modell verwendet: kuratiertes Register im Repository und vollständiges privates Roharchiv. Alte Chats werden, soweit exakt belegbar, rückwirkend aufgenommen und andernfalls klar als rekonstruiert gekennzeichnet.

Noch offen und nicht als MVP-Beschluss zu behandeln sind:

- ob die sichere Mehr-Unternehmen-Grundlage und/oder öffentliche Selbstregistrierung Teil der Diplomarbeit werden;
- wann der externe Endkunden-Partnershop umgesetzt wird;
- der von Prof. Hammer erwartete Umfang vollständiger KI-Antworten im Anhang.
