## 1. Parser und private Ablage (essenziell)

- [ ] 1.1 Anonymisierte Codex- und Claude-Code-JSONL-Fixtures mit Benutzer-, Assistant-, System-, Reasoning-, Tool- und unbekannten Ereignissen anlegen
- [ ] 1.2 Python-CLI mit exakter PAPO-Projektfilterung und getrennten Parsern für Codex und Claude Code implementieren
- [ ] 1.3 Atomare private Archivkopien, restriktive Dateirechte, SHA-256-Manifest, Dateilock und lokalen Verarbeitungsstatus implementieren
- [ ] 1.4 Normalisierte Dialogbelege erzeugen und System-, Reasoning-, Tool- sowie Subagenteninhalte nachweislich ausschließen

## 2. Register und Agentenintegration (essenziell)

- [ ] 2.1 Idempotenten Abgleich mit dem bestehenden Prompt-Register und datensparsamen Status `AUSSTEHEND` implementieren
- [ ] 2.2 CLI-Anreicherung für Zweck, Ergebnis und tatsächlich belegte Prüfungen implementieren, ohne vollständige Dialoge oder absolute Quellpfade ins Repository zu schreiben
- [ ] 2.3 `AGENTS.md` und `CLAUDE.md` um denselben kurzen Abschlussablauf für relevante PAPO-Arbeit ergänzen
- [ ] 2.4 Konfliktfall testen: Bei nicht sicher aktualisierbarem Register bleibt der Nachweis privat pending und bestehende Dokumentation unverändert

## 3. Automatischer Betrieb und Rückbestand (essenziell)

- [ ] 3.1 LaunchAgent-Vorlage und Installations-/Status-/Deinstallationsbefehle für Login- und periodischen Sync erstellen
- [ ] 3.2 Nach ausdrücklicher Freigabe den einzelnen LaunchAgent im Benutzerkonto installieren und einen erfolgreichen Hintergrundlauf prüfen
- [ ] 3.3 Alle eindeutig zuordenbaren historischen PAPO-Sitzungen aus Codex und Claude Code idempotent importieren
- [ ] 3.4 Manifest, Sitzungsanzahl, fehlende Quellen, Pending-Einträge und Repository-Datensparsamkeit nach dem Rückimport prüfen

## 4. Optionale Exportquellen

- [ ] 4.1 Optionalen Importbefehl für einen bereits bereitgestellten ChatGPT-/Claude-Kontoexport ergänzen, sobald ein echtes anonymisierbares Beispielschema vorliegt
- [ ] 4.2 Fehlende Web-/App-Exporte im Status ausweisen, ohne Kontozugriff oder automatischen Download zu versuchen

## 5. Verifikation und Übergabe (essenziell)

- [ ] 5.1 Automatisierte Tests für Projektfilter, Parser, Redaktionsgrenze, Idempotenz, wachsende Dateien, Locking und Fehlerverhalten ausführen
- [ ] 5.2 Einen vollständigen Testlauf mit temporärem Archiv durchführen und nachweisen, dass keine echten Sitzungsinhalte in Testausgaben oder Git gelangen
- [ ] 5.3 Implementierung und finalen Diff unabhängig auf Korrektheit, Datenschutz und unnötige Komplexität prüfen
- [ ] 5.4 OpenSpec validieren, Bedienung in `Documentation/KI-Nachweise/README.md` dokumentieren und erst nach erfolgreichem Dauerlauf archivieren
