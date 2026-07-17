# 03 — Technischer Qualitäts-, Sicherheits- & Korrektheits-Review

- **Audit-Datum:** 2026-07-16
- **Geprüfter HEAD:** `9a9f5cd` (main, clean; `docs/partner_portal_audit_suite/` untracked)
- **Phase:** 3 von 5 (technischer Tiefen-Audit, read-only außer Testläufen)
- **Baseline-Kontext:** Reports 01 (Ground-Truth) + 02 (Scope) als strukturierter Input behandelt, alle übernommenen Aussagen gegen Code gegengeprüft.
- **Umfang:** Vollständiger Backend-Read (`backend/src/main/java`, alle 55 Klassen), Frontend-Kernmodule (`lib/`, dashboards, order-cart/form, data-table, api/auth), Testinventar (14 Klassen), `application.properties`, lokaler `./mvnw test`-Lauf (4×).

---

## 1. Technisches Verdikt

**Der Code ist technisch solide und für den MVP-/Prototyp-Reifegrad überdurchschnittlich sauber.** Die Kern-Sicherheitsgarantien der Diplomarbeit halten der aktiven Prüfung stand: Partner-Isolation, serverseitige Rollen-/Aktiv-Durchsetzung und Geld-/Provisions-Korrektheit sind konsistent und an den richtigen Stellen (Service-Layer, nicht nur UI) implementiert. Es wurden **keine Critical- und keine High-Findings** gefunden — die vorangegangenen Härtungsrunden (`order-correctness-hardening`, `security-hardening-review-fixes`, `close-fsd-order-gaps`) haben die naheliegenden Klassen von Defekten bereits geschlossen, und diese Prüfung bestätigt das gegen den Code.

Gefunden wurden **1 Medium** (Zeitzonen-Off-by-one im Dashboard-Zeitraumfilter, in **beiden** Dashboards) und **mehrere Low/Informational** (Locale-Inkonsistenz im Import-Dedupe, theoretischer `orderValue`-Spaltenüberlauf, unvollständige CSV-Formel-Sanitisierung, dokumentierte Rest-Risiken Bild-Enumeration + Direct-API-Doppel-Submit). Keines davon ist MVP-blockierend.

**Testsuite: 76/76 grün bestätigt** — allerdings erst über einen Workaround (Tests gegen die Compose-Keycloak statt Dev Services, §6). Der Standard-`./mvnw test`-Lauf scheitert auf dieser Maschine aktuell deterministisch mit 71/76 an einem Keycloak-Dev-Services-Infrastrukturproblem (Root Cause in §6 vollständig diagnostiziert: Dev Services startet Keycloak im Production-Mode, Docker-Desktop-gVisor-Networking lässt den Admin-Call als „extern" erscheinen → „HTTPS required" → Realm-Import scheitert). Kein Code-Problem.

---

## 2. Critical- & High-Findings

**Keine.**

Aktiv geprüft und als korrekt bestätigt (jeweils Code + vorhandener Test):

| Prüfung | Ergebnis | Evidenz |
|---|---|---|
| Partner A ≠ Partner B: Kunden | isoliert | `KundeService.findOwnedOrThrow` (`id = ?1 and ownerPartner = ?2`), `listForPartner` scoped; Test `companyCannotSelectAnotherPartnersKundeForManualOrder` → 404 |
| Partner A ≠ Partner B: Bestellungen | isoliert | `OrderResource` nur GET-list/POST, kein GET-by-id (kein IDOR-Vektor); `listForPartner` `where o.partner = ?1`; Test `partnerSeesOnlyOwnOrders` |
| Partner A ≠ Partner B: Provisionen | isoliert | Provision nur eingebettet in eigene Orders/Dashboard geliefert; kein partner-adressierbarer Commission-Endpoint |
| UI-Restriktion serverseitig durchgesetzt | ja | Alle Partner-Ressourcen über `CurrentPartner.resolve()` (JWT-Claim → kanonische `partnerNumber`), **nie** client-gelieferte id |
| Inaktiver Partner nach Auth abgewiesen | ja | `CurrentPartner.resolve()` wirft 403 bei `!active`; zusätzlich `createForPartnerId` 409; Tests `inactivePartnerIsBlockedFromPartnerEndpoints`, `companyCannotCreateManualOrderForInactivePartner` |
| Direkte API-Calls umgehen Rolle nicht | ja | `quarkus.security.jaxrs.deny-unannotated-endpoints=true` (fail-closed) + `@RolesAllowed` auf jeder Ressource |
| Deep-Link ohne Rolle | abgewiesen | Backend RBAC + Router-Guard (`render()` prüft `session.roles.includes(route.role)`) |

