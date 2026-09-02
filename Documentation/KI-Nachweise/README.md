# KI-Nachweise für die PAPO-Diplomarbeit

## Zweck

Dieses Verzeichnis dokumentiert den Einsatz von ChatGPT/Codex und Claude/Claude Code nachvollziehbar, ohne geheime Konfigurationen, personenbezogene Firmendaten oder vollständige interne Werkzeugprotokolle in das Repository zu übernehmen.

Grundlage ist die BMBWF-Handreichung **„Die Verwendung KI-basierter Tools beim Erstellen abschließender Arbeiten“**, Stand 14.11.2023. Sie verlangt insbesondere:

- verwendetes KI-System bzw. Quellmaterial angeben;
- konkrete Prompts eindeutig identifizierbar machen;
- KI-Nutzung im Text und im Quellenverzeichnis kennzeichnen;
- Arbeitsverlauf, Einsatzphase und verwendete Hilfsmittel laufend im Begleitprotokoll dokumentieren;
- Ergebnisse eigenständig anhand vertrauenswürdiger Quellen prüfen;
- individuelle Leistung und Verantwortung bei den Verfassern belassen.

Offizielle Referenzen:

- [BMBWF-Handreichung als PDF](https://www.bmb.gv.at/dam/jcr%3A3bc6eb26-f4b1-499c-a601-675e7fd6fa0f/ki_abarb.pdf)
- [FAQ für Schülerinnen und Schüler zu Diplomarbeiten](https://www.diplomarbeiten-bbs.at/faq/faq-schuelerinnen)
- [Anthropic Privacy Center - Claude-Daten exportieren](https://privacy.claude.com/en/articles/9450526-export-your-claude-data)

Die Handreichung empfiehlt ausdrücklich eine Prompt-Tabelle im Anhang. Vollständige Antworten werden dort nicht pauschal für jeden Eintrag verlangt. Für PAPO werden deshalb alle relevanten Prompts registriert, während nur repräsentative Dialoge vollständig in den Anhang kommen. Die vollständigen Rohverläufe bleiben als privates Belegarchiv erhalten.

## Warum der Text des Vergleichsdokuments nicht übernommen werden darf

Das bereitgestellte Vergleichsdokument beschreibt KI ausschließlich als Rechtschreib-, Grammatik- und Ausdruckshilfe und behauptet, dass keine fachlichen Inhalte, Analysen, Programmierungen oder Entscheidungen mit KI-Unterstützung entstanden seien.

Diese Aussage wäre für PAPO sachlich falsch. KI wurde unter anderem für Kontextanalyse, Anforderungsabgrenzung, Architekturüberlegungen, OpenSpec-Artefakte, Implementierungsunterstützung, Tests, Reviews, Fehlersuche und Dokumentation eingesetzt. Übernommen werden kann nur die klare Idee eines eigenen Hinweises zur KI-Verwendung, nicht dessen inhaltliche Behauptung.

## Zweistufiger Nachweis

### 1. Kuratiertes Register im Repository

Das Register enthält je fachlich relevantem Arbeitsauftrag:

- eindeutige ID und Datum;
- verantwortliches Teammitglied;
- Werkzeug, Oberfläche und - soweit zuverlässig bekannt - Modell/Version;
- den exakten menschlichen Prompt oder einen eindeutigen Verweis auf den Rohbeleg;
- Zweck, Projektphase und betroffenen Diplomarbeitsabschnitt;
- verwendetes Ergebnis;
- eigene Prüfung, Korrektur und Entscheidung;
- zugehörige Dateien, OpenSpec-Changes, Tests oder Commits;
- Nachweisstatus.

Systemnachrichten, interne Modellüberlegungen, Tool-Ausgaben und automatisch erzeugte Agentenkommunikation sind keine Prompts des Teams und werden nicht als Eigenbeleg ausgegeben.

### 2. Privates Roharchiv außerhalb von Git

Das Roharchiv enthält unveränderte Exporte und lokale Sitzungsdateien. Es darf nicht öffentlich in GitHub landen, weil es Zugangsdaten, lokale Pfade, personenbezogene Angaben, vertrauliche Firmendaten oder umfangreiche fremde Inhalte enthalten kann.

Empfohlene Struktur:

```text
PAPO-KI-Roharchiv/
  chatgpt-export-YYYY-MM-DD/
  claude-export-YYYY-MM-DD/
  claude-code-sessions-YYYY-MM-DD/
  codex-sessions-YYYY-MM-DD/
  manifest-sha256.txt
```

Nach dem Export werden die Originaldateien schreibgeschützt abgelegt und mit SHA-256-Prüfsummen in einem Manifest erfasst. Für die Auswertung wird eine Arbeitskopie verwendet.

## Nachweisstatus

| Status | Bedeutung |
|---|---|
| `EXAKT` | Originalprompt und Antwort sind im Export oder lokalen Sitzungsprotokoll vorhanden. |
| `TEILWEISE` | Prompt oder Antwort ist nur teilweise erhalten; vorhandener Umfang wird genau angegeben. |
| `REKONSTRUIERT` | Einsatz ist über Git, OpenSpec, Dokumente oder Screenshots belegbar, der genaue Wortlaut aber nicht mehr vorhanden. Keine erfundenen Prompttexte. |
| `NICHT WIEDERHERSTELLBAR` | Der Einsatz ist bekannt, lässt sich aber nicht mehr ausreichend belegen. |

## Rückwirkende Aufarbeitung

1. ChatGPT- und Claude-Kontodaten exportieren.
2. Lokale Claude-Code- und Codex-Sitzungen unverändert in das private Roharchiv kopieren.
3. Exporte nach PAPO, Repository-Pfad, Dateinamen, OpenSpec-Change und Datum filtern.
4. Nur menschliche Prompts und sichtbare Endantworten übernehmen; System-, Reasoning- und Tool-Blöcke auslassen.
5. Sitzungen zeitlich mit Git-Commits, OpenSpec-Tasks, Meetingprotokollen und Testberichten verbinden.
6. Jeden Eintrag mit `EXAKT`, `TEILWEISE`, `REKONSTRUIERT` oder `NICHT WIEDERHERSTELLBAR` kennzeichnen.
7. Repräsentative Einträge für den Anhang auswählen: Anforderung, Architektur, Implementierung, Test/Review, Fehlerbehebung und Dokumentation.
8. Mit Prof. Hammer Umfang und Zitierform bestätigen, bevor die finale Diplomarbeit gesetzt wird.

## Lokaler Quellenbestand vom 25.08.2026

Die lokale Inventur erfolgte nur über Metadaten; sensible Inhalte wurden nicht ausgegeben.

### Claude Code

- 23 primäre PAPO-Sitzungsdateien im projektspezifischen Claude-Verzeichnis;
- Zeitraum der vorhandenen primären Sitzungen: 01.07.2026 bis 17.07.2026;
- rund 690 menschliche Textblöcke und 15 Bildblöcke;
- rund 2.102 sichtbare Assistant-Textblöcke;
- zusätzliche Subagenten-, Tool- und Systemereignisse, die nicht in den Prompt-Anhang gehören.

### Codex

- 10 PAPO-Sitzungen mit exakt passendem Repository-Arbeitsverzeichnis;
- Zeitraum: 13.07.2026 bis 25.08.2026;
- 80 protokollierte Benutzerereignisse und 237 sichtbare Agentenantwort-Ereignisse;
- aktuelle Sitzung und ältere archivierte Sitzungen sind lokal vorhanden.

### Noch fehlend

- ChatGPT-Web-/App-Verläufe sind nicht in den untersuchten Claude-/Codex-Verzeichnissen enthalten und müssen über den ChatGPT-Kontodatenexport ergänzt werden.
- Claude-Web-/App-Chats können zusätzlich über den Claude-Datenexport ergänzt werden; Claude-Code-Sitzungen liegen bereits lokal vor.

Die Zahlen sind ein Inventurstand, keine Anzahl zitierwürdiger Prompts. Tool-Rückgaben, Wiederholungen, Compactions und rein operative Nachrichten werden bei der Kuratierung entfernt.

## Lokale Automatisierung

Das Standardbibliothek-Werkzeug sichert Codex- und Claude-Code-Sitzungen mit exakter PAPO-Projektzuordnung. Vollständige Quellen und normalisierte Dialoge bleiben unter `~/Documents/PAPO-KI-Roharchiv`; Git erhält nur das datensparsame Register.

```bash
python3 tools/ai_evidence_log.py sync
python3 tools/ai_evidence_log.py status
python3 tools/ai_evidence_log.py enrich \
  --purpose "Zweck des Arbeitsblocks" \
  --result "Tatsächlich verwendetes Ergebnis" \
  --check "Wirklich ausgeführter Prüfungsbefehl: Ergebnis"
```

Ohne belegte technische Prüfung wird `--check` weggelassen. `enrich` wählt standardmäßig die zuletzt geänderte PAPO-Sitzung; bei Bedarf kann `--session-id <ID>` angegeben werden.

Der macOS-Hintergrundlauf wird so verwaltet:

```bash
python3 tools/ai_evidence_log.py install
python3 tools/ai_evidence_log.py agent-status
python3 tools/ai_evidence_log.py uninstall
```

`uninstall` entfernt nur den LaunchAgent. Das private Archiv bleibt erhalten. Fehlende ChatGPT-/Claude-Webexporte werden im Status ausgewiesen und niemals automatisch angefordert oder heruntergeladen.

## Export in der deutschsprachigen Oberfläche

### ChatGPT

1. ChatGPT im Browser oder in der Desktop-App öffnen.
2. Profilbild bzw. Namen öffnen.
3. **Einstellungen** wählen.
4. **Datenkontrollen** öffnen.
5. **Daten exportieren** wählen und bestätigen.
6. Den per E-Mail bereitgestellten ZIP-Export zeitnah herunterladen und unverändert im privaten Roharchiv ablegen.

Falls der Punkt in der mobilen App fehlt, den Vorgang im Browser auf `chatgpt.com` durchführen. Alternativ kann eine Datenkopie über das OpenAI-Datenschutzportal angefordert werden.

### Claude

Der Export ist für persönliche Free-, Pro- und Max-Konten in der Web- oder Desktop-App verfügbar, nicht in der iOS-/Android-App:

1. Initialen links unten öffnen.
2. **Einstellungen** wählen.
3. **Datenschutz** öffnen.
4. **Daten exportieren** wählen.
5. Download-Link aus der E-Mail öffnen; der Link ist nur befristet gültig.

Bei Team-/Enterprise-Konten kann der Export von den Berechtigungen des Kontos bzw. des Primary Owners abhängen.

## Ehrlicher Hinweistext für die Diplomarbeit - Entwurf

> Für Konzeption, Anforderungsanalyse, Implementierungsunterstützung, Testentwurf, Fehlersuche, Review und sprachliche Überarbeitung wurden ChatGPT/Codex und Claude/Claude Code unterstützend eingesetzt. Fachliche und technische Entscheidungen sowie übernommene Ergebnisse wurden durch Quellenprüfung, Code-Review, automatisierte Tests und manuelle Funktionsprüfungen eigenständig verifiziert und bei Bedarf überarbeitet. Verwendete Werkzeuge, Prompts, Einsatzphasen und Verwendungsentscheidungen sind im KI-Nachweisregister dokumentiert. Vollständige Rohverläufe werden aus Datenschutz- und Vertraulichkeitsgründen in einem privaten Belegarchiv aufbewahrt.

Dieser Text muss vor der endgültigen Übernahme mit den schulischen Vorgaben, der tatsächlichen Arbeitsweise und Prof. Hammer abgestimmt werden.
