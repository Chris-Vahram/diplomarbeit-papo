## Context

Codex und Claude Code speichern lokale Sitzungen als unterschiedliche JSONL-Formate. PAPO besitzt bereits ein kuratiertes Register und Regeln für ein privates Roharchiv, die Erfassung erfolgt derzeit jedoch manuell. Die Automatisierung muss ohne Cloud-Dienst, API-Schlüssel, zusätzliche Bibliotheken oder Veröffentlichung sensibler Inhalte funktionieren und darf die eigentliche Entwicklungsarbeit nicht blockieren.

Vollautomatisch feststellbar sind Quelle, Sitzungs-ID, Zeit, Projektpfad, menschliche Prompts, sichtbare Antworten und viele ausgeführte Prüfkommandos. Nicht zuverlässig automatisch feststellbar ist, ob Chris ein Ergebnis außerhalb des Agenten eigenständig geprüft oder bewusst verworfen hat. Solche Aussagen dürfen nicht erfunden werden.

## Goals / Non-Goals

**Goals:**

- Lokale PAPO-Sitzungen ohne Benutzerhandlung zeitnah und rückwirkend erfassen.
- Exakte Rohbelege privat, unverändert, zugriffsbeschränkt und hashbar erhalten.
- Aus den unterschiedlichen Formaten einen gemeinsamen Dialogbeleg ohne System-, Reasoning-, Tool- und Subagentenblöcke erzeugen.
- Das Repository automatisch mit datensparsamen, nachvollziehbaren Registereinträgen versorgen.
- Fachliche Zusammenfassung und belegbare Prüfungen durch die ohnehin arbeitenden Agenten ergänzen lassen; bei Ausfall nichts verlieren oder erfinden.
- Wiederholte und parallele Läufe sicher verarbeiten.

**Non-Goals:**

- Automatisches Anfordern oder Herunterladen von ChatGPT-/Claude-Kontoexporten.
- Veröffentlichung vollständiger Dialoge, lokaler Pfade, Zugangsdaten oder Firmendaten im Git-Repository.
- KI-basierte Bewertung der eigenen Leistung oder erfundene Behauptungen über manuelle Prüfungen.
- Änderung des PAPO-Produktcodes oder Einführung eines externen Dienstes.

## Decisions

### 1. Ein kleines Python-Standardbibliothek-Werkzeug

Ein repositorylokales CLI übernimmt Erkennung, Parser, Archivierung, Registerabgleich und Statusprüfung. Es verwendet ausschließlich Python 3 Standardbibliothek (`json`, `hashlib`, `pathlib`, `shutil`, `fcntl`, `tempfile`).

Alternativen wie eine Datenbank, Node-Paket oder externer Logging-Dienst erhöhen Abhängigkeiten und Datenschutzfläche, ohne für die geringe lokale Datenmenge einen Nutzen zu bringen.

### 2. Periodischer macOS-LaunchAgent als gemeinsamer Trigger

Ein benutzerspezifischer LaunchAgent führt den idempotenten Sync beim Anmelden und anschließend periodisch aus. Damit werden Codex und Claude Code mit einem Mechanismus abgedeckt. Ein Claude-spezifischer Hook und Änderungen am globalen Codex-`notify` sind nicht erforderlich.

Der LaunchAgent darf erst in der Implementierungsphase mit ausdrücklicher Freigabe außerhalb des Repositorys installiert werden. Bis dahin kann derselbe Sync manuell in Tests ausgeführt werden.

### 3. Strikte Projektzuordnung

Claude-Dateien werden nur aus dem exakten projektspezifischen Verzeichnis gelesen. Codex-Dateien werden nur aufgenommen, wenn ihr `session_meta.cwd` dem kanonischen PAPO-Repositorypfad entspricht. Verzeichnisnamen, Texttreffer oder bloße Erwähnungen von PAPO reichen nicht aus.

### 4. Privates Archiv und öffentliches Register trennen

Das private Archiv liegt standardmäßig unter `~/Documents/PAPO-KI-Roharchiv`, erhält restriktive Dateirechte und enthält:

- unveränderte Quellkopien je Werkzeug und Sitzungs-ID;
- normalisierte Dialoge mit menschlichen Prompts und sichtbaren Endantworten;
- atomar aktualisiertes SHA-256-Manifest;
- lokalen Verarbeitungsstatus und Fehlerprotokoll.

