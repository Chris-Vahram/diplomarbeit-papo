# 02 — Scope-Discipline-Review

- **Audit-Datum:** 2026-07-16
- **Baseline-Commit:** `8ef0d90` (2026-07-09) · **Geprüfter HEAD:** `9a9f5cd` (main, clean)
- **Phase:** 2 von 5 (read-only; kein Code geändert)
- **Kernfrage:** Baut Chris mehr, als Kunde, letztes Meeting (10.07.2026) oder die freigegebene Spezifikation verlangen?

---

## 1. Scope-Verdikt

**Chris is slightly beyond scope in the following areas:** (1) `add-product-image-upload` — Upload + Galerie gehen über die FSD-Anforderung „optionally … image reference" (FR-03, Z. 157) hinaus und sind laut eigenem Proposal „requested by Tobias, **not a client-meeting requirement**"; (2) `add-order-document-print` — kein Kunden-Ask, sondern eine interne Review-Entscheidung („Direction B Auftragsbeleg", 2026-07-09, von Chris freigegeben). Die damals festgestellte **B-08-Leakage** ist seit 2026-08-24 aufgelöst: Der vorhandene Partner-Dashboard-Trend ist als `STUB` dokumentiert.

Einordnung des Verdikts: Alle drei Punkte sind **klein, dokumentiert, freigegeben bzw. Tobias-authored** — kein Fall von unkontrolliertem Feature-Wildwuchs, keine verdeckte Teilimplementierung von B-01..B-07, keine spekulativen Abstraktionen. Die große Masse der Arbeit seit `8ef0d90` (7 von 10 Changes) ist direkt auf das Meeting vom 10.07., explizite FSD-Anforderungen oder notwendige Korrektheits-/Security-Härtung rückführbar. „Slightly", nicht „materially": die drei Ausreißer zusammen sind < 15 % des Aufwands im Audit-Fenster, alle bereits gemerged und getestet — Rückbau würde mehr kosten als Behalten.

## 2. Change-für-Change-Mapping

| Change | Quelle | Kundennutzen | Klassifikation | Komplexität | Empfehlung |
|---|---|---|---|---|---|
| `add-order-list-filters` | Meeting 10.07 wörtlich („Filter bei Bestellungen nach Endkunde/Partner") + Meeting 07.07 („Filter wichtig!") | hoch | **on-scope** | niedrig (frontend-only, client-side, kein Backend) | behalten |
| `add-dormant-kunde-visibility` | Meeting 10.07 wörtlich („Partner soll Endkunden sehen können, die länger nicht mehr bestellt haben") | hoch | **on-scope** | niedrig–mittel (1 JPQL-Query + 1 Page) | behalten; 60-Tage-Schwelle beim nächsten Termin bestätigen (bewusst hart codiert — korrekt, keine Konfigurations-UI gebaut) |
| `add-order-document-print` | KEIN Kunden-Ask; interne Review-Entscheidung 2026-07-09 (Direction B), von Chris freigegeben | mittel (Pick-Slip für Konfektionierung, Papierbeleg für Partner — plausibel, aber unerfragt) | **polish beyond request** | niedrig–mittel (frontend-only, keine Dependency, Browser-Print) | behalten (geliefert, günstig, echter Nutzwert); Muster künftig rationieren — unerfragte Ideen erst beim Termin anbieten, dann bauen |
| `partner-order-cart` | Meeting 10.07 („Produkt zu Bestellung hinzufügen: übersichtlicher — Suchleiste, Filter") + Chris' explizite Richtungsentscheidung 2026-07-14 („sehen UND suchen") + Mobile-Priorität des Kunden | hoch | **on-scope** | mittel–hoch, aber gerechtfertigt; hat netto Code GELÖSCHT (2 duplizierte Modals, ~250 Zeilen — Konsolidierung absorbiert) | behalten |
| `improve-order-form` | Meeting 10.07 („Suchleiste, Filter") + dokumentierte Review-Entscheidung Q1 (Geld-Vorschau, 2026-07-09) | hoch | **on-scope** (Combobox) + **documented earlier decision** (Geld-Vorschau) | mittel | behalten; Anmerkung §4: Partner-Seite wurde ~1 Tag später vom Cart abgelöst (Rework, kein Scope-Problem — Combobox lebt im Company-Flow weiter) |
| `remove-keycloak-username-field` | Kunden-Ask 07.07 + Chris' Nachtrag 10.07 („Feld unklar/unwichtig") | mittel | **on-scope** | mittel (bewusst als letztes sequenziert, größter Blast-Radius) | behalten |
| `add-keycloak-session-refresh` | Nachtrag 10.07 („man wird schnell ausgeloggt") + Kundenpriorität „einfache Benutzerführung" | hoch | **on-scope / required hardening** | niedrig–mittel (frontend + 1 Realm-Wert) | behalten |
| `self-host-fonts` | Projektregel „no CDN" + Offline-Anforderung Thesis-Demo; Google-Fonts-`@import` war eine Regelverletzung im Bestand | mittel (indirekt) | **required hardening** | niedrig | behalten |
| `order-correctness-hardening` | Review 2026-07-09: 5 echte Defekte (Doppelbestellung per Doppelklick, Quantity-Overflow, case-inkonsistente Duplikatprüfung, XLSX-Header-Shift, Dashboard-Float-Summen gegen archivierte Spec) | hoch (Datenintegrität, Geld) | **required hardening** — per Prompt-Regel ausdrücklich KEIN Scope-Creep | mittel | behalten |
| `add-product-image-upload` | Tobias-Wunsch 2026-07-09; Proposal selbst: „not a client-meeting requirement". FSD FR-03 verlangt nur „optionally … image reference" | mittel (real: ~800 Artikel ohne öffentliche Bild-URLs → Thumbnails blieben sonst leer) | **polish beyond request** (Upload); Galerie = Gold-Plating-Kandidat, §4 | mittel (neue Entity, Multipart, Serving-Endpoint, neue Angriffsfläche → Phase 3) | behalten, aber **einfrieren**: kein Cropping, keine Multi-Bilder, kein Resizing (Proposal scoped das bereits korrekt aus) |

### Weitere Changes im Audit-Fenster (via Git/OpenSpec entdeckt)

| Change | Quelle | Klassifikation | Empfehlung |
|---|---|---|---|
| `close-fsd-order-gaps` (archiviert 07-14) | FSD wörtlich: FR-17 Versandbestätigung, FR-23 Inaktiv-Block | **explizite FSD-Anforderung** | behalten (schloss echte Spec-Lücken) |
| `order-list-efficiency` (archiviert 07-14) | Effizienz-Review (N+1-Queries) | **required hardening**; lehnte Backend-Pagination bewusst AB (~10–15 Orders/Tag) — vorbildliche Scope-Disziplin | behalten |
| `security-hardening-review-fixes` (archiviert 07-14) | Security-Review | **required hardening** | behalten |
| `4ecfa46` netty-macos-DNS-Dependency | lokaler Test-Flake, dokumentiert | **reasonable implementation detail** | behalten |
| Docs-Commits (`974bfc7`, `745c569`, `eff9f4b`, `9a9f5cd`) | Doku-/Status-Sync | on-scope | — |
| `ci-cd-deployment-with-gh-actions` (0/40) | **Tobias-owned — nicht auditiert** (Regel eingehalten; Chris hat nichts daran gebaut, nur VM-Recon-Kontext übergeben) | — | Chris bleibt draußen |

## 3. Mögliches Deferred-Feature-Leakage (B-01..B-08)

- **B-08 „ggü. Vorperiode"-Trend — Finding SCOPE-001, erledigt 2026-08-24.** `frontend/src/lib/dashboard-data.js:58-77` (`previousPeriod`, `trendInfo`) + aktive Nutzung in `partner-dashboard-page.js:204-209`: Trend-Pfeile rendern, sobald ein expliziter Zeitraum gesetzt ist. Der Code stammt aus Tobias' `229a93e` und bleibt bestehen. `phase2_backlog.md` dokumentiert den vorhandenen Partner-Dashboard-Teil nun als `STUB`; vollständige unternehmensweite Trendanalyse bleibt Phase 2.
- **B-01 (externer Shop) — KEIN Leakage durch `partner-order-cart`:** Der Warenkorb ist das interne Kompositionswerkzeug des eingeloggten Partners (Keycloak-Auth, Enablement-gefiltert, Endkunde als Pflicht-Datensatz, keinerlei öffentliche Route). Das Proposal grenzt B-01 explizit ab. Ein „Warenkorb" macht kein Shop-Frontend.
- **B-04 (echter E-Mail-Versand) — kein Leakage:** Print/PDF ist Browser-nativ, kein Versand; Bestätigungen bleiben simuliert; Dormant-View verschickt keine Reminders (im Proposal explizit ausgeschlossen).
- **B-02, B-03, B-05, B-06, B-07 — keinerlei Spuren** in Code oder Changes des Audit-Fensters.

## 4. Gold-Plating & Overengineering

Geprüft gegen die Checkliste (Konfigurierbarkeit, spekulative Abstraktionen, generische Frameworks, Extra-Flows, Fehlerbehandlung für unmögliche Zustände, Duplikate, Wartungslast):

1. **Bildergalerie („pick from previously-uploaded") in `add-product-image-upload`** — der eigentliche Gold-Plating-Kandidat im Fenster: Der Kern-Nutzwert (Datei hochladen statt URL) hätte ohne Galerie-Endpoint + Gallery-UI funktioniert. Begründung „ein Bild für mehrere Produkte wiederverwendbar" ist bei ~800 Artikeln plausibel, aber unbelegt. Geliefert, getestet, klein → **behalten, nicht erweitern** (Finding SCOPE-002). Die Sicherheitsseite (Content-Type-Allowlist, SVG-Verbot, Size-Cap, Company-only) ist notwendige Härtung, kein Plating — Tiefenprüfung in Phase 3.
2. **`improve-order-form` → `partner-order-cart` Rework** — die Partner-Combobox wurde tage-später vom Cart abgelöst. Kein Scope-, sondern ein Sequenzierungs-Befund: Der 10.07-Ask wurde zweimal beantwortet (minimal, dann richtig). Verlust begrenzt (Combobox trägt weiter den Company-Flow; Kunde-Picker, Summen-Logik, `money.js`-Anbindung wurden wiederverwendet). Keine Aktion; Lehre: bei UX-Asks erst Zielbild mit Chris klären, dann bauen.
3. **Positiv (dokumentierte Zurückhaltung, explizit anzuerkennen):** keine Dormanz-Schwellen-Konfiguration (hart 60 Tage); keine Backend-Pagination bei ~10–15 Orders/Tag (bewusst abgelehnt); kein `data-table`-Umbau für Sortierung (page-lokal gelöst, Generalisierung „deferred on review"); Cart ohne Tab-Persistenz; Direct-Order = 1-Item-Cart (ein Mechanismus, kein Extra-Flow); URL-Hash-Filterpersistenz als „out of scope by default" markiert; Print ohne Library/Backend; `consolidate-partner-order-creation` absorbiert statt separat gebaut (netto ~250 Zeilen gelöscht). Kein generisches Framework, keine spekulative Abstraktion, keine unnötige Konfigurierbarkeit gefunden.

## 5. Arbeit, die bleiben soll

Alles Gemergte. Konkret: alle 7 meeting-/FSD-/härtungsgetriebenen Changes uneingeschränkt; `add-order-document-print` und `add-product-image-upload` trotz „beyond request" (geliefert, klein, real nützlich, Rückbau teurer als Behalten); B-08-Trendcode (funktioniert, client-seitig, exakt der im Backlog vorgezeichnete Weg).

## 6. Arbeit, die vereinfacht, verschoben oder entfernt werden soll

**Nichts entfernen.** Drei Pflege-/Entscheidungspunkte:

1. **SCOPE-001 (erledigt 2026-08-24):** B-08-Backlog-Drift durch `STUB`-Dokumentation aufgelöst; Code bleibt bestehen.
2. **SCOPE-002 (Confirmed, low):** `product-images` einfrieren — keine Erweiterungen (Cropping, Multi-Bild, Resizing bleiben out of scope wie im Proposal). Kein Rückbau.
3. **SCOPE-003 (Confirmed, low, Prozess):** Künftige unerfragte Ideen (Muster `order-document-print`) vor dem Bauen als Vorschlag beim Kunden-Termin platzieren. Die nächste Implementierungswelle sollte ausschließlich von den echten Import-Daten (ausständig seit 10.07) und Termin-Feedback getrieben sein.

## 7. Konfidenz & Limitationen

- **Konfidenz: hoch** für die Quellen-Zuordnung (jedes Proposal nennt seine Quelle; gegen Besprechungsprotokoll 10.07, FSD v2 FR-01..FR-25, `phase2_backlog.md` inkl. Decisions-Log und Git-Log seit `8ef0d90` verifiziert; B-08-Code + Autorschaft direkt per Grep + `git log -S trendInfo` geprüft; FR-03-Wortlaut „image reference" direkt nachgelesen).
- **Limitationen:** Klassifikation auf Proposal-/Design- und Stichproben-Code-Ebene — kein Zeilen-Diff-Audit jedes Changes (Phase 3). Aufwandsanteile („< 15 %") sind Schätzungen aus Change-Umfang/Task-Zahlen, nicht gemessene Stunden. Der Overview-Report wurde als strukturierter Input behandelt; alle übernommenen Kernaussagen (B-08-Herkunft, Image-Upload-Proposal-Selbstauskunft, Tobias-Ownership ci-cd) wurden an Primärquellen gegengeprüft. `ci-cd-deployment-with-gh-actions` bewusst nicht inspiziert (Tobias-Regel). Kein Produktivcode geändert.
