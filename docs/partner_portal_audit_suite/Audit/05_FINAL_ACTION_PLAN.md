# 05 — Finaler Aktionsplan (Audit-Synthese)

> **Status 2026-08-24:** Batch A und B sind implementiert und regression-reviewed; 77/77 Backendtests sind grün. B-08 ist als `STUB` geklärt und die Forschungsfrage vom Betreuer bestätigt. Die folgenden Prioritäten dokumentieren weiterhin die Audit-Herleitung vom 17.07.; der aktuelle Arbeitsstand steht im Continuation Prompt.

- **Audit-Datum:** 2026-07-17
- **Geprüfter HEAD:** `cd6a025` (main, clean)
- **Phase:** 5 von 5 (Synthese; read-only — kein Produktivcode geändert)
- **Input:** Reports 01–04 als strukturierter Input; alle Critical/High/Medium-Kernaussagen gegen Primärquellen re-verifiziert (Code-Grep, `openspec list`, `git log`, Meeting-Protokoll 10.07., CONTINUATION_PROMPT, CLAUDE.md).

**Neuigkeiten seit Report 04 (2026-07-17):**
1. `add-order-document-print` ist **komplett** (Task 4.5 Firefox-Print-Check erledigt, `8fd9ed1`) → offene Entscheidung #5 aus Report 01 ist **erledigt**.
2. OpenSpec-Change **`fix-ux-reload-and-cart-overlap` (0/9) existiert bereits** (`cd6a025`, Proposal deckt exakt UX-001 + UX-002) → die zwei High-Findings haben schon einen fertigen Fahrplan, nur Implementierung fehlt.

---

## 1. Executive Verdict

