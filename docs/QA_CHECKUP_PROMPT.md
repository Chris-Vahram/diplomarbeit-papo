# Partnerportal — Vollständiger Qualitäts- & Scope-Checkup (vor dem nächsten Termin)

Du bist Fable 5 und führst einen gründlichen, ehrlichen Audit des Partnerportals (HTL-Diplomarbeit von Chris + Tobias) durch. Du hast das volle Werkzeug: Skills, Subagents, Playwright, Bash, und die Codex-CLI zum Gegenprüfen. Dies ist ein **Report-Auftrag, kein Fix-Auftrag** — du änderst keinen Produktivcode, außer Tests auszuführen. Am Ende entscheidet Chris, was umgesetzt wird.

**Kein Plan-Mode, keine Approval-Zeremonie:** Dieser Audit ist read-only und produziert nur einen Report — er editiert nichts. Geh NICHT über EnterPlanMode/ExitPlanMode. Erde dich (§0) und fahre den Audit dann direkt aus.

Sprich und schreibe den Report auf klarem Deutsch (de-AT), technische Begriffe exakt.

## 0. Erst erden (bevor du irgendwas bewertest)
**Primäres Onboarding-Dokument: `Specification/CONTINUATION_PROMPT.md`** — lies es ZUERST und vollständig. Es ist der Master-Kontext (aktueller Stand, alle gebauten Changes mit Begründung, gelockte Entscheidungen, Team-Split, und die DEV-GOTCHAS-Sektion, die du im ganzen Audit respektieren musst: Playwright-bfcache/about:blank-Bounce, Port-5432-Konflikt, Keycloak-Flake-Retry, Geld=Integer-Cent, Umlaut-Pflicht, `ci-cd`-Change = Tobias). Alles Weitere unten ergänzt es.

Dann in dieser Reihenfolge, halte dich strikt an die dort gesetzte Scope:
1. `Specification/CONTINUATION_PROMPT.md` — Master-Kontext + Dev-Gotchas (s.o., zuerst gelesen).
2. `CLAUDE.md` (Repo-Root) — locked scope, Entscheidungen, Rollen, Entities.
3. `Specification/functional_specification_document_partnerportal_final.md` — FSD v2, MVP FR-01..FR-25.
4. `Specification/phase2_backlog.md` — zurückgestellte Features B-01..B-07 (dürfen NICHT im MVP sein) + Decisions-Log.
5. `Documentation/Besprechungsprotokolle/Besprechungsprotokoll10_07_2026.txt` — NEUESTE Kundenwünsche, überschreibt die FSD bei Konflikt.
6. `openspec/changes/` — `openspec list` für Live-Status.

Hard rules aus dem Projekt, die du im ganzen Audit respektierst: Deutsch de-AT überall (UI + Fehlermeldungen); Geld = BigDecimal/Integer-Cent, HALF_UP, `money.js`/`commissionCentsHalfUp` ist kanonisch; keine neuen Dependencies, kein CDN; Umlaute (ä/ö/ü/ß) sind Pflicht in jeder Text-I/O; Kunde ist auf den anlegenden Partner gescoped (ein Partner sieht NIE fremde Endkunden); Tech-Stack fix (native Web Components, Quarkus, Postgres, Keycloak). **Die Change `ci-cd-deployment-with-gh-actions` gehört Tobias — nicht anfassen, nicht auditieren.**

## 1. HAUPTFRAGE — Scope-Disziplin (das ist die wichtigste Sektion)
Chris' explizite Sorge: „Baue ich mehr, als im letzten Meeting verlangt war?" Beantworte das rigoros, nicht aus dem Bauch.

Nimm die zuletzt gebauten Changes (via `openspec list` + `git log --oneline -30`): u.a. `add-order-list-filters`, `add-dormant-kunde-visibility`, `add-order-document-print`, `partner-order-cart`, `improve-order-form`, `remove-keycloak-username-field`, `add-keycloak-session-refresh`, `self-host-fonts`, `order-correctness-hardening`, `add-product-image-upload`.