Das Repository enthält nur Sitzungs-ID, Datum, Werkzeug, datensparsame Themen-/Ergebnisbeschreibung, Verweis auf den privaten Rohbeleg, Nachweisstatus und belegbare Prüfhinweise. Vollständige Prompts bleiben privat und werden nur später gezielt für den Diplomarbeitsanhang übernommen.

### 5. Idempotenz über stabile Quellschlüssel

Der Schlüssel besteht aus Werkzeug und Sitzungs-ID. Bei wachsenden laufenden JSONL-Dateien wird die private Kopie atomar auf den neuesten vollständigen Stand gebracht; Registerzeilen werden aktualisiert statt dupliziert. Ein Dateilock verhindert kollidierende Läufe.

### 6. Automatische Grundregistrierung, kontextbewusste Anreicherung durch Agenten

Der Sync erzeugt für jede neue Sitzung mindestens einen sicheren Grunddatensatz. `AGENTS.md` und `CLAUDE.md` weisen die jeweiligen Agenten an, am Ende eines relevanten PAPO-Arbeitsblocks das CLI mit einer kurzen fachlichen Zusammenfassung und tatsächlich ausgeführten Prüfungen aufzurufen. Das CLI ermittelt den aktuellen Rohbeleg selbst.

Fehlt diese Anreicherung, bleibt der Eintrag sichtbar als `AUSSTEHEND`; ein späterer Agent kann ihn anhand des privaten Belegs nachziehen. Der Collector leitet keine Eigenprüfung aus bloßen Antworttexten ab und schreibt keine frei extrahierten Promptinhalte ins Repository.

### 7. Rückwirkender Import verwendet denselben Pfad

Der erste Sync verarbeitet alle vorhandenen lokalen PAPO-Sitzungen. Bereitgestellte ChatGPT-/Claude-Web-Exporte können später über einen expliziten Importpfad eingelesen werden. Nicht bereitgestellte Kontoexporte werden als fehlende Quelle ausgewiesen, nicht umgangen.

## Risks / Trade-offs

- **Eine Sitzung enthält Geheimnisse oder personenbezogene Daten** → Volltext bleibt im privaten Archiv; Repositoryausgabe verwendet nur explizite, kurze Agentenmetadaten und eine Sperrliste für offensichtliche Secret-Muster.
- **Der Agent beendet sich vor der Anreicherung** → LaunchAgent sichert die Sitzung trotzdem und markiert sie als `AUSSTEHEND`.
- **JSONL-Schema ändert sich** → Parser verwirft unbekannte Ereignistypen sicher, protokolliert die Abweichung und behält die unveränderte Quelle.
- **Eine Sitzung wächst während des Kopierens** → atomare Zielaktualisierung und erneuter idempotenter Lauf.
- **Automatischer Prozess verändert den Working Tree überraschend** → nur das klar abgegrenzte Register darf geändert werden; bei Konflikten wird ein Pending-Eintrag privat abgelegt statt bestehende Inhalte zu überschreiben.
- **LaunchAgent oder Archivpfad fällt aus** → nichtnull Exit-Code, lokales Fehlerprotokoll und `status`-Befehl; Entwicklungsbefehle werden nie blockiert.
- **Web-/App-Chats fehlen** → lokale Arbeit ist vollständig automatisiert; alte Web-Chats können technisch erst nach einem einmaligen Kontodatenexport importiert werden.

## Migration Plan

1. Parser und anonymisierte Fixtures implementieren und testen.
2. Sync zunächst gegen ein temporäres Archiv ausführen und Ausgabe prüfen.
3. Vorhandene lokale PAPO-Sitzungen in das private Archiv importieren und Manifest verifizieren.
4. Repositoryregister aus dem Import ergänzen; unsichere Einträge als `AUSSTEHEND` belassen.
5. Kontextregeln für Codex und Claude ergänzen.
6. Nach ausdrücklicher Freigabe LaunchAgent im Benutzerkonto installieren und einen Neustart-/Periodenlauf testen.
7. Rollback: LaunchAgent entladen und dessen einzelne Plist entfernen; Repositoryskript und privates Archiv bleiben erhalten. Eine Löschung des privaten Archivs ist kein automatischer Rollback-Schritt.

## Open Questions

- Herr Hammer muss nur noch bestätigen, ob die kuratierte Tabelle plus ausgewählte Volldialoge und privates Roharchiv der erwarteten schulischen Form entsprechen. Diese Antwort ändert nicht die technische Sicherung, sondern nur den späteren Umfang des Anhangs.
