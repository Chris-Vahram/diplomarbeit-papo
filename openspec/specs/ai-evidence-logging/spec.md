# ai-evidence-logging Specification

## Purpose
TBD - created by archiving change automate-ai-evidence-log. Update Purpose after archive.
## Requirements
### Requirement: PAPO-Sitzungen automatisch erkennen

Das Werkzeug SHALL lokale Codex- und Claude-Code-Sitzungen ausschließlich dann als PAPO-Nachweis erfassen, wenn sie dem kanonischen Repositorypfad eindeutig zugeordnet sind.

#### Scenario: Codex-Sitzung gehört zu PAPO
- **WHEN** eine Codex-Sitzung im `session_meta` exakt den PAPO-Repositorypfad als Arbeitsverzeichnis enthält
- **THEN** wird sie als PAPO-Quelle zur Verarbeitung ausgewählt

#### Scenario: Fremde Sitzung erwähnt PAPO nur im Text
- **WHEN** eine Sitzung aus einem anderen Arbeitsverzeichnis den Projektnamen oder Projektdateien erwähnt
- **THEN** wird sie nicht als PAPO-Quelle erfasst

### Requirement: Rohbelege privat und nachweisbar archivieren

Das Werkzeug SHALL unveränderte Sitzungsquellen außerhalb des Git-Repositorys mit restriktiven Zugriffsrechten ablegen und ihre SHA-256-Prüfsummen in einem atomar aktualisierten Manifest führen.

#### Scenario: Neue Sitzung wird archiviert
- **WHEN** eine neue PAPO-Sitzungsquelle erkannt wird
- **THEN** werden eine unveränderte private Kopie und deren SHA-256-Prüfsumme gespeichert

#### Scenario: Laufende Sitzung wächst
- **WHEN** eine bereits archivierte Sitzungsdatei neue vollständige Ereignisse enthält
- **THEN** werden Archivkopie und Manifest atomar auf den neuen Stand gebracht

### Requirement: Dialoge datensparsam normalisieren

Das Werkzeug SHALL aus unterstützten Sitzungsformaten menschliche Prompts und sichtbare Assistant-Antworten in ihrer Reihenfolge ableiten und SHALL Systemanweisungen, interne Reasoning-Inhalte, Tool-Ereignisse und Subagentenkommunikation aus dem normalisierten Dialog ausschließen.

#### Scenario: Sitzung enthält gemischte Ereignisse
- **WHEN** eine Quellsitzung Benutzer-, Assistant-, System-, Reasoning- und Tool-Ereignisse enthält
- **THEN** enthält der normalisierte Dialog nur menschliche Prompts und sichtbare Assistant-Antworten

#### Scenario: Unbekannter Ereignistyp tritt auf
- **WHEN** der Parser einen nicht unterstützten Ereignistyp findet
- **THEN** bleibt die unveränderte Quelle erhalten, der Typ wird diagnostisch vermerkt und nicht ungeprüft veröffentlicht

### Requirement: Verarbeitung ist idempotent und nebenläufigkeitssicher

Das Werkzeug SHALL wiederholte und gleichzeitig ausgelöste Synchronisationen ohne doppelte Registereinträge oder beschädigte Archivdateien verarbeiten.

#### Scenario: Unveränderte Quelle wird erneut verarbeitet
- **WHEN** der Sync mehrfach auf denselben unveränderten Sitzungsbestand angewendet wird
- **THEN** bleiben Anzahl und Inhalt der Nachweise unverändert

#### Scenario: Zwei Synchronisationen überlappen
- **WHEN** ein periodischer und ein agentenausgelöster Lauf gleichzeitig starten
- **THEN** verarbeitet höchstens ein schreibender Lauf den Bestand und alle Artefakte bleiben gültig

### Requirement: Repositoryregister bleibt datensparsam

