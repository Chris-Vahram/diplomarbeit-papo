# Findings Register

## Status Definitions

- Open
- Confirmed
- In Progress
- Fixed
- Verified
- Deferred
- Rejected
- Duplicate
- Decision Required

## Findings

| ID | Title | Category | Severity | Priority | Status | Owner | Source Report | Introduced Commit | Fix Commit | Verification Report |
|---|---|---|---|---|---|---|---|---|---|---|
| SCOPE-001 | B-08-Backlog-Drift: „ggü. Vorperiode"-Trend im Code aktiv, Backlog sagt CUT — Backlog auf STUB updaten oder Code entfernen | scope | low | P3 | Decision Required | Chris | `02_SCOPE_REVIEW_REPORT.md` §3 | `229a93e` (Tobias) | | |
| SCOPE-002 | `product-images` (Upload+Galerie) über FSD FR-03 „image reference" hinaus; Team-Wunsch, kein Kunden-Ask — behalten, aber einfrieren (kein Cropping/Multi-Bild/Resizing) | scope | low | P4 | Confirmed | Chris | `02_SCOPE_REVIEW_REPORT.md` §2/§4 | `75ae88a` | | |
| SCOPE-003 | Prozess: unerfragte Ideen (Muster `add-order-document-print`) künftig erst beim Kunden-Termin anbieten, dann bauen | scope / process | low | P4 | Confirmed | Chris | `02_SCOPE_REVIEW_REPORT.md` §6 | `032ec50` | | |
| TECH-001 | Dashboard-Zeitraumfilter verschiebt Grenzdatum um 1 Tag (`toISOString` UTC-Konversion); beide Dashboards, nur bei explizitem Zeitraum | correctness | medium | P2 | Confirmed | Chris | `03_TECHNICAL_REVIEW_REPORT.md` §3 | — | | |
| TECH-002 | Import-Dedupe: In-Datei-Prüfung Default-Locale vs. DB-Prüfung `Locale.ROOT` (Turkish-I-Inkonsistenz) | correctness | low | P4 | Confirmed | Chris | `03_TECHNICAL_REVIEW_REPORT.md` §3 | — | | |
| TECH-003 | `orderValue` precision(10,2) bei Extrem-Menge×Preis überlaufbar → ungraceful 500 statt 400 | correctness | low | P4 | Confirmed | Chris | `03_TECHNICAL_REVIEW_REPORT.md` §3 | — | | |
| SEC-001 | Produktbild-IDs enumerierbar (jeder Partner liest jedes Bild, kein Enablement-Check) — im Code als akzeptiertes Risiko dokumentiert | security | info | P4 | Deferred | Chris | `03_TECHNICAL_REVIEW_REPORT.md` §4 | — | | |
| SEC-002 | Direct-API-Doppel-Submit von Bestellungen nur frontendseitig geguardet (kein Server-Dedupe) | security | info | P4 | Deferred | Chris | `03_TECHNICAL_REVIEW_REPORT.md` §4 | — | | |
| SEC-003 | CSV-Formel-Sanitizer deckt `\t`/`\r` nicht ab (OWASP-erweiterte Menge) | security | low | P4 | Confirmed | Chris | `03_TECHNICAL_REVIEW_REPORT.md` §4 | — | | |
