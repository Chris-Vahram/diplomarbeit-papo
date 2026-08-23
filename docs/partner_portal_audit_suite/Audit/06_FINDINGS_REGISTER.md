# Findings Register

## Status Definitions

- Open — erfasst, aber noch nicht ausreichend bestätigt
- Confirmed — gegen Evidenz bestätigt und noch nicht begonnen
- In Progress — Implementierung läuft; noch nicht abschließend getestet
- Fixed — Implementierung und Fix-Batch-Verifikation erfolgreich; separate Regression Review noch offen
- Verified — durch eine separate Regression Review bestätigt
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
| TECH-001 | Dashboard-Zeitraumfilter verschiebt Grenzdatum um 1 Tag (`toISOString` UTC-Konversion); beide Dashboards, nur bei explizitem Zeitraum | correctness | medium | P1 | Verified | Chris | `03_TECHNICAL_REVIEW_REPORT.md` §3 | — | `540d6fa` | `HISTORY/2026-07-19_REGRESSION_REVIEW_PRE_TERMIN_ORDER_DASHBOARD.md` |
| TECH-002 | Import-Dedupe: In-Datei-Prüfung Default-Locale vs. DB-Prüfung `Locale.ROOT` (Turkish-I-Inkonsistenz) | correctness | low | P4 | Confirmed | Chris | `03_TECHNICAL_REVIEW_REPORT.md` §3 | — | | |
| TECH-003 | `orderValue` precision(10,2) bei Extrem-Menge×Preis überlaufbar → ungraceful 500 statt 400 | correctness | low | P4 | Confirmed | Chris | `03_TECHNICAL_REVIEW_REPORT.md` §3 | — | | |
| SEC-001 | Produktbild-IDs enumerierbar (jeder Partner liest jedes Bild, kein Enablement-Check) — im Code als akzeptiertes Risiko dokumentiert | security | info | P4 | Deferred | Chris | `03_TECHNICAL_REVIEW_REPORT.md` §4 | — | | |
| SEC-002 | Direct-API-Doppel-Submit von Bestellungen nur frontendseitig geguardet (kein Server-Dedupe) | security | info | P4 | Deferred | Chris | `03_TECHNICAL_REVIEW_REPORT.md` §4 | — | | |
| SEC-003 | CSV-Formel-Sanitizer deckt `\t`/`\r` nicht ab (OWASP-erweiterte Menge) | security | low | P4 | Confirmed | Chris | `03_TECHNICAL_REVIEW_REPORT.md` §4 | — | | |
| UX-001 | Harter Reload/Deep-Link auf Listen-Seiten → leere Tabelle (Custom-Element-Upgrade-Race in `router.js`); Fix-Change `fix-ux-reload-and-cart-overlap` proposed | ux / correctness | high | P1 | Verified | Chris | `04_UX_REVIEW_REPORT.md` §2 | — | `e3baab4` | `HISTORY/2026-07-18_REGRESSION_REVIEW_UX_RELOAD_CART.md` |
| UX-002 | Warenkorbzeile: Produktname 0px breit, überlappt Stückpreis (alle Viewports); im selben Fix-Change gescoped | ux | high | P1 | Verified | Chris | `04_UX_REVIEW_REPORT.md` §2 | — | `e3baab4` | `HISTORY/2026-07-18_REGRESSION_REVIEW_UX_RELOAD_CART.md` |
| UX-003 | Endkunden-Dropdown wählt stillschweigend ersten Kunden vor — keine „Bitte wählen…"-Pflichtoption → stille Fehlbuchung möglich | ux / data-quality | medium | P1 | Verified | Chris | `04_UX_REVIEW_REPORT.md` §2 | — | `540d6fa` | `HISTORY/2026-07-19_REGRESSION_REVIEW_PRE_TERMIN_ORDER_DASHBOARD.md` |
| UX-004 | „Meine Bestellungen" sortiert älteste zuerst (`order by o.id` asc); neue Bestellung landet unten | ux | medium | P1 | Verified | Chris | `04_UX_REVIEW_REPORT.md` §2 | — | `540d6fa` | `HISTORY/2026-07-19_REGRESSION_REVIEW_PRE_TERMIN_ORDER_DASHBOARD.md` |
| UX-005 | Import: grüner Erfolgs-Toast bei „0 von 3 importiert"; Bestätigen trotz fehlender Pflicht-Zuordnung klickbar | ux | medium | P2 | Confirmed | Chris | `04_UX_REVIEW_REPORT.md` §2 | — | | |
| UX-006 | Profil-/Import-Formulare ohne max-width (überbreite Felder, ~800px Leerraum im Mapping) | ux / visual | low | P3 | Confirmed | Tobias (FD.4), Chris falls schneller | `04_UX_REVIEW_REPORT.md` §2 | — | | |
| UX-007 | Abgelaufene Session → stiller Login-Redirect ohne „Sie wurden abgemeldet."-Hinweis | ux | low | P3 | Confirmed | Chris | `04_UX_REVIEW_REPORT.md` §2 | — | | |
| UX-008 | Demo-Zugangsdaten hart codiert auf Login-Seite — vor Hetzner-Deployment entfernen/auf dev gaten (Deployment-Gate) | security / deployment | medium | P4 | Confirmed | Chris (koordiniert mit Tobias' ci-cd) | `04_UX_REVIEW_REPORT.md` §2 | — | | |
| M-001 | Mobile: Tabellenspalten enden unsichtbar hinter internem Scroll — keine Affordanz (Badges/Beträge wirken abgeschnitten) | ux / mobile | medium | P3 | Confirmed | Tobias (FD.4) + Chris | `04_UX_REVIEW_REPORT.md` §3 | — | | |
| M-002 | Mobile: Warenkorb unter gesamtem Katalog — skaliert nicht auf reale Sortimente (~800 Artikel); Sticky-Mini-Cart vor erstem Partner-Rollout | ux / mobile | medium | P4 | Confirmed | Chris + Tobias | `04_UX_REVIEW_REPORT.md` §3 | — | | |
| M-003 | Mobile-Kleinigkeiten: Top-Endkunden-Balken degeneriert, Touch-Targets < 44px | ux / polish | low | P3 | Confirmed | Tobias (FD.4) | `04_UX_REVIEW_REPORT.md` §3 | — | | |
| A11Y-001 | Klickbare Tabellenzeilen (Details/Beleg) per Tastatur unerreichbar (kein tabindex/role/Enter-Handler) | accessibility | medium | P3 | Confirmed | Chris | `04_UX_REVIEW_REPORT.md` §4 | — | | |
| A11Y-002 | Primärbutton-Kontrast verfehlt WCAG AA (3,2:1 bzw. 4,1:1) — Token-Entscheidung primary-600/700 | accessibility | medium | P3 | Decision Required | Tobias (Token), Chris (Umsetzung) | `04_UX_REVIEW_REPORT.md` §4 | — | | |
| A11Y-003 | Filter-/Datums-Inputs ohne zugängliche Namen (aria-label fehlt) | accessibility | low | P3 | Confirmed | Chris | `04_UX_REVIEW_REPORT.md` §4 | — | | |
| A11Y-004 | Bildergalerie: Buttons ohne aria-label, kein sichtbarer Selected-State, Tab-Kontrast | accessibility | low | P3 | Confirmed | Chris/Tobias | `04_UX_REVIEW_REPORT.md` §4 | — | | |
| DS-001..004 | Design-System-Inkonsistenzen: leere Metric-Card-Kreise, gemischte €-/％-Formate (kein `Intl.NumberFormat("de-AT")`), fehlendes Token `--color-neutral-0`, Badge-/Button-/Preisformat-Inkonsistenzen | visual | low | P3 | Confirmed | Chris + Tobias (FD.4) | `04_UX_REVIEW_REPORT.md` §6 | — | | |
| DS-005 | Info: `Design/variables.css` lädt Google-Fonts per CDN-@import (nur Design-Quelle stale, kein Runtime-Problem) | docs | info | P3 | Confirmed | Tobias | `04_UX_REVIEW_REPORT.md` §6 | — | | |
| INFRA-001 | Standard-`./mvnw test` lokal deterministisch 71/76 rot (Dev-Services Keycloak Production-Mode + Docker-Desktop-gVisor → „HTTPS required"); Compose-Workaround → 76/76 grün — Workaround dokumentieren oder Netzwerkmodus umstellen | infra / tests | medium | P2 | Confirmed | Chris | `03_TECHNICAL_REVIEW_REPORT.md` §6 | — | | |
| HYG-001 | OpenSpec-Hygiene: 8 fertige Changes unarchiviert (mvp zuerst → entsperrt order-correctness-hardening); `DATA_MODEL.md` re ProductImage stale | docs / process | low | P2 | Confirmed | Chris | `01_PROJECT_OVERVIEW_REPORT.md` §4 | — | | |
| TEST-001 | High-Value-Testlücken: `dashboard-data.js`-Zeitraumfilter (mit TECH-001), FR-25 POST bei 0 Enablements → 400, custom-%-Provision byte-genau, `sanitizeFormula` | tests | low | P3 | Confirmed | Chris | `03_TECHNICAL_REVIEW_REPORT.md` §7 | — | | |