Das Werkzeug SHALL im Repository nur die für den KI-Nachweis benötigten Metadaten und ausdrücklich bereitgestellten fachlichen Zusammenfassungen speichern. Es MUST NOT automatisch vollständige Prompts, vollständige Antworten, lokale absolute Quellpfade, Zugangsdaten oder erkannte Secret-Werte in das Repository schreiben.

#### Scenario: Grunddatensatz ohne fachliche Anreicherung
- **WHEN** eine Sitzung gesichert wurde, aber keine verlässliche fachliche Zusammenfassung vorliegt
- **THEN** erhält sie einen datensparsamen Register- oder Pending-Eintrag mit Status `AUSSTEHEND`

#### Scenario: Agent liefert Zusammenfassung und Prüfbelege
- **WHEN** der arbeitende Agent Zweck, Ergebnis und tatsächlich ausgeführte Prüfungen registriert
- **THEN** aktualisiert das Werkzeug genau den zugehörigen Sitzungseintrag, ohne private Dialoginhalte zu veröffentlichen

### Requirement: Fehlende Eigenprüfung wird nicht erfunden

Das Werkzeug SHALL nur explizit angegebene oder maschinell belegte Prüfkommandos dokumentieren und SHALL fehlende menschliche Prüfentscheidungen als offen kennzeichnen.

#### Scenario: Testkommando ist im Beleg vorhanden
- **WHEN** ein ausgeführtes Testkommando und sein Ergebnis eindeutig in der Sitzung belegt sind
- **THEN** darf das Werkzeug diesen technischen Prüfbeleg registrieren

#### Scenario: Eigene Bewertung ist nicht belegt
- **WHEN** aus der Sitzung nicht hervorgeht, ob Chris einen Vorschlag fachlich geprüft oder verworfen hat
- **THEN** wird keine entsprechende Eigenleistung behauptet

### Requirement: Erfassung läuft ohne Benutzerhandlung

Die installierte Automatisierung SHALL beim Benutzerlogin sowie periodisch im Hintergrund synchronisieren und SHALL Entwicklungsarbeit bei einem Fehler nicht blockieren.

#### Scenario: Neue lokale Sitzung entsteht
- **WHEN** Codex oder Claude Code eine neue PAPO-Sitzung schreibt
- **THEN** wird sie spätestens beim nächsten periodischen Lauf privat gesichert, ohne dass Chris einen Befehl ausführen muss

#### Scenario: Synchronisation schlägt fehl
- **WHEN** Quelle oder Ziel vorübergehend nicht gelesen beziehungsweise geschrieben werden kann
- **THEN** endet der Hintergrundlauf mit diagnostischem Fehlerstatus und lässt Codex, Claude Code und PAPO unverändert weiterarbeiten

### Requirement: Historische und bereitgestellte Exportdaten importieren

Das Werkzeug SHALL vorhandene lokale PAPO-Sitzungen rückwirkend verarbeiten und SHALL einen expliziten Import für bereitgestellte ChatGPT-/Claude-Exporte unterstützen.

#### Scenario: Erster Lauf mit historischem lokalen Bestand
- **WHEN** der Sync erstmals auf dem bestehenden Rechner ausgeführt wird
- **THEN** werden alle eindeutig zuordenbaren lokalen PAPO-Sitzungen über denselben idempotenten Archivpfad erfasst

#### Scenario: Kontoexport wurde nicht bereitgestellt
- **WHEN** kein ChatGPT- oder Claude-Web-Export verfügbar ist
- **THEN** weist der Status die fehlende Quelle aus, ohne Kontozugriff oder Download zu versuchen

### Requirement: Automatisierung ist überprüfbar

Parser, Projektfilter, Normalisierung, Redaktionsgrenze, Idempotenz und Fehlerverhalten SHALL mit anonymisierten lokalen Fixtures reproduzierbar getestet werden.

#### Scenario: Tests laufen ohne echte Sitzungsdaten
- **WHEN** die Tests in einer sauberen Arbeitsumgebung ausgeführt werden
- **THEN** verwenden sie ausschließlich anonymisierte Fixtures und benötigen weder Kontozugang noch Netzwerk
