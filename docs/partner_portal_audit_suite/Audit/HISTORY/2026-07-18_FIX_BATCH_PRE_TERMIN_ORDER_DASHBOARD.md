# Fix Batch: Endkundenauswahl, Bestellreihenfolge und Dashboard-Zeitraum

- **Datum:** 2026-07-18
- **Branch:** `fix/pre-termin-order-and-dashboard`
- **OpenSpec-Change:** `fix-pre-termin-order-and-dashboard`
- **Findings:** UX-003, UX-004, TECH-001
- **Ergebnis:** Alle drei Findings implementiert und im Fix-Batch erfolgreich verifiziert; separate Regression Review noch offen.

## Geänderte Dateien

- `frontend/src/lib/order-form.js`
- `frontend/src/lib/dashboard-data.js`
- `frontend/src/pages/company-dashboard-page.js`
- `frontend/src/pages/partner-dashboard-page.js`
- `backend/src/main/java/at/papo/partnerportal/order/OrderService.java`
- `backend/src/test/java/at/papo/partnerportal/order/OrderResourceTest.java`
- `openspec/changes/fix-pre-termin-order-and-dashboard/tasks.md`
- `docs/partner_portal_audit_suite/Audit/06_FINDINGS_REGISTER.md`
- `docs/partner_portal_audit_suite/Audit/HISTORY/2026-07-18_FIX_BATCH_PRE_TERMIN_ORDER_DASHBOARD.md`

## Implementierung

- UX-003: Das gemeinsame Endkunden-Select zeigt zuerst die deaktivierte Option `Bitte Endkunden wählen…`, ist im Bestandskundenmodus verpflichtend und verwendet die explizite deutsche Validierungsmeldung `Bitte Endkunden wählen.`. Beim Wechsel in den Neukundenmodus wird die Select-Pflicht samt Custom Validity aufgehoben.
- UX-004: Ausschließlich `OrderService.listForPartner` sortiert mit `order by o.id desc`. Firmenliste, API-Form und Partnerfilter blieben unverändert.
- TECH-001: `dashboard-data.js` stellt `toLocalIsoDate(date)` über lokale Kalender-Getter und Zero-Padding bereit. Beide Dashboards verwenden diesen Helper statt `toISOString()`.
- Keine neue Abhängigkeit, API, Datenbankänderung, Authentifizierungs-, Autorisierungs-, Geld- oder Deferred-Scope-Änderung.

## Tests und Browser-Verifikation

### Automatisierter Backend-Test

`OrderResourceTest.partnerSeesNewestOwnOrdersFirst` erzeugt zwei Bestellungen für den angemeldeten Partner und prüft:

- alle zurückgegebenen IDs sind strikt absteigend sortiert;
- jede Zeile gehört weiterhin zu `PARTNER-DEMO`.

Rot-Nachweis vor dem Fix: erwartet `[2, 1]`, erhalten `[1, 2]`. Nach dem Query-Fix: 1 Test, 0 Fehler.

### Browser — UX-003

- Partnerflow ohne Auswahl: Bestellung blieb bei 15 Einträgen, Select leer und invalid, Meldung exakt `Bitte Endkunden wählen.`.
- Partnerflow nach Auswahl: Bestellung Nr. 22 für Fischer GmbH erfolgreich bestätigt.
- Partner-Neukundenmodus: verstecktes Bestandskunden-Select `required=false`, valide und ohne Custom-Validity-Meldung; Bestellung Nr. 23 für Browser-Neukunde erfolgreich bestätigt.
- Firmenflow ohne Auswahl: Firmenliste blieb bei 23 Einträgen, Select leer und invalid, Meldung exakt `Bitte Endkunden wählen.`.
- Firmenflow nach Auswahl: manuelle Bestellung Nr. 24 für Browser-Neukunde erfolgreich bestätigt.

### Browser — UX-004

- Die frisch angelegte Partnerbestellung Nr. 23 stand in `Meine Bestellungen` in der ersten Zeile.
- Der Detaildialog zeigte Bestellnummer, Endkunde, Artikel, Provision und Gesamtwert korrekt.
- Endkundenfilter `Browser-Neukunde`: 1 sichtbare Zeile; Reset: 17 sichtbare Zeilen und die neue Bestellung weiterhin zuerst.

### Browser — TECH-001

