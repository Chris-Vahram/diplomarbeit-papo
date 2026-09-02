## 1. Parser und private Ablage (essenziell)

- [x] 1.1 Anonymisierte Codex- und Claude-Code-JSONL-Fixtures mit Benutzer-, Assistant-, System-, Reasoning-, Tool- und unbekannten Ereignissen anlegen
- [x] 1.2 Python-CLI mit exakter PAPO-Projektfilterung und getrennten Parsern für Codex und Claude Code implementieren
- [x] 1.3 Atomare private Archivkopien, restriktive Dateirechte, SHA-256-Manifest, Dateilock und lokalen Verarbeitungsstatus implementieren
- [x] 1.4 Normalisierte Dialogbelege erzeugen und System-, Reasoning-, Tool- sowie Subagenteninhalte nachweislich ausschließen

## 2. Register und Agentenintegration (essenziell)

- [x] 2.1 Idempotenten Abgleich mit dem bestehenden Prompt-Register und datensparsamen Status `AUSSTEHEND` implementieren
- [x] 2.2 CLI-Anreicherung für Zweck, Ergebnis und tatsächlich belegte Prüfungen implementieren, ohne vollständige Dialoge oder absolute Quellpfade ins Repository zu schreiben
- [x] 2.3 `AGENTS.md` und `CLAUDE.md` um denselben kurzen Abschlussablauf für relevante PAPO-Arbeit ergänzen
- [x] 2.4 Konfliktfall testen: Bei nicht sicher aktualisierbarem Register bleibt der Nachweis privat pending und bestehende Dokumentation unverändert

## 3. Automatischer Betrieb und Rückbestand (essenziell)

- [x] 3.1 LaunchAgent-Vorlage und Installations-/Status-/Deinstallationsbefehle für Login- und periodischen Sync erstellen
- [x] 3.2 Nach ausdrücklicher Freigabe den einzelnen LaunchAgent im Benutzerkonto installieren und einen erfolgreichen Hintergrundlauf prüfen
- [x] 3.3 Alle eindeutig zuordenbaren historischen PAPO-Sitzungen aus Codex und Claude Code idempotent importieren
- [x] 3.4 Manifest, Sitzungsanzahl, fehlende Quellen, Pending-Einträge und Repository-Datensparsamkeit nach dem Rückimport prüfen

## 4. Optionale Exportquellen

- [x] 4.1 Optionaler Import bewusst zurückgestellt: Es wurde noch kein echtes anonymisierbares ChatGPT-/Claude-Exportschema bereitgestellt
- [x] 4.2 Fehlende Web-/App-Exporte im Status ausweisen, ohne Kontozugriff oder automatischen Download zu versuchen

## 5. Verifikation und Übergabe (essenziell)

- [x] 5.1 Automatisierte Tests für Projektfilter, Parser, Redaktionsgrenze, Idempotenz, wachsende Dateien, Locking und Fehlerverhalten ausführen
- [x] 5.2 Einen vollständigen Testlauf mit temporärem Archiv durchführen und nachweisen, dass keine echten Sitzungsinhalte in Testausgaben oder Git gelangen
- [x] 5.3 Implementierung und finalen Diff unabhängig auf Korrektheit, Datenschutz und unnötige Komplexität prüfen
- [x] 5.4 OpenSpec validieren, Bedienung in `Documentation/KI-Nachweise/README.md` dokumentieren und erst nach erfolgreichem Dauerlauf archivieren