Für JEDEN dieser Changes: mappe ihn auf entweder (a) einen konkreten 10.07.-Meeting-Ask, (b) ein FSD-FR, (c) eine dokumentierte frühere Entscheidung, oder (d) markiere ihn als Scope-Creep / Gold-Plating / verkappte B-01..B-07-Arbeit. Gib eine Tabelle aus: `Change → Begründung/Quelle → Verdikt (on-scope | polish-über-ask | creep | verletzt-MVP-scope)`.

Prüfe zusätzlich: Ist irgendwo ein zurückgestelltes Feature (B-01..B-07) versehentlich teilweise reingebaut worden? Ist irgendein Feature komplexer als der Kunde/die FSD verlangt (unnötige Konfigurierbarkeit, Abstraktion für Einmal-Code, Fehlerbehandlung für unmögliche Fälle)?

**Liefere ein klares Headline-Urteil**: „Chris ist im Scope / leicht drüber bei X / deutlich drüber bei Y" — mit Begründung.

## 2. Korrektheit / Bugs
Fokus auf die Diffs der letzten Changes. Nutze die Subagents `java-reviewer`, `silent-failure-hunter`, `database-reviewer` und den Skill `/code-review` (auf `git diff` gegen einen sinnvollen Basis-Commit). Achte besonders auf:
- Geld/Provision: Integer-Cent-Exaktheit, HALF_UP, Rundungs-Kanten, `CommissionRecord`-Snapshot vs. Live-Berechnung, custom-commissionPercent auf krummen Summen.
- Order-Flow: Cart-Quantity-Clamp (`@Min(1) @Max(10000)`, client + server), Doppel-Submit-Guard, OrderItem-Unit-Price-Snapshot.
- Partnernummer-Kanonisierung (`PartnerNumber.canonicalize()`, upper-case Locale.ROOT) auf allen Schreibpfaden inkl. Import.
- Dormant-Kunde-Query (Left-Join, kein N+1 — mit Hibernate-Statistics gegenprüfen), 60-Tage-Grenze exakt.
- Print/Beleg-Pagination (mehrseitig, `#print-root`-Klon-Mechanismus).

## 3. Security
Skill `/security-review` + Subagent `security-reviewer`. Prüfe: Rollen-Scoping serverseitig (Partner kann via API NICHT an fremde Kunden/Orders/Commission — teste das aktiv, nicht nur lies den Code), `active`-Flag-Enforcement nach Auth, XSS in gerenderten Feldern, CSV-Injection im Import, Secret-Handling, Keycloak redirect/webOrigins.

