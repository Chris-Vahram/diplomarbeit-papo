# Audit Index

## Current Baseline

- Initial audit date: 2026-07-16
- Baseline commit: `8ef0d90` (letzter Stand vor `add-product-image-upload`, 2026-07-09)
- Current reviewed commit: `9a9f5cd` (main)
- Latest meeting document: `Documentation/Besprechungsprotokolle/Besprechungsprotokoll10_07_2026.txt`

## Completed Reports

| Phase | Status | Report | Reviewed Commit |
|---|---|---|---|
| Project Overview | Done (2026-07-16) | `Audit/01_PROJECT_OVERVIEW_REPORT.md` | `9a9f5cd` |
| Scope Review | Done (2026-07-16) | `Audit/02_SCOPE_REVIEW_REPORT.md` | `9a9f5cd` |
| Technical Review | Done (2026-07-16) | `Audit/03_TECHNICAL_REVIEW_REPORT.md` | `9a9f5cd` |
| UX Review | Done (2026-07-17) | `Audit/04_UX_REVIEW_REPORT.md` | `9a9f5cd` |
| Final Synthesis | Not started | `Audit/05_FINAL_ACTION_PLAN.md` | |

## Finding Summary

| Status | Count |
|---|---:|
| Open | 0 |
| Confirmed | 5 |
| In Progress | 0 |
| Fixed | 0 |
| Verified | 0 |
| Deferred | 0 |
| Rejected | 0 |
| Decision Required | 1 |

Formale Findings (IDs): SCOPE-001..003 (Phase 2) + TECH-001..003 / SEC-001..003 (Phase 3) + UX-001..008 / M-001..003 / A11Y-001..004 / DS-001..005 (Phase 4). Phase 4 bringt **2 High** (UX-001 Reload-Leertabelle, UX-002 Cart-Zeilen-Überlappung — beide vor dem nächsten Termin fixen). Phase-1-Auffälligkeiten in `01_PROJECT_OVERVIEW_REPORT.md` §4–§7. Zählung oben wird in Phase 5 (Register) konsolidiert.

## Tools Used (Phase 3)

Vollständiger Backend-Read (55 Klassen), Frontend-Kernmodule, `application.properties`; `./mvnw test` 6 Läufe — Standard-Dev-Services-Pfad 71/76 fail (Root Cause: Dev Services startet Keycloak im Production-Mode + Docker-Desktop-gVisor → „HTTPS required", Report 03 §6), **mit Compose-Keycloak-Workaround 76/76 grün**. `docker inspect`/`ps`/`lsof`/`curl` zur Root-Cause. Read-only, kein Produktivcode geändert.

## Latest Incremental Review

- Date: —
- Baseline commit: —
- Reviewed commit: —
- Report: —

## Next Actions

1. Phase 5 Final Synthesis (`Audit/prompts/05_FINAL_SYNTHESIS_PROMPT.md`) — Register konsolidieren, P0–P4, „3 Dinge vor dem Termin".
2. **UX-001 + UX-002 fixen:** Change **`fix-ux-reload-and-cart-overlap` ist bereits proposed** (proposal+design+specs+tasks, Root-Cause in dessen `design.md`) — **noch NICHT applied**. Entscheidung Chris 2026-07-17: als **erster Fix-Batch nach Phase 5** applien, aber **UX-001 (Reload → leere Listen) muss vor jeder Live-Demo beim Termin gefixt sein** (Task-Blocker). Branch `fix/ux-reload-and-cart-overlap` von aktuellem main → `/opsx:apply` (mit Sonnet). Report 04 §2.
3. TECH-001 fixen (Chris): Dashboard-Zeitraumfilter Off-by-one (`toISOString` → manuelle lokale ISO-Komposition), beide Dashboards, + Unit-Test in `dashboard-data.js`.
4. Dev-Services-Testlauf reparieren (kleiner separater Change): Docker-Desktop-Netzwerkmodus prüfen oder Compose-Keycloak-Workaround dokumentieren (Report 03 §6; Suite via Workaround bereits 76/76 grün bestätigt).
5. SCOPE-001 entscheiden (Chris): B-08-Backlog auf STUB updaten (empfohlen, 1 Zeile) oder Trend-Code entfernen.
6. Import-Daten beim Kunden nachfassen (seit 10.07 ausständig).