- Shared Helper: lokale Datumsobjekte ergaben für Winter `2026-01-16` und Sommer `2026-07-16` exakt, ohne UTC-Verschiebung.
- Partnerdashboard, Wintergrenze `2026-02-10`: Eingaben blieben exakt, 1 erwartete und 1 angezeigte erfüllte Bestellung, 1 erwartete und 1 angezeigte Tabellenzeile.
- Partnerdashboard, Sommergrenze `2026-07-17`: Eingaben blieben exakt, die eine eingereichte Bestellung wurde in der Latest-Liste eingeschlossen. Produktanzahl blieb vor, während und nach dem Filter bei 3; Reset stellte 11 erfüllte Allzeit-Bestellungen wieder her.
- Firmendashboard, Wintergrenze `2026-02-10`: 1 erfüllte Bestellung und Umsatz `€ 347,90` stimmten exakt mit den API-Daten überein.
- Firmendashboard, Sommergrenze `2026-07-18`: 0 erfüllte Bestellungen und `€ 0,00` stimmten exakt; alle 3 Partnerzeilen blieben sichtbar. Reset stellte 16 erfüllte Bestellungen und `€ 3.473,50` wieder her.

## Ausgeführte Befehle

```bash
cd backend
./mvnw -Dtest=OrderResourceTest#partnerSeesNewestOwnOrdersFirst test -Dquarkus.http.test-port=8091 -Dquarkus.keycloak.devservices.enabled=false -Dquarkus.oidc.auth-server-url=http://localhost:8081/realms/partnerportal -Dquarkus.oidc.client-id=partnerportal-frontend
./mvnw test
./mvnw test -Dquarkus.http.test-port=8091 -Dquarkus.keycloak.devservices.enabled=false -Dquarkus.oidc.auth-server-url=http://localhost:8081/realms/partnerportal -Dquarkus.oidc.client-id=partnerportal-frontend
./mvnw package -Dquarkus.http.test-port=8091 -Dquarkus.keycloak.devservices.enabled=false -Dquarkus.oidc.auth-server-url=http://localhost:8081/realms/partnerportal -Dquarkus.oidc.client-id=partnerportal-frontend

cd ..
/Users/mitgl/.codex/skills/playwright/scripts/playwright_cli.sh open http://localhost:3000/#/login --headed
/Users/mitgl/.codex/skills/playwright/scripts/playwright_cli.sh run-code "<UX-003-, UX-004- und TECH-001-Prüfungen>"
openspec validate fix-pre-termin-order-and-dashboard
git diff --check
```

## Ergebnisse

- Fokussierter Test: vor dem Fix rot, nach dem Fix 1 Test, 0 Fehler.
- Standard-`./mvnw test`: bekannter INFRA-001-Fehler; 77 Tests, 72 tokenabhängige Fehlschläge durch fehlenden Dev-Services-Realm (HTTP 404), 5 Tests grün.
- Compose-Keycloak-Testlauf: 77 Tests, 0 Fehler, 0 Fehlschläge.
- `./mvnw package` mit demselben INFRA-001-Workaround: 77 Tests grün, `BUILD SUCCESS`.
- Browserprüfungen beider Rollen: vollständig grün.
- `openspec validate`: erfolgreich.
- `partnerportal-quality`: keine Findings.
- Correctness- und Ponytail-Review: keine Findings.
- `git diff --check`: keine Fehler.

## Offene Punkte

- UX-003, UX-004 und TECH-001 haben im Fix-Batch keine bekannte Restlücke.
- INFRA-001 bleibt außerhalb dieses Batches offen.
- Der Fix-Commit wird nach dem Commit im Findings Register und in der separaten Regression Review eingetragen.
- Der Status bleibt bis zu dieser separaten Regression Review `Fixed`, nicht `Verified`.

## Empfohlener Regression-Review-Umfang

- Diff des gemeinsamen Endkundenpickers sowie Partner- und Firmen-Bestellflow mit Bestands- und Neukunde.
- Partner-Order-API mit mehreren eigenen und mindestens einer fremden Bestellung; Browserliste, Detaildialog und Filter.
- Beide Dashboards mit je einer erfüllten Wintergrenze und einer Sommergrenze, einschließlich Umsatz/Provision/Anzahl, Reset und unveränderter Produktanzahl beim Partner.