**Der MVP ist funktional fertig, technisch überdurchschnittlich sauber und sicher — aber zwei UX-Defekte (leere Tabellen nach Reload, überlappende Warenkorbzeile) müssen vor dem nächsten Kundentermin gefixt werden.** Es gibt **keine Critical- und keine sicherheitsrelevanten High-Findings**: Partner-Isolation, serverseitige Rollen-/Aktiv-Durchsetzung und Geld-/Provisionskorrektheit halten der aktiven Prüfung stand (Report 03 §2, gegen Code re-verifiziert). Scope-Disziplin ist gut („slightly beyond scope", drei kleine, dokumentierte Ausreißer — nichts zurückbauen). Der kritische Pfad zum Termin besteht aus **einem bereits proposeten Fix-Change plus drei Ein-Zeilen-Fixes**; Gesamtaufwand grob ein Arbeitstag.

## 2. Aktuelle MVP-Reife

| Dimension | Stand | Beleg |
|---|---|---|
| Funktional (FR-01..FR-25) | 24/25 complete; FR-04 wartet nur auf echte Kundendaten | Report 01 §3 |
| Sicherheit / Isolation | Keine Critical/High; Isolation, RBAC, Inaktiv-Block, Upload-Härtung bestätigt | Report 03 §2/§4 |
| Finanzkorrektheit | Byte-genau (BigDecimal HALF_UP ↔ `money.js` Integer-Cent); einziger Defekt: Anzeige-Off-by-one im Zeitraumfilter (TECH-001) | Report 03 §3 |
| Tests | Aktuell 77/77 grün; letzter vollständiger Batch-B-Lauf via Compose-Keycloak-Pfad | Batch-B Regression Review |
| UX | Kernfluss trägt; 2 High-Defekte (UX-001/002) + 3 Medium vor Termin sinnvoll | Report 04 |
| OpenSpec-Hygiene | 8 fertige Changes unarchiviert; `DATA_MODEL.md` re ProductImage stale | Report 01 §4 |

**Termin-Readiness: JA nach Batch A+B (unten)** — ohne sie ist der Reload-Bug ein Live-Demo-Risiko.

## 3. Finales Scope-Fazit

Übernahme von Report 02 unverändert (re-verifiziert): **nichts entfernen, nichts zurückbauen.** `add-order-document-print` + `add-product-image-upload` bleiben (geliefert, getestet, Rückbau teurer); Bild-Feature **eingefroren** (kein Cropping/Multi-Bild/Resizing). B-08 ist seit 2026-08-24 als `STUB` dokumentiert; der vorhandene Partner-Dashboard-Trend bleibt. Nächste Implementierungswelle ausschließlich: Audit-Fixes + echte Import-Daten + Termin-Feedback.

## 4. Bestätigte Critical- & High-Findings

Keine Critical. Zwei High (beide UX, beide code-re-verifiziert in dieser Phase):

| ID | Befund | Verifikation Phase 5 |
|---|---|---|
| UX-001 (High) | Harter Reload/Deep-Link auf jeder Listen-Seite → leere Tabelle (Custom-Element-Upgrade-Race) | `router.js:269-281`: ungeordnete parallele Imports bestätigt; Fix-Change `fix-ux-reload-and-cart-overlap` proposed |
| UX-002 (High) | Warenkorbzeile: Produktname 0px, überlappt Stückpreis, alle Viewports | `base.css`: `flex-wrap` nur ≤48em; im selben Fix-Change gescoped |

Höchstes verbleibendes technisches Finding: TECH-001 (Medium) — Dashboard-Zeitraumfilter ±1 Tag; `toISOString().slice(0,10)` in **beiden** Dashboards re-verifiziert (`partner-dashboard-page.js:17`, `company-dashboard-page.js:11`).

## 5. Entfernen / Verschieben / Verwerfen / Entscheiden

- **REMOVE:** nichts.
- **DEFER (Phase 2 / nach MVP):** SEC-001 (Bild-Enumeration, dokumentiert akzeptiert), SEC-002 (API-Doppel-Submit, Idempotenz = Enterprise-Ceremony), TECH-003 (precision-Überlauf, dokumentieren statt fixen), M-002 (Mobile Sticky-Cart — vor erstem Partner-Rollout, nicht vor Termin), SCOPE-002/003 (eingefroren bzw. Prozessregel, keine Arbeit).
- **NO ACTION:** Company-Listen-Seed-Gruppierung, NEU-Badge-Doppelsignal (Report 04 §2, beobachten); Cross-Model-Review (Report 03 §9: nicht nötig, keine Critical/High-Technik-Findings).
- **RESOLVED:** SCOPE-001 — B-08 als `STUB` dokumentiert; Forschungsfrage vom Betreuer bestätigt.
- **DECISION REQUIRED:**
  1. Dormanz-Schwelle 60 Tage bestätigen. **Kunde (nächster Termin).**
  2. Import-Spaltensets/echte Daten — „1–2 Wochen" vom 10.07. ist abgelaufen → nachfassen. **Chris → Kunde.**
  3. A11Y-002 Primärbutton-Kontrast: Token-Entscheidung (primary-600/700). **Tobias.**

## 6. Priorisierter Aktionsplan

Kein P0 (nichts blockiert sofort: App läuft, Tests grün via Workaround, kein Sicherheitsloch).

| Priority | Finding ID | Action | Reason | Owner | Effort | Dependencies | Risk if Deferred | Definition of Done |
|---|---|---|---|---|---|---|---|---|
| P1 | UX-001 | Change `fix-ux-reload-and-cart-overlap` implementieren (Router awaitet Komponenten-Registrierung) | Task-Blocker + Live-Demo-Risiko (F5 = Standard-Reflex nicht-technikaffiner Partner) | Chris | S | keine (Change proposed, 0/9) | Demo bricht sichtbar; Partner halten App für kaputt | Tasks 1.x+3.1/3.2 des Change grün: Direct-Load aller Listen-Seiten rendert Header+Zellen |
| P1 | UX-002 | Cart-Line-Layout im selben Change (`flex-wrap` + `min-width` auf Name) | Kernscreen des vom Kunden gelobten Bestellflusses unlesbar | Chris | XS | mit UX-001 im Change | Demo-Kernscreen wirkt kaputt | Task 2.x+3.3/3.4 grün: keine Überlappung auf 1440/390/1920 |
| P1 | UX-003 | „Bitte wählen…"-Pflichtoption im Endkunden-Dropdown (`order-form.js`) | Stille Fehlbuchung auf falschen Endkunden = Datenqualitäts-Risiko beim echten Einsatz | Chris | XS | keine | Falsch zugeordnete Bestellungen ohne Fehlermeldung | Submit ohne Auswahl → deutsche Fehlermeldung; re-verifiziert per Playwright |
| P1 | UX-004 | Partner-Bestellliste neueste zuerst (`order by o.id desc` bzw. Client-Sort wie Company-Seite) | Häufigster Kontrollblick („wo ist meine Bestellung?") läuft ins Leere | Chris | XS | keine | Verwirrung direkt nach jedem Bestellabschluss | Frische Bestellung erscheint oben; Playwright-Check |
| P1 | TECH-001 | `toIsoDate` beide Dashboards auf manuelle lokale Datums-Komposition umstellen (Muster aus `format.js`) + Unit-Test auf `dashboard-data.js` | Finanz-Anzeige (Provision/Umsatz) bei explizitem Zeitraum um 1 Tag falsch — Ranking-Kriterium 2 (financial correctness) | Chris | S | keine | Kunde sieht falsche Zahlen bei Zeitraum-Demo | Test „lokale Mitternacht 16.07. → '2026-07-16'" grün; beide Dashboards gefixt |
| P1 | — | Import-Daten beim Kunden nachfassen (Frist 10.07.+1–2 Wochen abgelaufen) | FR-04-Abschluss + Termin-Planung hängen daran („dann umsetzen → dann neuen Termin") | Chris | XS | keine | Projektfluss stockt; Termin verschiebt sich | Antwort des Kunden liegt vor (Daten oder neues Datum) |
| P2 | UX-005 | Import-Feedback: Bestätigen-Button gaten solange Pflicht-Zuordnung fehlt; Toast-Ton an Ergebnis koppeln (0 importiert ≠ grün); Fehlertext-Interpunktion | Wird kritisch, sobald echte Kundendateien eintreffen | Chris | S | sinnvoll NACH Eintreffen echter Daten (ein Change mit Spalten-Mapping-Finalisierung) | Anwender fehlinterpretiert Importergebnis mit Echtdaten | 0-Zeilen-Import → Warn-/Fehler-Toast; Button disabled bei fehlender Pflichtspalte |
| P2 | TECH-002 | `ImportService.java:44` → `toLowerCase(Locale.ROOT)` | Konsistenz beider Dedupe-Pfade; Codebasis hat die Regel schon | Chris | XS | mit UX-005-Batch | Nur auf exotischer Server-Locale relevant | Eine Zeile geändert; bestehende Import-Tests grün |
| P2 | INFRA-001 | Keycloak-Dev-Services-Workaround festhalten: dokumentiertes Skript/`%test-compose`-Profil ODER Docker-Desktop gVisor→legacy umstellen | Standard-`./mvnw test` lokal deterministisch rot (72/77) — täglicher Reibungsverlust, Verwechslungsgefahr mit echten Fehlern | Chris | S | keine; NICHT in Feature-Branches mischen | Jeder Testlauf braucht Insider-Wissen; falsche „Tests kaputt"-Schlüsse | Dokumentierter Ein-Befehl-Weg zu 77/77 lokal (docs/SETUP.md o.ä.) |
| P2 | HYG-001 | OpenSpec-Hygiene: `build-partnerportal-mvp` archivieren (nach 8.3/8.4-Klärung) → entsperrt `order-correctness-hardening`; die übrigen 7 fertigen Changes archivieren; `DATA_MODEL.md` re ProductImage nachziehen | Spec = Wahrheitsquelle der Diplomarbeit; Drift wächst | Chris | S | Reihenfolge: mvp zuerst (Spec-Sync) | Doku-Drift, Bewertungsrisiko Diplomarbeit | `openspec list` zeigt nur aktive Changes; `DATA_MODEL.md` enthält ProductImage |
| Done | SCOPE-001 | B-08 als `STUB` dokumentiert; vorhandenen Partner-Dashboard-Trend behalten | Doku ↔ Code-Widerspruch aufgelöst | Chris | XS | — | — | Erledigt 2026-08-24 |
| P3 | A11Y-001 | Klickbare Tabellenzeilen tastaturbedienbar (`tabindex`, `role`, Enter/Space in `data-table.js`) | Details/Beleg per Tastatur unerreichbar; Diplomarbeit-Anspruch | Chris | S | keine | A11y-Lücke in Abgabe-Bewertung | Enter auf fokussierter Zeile öffnet Details |
| P3 | A11Y-002 | Primärbutton auf primary-600/700-Token heben | WCAG-AA-Fail (3,2:1) auf allen Primäraktionen | Tobias (Token), Chris (Umsetzung) | XS | Token-Entscheid §5 | Kontrast-Fail bleibt in jeder Ansicht | Kontrast ≥4,5:1 gemessen |
| P3 | A11Y-003/004 | `aria-label` auf Filter-/Datums-Inputs + Galerie-Buttons; sichtbarer Selected-State Galerie | Kleine, billige A11y-Restposten | Chris | XS–S | keine | gering | DOM-Check: alle Inputs benannt |
| P3 | M-001 | Scroll-Affordanz für Tabellen auf Mobile (Fade-Gradient) und/oder Spalten-Priorisierung | Kundenpriorität „gute mobile Version"; Beträge wirken abgeschnitten | Tobias (FD.4) + Chris | S | FD.4-Lane | Mobile wirkt unfertig | 390px: sichtbarer Hinweis auf weitere Spalten |
| P3 | UX-006/007 + DS-001..004 + Favicon | Polish-Sammelbatch: Formular-`max-width`, Logout-Hinweis, Metric-Card-Icons, `Intl.NumberFormat("de-AT")` für Achsen/Quoten, Badge-/Button-Konsistenz, Preis-Format „89,90", Favicon | Konsistenz/Feinschliff vor Abgabe | Chris + Tobias (FD.4) | M (gesamt) | nach P1/P2 | Nur kosmetisch | Sichtprüfung je Punkt; de-AT-Format einheitlich |
| P3 | TEST-001 | High-Value-Testlücken schließen: FR-25-Resource-Test (POST bei 0 Enablements → 400), custom-%-Provisionstest byte-genau, `sanitizeFormula`-Test | Report 03 §7; billige Absicherung der Kerngarantien | Chris | S | keine | Regressionsrisiko unbemerkt | 3 neue Tests grün im Suite-Lauf |
| P4 | UX-008 | Demo-Zugangsdaten von Login-Seite entfernen/auf dev gaten — **Deployment-Gate, nicht Abgabe-Gate** | Sonst stehen gültige Logins öffentlich auf der echten Hetzner-Instanz | Chris (mit Tobias' ci-cd-Umgebungsschaltung koordinieren) | XS | Tobias' ci-cd-Change | Öffentlich erreichbare Demo-Credentials nach Deployment | Prod-Build zeigt keine Credentials |
| P4 | M-002 | Mobile Sticky-Mini-Cart (vor erstem Partner-Rollout mit echtem Sortiment) | Erst mit realen Kataloggrößen (~800 Artikel) relevant | Chris + Tobias | M | echte Produktdaten | Mobile Bestellfluss unbrauchbar bei 50+ Artikeln | Submit auf 390px ohne Ganz-Seiten-Scroll erreichbar |
| P4 | TECH-003 | `orderValue`-Überlauf: dokumentieren; optional `@DecimalMax` auf Preis | Extremwert-Randfall, Transaktion rollt sauber zurück | Chris | XS | keine | 500 statt 400 im Extremfall | Notiz in Doku bzw. Validierung + Test |
| P4 | SEC-003 | `sanitizeFormula` um `\t`/`\r` erweitern | OWASP-Vollständigkeit, geringer Impact | Chris | XS | mit TEST-001 kombinierbar | gering | Erweiterter Test grün |
| DEFER | SEC-001, SEC-002 | Keine Aktion im MVP (dokumentiert akzeptiert) | Report 03 §4; interne, authentifizierte Nutzer | — | — | — | akzeptiert | Bei Phase-2-Vertraulichkeits-/Volumenänderung neu bewerten |

## 7. Empfohlene Fix-Batches

Reihenfolge berücksichtigt Abhängigkeiten (Scope-Entscheidungen sind gefallen — nichts blockiert die Fixes):

1. **Batch A — „Termin-Blocker" (existiert schon):** `fix-ux-reload-and-cart-overlap` implementieren (UX-001 + UX-002). Branch ab aktuellem main, `/opsx:apply`. ~½ Tag inkl. Playwright-Verifikation.
2. **Batch B — „Pre-Termin-Kleinfixes" (neuer kleiner Change):** UX-003 + UX-004 + TECH-001 (+ Unit-Test `dashboard-data.js`). Drei chirurgische Fixes, ein gemeinsamer Change ist vertretbar (alle „vor Termin", kein Feature, < ½ Tag). Kein Overlap mit Batch A (andere Dateien) — parallelisierbar, aber sequenziell sauberer.
3. **Batch C — „Import-Finalisierung" (wenn echte Daten da):** Spalten-Mapping gegen Echtdaten + UX-005 + TECH-002 in einem Change.
4. **Batch D — „Hygiene" (kein Code):** HYG-001 Archivierung + `DATA_MODEL.md` + SCOPE-001-Backlog-Zeile + INFRA-001-Doku.
5. **Batch E — „A11y & Polish vor Abgabe":** A11Y-001/003/004 + TEST-001 + SEC-003 (Chris); A11Y-002 + M-001 + DS-001..004 in Tobias' FD.4-Lane.
6. **Deployment-Gate (mit Tobias' ci-cd):** UX-008; M-002 vor erstem echten Partner-Rollout.

## 8. Erforderliche Entscheidungen

Siehe §5: offen bleiben (1) Dormanz 60 Tage → Kunde, (2) Import-Daten nachfassen → Chris sofort und (3) Button-Kontrast-Token → Tobias. B-08 und Forschungsfrage sind geklärt. Keine der offenen Entscheidungen blockiert Batch A/B.

## 9. Wenn nur drei Dinge vor dem Termin

1. **`fix-ux-reload-and-cart-overlap` implementieren** (UX-001 + UX-002) — nimmt das einzige Live-Demo-Risiko und den einzigen Task-Blocker vom Tisch; Change liegt fertig proposed.
2. **UX-003 + UX-004 fixen** („Bitte wählen…"-Pflichtoption + Bestellliste neueste zuerst) — zwei Minuten-Fixes gegen stille Fehlbuchung und den häufigsten Verwirrungsmoment direkt nach der Demo-Bestellung.
3. **TECH-001 fixen** (Dashboard-Zeitraum ±1 Tag, beide Dashboards + Unit-Test) — die einzige falsche Geldzahl im Produkt; genau das, was beim Termin gezeigt wird, wenn jemand den Zeitraum-Picker anfasst.

## 10. Audit-Limitationen

- Synthese stützt sich auf Reports 01–04; in Phase 5 re-verifiziert wurden nur die High-/Medium-Kernbefunde (UX-001-Root-Cause in `router.js`, UX-002-CSS, TECH-001 `toISOString`, UX-003 fehlende Leer-Option, UX-004 `order by o.id`) plus Live-Status (`git log`, `openspec list`) — Low/Info-Findings wurden aus den Quellreports übernommen, nicht erneut geprüft.
- Aufwandsschätzungen (XS–M) sind Erfahrungswerte aus den bisherigen Change-Größen, keine gemessenen Stunden.
- `ci-cd-deployment-with-gh-actions` weiterhin bewusst nicht bewertet (Tobias-owned); Deployment-bezogene Punkte (UX-008, CORS/Issuer/Secrets) sind nur als Gates markiert, nicht geprüft.
- Termindatum des nächsten Kundentermins unbekannt (hängt an den Import-Daten) — P1 heißt „vor Termin", faktisch: als Nächstes.
- Kein Produktivcode geändert; Findings-Register (06) und Audit-Index wurden im Anschluss an die Synthese konsistent fortgeschrieben.
