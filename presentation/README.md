# PAPO-Präsentationen

Zwei getrennte, gekürzte Präsentationen mit gemeinsamem Theme und lokalem reveal.js.
Die vorherigen 35 Folien sind auf insgesamt 21 reduziert; keine Zeiterfassung.

## 1. Allgemeiner Projektüberblick — 13 Folien

Öffnen: [Projektüberblick](mvp-deck/index.html).

Für die Vorstellung des Projekts, unabhängig vom aktuellen Sprint:

- Beschreibung, Unternehmen–Partner–Endkunde, Ausgangslage und Nutzen.
- Bestellablauf und Kernfunktionen.
- Digitale Souveränität auf zwei Folien: Kontrolle, Umsetzung und Grenzen.
- Technologie-Stack und Architektur.
- Drei Folien mit bestehenden Browser-/Screenshotbeispielen; anschließend Live-Demo.

Keine Sprintfragen, Roadmap oder Clockify-Folie. GitHub-Stars sind nur ein kompakter,
ausdrücklich historischer Hinweis vom 14.09.2026 auf der Stack-Folie.

## 2. Sprint-Update — 8 Folien

Öffnen: [Sprint-Update vom 06.10.2026](sprint-deck/index.html).

1. Projektstand und aktuelle Demo-Grenzen.
2. Implementierte Stammdaten-/Nachweisfunktionen.
3. Implementierte Migrations-/Deployment-Absicherung.
4. Gewünschte, noch nicht umgesetzte Funktionen.
5. Nächster technischer Schritt und menschliche Abnahmen.
6. Firmenentscheidungen B-09: Geldbasis, Team, Zahlung und Regelversion.
7. Firmenentscheidungen B-09: Rechte, Affiliate, Punkte/Karriere und Bonus.
8. Weitere Firmen-/Betriebsfragen: Staging, BMD, Mails, B-03 und B-07.

Die acht B-09-Fragegruppen bleiben enthalten; vollständige Unterfragen und Belege:
[Projektstand](PROJEKTSTAND_2026-10-06.md) und
[Teamprovisions-Erkundung](../Specification/TEAM_COMMISSION_DISCOVERY.md).
Der Firmentermin ist noch nicht bestätigt; Empfehlungen sind keine Firmenfreigabe.

## Öffnen und Vorführen

Die jeweilige `index.html` im Browser öffnen. Kein Build-Schritt nötig.
Pfeiltasten blättern, `Esc` zeigt die Übersicht, `F` aktiviert Vollbild.
Auf kleinen Displays verwendet reveal.js seine Scrollansicht.

Beide Decks verwenden `mvp-deck/theme.css` und `mvp-deck/reveal/` gemeinsam;
keine neuen Abhängigkeiten, Webfonts oder CDN-Laufzeit. Die Browseraufnahmen liegen
unter `output/playwright/`. Für Weitergabe die Dateien im Repository-Verbund
behalten, nicht nur das einzelne HTML kopieren. Die Live-Anwendung braucht Netzwerk.

Das [Demo-Drehbuch](demo-drehbuch.md) bleibt für die Vorführung verfügbar.
Screenshot-/Fachabnahme durch Chris und Tobias bleibt davon getrennt.

## Nachweisgrenzen

Notiz und Steuerkennung wurden am 14.09. ergänzt; die Uhrzeit der Besprechung ist
nicht dokumentiert. Register/Nachweise und Migrationsfix folgten am 25.09.,
Deployment-Absicherung am 28.09.2026. Es wurden keine neuen Backendfunktionen
implementiert und keine neuen Firmenregeln festgelegt. Das bestehende Theme und
Tobias' Design bleiben unverändert. Keine neue angemeldete Live-Anwendungsabnahme.

## Prüfung der Aufteilung — 06.10.2026

- HTML-Struktur, 13 + 8 Folien und alle acht B-09-Fragegruppen geprüft.
- `node --check` für beide Inline-Skripte und `git diff --check` erfolgreich.
- Sieben lokale Ressourcen im Überblick, drei im Sprintdeck und 24 lokale
  Markdown-Ziele vorhanden; kein Clockify-/Stundeninhalt in beiden Decks.
- Beide Decks bei 1280 × 720 und 390 × 844 ohne Überlauf geprüft; Bilder,
  Pfeilnavigation und Übersicht funktionieren. Geänderte Folien visuell geprüft.
- Keine externen Requests oder JavaScript-Laufzeitfehler beim lokalen Laden.
  Direkte `file:`-Navigation ist im Browsertool gesperrt und nicht separat geprüft.
- Unabhängiger Inhaltsreview: Entscheidung zur Karriere-Geldwirkung korrekt
  gekennzeichnet. Keine neuen Firmenregeln, keine Anwendung implementiert.

## Kontextabgleich und Veröffentlichung — 07.10.2026

Chris hat Commit/Push/Merge der beiden Decks und Kontextdateien beauftragt.
Zuordnung zu PR #35 (Inhaltscommit `67a5acf`) über den Titel
`docs: publish split presentations and session context`; Merge und neuesten
Main-Workflow beim Wiedereinstieg prüfen. Der Sprint-Inhaltsstand bleibt 06.10.2026.
Ein neuer Lehrer-/Firmentermin und weitere fachliche Freigaben sind nicht bestätigt.

Erneut geprüft: `git diff --check`, gültige HTML-Struktur mit 13 + 8 Folien,
acht Firmenfragegruppen, `node --check` beider Inline-Skripte, sieben bzw. drei
lokale Deckressourcen und 31 lokale Markdown-Ziele. Alle drei aktiven Changes
bestehen `openspec validate <change> --strict`. Die globale Spec-Prüfung hatte
am 06.10.2026 24 bestehende Purpose-Platzhalterfehler; keine Spec-Bereinigung
oder neue Live-Anwendungsabnahme Bestandteil dieser Dokumentationsarbeit.
