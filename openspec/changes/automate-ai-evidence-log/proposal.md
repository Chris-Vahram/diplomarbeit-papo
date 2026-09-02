## Why

Die bisherige manuelle KI-Dokumentation ist lückenanfällig und verlangt nach jeder Codex- oder Claude-Code-Sitzung zusätzliche Arbeit. PAPO benötigt deshalb einen lokalen, automatischen Nachweisprozess, der vorhandene Sitzungen zuverlässig sichert und nur datenschutzgeeignete Metadaten in das Repository übernimmt.

## What Changes

- Neue PAPO-Sitzungen aus Codex und Claude Code werden automatisch und idempotent erkannt.
- Unveränderte Quellen und normalisierte Dialogbelege werden ausschließlich in einem privaten Archiv außerhalb von Git gespeichert und durch ein SHA-256-Manifest nachweisbar gemacht.
- Das Repository erhält ein automatisch fortgeführtes, datensparsames Sitzungsregister und eine sichtbare Warteschlange für Einträge, deren fachliche Einordnung oder Prüfbelege noch nicht sicher ableitbar sind.
- Projektkontexte weisen Codex und Claude an, relevante Arbeit beim Abschluss mit Zweck, Ergebnis und tatsächlich vorhandenen Prüfbelegen zu registrieren.
- Der vorhandene lokale PAPO-Sitzungsbestand wird einmalig über denselben Prozess nachgezogen.
- ChatGPT- und Claude-Web-Exporte können nach ihrer Bereitstellung importiert werden; das System versucht nicht, Kontodaten oder Exporte ohne Benutzerfreigabe abzurufen.

## Capabilities

### New Capabilities

- `ai-evidence-logging`: Automatische, private und nachvollziehbare Erfassung sowie registerfähige Aufbereitung von PAPO-KI-Sitzungen.

### Modified Capabilities

Keine.

## Impact

- Neue lokale Automatisierungsskripte und anonymisierte Parser-Tests im Repository.
- Ergänzungen an `AGENTS.md`, `CLAUDE.md` und `Documentation/KI-Nachweise/`.
- Ein benutzerspezifischer macOS-LaunchAgent sowie ein privates Archiv außerhalb des Repositorys werden bei der späteren Installation angelegt.
- Keine Änderung an PAPO-Produktcode, REST-APIs, Datenbank oder MVP-Fachverhalten; keine neue Laufzeitabhängigkeit.
