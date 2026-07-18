# Fix Batch: UX Reload und Warenkorb

- **Datum:** 2026-07-18
- **Branch:** `fix/ux-reload-and-cart-overlap`
- **OpenSpec-Change:** `fix-ux-reload-and-cart-overlap`
- **Findings:** UX-001, UX-002
- **Ergebnis:** Beide Findings implementiert und im Fix-Batch erfolgreich verifiziert; separate Regression Review noch offen.

## Geänderte Dateien

- `frontend/src/lib/router.js`
- `frontend/src/styles/base.css`
- `openspec/changes/fix-ux-reload-and-cart-overlap/tasks.md`
- `docs/partner_portal_audit_suite/Audit/06_FINDINGS_REGISTER.md`
- `docs/partner_portal_audit_suite/Audit/HISTORY/2026-07-18_FIX_BATCH_UX_RELOAD_CART.md`

## Implementierung

- UX-001: Der Router wartet auf `components/index.js`, bevor er den ersten Seiten-Render ausführt. Der `hashchange`-Listener wird erst danach registriert; Seitenmodule bleiben parallel geladen.
- UX-002: Warenkorbzeilen umbrechen bei allen Viewports. Produktnamen haben mindestens `8rem` Breite und dürfen lange Wörter umbrechen.
- Keine neuen Abhängigkeiten, APIs, Markup-, Backend- oder fachlichen Verhaltensänderungen.

## Tests und Verifikation

Keine persistente Testdatei wurde ergänzt; das Projekt hat keinen Frontend-Test-Runner. Die OpenSpec-Browserprüfungen wurden mit einem echten Playwright-Browser ausgeführt und in `tasks.md` dokumentiert.

### Vorher-Reproduktion

- Direct-load `#/partner/customers`: 0 Header, 0 Zellen, 5 nackte Zeilen.
- Direct-load `#/partner/orders`: 0 Header, 0 Zellen, 15 nackte Zeilen.
- Warenkorb bei 1440×900: `.cart-line__name` hatte 0 px Breite.

### Nachher-Prüfung

- Endkunden direct/reload: jeweils 3 Header, 15 Zellen, 1 Filter.
- Partnerbestellungen direct/reload: jeweils 5 Header, 75 Zellen, 4 Filter.
- SPA-Listen ohne Regression:
  - Partner: Endkunden 3/15, Bestellungen 5/75, Produkte 4/12 (Header/Zellen).
  - Firma: Produkte 6/48, Partner 5/15, Bestellungen 6/120 (Header/Zellen); Filter/Suche vorhanden.
- Testprodukt: „Größenwahn-Übermüller Präzisionswerkzeug für außergewöhnliche Baustellen“.
- Cart-Bounding-Boxes:
  - 1440×900: Name 139,59 px, keine Preisüberschneidung.
  - 390×844: Name 130,59 px, keine Preisüberschneidung.
  - 1920×1080: Name 139,59 px, keine Preisüberschneidung.
- Happy Path: Gesamtwert 42,50 Euro, Provisionsvorschau 4,25 Euro bei 10 %, Bestellung Nr. 22 erfolgreich bestätigt.

## Ausgeführte Befehle

```bash
/Users/mitgl/.codex/skills/playwright/scripts/playwright_cli.sh open http://localhost:3000/#/login --headed
/Users/mitgl/.codex/skills/playwright/scripts/playwright_cli.sh resize 1440 900
/Users/mitgl/.codex/skills/playwright/scripts/playwright_cli.sh run-code "<Direct-load-, Reload-, SPA- und Bounding-Box-Prüfungen>"
openspec validate fix-ux-reload-and-cart-overlap
cd backend
./mvnw package
./mvnw test -Dquarkus.http.test-port=8091 -Dquarkus.keycloak.devservices.enabled=false -Dquarkus.oidc.auth-server-url=http://localhost:8081/realms/partnerportal -Dquarkus.oidc.client-id=partnerportal-frontend
./mvnw package -DskipTests
git diff --check
```

## Ergebnisse

- `openspec validate`: erfolgreich.
- Standard-`./mvnw package`: wegen des bekannten INFRA-001-Keycloak-Dev-Services-Problems fehlgeschlagen (71 von 76 tokenabhängige Tests mit 404; Realm wurde nicht importiert).
- Dokumentierter Compose-Keycloak-Testlauf: 76 Tests, 0 Fehler, 0 Fehlschläge.
- `./mvnw package -DskipTests`: erfolgreich; Tests waren unmittelbar davor vollständig grün.
- `partnerportal-quality`: keine Findings.
- Correctness-Code-Review: keine Findings.
- `git diff --check`: keine Fehler.

## Offene Punkte

- UX-001 und UX-002 haben im Fix-Batch keine bekannte Restlücke.
- INFRA-001 bleibt außerhalb dieses Batches offen; der dokumentierte Compose-Keycloak-Testweg ist weiterhin erforderlich.
- Der Status bleibt bis zu einer separaten Regression Review `Fixed`, nicht `Verified`.

## Empfohlener Regression-Review-Umfang

- Diff von `frontend/src/lib/router.js` und `frontend/src/styles/base.css`.
- Direct-load und F5 auf Partner-Endkunden/-Bestellungen sowie SPA-Navigation über alle Partner- und Firmenlisten.
- Warenkorb mit dem langen Umlaut-Produkt bei 1440×900, 390×844 und 1920×1080.
- Ein Bestellabschluss mit Kontrolle von Gesamtwert, Provisionsvorschau und Bestätigung.
