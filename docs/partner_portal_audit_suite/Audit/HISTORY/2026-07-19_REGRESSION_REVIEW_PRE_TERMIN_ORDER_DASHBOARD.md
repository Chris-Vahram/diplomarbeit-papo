# Regression Review: Endkundenauswahl, Bestellsortierung und Dashboard-Zeitraum

- **Datum:** 2026-07-19
- **Review-Branch:** `review/pre-termin-order-and-dashboard`
- **Geprüfter Fix-Commit:** `540d6fa`
- **Merge-Commit auf `main`:** `7280ebd`
- **Vergleich:** `540d6fa^..540d6fa`
- **Findings:** UX-003, UX-004, TECH-001
- **Ergebnis:** Alle drei Findings separat reproduziert und erfolgreich als behoben verifiziert. Keine direkt betroffene Regression gefunden.
- **Scope:** Nur Endkundenauswahl in den beiden Bestellflows, Sortierung der Partner-Bestellliste und lokale Datumsgrenzen der beiden Dashboards.

## Implementierungs-Diff

- `frontend/src/lib/order-form.js`: Der bestehende Endkunde startet mit einer deaktivierten „Bitte Endkunden wählen…“-Option. Das Select ist im Bestandskundenmodus verpflichtend und zeigt „Bitte Endkunden wählen.“; beim Wechsel zum Neukundenmodus wird die Auswahl entfernt und die Pflichtbedingung deaktiviert.
- `backend/src/main/java/at/papo/partnerportal/order/OrderService.java`: Partnerbestellungen werden nach `o.id desc` statt aufsteigend geliefert. Partner-Scoping und übrige Abfragebedingungen bleiben unverändert.
- `backend/src/test/java/at/papo/partnerportal/order/OrderResourceTest.java`: Der ergänzte Test prüft absteigende IDs und weiterhin ausschließlich Bestellungen des authentifizierten Partners.
- `frontend/src/lib/dashboard-data.js`, `partner-dashboard-page.js` und `company-dashboard-page.js`: Datumsgrenzen werden mit lokalen Kalenderbestandteilen statt über `toISOString()` erzeugt.
- Der Diff ist auf die drei Root Causes begrenzt. Rollen-, Tenant-, Geld-, Status- und allgemeine Bestelllogik wurden nicht erweitert.

## UX-003 — Stille Vorbelegung des ersten Endkunden

- **Verifikationsergebnis:** Verified fixed
- **Partner-Flow:** Bei „Neue Bestellung“ war „Bitte Endkunden wählen…“ sichtbar, deaktiviert und ausgewählt. Nach Hinzufügen eines Artikels wurde das Absenden ohne Endkunde browserseitig blockiert; das Select hatte `value=""`, `required=true`, `checkValidity()=false` und die Meldung „Bitte Endkunden wählen.“. Es wurde kein POST ausgelöst.
- **Company-Flow:** Bei „Bestellung manuell anlegen“ trat nach Partner- und Artikelauswahl dasselbe Verhalten auf: leere Pflichtauswahl, deutsche Validierungsmeldung und kein POST ohne Endkunde.
- **Positiver Pfad:** Nach expliziter Auswahl von Fischer GmbH wurden Bestellungen in beiden Rollen erfolgreich angelegt (`200 OK`).
- **Direkt betroffene Regressionen:** Der Wechsel zu „Neuen Endkunden anlegen“ entfernt das Bestandskunden-Select und zeigt die Neukundenfelder; beim Zurückwechseln wird die leere, verpflichtende Prompt-Auswahl wiederhergestellt. Gemeinsames Formularverhalten ist damit in beiden Einbindungsstellen geprüft.
- **Beobachtetes Verhalten:** Eine Bestellung kann im Bestandskundenmodus nicht mehr unbemerkt dem ersten Listeneintrag zugeordnet werden.
- **Verbleibendes Risiko:** Das Repository besitzt keinen automatisierten Frontend-Test-Runner; die Absicherung des nativen Constraint-Validation-Flows bleibt browserbasiert.
- **Follow-up:** Keines für UX-003.

## UX-004 — Neue Partnerbestellung erscheint am Listenende

