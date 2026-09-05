# Effizienter Spec-driven Workflow für PAPO

**Stand:** 03.09.2026
**Zweck:** Qualität dort absichern, wo Fehler teuer sind, ohne jede Phase in einer neuen Sitzung nochmals zu prüfen.

## Grundsatz

PAPO ist Brownfield. FSD, OpenSpec, Code und Tests existieren; pro Arbeitsblock wird nur der betroffene Ausschnitt gelesen. Eine Prüfung wird nicht unter einem neuen Namen wiederholt.

```text
klein:      prüfen -> ändern -> fokussierter Check
normal:     planen -> apply -> fokussierte Checks -> validate/archive
hohes Risiko: planen -> 1 unabhängiger Plan+Readiness-Review
             -> apply/tests -> 1 finaler Review -> validate/archive
```

Hohes Risiko umfasst insbesondere Authentifizierung/Autorisierung, Partnerisolation, Geld, Datenverlust, destruktive Migrationen, Secrets und produktives Deployment. Umfang allein macht einen Change nicht automatisch hochriskant.

## Dokumentrollen

| Quelle | Zweck |
|---|---|
| `AGENTS.md` | dauerhafte Regeln, Quellenpriorität, Security, Git und Checks |
| `Specification/CONTINUATION_PROMPT.md` | kurzer Router zum nächsten konkreten Ergebnis |
| `Specification/NEXT_SESSION_PLAN.md` | aktuelles Arbeitspaket, Reihenfolge, DoD und externe Blocker |
| `Specification/continuations/` | Handoff nur bei echtem Sitzungs-/Modellwechsel |
| `openspec/changes/<id>/` | Proposal, Design, Delta-Spec und `tasks.md` eines Changes |
| `openspec/specs/` | archiviertes Sollverhalten |
| `Documentation/KI-Nachweise/prompt-register.md` | Prompt-Backlog: offene kuratierbare KI-Nachweise, kein Roharchiv |
| Code und Tests | implementiertes Verhalten und Nachweis |
| Besprechungsprotokolle/FSD | neuere Beschlüsse bzw. fachliche Baseline |

Information einmal an der zuständigen Stelle pflegen und sonst verlinken. Scratch, Handoffs und KI-Nachweise enthalten keine Zugangsdaten oder privaten Links.

## Start eines Arbeitsblocks

Nur den aktuellen Zustand und die tatsächlich benötigten Quellen laden:

```bash
git status --short --branch
openspec list --json
openspec status --change <change-id> --json
```

`git fetch`, Remote-/VM-Prüfungen und volatile Webrecherche nur ausführen, wenn sie für den aktuellen Task gebraucht werden. Keine vollständigen Verzeichnis-, Dokument- oder Toolkataloge laden.

Skills, Plugins und CLI-Versionen nicht vor jedem Change inventarisieren. Wartung erfolgt nur bei Einrichtung, ausdrücklichem Wartungsauftrag, konkretem Fehler/Kompatibilitätshinweis oder wenn eine versionsabhängige Funktion tatsächlich gebraucht wird. Dann nur die betroffene Komponente prüfen und aktualisieren; globale Updates dokumentieren.

## Risikobasierter Ablauf

### Kleine/mechanische Arbeit

Kein OpenSpec für Erklärungen, Diagnosen, Tippfehler, kleine Dokumentationskorrekturen oder offensichtlich verhaltensneutrale Änderungen. Relevante Datei lesen, minimal ändern, kleinsten aussagekräftigen Check ausführen.

### Normaler Change

1. Relevante FSD-/Protokollstelle, betroffene Spec sowie Code/Testpfad lesen.
2. Einen vorhandenen OpenSpec-Change aktualisieren oder einen neuen kleinen Change anlegen.
3. Proposal, Design nur bei echter Designentscheidung, Delta-Spec und ausführbare Tasks erstellen.
4. Nach eigener Konsistenzprüfung direkt implementieren; benachbarte, unabhängige Tasks dürfen in einem Block erledigt werden.
5. Fokussierte Tests während der Arbeit, kanonische Checks einmal vor Abschluss.
6. `openspec validate <change-id>`, dann nach Freigabe archivieren.

Kein verpflichtender Modellwechsel, kein separater Readiness- oder SELF-SUFFICIENT-Termin.

Vollständig spezifizierte GitHub-/VM-Apply-Schritte laufen mit Chris' stehender Freigabe bis zur dokumentierten Stopbedingung. Commit, Push, Merge, Deploy-Aktivierung und Archivierung bleiben jeweils separate Git-/Release-Gates.

### Hochrisiko-Change

