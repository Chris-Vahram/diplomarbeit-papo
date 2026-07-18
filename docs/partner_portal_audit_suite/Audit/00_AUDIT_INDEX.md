# Audit Index

## Current Baseline

- Initial audit date: 2026-07-16
- Baseline commit: `8ef0d90` (letzter Stand vor `add-product-image-upload`, 2026-07-09)
- Current reviewed commit: `cd6a025` (main)
- Latest meeting document: `Documentation/Besprechungsprotokolle/Besprechungsprotokoll10_07_2026.txt`

## Completed Reports

| Phase | Status | Report | Reviewed Commit |
|---|---|---|---|
| Project Overview | Done (2026-07-16) | `Audit/01_PROJECT_OVERVIEW_REPORT.md` | `9a9f5cd` |
| Scope Review | Done (2026-07-16) | `Audit/02_SCOPE_REVIEW_REPORT.md` | `9a9f5cd` |
| Technical Review | Done (2026-07-16) | `Audit/03_TECHNICAL_REVIEW_REPORT.md` | `9a9f5cd` |
| UX Review | Done (2026-07-17) | `Audit/04_UX_REVIEW_REPORT.md` | `9a9f5cd` |
| Final Synthesis | Done (2026-07-17) | `Audit/05_FINAL_ACTION_PLAN.md` | `cd6a025` |

## Finding Summary

| Status | Count |
|---|---:|
| Open | 0 |
| Confirmed | 25 |
| In Progress | 0 |
| Fixed | 0 |
| Verified | 0 |
| Deferred | 2 |
| Rejected | 0 |
| Duplicate | 0 |
| Decision Required | 2 |

Die Zusammenfassung zählt die 29 Registerzeilen; `DS-001..004` bleibt dabei eine konsolidierte Sammelzeile. Alle Findings wurden in Phase 5 im Register (`06_FINDINGS_REGISTER.md`) konsolidiert und im Aktionsplan (`05_FINAL_ACTION_PLAN.md`) priorisiert. **Kein Critical, 2 High** (UX-001 Reload-Leertabelle, UX-002 Cart-Zeilen-Überlappung — Fix-Change `fix-ux-reload-and-cart-overlap` bereits proposed).

## Tools Used (Phase 3)

Vollständiger Backend-Read (55 Klassen), Frontend-Kernmodule, `application.properties`; `./mvnw test` 6 Läufe — Standard-Dev-Services-Pfad 71/76 fail (Root Cause: Dev Services startet Keycloak im Production-Mode + Docker-Desktop-gVisor → „HTTPS required", Report 03 §6), **mit Compose-Keycloak-Workaround 76/76 grün**. `docker inspect`/`ps`/`lsof`/`curl` zur Root-Cause. Read-only, kein Produktivcode geändert.

## Latest Incremental Review

- Date: —
- Baseline commit: —
- Reviewed commit: —
- Report: —

## Next Actions

Vollständige Priorisierung + Batches: `Audit/05_FINAL_ACTION_PLAN.md` (§6/§7). Kurzfassung:

1. **Batch A (P1):** `fix-ux-reload-and-cart-overlap` implementieren (UX-001 + UX-002) — Change proposed, 0/9; Branch von aktuellem main → `/opsx:apply` (mit Sonnet). Muss vor jeder Live-Demo gefixt sein.
2. **Batch B (P1):** UX-003 („Bitte wählen…") + UX-004 (Bestellliste neueste zuerst) + TECH-001 (Dashboard-Zeitraum ±1 Tag, beide Dashboards + Unit-Test) als kleiner gemeinsamer Fix-Change.
3. **Import-Daten nachfassen** (Frist 10.07.+1–2 Wochen abgelaufen) — blockt Batch C (Mapping-Finalisierung + UX-005 + TECH-002) und Terminplanung.
4. **Batch D (Hygiene, kein Code):** OpenSpec-Archivierung (mvp zuerst), `DATA_MODEL.md`, SCOPE-001-B-08-Backlog-Zeile, Dev-Services-Workaround dokumentieren (INFRA-001).
5. **Batch E (vor Abgabe):** A11y (A11Y-001..004) + Test-Lücken (TEST-001) + Polish; Tobias: Kontrast-Token (A11Y-002), M-001, DS-001..004 in FD.4.
6. **Deployment-Gate:** UX-008 (Demo-Credentials von Login-Seite) vor Hetzner-Go-Live; M-002 vor erstem Partner-Rollout.
