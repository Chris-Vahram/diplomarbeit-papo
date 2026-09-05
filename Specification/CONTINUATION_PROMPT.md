# Continuation Prompt — PAPO bis 06.09.2026

**Stand:** 05.09.2026

```text
Setze zuerst ein klares Arbeitsziel mit:
/goal <objective>

Ziel: Arbeite `Specification/NEXT_SESSION_PLAN.md` bis zum nächsten echten
externen Blocker ab. Implementiere nur Tasks, die durch Proposal, Design,
Capability-Spec und tasks.md vollständig spezifiziert und ausführbar sind.
Wenn ein Task nicht vollständig geplant ist oder eine neue fachliche,
Architektur- oder Securityentscheidung benötigt, stoppe vor Codeänderungen und
sage Chris ausdrücklich, dass für Planung/Review auf ein stärkeres Modell
gewechselt werden soll. Ansonsten iteriere weiter: implementieren, fokussiert
prüfen, tasks.md aktualisieren, nächsten unabhängigen Task übernehmen.

Start:
1. Lies `AGENTS.md`; als Claude zusätzlich `CLAUDE.md`.
2. Lies `docs/AI_WORKFLOW_EFFICIENCY.md`, `Specification/NEXT_SESSION_PLAN.md`
   und `Specification/continuations/sunday-2026-09-06-delivery.md`.
3. Prüfe `git status --short --branch`, `openspec list --json`, danach Status
   und Apply-Instruktionen nur für den aktiven Change.
4. Lies alle vom Apply-Workflow genannten Change-Artefakte. `tasks.md` ist die
   Fortschrittsquelle; nur vollständig nachgewiesene Tasks abhaken.

Aktueller Stand:
- `ci-cd-deployment-with-gh-actions`: 41/69. Tasks 5.9, 5.10a und 5.10b
  sind nachgewiesen; nächster Task ist 5.11.
- `build-partnerportal-mvp`: 52/53. Nur 8.4 offen; blockiert bis echte
  Deployment-Smoke-/Backup-Restore-Evidenz vorliegt.
- GHCR ist privat; anonymer Manifest-Request ergab 401 und der exakte private
  SHA-Pull auf der VM gelang. Tasks 3.5 und 5.4 sind erledigt.
- Tasks 5.2/5.3 wurden praktisch ausgeführt. Der Review zeigte danach: OpenSSH
  `restrict` sperrt keine beliebigen Remote-Kommandos. Der interimistische
  Actions-Key ist deshalb in 5.9 zu widerrufen und darf nie deployen.
- VM-Konfiguration/Releases (5.6) und Branch Protection (3.7) sind nachgewiesen.
- Backup-Harness 7.2/7.3 ist grün. UI-Doku 8.3 und FD.4 inklusive einseitiger
  Druckvorschau sind erledigt.
- Deploy bleibt `if: false`, bis alle Voraussetzungen von 6.3 erfüllt sind.

Routing:
- Deployment: PR #5 enthält den widerrufenen Interimsschlüssel sowie den
  deaktivierten Forced-Command-Gateway. `Test backend` ist grün; Build und
  Deploy sind im PR übersprungen. Nicht mergen. Als Nächstes 5.11 die
  VM-Grenze einrichten, dann 5.12 ohne Deploy beweisen.
- Danach 6.3a–6.3d, 6.4–6.6, 7.5a–7.7c, 9.1a–9.3b,
  Deployment-8.3 und 9.4/9.5 exakt nach `tasks.md`.
- Chris hat am 04.09.2026 die im genehmigten Plan beschriebenen GitHub-/VM-
  Arbeiten freigegeben. Trotzdem nie Credentials ausgeben; bei Fingerprint-,
  Owner-, Rechte-, Volume- oder Datenabweichung sofort stoppen.
- Commit, Push, Merge, Deploy-Aktivierung und Archivierung bleiben ohne separate
  ausdrückliche Freigabe verboten.
- Präsentation, Demo, Clockify und KI-Nachweise gemäß Gesamtplan fertigstellen.

Modellregel:
- Aktuelles kosteneffizientes Coding-Modell für vollständig spezifizierte
  Implementierung, Tests und Dokumentation.
- Starkes Reasoning-Modell für neue Planung/Architektur/Securityentscheidung,
  hartnäckige Fehler sowie finalen Security-/Diff-Review 9.5.

Checks:
- Fokussierter Check je Task; vollständige Backend-Suite/Package erst 9.4.
- OpenSpec nach Artefakt-/Taskänderung validieren.
- Pro substantivem Arbeitsblock KI-Nachweis synchronisieren, aktuelle Sitzungs-ID im `AUSSTEHEND-*`-Prompt-Backlog verifizieren und nur mit `enrich --session-id` anreichern; keine erfundene Rückschau.
```

Direkte Quellen:

- `Specification/NEXT_SESSION_PLAN.md`
- `Specification/continuations/sunday-2026-09-06-delivery.md`
- `docs/AI_WORKFLOW_EFFICIENCY.md`
- `openspec/changes/ci-cd-deployment-with-gh-actions/`
- `openspec/changes/build-partnerportal-mvp/tasks.md`
- `Documentation/Besprechungsprotokolle/Besprechungsprotokoll31_08_2026.md`
- `presentation/mvp-deck/`