## 4. Frontend / UX / Design
Skill `frontend-design`. Vergleiche die NEUEN Screens gegen das Design-System (`Design/variables.css` Tokens v1.1 + `frontend/src/styles/base.css`): Produktkatalog+Warenkorb (`/partner/orders/new`), „Meine Endkunden", Auftragsbeleg-Druck, Order-Filter, Bildergalerie. Suche nach:
- Undesignten/inkonsistenten Buttons, uneinheitlichen Abständen, Farben außerhalb der Tokens, hardcodierten Werten statt CSS-Variablen.
- Komischer UX für **nicht-technikaffine Partner** (der Kunde hat das explizit betont): unklare Beschriftungen, versteckte Aktionen, zu viele Schritte, fehlendes Feedback.
- **Beide Viewports gleichwertig prüfen** — fahre jeden Kern-Flow mit Playwright einmal auf **Desktop-Fullscreen** (z.B. 1920×1080 und zusätzlich 1440×900) UND einmal **Mobile** (z.B. 390×844):
  - Desktop-Fullscreen: keine ins Leere laufenden Layouts / über die ganze Breite gezogene Formulare ohne max-width, überbreite Tabellen, leere Flächen, falsch zentrierte Inhalte, Katalog-Karten-Raster das bei viel Breite bricht. Das ist der primäre Nutzungsmodus der Firma (Partner am Desktop) — nicht vernachlässigen.
  - Mobile (10.07.-Ask „gute Mobile-Version"): Umbrüche, horizontales Scrollen, zu kleine Touch-Ziele, abgeschnittene Beträge/Provisions-Vorschau.
- Umlaut-Rendering in allen neuen Texten/Feldern.

Playwright-Setup: Dev-Stack hochfahren (falls `my_postgres` Port 5432 blockt: `docker stop my_postgres` zuerst; dann docker-compose Keycloak+Postgres, `cd backend && ./mvnw quarkus:dev`, Frontend statisch servieren). **Gotcha:** `page.goto()` auf dieselbe URL kann bfcache-No-op sein — immer erst `about:blank`, dann Ziel-URL. Demo-Logins: `company-demo` / `partner-demo`.

## 5. Test-Abdeckung
`./mvnw test` laufen lassen (Docker up; der Keycloak-Dev-Services-Flake auf macOS ist bekannt — bei Timeout einmal retryen). Prüfe: Sind die letzten Changes getestet? Gibt es Umlaut-Tests wo Text-I/O berührt wurde? Geld-Grenzwerte? Fehlende Unhappy-Path-Tests? Liste konkrete Lücken mit Datei-Vorschlag.

## 6. Projekt-Regel-Konformität
Skill `partnerportal-quality` auf den Gesamt-Diff der letzten Changes: Deutsch überall, Geld-Handling, keine neuen Deps/CDN, MVP-Scope-Disziplin, Rollen-Scoping.

## 7. OpenSpec-Hygiene
`openspec list` + validate: unarchivierte fertige Changes, Spec-Drift, offene Tasks die faktisch erledigt sind (oder umgekehrt).

## CROSS-CHECK mit Codex (automatisch, für jedes nicht-triviale Finding)
Für die substanziellen Findings (v.a. Scope-Urteil §1, Bugs §2, Security §3): verifiziere gegen Codex. Muster (macOS, kein `timeout` verfügbar — im Hintergrund + pollen):
```
# Diff/Kontext in eine Datei schreiben, kurzen gezielten Prompt bauen, dann:
nohup codex exec -m gpt-5.6-sol -c model_reasoning_effort="high" "<kurzer, gezielter Prompt: Stack in 2 Sätzen, was geändert, 3-4 konkrete Fokusfragen, 'nur file:line-Findings, kein Lob'>" > /tmp/codex-out.txt 2>&1 & echo $!
```
Dann per `Monitor`-Tool `until ! kill -0 <pid> 2>/dev/null; do sleep 5; done` mit gebundenem Timeout warten (nicht blockierend im Vordergrund). Wenn Codex einem Finding widerspricht: nicht stillschweigend übernehmen, sondern die Diskrepanz im Report vermerken. Halte die Codex-Prompts KURZ und gezielt (lange/breite Prompts haben Codex schon mal 25 min hängen lassen).

## OUTPUT — ein einziger priorisierter Report
Struktur, severity-sortiert innerhalb jeder Sektion:
1. **Scope-Urteil** (die Headline-Antwort auf „bin ich zu viel?" — zuerst, weil es Chris' Kernfrage ist).
2. **Echte Bugs** (must-fix) — je: `Datei:Zeile`, was, warum es bricht, Fix-Vorschlag.
3. **Security-Findings.**
4. **UX/Design-Politur** (inkl. Mobile).
5. **Test-Lücken.**
6. **Nice-to-have / später.**
7. **„Wenn du vor dem Termin nur 3 Dinge machst"** — die 3 höchsten-Hebel-Punkte.

Für jedes Finding: Confidence + ob Codex es bestätigt/bestritten hat. Kein Auto-Fix. Bleib read-only außer Tests. Wenn etwas außerhalb der geplanten Scope auftaucht (unerwartete Datei, UX-Entscheidung), STOP und frag Chris, statt still zu entscheiden.
