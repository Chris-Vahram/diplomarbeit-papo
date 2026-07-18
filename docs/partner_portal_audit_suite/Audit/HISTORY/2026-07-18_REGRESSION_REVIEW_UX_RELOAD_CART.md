# Regression Review: UX Reload und Warenkorb

- **Datum:** 2026-07-18
- **Branch:** `fix/ux-reload-and-cart-overlap`
- **Geprüfter Fix-Commit:** `e3baab4`
- **Vergleich:** `e3baab4^..e3baab4`
- **Findings:** UX-001, UX-002
- **Ergebnis:** Beide Findings separat reproduziert und erfolgreich als behoben verifiziert. Keine direkt betroffene Regression gefunden.
- **Scope:** Nur Router-Bootstrap, Listen-Rendering und Warenkorbzeilen einschließlich Bestellabschluss; kein allgemeiner UX- oder Technik-Audit.

## Implementierungs-Diff

- `frontend/src/lib/router.js`: `components/index.js` wird vor dem ersten `render()` awaited. Der `hashchange`-Listener wird erst danach registriert; beim anschließenden `render()` wird der dann aktuelle Hash ausgewertet. Seitenmodule bleiben parallel geladen.
- `frontend/src/styles/base.css`: `.cart-line` verwendet an allen Viewports `flex-wrap: wrap`; `.cart-line__name` erhält `min-width: 8rem` und `overflow-wrap: anywhere`; die redundante Mobile-Regel wurde entfernt.
- Der Diff ist auf die beiden Root Causes begrenzt. Keine Abhängigkeits-, API-, Markup-, Backend-, Rollen-, Tenant- oder Geldlogikänderung.
- Projektspezifische Qualitätsprüfung und generischer Correctness-Review: keine Findings.

## UX-001 — Reload/Deep-Link auf Listen-Seiten

- **Verifikationsergebnis:** Verified fixed
- **Vorher-Reproduktion:** Der Parent-Stand wurde read-only direkt aus Git bereitgestellt. Mit deterministisch verzögerter Registrierung von `components/index.js` zeigte `#/partner/customers` den ursprünglichen Bootstrap-Fehler: leere Tabelle, 0 Header, 0 Zellen und eine eigene `columns`-Property auf dem noch nicht registrierten `<data-table>`. Damit ist das Custom-Element-Property-Shadowing unabhängig reproduziert. Der genaue nackte-Zeilen-Zustand aus Report 04 hing vom Verhältnis zwischen API-Antwort und Component-Import ab; hier antwortete die API vor der absichtlich verzögerten Registrierung.
- **Fix-Verifikation:** Auf `e3baab4` ergaben Direct-Load und anschließender Reload:
  - `#/partner/customers`: jeweils 3 Header, 15 Zellen, 5 Zeilen, 1 Filtergruppe und Suche vorhanden.
  - `#/partner/orders`: jeweils 5 Header, 75 Zellen, 15 Zeilen und 1 Filtergruppe.
- **Direkt betroffene Regressionen:** SPA-Navigation blieb vollständig:
  - Partner: Endkunden 3/15, Bestellungen 5/75, Produkte 4/12 Header/Zellen.
  - Firma: Partner 5/15, Produkte 6/48, Bestellungen 6/120 Header/Zellen.
  - Produktsuche `helm` reduzierte korrekt auf 1 Zeile mit 4 Zellen (`Schutzhelm gelb`). Filter- und Suchsteuerelemente blieben vorhanden.
- **Beobachtetes Verhalten:** Direct-Load, F5 und SPA-Navigation rendern Tabellen mit Headern, Zellen, Filtern und Suche. Kein leeres Listen-Rendering und kein Property-Shadowing auf dem Fix-Stand beobachtet.
- **Verbleibendes Risiko:** Das Projekt hat keinen persistenten Frontend-Test-Runner; die Absicherung ist daher browserbasiert. Für den geprüften Bootstrap- und Listen-Scope besteht keine bekannte Restlücke.
- **Follow-up:** Keines für UX-001.

## UX-002 — Warenkorbzeile überlappt Stückpreis

- **Verifikationsergebnis:** Verified fixed
- **Vorher-Reproduktion:** Auf dem Parent-Stand hatte der lange Testartikel „Größenwahn-Übermüller Präzisionswerkzeug für außergewöhnliche Baustellen“ bei 1440×900 eine Namensbreite von 0 px; `.cart-line` verwendete `flex-wrap: nowrap`. Der ursprüngliche Defekt ist damit unabhängig reproduziert.
- **Fix-Verifikation:** Derselbe Artikel wurde auf `e3baab4` in den Warenkorb gelegt. Gemessene Bounding-Boxes:

  | Viewport | Namensbreite | Preisbreite | Überschneidungsfläche | `flex-wrap` |
  |---|---:|---:|---:|---|
  | 1440×900 | 139,59 px | 50,41 px | 0 px² | `wrap` |
  | 390×844 | 130,59 px | 50,41 px | 0 px² | `wrap` |
  | 1920×1080 | 139,59 px | 50,41 px | 0 px² | `wrap` |

- **Direkt betroffene Regressionen:** Auf 390 px entstand kein horizontales Dokument-Scrolling (`documentElement.scrollWidth` 375 px bei 390 px Viewport). Gesamtwert und Provision blieben bei Menge 1 korrekt: 42,50 € und 4,25 € bei 10 %.
- **Beobachtetes Verhalten:** Der lange Produktname bricht lesbar um und kollidiert an keinem geprüften Viewport mit dem Stückpreis.
- **Bestellabschluss:** Bestellung Nr. 22 wurde über den geprüften Warenkorb erfolgreich für Fischer GmbH angelegt. Bestätigung: Gesamtwert 42,50 €. Anschließende Bestellliste: Wert 42,50 €, Provision 4,25 €, Status „Eingereicht“.
- **Verbleibendes Risiko:** Kein automatisierter visueller Regressionstest im Repository; für die drei geforderten Viewports und den realen Abschluss besteht keine bekannte Restlücke.
- **Follow-up:** Keines für UX-002.

## Ausgeführte Prüfungen

```bash
openspec validate fix-ux-reload-and-cart-overlap
git diff --check e3baab4^ e3baab4
cd backend
./mvnw test -Dquarkus.http.test-port=8091 -Dquarkus.keycloak.devservices.enabled=false -Dquarkus.oidc.auth-server-url=http://localhost:8081/realms/partnerportal -Dquarkus.oidc.client-id=partnerportal-frontend
./mvnw package -DskipTests
```

- `openspec validate`: erfolgreich; Change ist gültig.
- `git diff --check`: erfolgreich; keine Whitespace-Fehler.
- Backend-Testlauf über den dokumentierten Compose-Keycloak-Pfad: 76 Tests, 0 Failures, 0 Errors, 0 Skipped; Build erfolgreich.
- `./mvnw package -DskipTests`: erfolgreich.
- Browser: Chromium/Playwright, echter Dev-Stack, Rollen `partner` und `company`, 390×844, 1440×900 und 1920×1080.

## Abschlussurteil

UX-001 und UX-002 sind auf Fix-Commit `e3baab4` vollständig behoben. Beide Findings dürfen im Findings Register von `Fixed` auf `Verified` gesetzt werden.