- **Verifikationsergebnis:** Verified fixed
- **Fix-Verifikation:** Nach dem erfolgreichen Partner-Bestellabschluss erschien Bestellung Nr. 22 mit Datum 19.07.2026 als erste Tabellenzeile vor den Seed-Bestellungen vom 18.07.2026 und früher. Die API-Antwort lieferte die IDs ebenfalls streng absteigend.
- **Automatisierter Nachweis:** `OrderResourceTest#partnerSeesNewestOwnOrdersFirst` lief separat grün und prüfte zusätzlich das unveränderte Partner-Scoping.
- **Direkt betroffene Regressionen:** Die Detailansicht der neuen Bestellung öffnete korrekt und zeigte Bestellnummer, Datum, Endkunde, Artikel, Betrag und Provision. Der Endkundenfilter reduzierte die Liste auf die drei Bestellungen von Anna Berger; „Filter zurücksetzen“ stellte die vollständige, weiterhin absteigende Liste wieder her.
- **Beobachtetes Verhalten:** Die zuletzt angelegte Partnerbestellung ist ohne Scrollen oben sichtbar; Listeninteraktion und Detaildialog bleiben funktionsfähig.
- **Verbleibendes Risiko:** Die Sortierung folgt weiterhin der monotonen Datenbank-ID und nicht explizit `orderDate`; für den aktuellen Erstellungsflow ist dies beabsichtigt und deckt das Finding ab.
- **Follow-up:** Keines für UX-004.

## TECH-001 — UTC-Verschiebung an Dashboard-Datumsgrenzen

- **Verifikationsergebnis:** Verified fixed
- **Partner-Dashboard:** Der lokale Einzelstichtag 11.02.2026 lieferte exakt die Seed-Bestellung vom 11.02.2026: 1 abgeschlossene Bestellung, 347,90 € Umsatz und 34,79 € Provision. Die Produktfreischaltung blieb unverändert bei 3 von 8. Nach „Zurücksetzen“ erschienen wieder die Allzeitwerte mit 11 abgeschlossenen Bestellungen.
- **Company-Dashboard:** Derselbe Einzelstichtag lieferte exakt 1 abgeschlossene Bestellung, 347,90 € Umsatz, 34,79 € Provision und 347,90 € Durchschnittswert. Partner ohne Umsatz blieben mit Nullwerten sichtbar.
- **Direkt betroffene Regressionen:** Zeitraumlabel, Allzeit-Reset, Status-/Kundenauswertung im Partner-Dashboard sowie Partneraufschlüsselung im Company-Dashboard blieben funktionsfähig. Die API-Daten bestätigten das Grenzdatum als `orderDate: "2026-02-11"`.
- **Beobachtetes Verhalten:** Der in Europe/Vienna gewählte lokale Kalendertag wird in beiden Dashboards unverändert als `YYYY-MM-DD` ausgewertet; keine Verschiebung auf den Vortag trat auf.
- **Verbleibendes Risiko:** Es gibt weiterhin keinen automatisierten Browser-Zeitzonentest. Der betroffene Nicht-UTC-Zeitzonenfall wurde deshalb real im Browser mit Europe/Vienna verifiziert.
- **Follow-up:** Keines für TECH-001.

## Ausgeführte Prüfungen

```bash
git diff --check 540d6fa^ 540d6fa
openspec validate fix-pre-termin-order-and-dashboard
cd backend
./mvnw -Dtest=OrderResourceTest#partnerSeesNewestOwnOrdersFirst test \
  -Dquarkus.http.test-port=8091 \
  -Dquarkus.keycloak.devservices.enabled=false \
  -Dquarkus.oidc.auth-server-url=http://localhost:8081/realms/partnerportal \
  -Dquarkus.oidc.client-id=partnerportal-frontend
./mvnw test \
  -Dquarkus.http.test-port=8091 \
  -Dquarkus.keycloak.devservices.enabled=false \
  -Dquarkus.oidc.auth-server-url=http://localhost:8081/realms/partnerportal \
  -Dquarkus.oidc.client-id=partnerportal-frontend
```

- `git diff --check`: erfolgreich; keine Whitespace-Fehler im Fix-Commit.
- `openspec validate`: erfolgreich; Change `fix-pre-termin-order-and-dashboard` ist gültig.
- Fokustest: 1 Test, 0 Failures, 0 Errors, 0 Skipped; Build erfolgreich.
- Vollständiger Backend-Testlauf über den dokumentierten Compose-Keycloak-Pfad: 77 Tests, 0 Failures, 0 Errors, 0 Skipped; Build erfolgreich.
- Browser: Chromium/Playwright mit echtem lokalem Dev-Stack, Rollen `partner` und `company`, lokale Zeitzone Europe/Vienna.

## Abschlussurteil

UX-003, UX-004 und TECH-001 sind auf Fix-Commit `540d6fa` vollständig behoben. Die drei Findings dürfen im Findings Register von `Fixed` auf `Verified` gesetzt werden.