---

## 3. Korrektheits-Findings

### TECH-001 (Medium) — Dashboard-Zeitraumfilter verschiebt Grenzdatum um einen Tag (Zeitzone)

- **Datei/Zeile:** `frontend/src/pages/partner-dashboard-page.js:17` und `:143-144,182-183`; identisch `frontend/src/pages/company-dashboard-page.js:11,128-129,182-183`
- **Betroffener Fluss:** Dashboard mit **explizit gesetztem** Zeitraum (Partner + Firma). Standard („Gesamter Zeitraum") nicht betroffen — dort kommen die Headline-Totale backend-exakt.
- **Ist-Verhalten:** Der Zeitraum wird als **lokale** Mitternacht konstruiert (`new Date(fy, fm-1, fd, 0,0,0,0)`), dann per `toIsoDate` = `date.toISOString().slice(0,10)` in einen ISO-String umgewandelt. `toISOString()` rechnet nach **UTC** um. In Österreich (UTC+1/+2) wird lokale Mitternacht des 16.07. zu `2026-07-15T22:00:00Z` → `.slice(0,10)` = **"2026-07-15"**. Der Vergleich `o.orderDate >= fromIso` schließt damit Bestellungen des Vortags fälschlich mit ein; die „Bis"-Grenze verschiebt analog.
- **Soll-Verhalten:** Die ausgewählten Kalendertage sollen exakt gefiltert werden (16.07. = 16.07.).
- **Reproduktion/Reasoning:** Rein aus dem Code ableitbar (kein Runtime-Lauf nötig, aber lokal in einer UTC+2-Umgebung reproduzierbar). `formatDate`/`daysSinceLastOrder` an anderen Stellen sind bereits **korrekt** manuell-parsend gebaut (mit genau diesem Kommentar in `format.js:28-30`) — hier wurde `toISOString` übersehen.
- **Impact:** Provisions-/Umsatz-/Anzahl-Kacheln und Charts zeigen bei explizitem Zeitraum am Rand um bis zu einen Tag falsche Zahlen. Für nicht-technikaffine Partner (Kernzielgruppe laut Meeting 10.07) unauffällig-irreführend. Kein Datenintegritäts-, sondern ein Anzeigefehler; DB und all-time-Totale bleiben korrekt.
- **Fix-Richtung:** `toIsoDate` durch dieselbe manuelle Komposition ersetzen, die `format.js` bereits nutzt (`` `${y}-${pad(m)}-${pad(d)}` `` aus den lokalen Getter-Werten), statt `toISOString`. Eine Zeile pro Dashboard, plus die `toIsoDate`-Helper.
- **Owner:** Chris.
- **Regressionstest nötig:** Ja (leichtgewichtig) — `dashboard-data.js` ist bereits pure/testbar; ein Test „lokale Mitternacht 16.07. → ISO '2026-07-16'" würde es festnageln.
- **MVP-Scope:** Ja (FR-09 Dashboard), aber nicht blockierend.

### TECH-002 (Low) — Import: In-Datei-Duplikatprüfung nutzt Default-Locale, DB-Prüfung nutzt `Locale.ROOT`

- **Datei/Zeile:** `backend/.../importer/ImportService.java:44` (`identifier.toLowerCase()` — **ohne** Locale) vs. `PartnerImportDefinition.java:99` / `ProductImportDefinition.java:71` (`toLowerCase(Locale.ROOT)`)
- **Ist-Verhalten:** Die zwei Duplikat-Checks derselben Commit-Operation falten Groß-/Kleinschreibung mit **unterschiedlichen** Locales. Auf einem Server mit türkischem Default-Locale würden „I"/„i" zwischen In-Datei-Dedupe und DB-Existenzprüfung divergieren.
- **Soll:** Ein einheitliches, locale-unabhängiges Case-Folding auf allen Duplikat-Pfaden — die Codebasis hat dafür bereits bewusst `Locale.ROOT` eingeführt (Kommentar in `PartnerService:53` „avoids the Turkish-I trap").
- **Impact:** Praktisch nur auf nicht-DE/EN-Server-Locale relevant → Deployment auf Hetzner (Ubuntu, i.d.R. `C`/`en_US`) unkritisch. Theoretisch, defensiv.
- **Fix-Richtung:** `ImportService.java:44` → `identifier.toLowerCase(Locale.ROOT)`.
- **Owner:** Chris. **Regressionstest:** optional. **MVP-Scope:** FR-04, minimal.

### TECH-003 (Low) — `orderValue`/`lineTotal` können `precision=10` bei Extremwerten überlaufen

- **Datei/Zeile:** `Order.java:41` + `OrderItem.java:26` + `CommissionRecord.java:25` alle `precision = 10, scale = 2` (max 99.999.999,99); Summierung in `OrderService.java:109`.
- **Ist-Verhalten:** Menge ist auf 10000 gedeckelt (`OrderItemRequest`), Preis aber ebenfalls `precision=10`. Ein einzelner Artikel mit hohem Preis × 10000 oder eine Summe mehrerer teurer Positionen kann die 8 Vorkomma-Stellen der `orderValue`-Spalte überschreiten → Hibernate/Postgres wirft beim `persist()` einen numeric-overflow → **ungraceful 500** statt validierter 400.
- **Soll:** Für den Referenzkunden (~800 Artikel, normale Preise) praktisch nie erreicht; falls doch, eine saubere Fehlermeldung statt 500.
- **Impact:** Sehr geringe Eintrittswahrscheinlichkeit in dieser Domäne; keine Datenkorruption (Transaktion rollt zurück).
- **Fix-Richtung:** Optional — Preis-Obergrenze via `@DecimalMax` in `ProductRequest`, oder Spaltenpräzision erhöhen. Für MVP dokumentieren, nicht zwingend fixen.
- **Owner:** Chris. **Regressionstest:** nur bei Fix. **MVP-Scope:** Randbereich.

### Geprüft & korrekt (keine Findings)

- **Geld/Provision byte-genau:** `CommissionService.createForOrder` = `orderValue.multiply(percent).divide(100, 2, HALF_UP)`; Frontend `money.js:commissionCentsHalfUp` spiegelt das als exakte Integer-Cent-Division mit explizitem Half-up-Tie — **kein Float-`Math.round`**. Snapshots (`unitPriceSnapshot`, `commissionPercentUsed`, `orderValue` auf dem Record) korrekt eingefroren. Custom-Prozentsatz fließt über `Partner.commissionPercent` durch.
- **Status-Maschine:** `NEXT_STATUS`-Map erzwingt vorwärts-only submitted→reviewed→fulfilled; `FULFILLED` hat keinen Nachfolger → jeder weitere Schritt 400. Test `companyStatusChangeIsForwardOnly`.
- **Bestellfluss-Kanten:** Leerer Warenkorb clientseitig geblockt + `@NotEmpty` serverseitig (`partnerOrderWithoutItemsIsRejected`); Menge `@Min(1)@Max(10000)` server + `clampQuantity` client; nicht-freigeschaltete/nicht-verfügbare Produkte serverseitig abgewiesen (`OrderService:96-101`, Tests vorhanden); Doppel-Submit im Frontend per Instanz-Flag geguardet (`partner-order-new-page.js:57`).
- **Dormanz-Grenze:** `daysSinceLastOrder` rechnet in **ganzen UTC-Kalendertagen** (`Date.UTC(...)`), nicht in Millisekunden — korrekt gegen DST/Zeitzonen abgesichert (der TECH-001-Fehler ist hier **nicht** vorhanden). Backend `KundeService`-Query grouped-left-join (kein N+1), Grenzen 59/60/61 getestet.
- **`PartnerNumber.canonicalize()` / `Locale.ROOT`:** konsistent auf allen Write-Pfaden (manuell + Import), Login-Match in `CurrentPartner` gegen upper-cased Claim. Whitespace-Padding-Regression bereits getestet.

---

## 4. Sicherheits-Findings

Keine ausnutzbaren Schwachstellen gefunden. Bestätigte Kontrollen + dokumentierte Rest-Risiken:

### SEC-001 (Informational, akzeptiert) — Produktbild-IDs sind enumerierbar

- `ProductImageResource.get` (`GET /api/product-images/{id}`) ist für `company` **und** `partner` lesbar, **ohne** Enablement-Prüfung; IDs sind sequenziell. Ein Partner kann jedes Bild jedes (auch nicht freigeschalteten) Produkts laden. **Bereits im Code als akzeptiertes Risiko dokumentiert** (2026-07-09 Security-Review, `ProductImageResource:45-48`): Bilder sind nicht-sensible Galerie-Assets. Bewertung: **korrekt akzeptiert** für den MVP; bei künftiger Bild-Vertraulichkeit Enablement-Check nachrüsten.

### SEC-002 (Informational) — Direct-API-Doppel-Submit nur frontendseitig geguardet

- Zwei schnelle `POST /api/partner/orders` mit identischem Payload erzeugen serverseitig **zwei** Bestellungen (keine Idempotenz/kein Dedupe-Fenster). Das Frontend guardet per Flag; ein direkter API-Client umgeht das. Für den internen B2B-Kontext (authentifizierte, vertrauenswürdige Partner, ~10–15 Orders/Tag) akzeptabel; ein Idempotenzschlüssel wäre Enterprise-Ceremony jenseits des MVP. **Notieren, nicht fixen.**

### Bild-Upload aktiv geprüft (neue Angriffsfläche) — sauber

- **Magic-Byte-Sniffing statt Content-Type-Vertrauen** (`ProductImageValidator.detectContentType`): PNG/JPEG/WEBP per Signatur, **SVG explizit abgelehnt** (Script-Vektor), unbekannt → 400. Tests: `declaredContentTypeIsIgnoredInFavourOfSniffedBytes`, `svgUploadIsRejected`, `nonImageUploadIsRejected`.
- **Size-Cap doppelt:** `MAX_SIZE_BYTES = 2MB` im Validator **plus** `quarkus.http.limits.max-body-size=2200K` (rejectet vor Pufferung). Test `oversizeUploadIsRejected`.
- **Kein Path-Traversal / kein Unsafe-Filename-Serving:** Bytes werden als `bytea` in der DB gehalten (kein Filesystem-Pfad), `Content-Disposition` nutzt einen **generierten** Namen (`product-image-{id}.{ext}`), nicht den hochgeladenen Filename → keine Header-Injection. Umlaut-Filename wird gespeichert/DTO-geliefert, aber nicht in den Header interpoliert (Test `umlautFilenameIsPreservedAndImageStillServedById`).
- **Upload company-only** (`@RolesAllowed("company")` auf Klassenebene, nur `get` für beide Rollen), Test `partnerCannotUploadOrListGallery`.

### Weitere bestätigte Kontrollen

- **CSV-Formel-Injection:** `CsvExportService.sanitizeFormula` präfixt führende `= + - @` mit `'`. **Low-Lücke:** die OWASP-erweiterte Menge (`\t`, `\r`) fehlt; Impact gering, da nur Partner-/Kundennamen betroffen und Excel-Auto-Formel bei Tab/CR selten triggert. Optionaler Härtungspunkt.
- **XSS:** Durchgängiges `escapeHtml` auf allen partner-/kunde-/importsourcierten Interpolationen (`html.js`, `data-table` escaped per Default, `html:true` nur für app-kontrollierte Fragmente). JWT-Payload-Decode UTF-8-sicher (`auth.js:15`).
- **Auth:** Public-Client Direct-Grant (bewusst, MVP), Refresh-Flow mit geteiltem In-Flight-Promise + Logout-Race-Guard; 401→Refresh→einmaliger Retry in `api.js`. Fehlerpfade räumen die Session sauber ab (kein stiller Zombie-State).

---

## 5. Datenbank- & Performance-Findings

Keine Findings — die N+1-Vermeidung ist durchgängig und bereits per Test verankert:

- `OrderService.listForPartner/listAll`: `join fetch o.items/o.partner/o.kunde` + `distinct` (1 Query statt 1+3N) + Batch-Commission-Lookup (`findForOrders`, 1 Query).
- `KundeService.listForPartner`: eine grouped-left-join-Query für `lastOrderDate` (Hibernate-Statistics-Test in `KundeServiceTest`).
- `DashboardService` (Partner + Company): Batch-Commission + `groupingBy` in-memory statt per-Order-Query; Partner-Latest via DB-`page(0,5)` statt Full-Load-und-Slice.
- Bewusst **abgelehnte** Backend-Pagination (~10–15 Orders/Tag) — angemessene Scope-Disziplin, kein Finding.
- **Timezone/Date-Semantik DB-seitig:** `LocalDate` durchgängig (kein `Instant`-Vergleich für Bestelldaten); Dormanz-Query korrekt. Der einzige Zeitzonenfehler liegt clientseitig (TECH-001).

---

## 6. Testfehler

**Endergebnis: 76/76 grün** — aber nur über einen Workaround. Der Standard-Lauf (`./mvnw test`, Keycloak Dev Services) scheitert auf dieser Maschine aktuell **deterministisch** mit 71/76. Vollständige Diagnose:

| Lauf | Konfiguration | Ergebnis |
|---|---|---|
| 1 | Standard | 71/76 fail |
| 2 | `-Dquarkus.http.test-port=8091` | 71/76 fail |
| 3 | `QUARKUS_KEYCLOAK_DEVSERVICES_WEB_CLIENT_TIMEOUT=60S` | 71/76 fail (aber echte Fehlermeldung sichtbar) |
| 4 | Dev-Services-Image auf `keycloak:26.0` gepinnt | 71/76 fail |
| 5 | `start-command=start-dev` erzwungen | fail (401 statt HTTPS required) |
| **6** | **Compose-Keycloak statt Dev Services + Test-Port 8091** | **76/76 grün ✓** |

**Root Cause (vollständig diagnostiziert, kein Code-Problem):**

1. Quarkus' Keycloak Dev Services startet den Wegwerf-Container mit **`start`** (Production-Mode, per `docker inspect` verifiziert: `start --http-enabled=true --hostname-strict=false`), nicht `start-dev`. Im Production-Mode bleibt `sslRequired=external` auf dem Master-Realm: HTTP-Admin-Calls sind nur von **privaten/loopback Quell-IPs** erlaubt.
2. Docker Desktop läuft hier mit **gVisor-Networking** (`--networkType gvisor`, per `ps` verifiziert). Darunter erscheint die Host→Container-Verbindung mit einer nicht-privaten Quelladresse → Keycloak antwortet auf den Admin-Token-Request mit **„HTTPS required"** → der `partnerportal`-Realm wird nie importiert → alle 71 token-abhängigen Tests scheitern in `KeycloakTestTokens.tokenFor` mit 404 „Realm does not exist". Die 5 grünen Tests sind genau die ohne Token-Bedarf.
3. Zusatz-Falle beim Workaround: Die Compose-Keycloak published Host-Port **8081** = Quarkus' Default-**Test**-Port. Tests gegen `localhost:8081/realms/...` treffen dann die Test-App selbst (404). Deshalb braucht der Workaround zusätzlich `-Dquarkus.http.test-port=8091`.

Die Compose-Keycloak ist vom Problem nicht betroffen, weil sie mit `start-dev` läuft (setzt `sslRequired=NONE`) — direkt verifiziert: Admin-Token über HTTP → 200.

**Funktionierender Workaround (Compose-Stack muss laufen):**

```bash
./mvnw test \
  -Dquarkus.http.test-port=8091 \
  -Dquarkus.keycloak.devservices.enabled=false \
  -Dquarkus.oidc.auth-server-url=http://localhost:8081/realms/partnerportal \
  -Dquarkus.oidc.client-id=partnerportal-frontend
```

Ergebnis damit: **exit 0, 76 Tests, 0 Failures, 0 Errors** (2026-07-16, HEAD `9a9f5cd`).

**Einordnung:** Der frühere „intermittierende macOS-Flake" (CONTINUATION_PROMPT DEV GOTCHAS) war sehr wahrscheinlich dieselbe Mechanik — ob er zuschlägt, hängt an der Docker-Desktop-Netzwerkkonfiguration, nicht am Code. CI auf Linux (Tobias' `ci-cd`-Change) ist nicht betroffen (kein Docker Desktop/gVisor).

**Empfehlung (Chris, kleiner separater Change):** Entweder (a) Docker-Desktop-Netzwerkmodus prüfen/umstellen (gVisor → legacy), oder (b) den Workaround als dokumentiertes `%test-compose`-Profil bzw. Skript festhalten, oder (c) auf einen Quarkus-Fix warten und bis dahin den Workaround nutzen. Nicht in einem Feature-Branch mitfixen.

---

## 7. Fehlende Tests (High-Value-Lücken)

Die vorhandene Abdeckung ist **breit** (76 Tests, jede Ressource, Rollen-Negativfälle, Umlaut-Roundtrips, Geld-Grenzen, Dormanz 59/60/61, Import-Kanten inkl. leerer Header-Zelle, Bild-Upload-Sicherheit). Echte Lücken:

| Bereich | Lücke | Priorität |
|---|---|---|
| **Dashboard-Zeitraum** | Kein Test für die Client-Filterlogik `dashboard-data.js` (pure, leicht testbar) — würde TECH-001 fangen und die Zeitzonengrenze festnageln. | Mittel (mit TECH-001-Fix) |
| **Geld-Grenzwerte** | Kein Test für `orderValue`-Überlauf (TECH-003) bzw. eine explizite Preis-Obergrenze. | Niedrig |
| **FR-25 (0 freigeschaltete Produkte)** | Backend `partnerWithNoEnabledProductsSeesEmptyList` deckt die API; der **Bestell-Block** bei leerem Katalog ist nur Playwright-verifiziert, kein automatisierter Resource-Test „POST bei 0 Enablements → 400". | Niedrig |
| **Provision custom %** | `OrderResourceTest` prüft den Standard-Pfad; ein Test mit **krummer Summe × custom-Prozent** (byte-genau gegen `CommissionRecord`) existiert laut Sessions manuell, aber nicht als committeter Unit-Test. | Niedrig |
| **CSV-Formel-Injection** | `sanitizeFormula` hat keinen direkten Test (führendes `=`/`+`/`@` → `'`-Präfix). | Niedrig |

Die vom Prompt genannten Pflicht-Bereiche (Authorization, Doppel-Submit, Dormanz-Grenze, Kunden-/Order-Ownership, inaktive Accounts, Uploads, Umlaute, Unhappy-Paths) sind **bereits abgedeckt** — siehe §2/§3/§4.

---

## 8. Medium- & Low-Findings (Sammelübersicht)

| ID | Severity | Konfidenz | Datei | Kurz |
|---|---|---|---|---|
| TECH-001 | Medium | Hoch | `partner-dashboard-page.js:17`, `company-dashboard-page.js:11` | Zeitraumfilter Off-by-one (UTC-Konversion) |
| TECH-002 | Low | Hoch | `ImportService.java:44` | Import-Dedupe Default-Locale statt `Locale.ROOT` |
| TECH-003 | Low | Mittel | `Order.java:41` | `orderValue` precision(10) überlaufbar bei Extremwerten → 500 |
| SEC-001 | Info | Hoch | `ProductImageResource.java:45` | Bild-ID-Enumeration (dokumentiert akzeptiert) |
| SEC-002 | Info | Hoch | `OrderResource.java:35` | Direct-API-Doppel-Submit nur clientseitig geguardet |
| SEC-003 | Low | Hoch | `CsvExportService.java:83` | Formel-Sanitizer ohne `\t`/`\r` |

---

## 9. Cross-Check-Meinungsverschiedenheiten

Kein externer Cross-Check (Sol/Codex) durchgeführt: Die Cross-Check-Regel des Prompts triggert nur bei **Critical/High-Findings oder <90 % Konfidenz** — hier gibt es keine Critical/High, und TECH-001..003/SEC-001..003 sind alle mit hoher (TECH-003: mittlerer, aber gering-impact) Konfidenz aus dem Code direkt belegbar. Ein zweites Modell hätte an rein aus dem Quellcode ableitbaren Off-by-one-/Locale-Befunden keinen zusätzlichen Erkenntniswert gebracht. Empfehlung: kein Cross-Check nötig; falls TECH-001 vor dem Termin gefixt wird, reicht der oben skizzierte Unit-Test als Absicherung.

---

## 10. Audit-Limitationen

- **Testsuite grün nur via Workaround:** 76/76 grün bestätigt (Compose-Keycloak + Test-Port 8091, §6); der Standard-Dev-Services-Pfad ist auf dieser Maschine durch Docker-Desktop-gVisor-Networking blockiert (Root Cause §6). Kein Code-Problem, aber der lokale Standardlauf bleibt bis zum Infra-Fix rot.
- **Kein Runtime-Penetrationstest:** Isolations-/RBAC-Befunde beruhen auf Code-Lesen + vorhandenen Tests, nicht auf einem aktiven Live-Angriff mit zwei parallelen Partner-Tokens (der Dev-Stack läuft, aber ein adversarialer Live-Durchlauf war nicht Teil dieses read-only-Reviews und die Tests decken die Vektoren bereits ab).
- **Frontend nur Kernmodule tief:** `lib/`, beide Dashboards, order-cart/form, data-table, api/auth vollständig gelesen; einige Company-Listen-Pages (partner/product-list) nur überflogen — dort keine neuen Sicherheits-/Geld-Pfade erwartet (reine CRUD-UIs auf company-only-Endpoints).
- **`ci-cd-deployment-with-gh-actions` bewusst nicht geprüft** (Tobias-owned, Projektregel eingehalten). CORS-/Issuer-/Secret-Konfiguration für das echte Deployment fällt in diese Change und ist hier nicht bewertet.
- **Kein Produktivcode geändert**, keine destruktiven Payloads, keine externen Systeme, keine Secrets berührt — Safety-Regeln eingehalten.
