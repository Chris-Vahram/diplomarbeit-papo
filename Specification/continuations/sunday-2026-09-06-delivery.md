# PAPO — Cold Handoff bis Sonntag, 06.09.2026

**Stand:** 05.09.2026

## Ziel und Arbeitsweise

- Ziel: bestehenden MVP sicher deployen, mit synthetischen Daten prüfen,
  dokumentieren und präsentieren. Keine neue Produktfunktion.
- Setze im neuen Chat zuerst `/goal <objective>` und arbeite danach den
  Gesamtplan bis zum nächsten echten Blocker ab.
- Vollständig spezifizierte Tasks direkt implementieren. Bei fehlender Spec,
  neuer Architektur-/Securityentscheidung oder widersprüchlichem Bestand stoppen
  und stärkeres Modell für Planung/Review verlangen.
- Quellen: `AGENTS.md`, für Claude zusätzlich `CLAUDE.md`,
  `docs/AI_WORKFLOW_EFFICIENCY.md`, `Specification/NEXT_SESSION_PLAN.md` und die
  Artefakte des aktiven OpenSpec-Changes.

## Verifizierter Status

- Branch: `docs/next-session-plan`; Worktree absichtlich dirty. Keine fremden
  Änderungen, `tmp/`, `.claude/`, `CLAUDE.md` oder Tobias-Designarbeit verwerfen.
- Deployment-Change: **41/69**; 5.9–5.10b sind nachgewiesen. Nächster Task:
  5.11.
- MVP-Change: **52/53**; validiert. Nur 8.4 offen und bis Deployment-/Restore-
  Evidenz blockiert.
- Seit letztem Handoff abgeschlossen:
  - Deployment 5.2–5.4/5.6: interimistischer Actions-Key, GitHub-Werte,
    VM-Konfiguration und exakter privater GHCR-SHA-Pull geprüft. Der Key wird
    wegen der zu breiten Rechte in 5.9 widerrufen und nie zum Deploy verwendet.
  - Deployment 3.7: Branch Protection per API mit Pflichtcheck `Test backend`
    und Admin-Enforcement nachgewiesen.
  - Deployment 7.2/7.3: isolierter PATH-Stub-Harness für Erfolg, Dumpfehler,
    Uploadfehler, mode-600-Dateien und Retention 14/DB grün.
  - Deployment 3.5: GitHub UI zeigt GHCR-Package `Private`; anonymer Manifest-
    Request liefert 401.
  - MVP 8.3: `docs/UI_UX.md` erstellt und geprüft.
  - MVP FD.4: Desktop/Mobil-Flows geprüft; Modal-Fokus korrigiert; Chris lieferte
    lesbare einseitige Druckvorschau mit synthetischen Daten.
- PR #5 gegen `main` enthält den gehärteten, weiter deaktivierten Workflow.
  `Test backend` ist grün; Build und Deploy sind im PR übersprungen. Remote
  `main` enthält bis zu einem ausdrücklich freigegebenen Merge die alte Pipeline.

## Nächste Reihenfolge

1. **5.9–5.10b:** erledigt — Interimsschlüssel widerrufen, Forced-Command-
   Gateway lokal geprüft und in PR #5 gepusht; `Test backend` grün,
   Build/Deploy übersprungen. Nicht mergen.
2. **5.11/5.12:** `papo-deploy`, root-owned Gateway/Aktivierung, enges sudoers,
   VM-lokalen read-only Repository-Key einrichten und alle Grenzen ohne Deploy
   praktisch prüfen.
3. Danach **6.3a–6.3d** Preflight, getrennte Aktivierung/Freigabe und ersten
   Release; dann 6.4–6.6, 7.5a–7.7c, 9.1a–9.3b, Deployment-8.3 und 9.4/9.5.
4. Nach echter Smoke-/Restore-Evidenz MVP-8.4 mit der in `tasks.md` festgelegten
   vollständigen Traceability-Matrix erstellen.

## Freigaben und Grenzen

- Chris hat geplante GitHub-/VM-Arbeit aus obiger Reihenfolge am 04.09.2026
  freigegeben. Vor jeder Änderung Zielpfad/System read-only prüfen.
- Keine Secrets, Passwörter, Tokens, private Keys oder S3-/Vaultwarden-Werte in
  Prompt, Git, Logs oder Screenshots.
- Stoppen bei Fingerprint-/SHA-/Owner-Abweichung, unerwarteten VM-Inhalten oder
  Volumes, Datenverlust-/Isolationsrisiko oder neuem Scope.
- Commit, Push, Merge, Deployment-Freischaltung und OpenSpec-Archivierung nur mit
  dafür ausdrücklich dokumentierter Freigabe; bestehende generelle VM-Freigabe
  ersetzt diese Git-Grenze nicht.
- Pro substantivem Block KI-Nachweis sync, aktuelle Sitzungs-ID im passenden
  `AUSSTEHEND-*`-Prompt-Backlog verifizieren und nur mit `enrich --session-id`
  anreichern; keine alte Sitzung ohne Primärbeleg rekonstruieren.

## Readiness-Ergebnis 05.09.2026

- OpenSSH `restrict` allein ist **kein** Command-Allowlisting. Decision 16 ersetzt
  diesen Ansatz durch einen dedizierten Account mit root-owned Forced Command,
  engem Aktivierungsprogramm und Bindung an den aktuellen geschützten `main`-SHA.
- Der frühere Zyklus 3.7/6.3 ist aufgelöst: 3.7 ist per API belegt, 5.10b liefert
  den ungefährlichen PR-Nachweis, 6.3a–6.3d trennen Preflight, Aktivierung,
  Freigabe und Release-Nachweis.
- 6.5, 7.5, 7.7 und 9.1–9.3 sind in ausführbare Teiltasks zerlegt. Deployment-8.3
  folgt erst nach dem praktischen Rollback/Forward-Nachweis 9.3b.
- MVP-8.4 hat feste Quellen, Spalten, Statuswerte und Nachweisvoraussetzungen.
- Ergebnis: **READY für kostengünstigen Apply ab 5.9**. Root-/GitHub-Admin-Zugang,
  Commit/Push, Merge/Deploy und S3-Daten bleiben ausdrücklich externe Gates.

## Bereits ausgeführte Checks

- `openspec validate ci-cd-deployment-with-gh-actions`: grün.
- `openspec validate build-partnerportal-mvp`: grün.
- Backup-Harness: Erfolg/Dumpfehler/Uploadfehler/16 Dumps je DB/Retention grün.
- Browser: Company-/Partner-Flows Desktop und 390×844 px; Druckvorschau manuell
  durch Chris bestätigt.
- KI-Nachweis für Backup-Arbeitsblock synchronisiert/angereichert.