1. Wie beim normalen Change planen.
2. Genau einen unabhängigen Review durchführen. Er prüft gleichzeitig Scope, Architektur, Security, Ausführbarkeit, Checks und Stopbedingungen. Ergebnis: `READY` oder konkrete Findings.
3. Findings nachvollziehbar anhängen/einarbeiten und danach direkt applyen; nur eine neue fachliche oder sicherheitsrelevante Entscheidung geht zurück in den Review.
4. Nach Tests genau einen finalen Diff-/Evidenzreview durchführen. Für PAPO: `partnerportal-quality` plus generischer Korrektheits-/Security-Review.
5. Validieren und nach Chris' Freigabe archivieren.

Damit ersetzt der unabhängige Planreview das frühere zusätzliche SELF-SUFFICIENT-Gate.

## Tasks und Checks

Ein Task enthält nur, was zur sicheren Ausführung nötig ist:

- Ziel und echte Voraussetzungen,
- betroffene Dateien/Systeme,
- relevantes Verhalten, Daten- und Securityfolgen,
- einen reproduzierbaren Check mit erwartetem Ergebnis,
- DoD und Stopbedingung bei gefährlichen oder extern blockierten Schritten.

Keine Mini-Plan-Bestätigung vor jedem bereits freigegebenen Task. Stoppen nur bei neuem Scope, fehlender Berechtigung, widersprüchlichem Bestand, Datenrisiko oder zwei erfolglosen Fix-Versuchen.

Validierung sparsam staffeln:

- fokussierter Test direkt nach geänderter Logik,
- komplette Backend-Suite und Package einmal vor Abschluss eines Backend-Changes,
- `openspec validate <change-id>` nach geänderten Change-Artefakten,
- `openspec validate --all` nur vor Merge/Archivierung oder nach OpenSpec-/Schema-Wartung,
- Browserprüfung nur bei betroffener UI bzw. Runtime.

## Kontext und Handoffs

- Eine Sitzung darf Planung und Apply desselben freigegebenen Changes enthalten.
- `/context` nur am Start langer Blöcke, nach großen Reads oder bei sichtbarem Kontextdruck prüfen.
- Handoff um etwa 75% vorbereiten und spätestens um 85% neu starten.
- Kein Handoff bei bloßem Phasenwechsel oder für kleine abgeschlossene Änderungen.
- Ein Handoff enthält nur Branch, Ziel, Entscheidungen, offene Task-ID, echte Checks, Blocker und nächsten Befehl/Prompt.

## OpenSpec-Kurzreferenz

| Absicht | Codex-Skill | CLI-Kontrolle |
|---|---|---|
| erkunden | `openspec-explore` | `openspec status --change <id> --json` |
| vorschlagen | `openspec-propose` | `openspec validate <id>` |
| umsetzen | `openspec-apply-change` | `openspec status --change <id>` |
| Specs synchronisieren | `openspec-sync-specs` | `openspec validate --specs` |
| abschließen | `openspec-archive-change` | `openspec archive <id>` |

OpenSpec 1.12 synchronisiert beim normalen Archivieren die Delta-Specs. `--skip-specs` nur für Changes ohne Capability-Delta.

## Kompakte Prompts

Normal:

```text
Lies CONTINUATION_PROMPT.md und bearbeite <change-id> bis zum nächsten echten externen Blocker. Plane minimal, implementiere freigegebene Tasks direkt, teste fokussiert und halte tasks.md aktuell. Stoppe bei neuer fachlicher/Security-Entscheidung.
```

Hohes Risiko — kombinierter Review:

```text
Prüfe <change-id> unabhängig auf Scope, Architektur, Security, Ausführbarkeit und Tests. Hänge Findings an und korrigiere nur Planartefakte. Melde READY oder konkrete Blocker; kein separater Readiness-Review danach.
```

## Effizienzregeln

- Dieselbe Quelle innerhalb eines Arbeitsblocks nicht erneut vollständig lesen, solange sie sich nicht geändert hat.
- Toolausgaben nach Pfad, Trefferzahl oder Zeilen begrenzen.
- Recherche parallelisieren, aber serielle Implementierung nicht an Subagents verteilen.
- KI-Nachweis einmal pro substantivem Arbeitsblock synchronisieren; aktuelle Sitzungs-ID im `AUSSTEHEND-*`-Backlog prüfen und nur mit `enrich --session-id` anreichern. Danach nur belegbare, zum Block gehörende Einträge kuratieren. Keine erfundenen Prompts und keine Massenaufarbeitung alter Sitzungen.
- Stärkere Modelle nur für echte Architektur-/Securityentscheidungen, hartnäckige Fehler und Hochrisiko-Reviews einsetzen.
