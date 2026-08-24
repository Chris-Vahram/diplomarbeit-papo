# 01 — Projekt-Übersicht & Ground-Truth-Report

- **Audit-Datum:** 2026-07-16
- **Baseline-Commit (Beginn Audit-Fenster):** `8ef0d90` (letzter Stand vor `add-product-image-upload`, 2026-07-09)
- **Geprüfter HEAD:** `9a9f5cd` (main, clean)
- **Neuestes Meeting-Dokument:** `Documentation/Besprechungsprotokolle/Besprechungsprotokoll10_07_2026.txt`
- **Phase:** 1 von 5 (Orientierung, read-only — kein Bug-/Security-/UX-Audit in diesem Report)

---

## 1. Executive Overview

PAPO (Partnerportal) ist ein webbasiertes, **internes B2B-Bestellportal** für partnerbasierte Vertriebsmodelle (Kette: Firma → Partner → Endkunde). HTL-Diplomarbeit von **Chris (Implementierung: Backend + funktionales Frontend)** und **Tobias (visuelles Frontend-Design)**, mit realem, unbenanntem Referenzkunden (~200 Partner, ~800 Artikel; Bestellungen heute manuell per E-Mail/Telefon). **Reale Deployment-Absicht**: Hetzner-VM ist provisioniert, Deployment-Change (`ci-cd-deployment-with-gh-actions`) gehört Tobias.

**Stand:** Der MVP ist funktional im Wesentlichen fertig (50/53 Tasks; die 3 offenen sind Doku/Design, kein Code). Alle Asks aus dem Kundenmeeting 10.07.2026 sind umgesetzt und gemerged. Aktueller verifizierter Stand: **77/77 Tests grün**. Es wartet primär: echte Import-Daten vom Kunden (Spaltensets), Thesis-Writeup, Tobias' Design-Polish + Deployment.

## 2. Architektur & Kernflüsse

**Stack (fix):** Frontend HTML/CSS/native Web Components (kein Framework, Hash-Router-SPA, statisch serviert) · Backend Quarkus 3 (REST, Hibernate ORM Panache, OIDC) · PostgreSQL · Keycloak (Realm `partnerportal`, Rollen `company`/`partner`) · Import: Apache POI + opencsv. Schema: Hibernate-Autogenerierung (dev/test drop-and-create + `DemoDataSeeder`; demo/prod update), kein Flyway.

**Backend-Pakete** (`backend/src/main/java/at/papo/partnerportal/`): `partner` (CRUD, Self-Edit, `PartnerNumber.canonicalize()`), `product` (CRUD + `ProductImage`-Upload/Galerie/Serving), `enablement`, `customer` (Kunde, partner-gescoped, `lastOrderDate`-Query), `order` (Order/OrderItem mit Unit-Price-Snapshot, Status submitted→reviewed→fulfilled), `commission` (`CommissionRecord`), `dashboard`, `importer` (Mapping + Dedupe + Validierungsreport), `export` (CSV), `security` (`CurrentPartner.resolve()`: Login-Link via kanonisierter `partnerNumber`, `active`-Flag serverseitig), `seed`.

**Frontend** (`frontend/src/`): 12 Pages (Login, je Rolle Dashboard/Listen, `partner-order-new` = Katalog+Warenkorb, `partner-customer-list` = „Meine Endkunden"), `lib/` (`money.js` = kanonische Integer-Cent/HALF_UP-Mathematik, `api.js` mit deutschen Fehler-Toasts, `auth.js` mit Keycloak-Refresh-Flow, `order-cart.js`, `order-form.js` [nur noch Company-Flow], `order-details.js`, `router.js` mit `#print-root`-Druckmechanismus), `components/` (u.a. `data-table.js`, `image-chooser.js`).

**Kernfluss:** Partner loggt ein (Keycloak) → Dashboard (Landing) → wählt/erstellt Endkunden (Nachname+Adresse Pflicht) → bestellt mehrere freigeschaltete Artikel via Katalog+Warenkorb (Live-Summen + Provisions-Vorschau) → Order automatisch dem Partner zugeordnet → Bestätigung on-screen + simulierte E-Mail → Firma schaltet Status weiter, Provision als `CommissionRecord`-Snapshot.

## 3. MVP-Status-Matrix (FR-01..FR-25)

Konfidenz: H = Code + Tests + Session-Verifikation; M = Code vorhanden, in dieser Phase nicht tief geprüft. Kein Tiefen-Audit hier — Auffälligkeiten sind als spätere Review-Ziele markiert (§6).

| FR | Beschreibung (kurz) | Status | Implementierungs-Evidenz | Test-Evidenz | Offen | Konf. |
|---|---|---|---|---|---|---|
| FR-01 | Partnerverwaltung (inkl. Provision %, aktiv/inaktiv) | complete | `partner/PartnerService`, `company-partner-list-page` | `PartnerResourceTest` | — | H |
| FR-02 | Produktverwaltung | complete | `product/ProductService`, `company-product-list-page` | `ProductResourceTest` | — | H |
| FR-03 | Produktdaten (Name, Preis, Beschreibung, Verfügbarkeit; opt. Kategorie/Bild) | complete | `Product`-Entity; Bild via URL **oder Upload** (`ProductImage`) | `ProductResourceTest`, `ProductImageResourceTest` | Upload geht über „image reference" hinaus → Scope-Review §2 | H |
| FR-04 | Excel/CSV-Import (Mapping, Dedupe, Report) | partially / blocked | `importer/ImportService` (suggestMapping, mapRow), `company-import-page` | `PartnerImportResourceTest`, `ProductImportResourceTest` | Echte Client-Spaltensets ausständig (seit 10.07, ~1 Woche) → Default-Mapping unbestätigt | H |
| FR-05 | Produktfreischaltung je Partner | complete | `enablement/` | `EnablementResourceTest` | — | H |
| FR-06 | Partner sieht/bestellt nur Freigeschaltetes | complete | Partner-Endpoints filtern auf Enablement | `EnablementResourceTest`, `OrderResourceTest` | serverseitige Durchsetzung in Phase 3 aktiv testen | H |
| FR-07 | Interner Partnerbereich | complete | `partner-*-page.js` (Profil, Produkte, Orders, Dashboard) | Playwright-Verifikationen (Sessions 07-14/15) | — | H |
| FR-08 | Partner-Self-Edit (begrenzt) | complete | `PartnerService`-Self-Edit, `partner-profile-page` | `PartnerResourceTest` | — | H |
| FR-09 | Partner-Dashboard (fulfilled-only Totale, Landing) | complete | `dashboard/`, `partner-dashboard-page`; all-time-Default korrigiert (`order-correctness-hardening`) | `DashboardResourceTest` | „ggü. Vorperiode"-Trend rendert bei explizitem Zeitraum → B-08-Grauzone, Scope-Review | H |
| FR-10 | Kunde wählen/anlegen, wiederverwendbar, partner-gescoped | complete | `customer/KundeService` (Scoping + `lastOrderDate`) | `KundeResourceTest`, `KundeServiceTest` (59/60/61-Tage-Grenzen) | — | H |
| FR-11 | Bestellung mit mehreren Artikeln+Mengen | complete | `order/OrderService`, `order-cart.js` | `OrderResourceTest` + Playwright | — | H |
| FR-12 | Auto-Zuordnung Order→Partner | complete | `OrderService` via `CurrentPartner` | `OrderResourceTest` | — | H |
| FR-13 | Order-Daten (ID, Datum, Partner, Kunde, Items, Status, Wert) | complete | `Order`/`OrderItem` (Unit-Price-Snapshot), Tabelle `orders` | `OrderResourceTest` | — | H |
| FR-14 | Sichtbarkeit: Firma alle / Partner eigene | complete | getrennte Resources | `OrderResourceTest` | Isolation in Phase 3 aktiv angreifen | H |
| FR-15 | Status submitted→reviewed→fulfilled, nur Firma | complete | `CompanyOrderResource` | `OrderResourceTest` | — | H |
| FR-16 | On-Screen-Bestätigung | complete | `renderOrderConfirmationHtml` | Playwright | — | H |
| FR-17 | Simulierte Bestell- + Versandbestätigung | complete | in-app gerendert; Versandbestätigung nachgerüstet (`162424f`) | Playwright | — | H |
| FR-18 | Produktinhalte nur Firma | complete | keine Partner-Schreib-API auf Produkte | Test-Abdeckung prüfen (Phase 3) | — | M |
| FR-19 | Provision je Order (%, Dropdown 5/10/15+custom, eigener Record) | complete | `commission/CommissionRecord`, BigDecimal HALF_UP; Frontend-Spiegel `money.js` | `OrderResourceTest`; Byte-genaue Verifikation 7 % auf krummer Summe (Session 07-14) | Geld-Grenzfälle nochmal in Phase 3 | H |
| FR-20 | RBAC Keycloak, kein Deep-Link-Zugriff | complete | `@RolesAllowed`, Router-Guards | `KeycloakTestTokens`-basierte Tests | Phase 3: aktiv testen | H |
| FR-21 | Pflichtfeld-Validierung | complete | Bean Validation, deutsche Messages | Resource-Tests | — | H |
| FR-22 | Verständliches Feedback | complete | `api.js` extractErrorMessage → deutsche Toasts | Playwright | — | H |
| FR-23 | Inaktiver Partner: kein Login/keine Orders | complete | `Partner.active` in `CurrentPartner.resolve()`; Block nachgerüstet (`162424f`) | `OrderResourceTest`/`PartnerResourceTest` | Phase 3: jeden Endpoint prüfen | H |
| FR-24 | Nicht verfügbare Produkte nicht bestellbar | complete | Katalog disabled + serverseitige Prüfung | `OrderResourceTest` | Unhappy-Path-Abdeckung prüfen | M |
| FR-25 | Keine freigeschalteten Produkte → Meldung, keine Order | complete | Leer-Zustand `partner-order-new` | Playwright (temporärer Zero-Enablement-Partner, Session 07-14) | automatisierter Test fehlt evtl. | M |

**Kein FR ist „not implemented" oder „blocked" außer dem Client-Daten-Anteil von FR-04.**

## 4. Letzte Arbeiten (OpenSpec + Git)

### Abgeschlossen & gemerged, aber NICHT archiviert (8 Changes — Hygiene-Punkt)
`add-order-list-filters`, `improve-order-form`, `add-keycloak-session-refresh`, `self-host-fonts`, `order-correctness-hardening`, `add-dormant-kunde-visibility`, `partner-order-cart`, `remove-keycloak-username-field` — alle ✓ Complete laut `openspec list`, liegen aber noch in `changes/` statt `archive/`. `build-partnerportal-mvp` (50/53) blockiert laut CONTINUATION_PROMPT die Archivierung von `order-correctness-hardening` (Spec-Sync-Reihenfolge).

### Aktiv / offen
- `build-partnerportal-mvp` 50/53 — offen nur: 8.3 (UI/UX-Doku, **Tobias**), 8.4 (Thesis-Writeup, C+T), FD.4 (Design-Polish neue Screens, **Tobias**).
- `add-order-document-print` 9/10 — offen nur Task 4.5/5.5: Firefox-Print-Preview-Check (braucht Chris' Maschine).
- `ci-cd-deployment-with-gh-actions` 0/40 — **Tobias, nicht auditiert** (Regel eingehalten).

### Änderungen mit unklarem/prüfwürdigem Ursprung (→ Phase 2 Scope-Review)
- `add-order-document-print` — kein Kunden-Ask; interne Review-Entscheidung „Direction B Auftragsbeleg" (2026-07-09, von Chris freigegeben).
- `add-product-image-upload` — im Proposal selbst als „requested by Tobias, **not a client-meeting requirement**" markiert.
- **B-08 (resolved 2026-08-24):** `frontend/src/lib/dashboard-data.js` (`previousPeriod`/`trendInfo`, „ggü. Vorperiode") stammt aus Tobias' `229a93e` und rendert Trend-Pfeile bei einem expliziten Zeitraum. Der Backlog dokumentiert diesen Partner-Dashboard-Teil nun als `STUB`; vollständige unternehmensweite Trendanalyse bleibt deferred.

### Tobias zuzuordnen
`ci-cd-deployment-with-gh-actions` (0/40), `229a93e`/`8ef0d90` (frontend-fixes-and-additions inkl. B-08-Trend), Tasks 8.3 + FD.4, Design-Lane „einfache Benutzerführung + gute Mobile-Version" aus 10.07.

## 5. Aktuelle Projektrisiken

1. **Import-Feindaten fehlen** (FR-04): „1–2 Wochen" ab 10.07 läuft; ohne echte Spaltensets bleibt das Default-Mapping unbestätigt → Nachfassen.
2. **Rollen-Isolation nie adversarial getestet** (in dieser Audit-Runde): Kunde-/Order-/Commission-/CSV-Export-Scoping ist implementiert und unit-getestet, aber ein aktiver Cross-Partner-Angriffstest steht aus → Phase 3.
3. **Neue Angriffsfläche Bild-Upload**: Multipart-Upload, Content-Type-Validierung, Byte-Serving — sicherheitsrelevant, neu, erst oberflächlich geprüft → Phase 3.
4. **B-08-Drift (erledigt):** vorhandenen Partner-Dashboard-Trend behalten und als `STUB` dokumentieren; vollständige Analyse bleibt Phase 2.
5. **OpenSpec-Hygiene**: 8 fertige Changes unarchiviert; `DATA_MODEL.md` laut CONTINUATION_PROMPT bzgl. ProductImage stale.
6. **Session-Refresh-Fluss** (`auth.js`): still-scheiternde Refreshes wären für nicht-technikaffine Partner besonders verwirrend → Phase 3 (Silent-Failure-Fokus).

## 6. Empfohlene Ziele für spätere Phasen

- **Phase 2 (Scope):** Verdikt je Change der Liste in §4; Gold-Plating-Check Bild-Upload (Galerie nötig?) und Print; Komplexitätscheck order-cart. B-08 ist als `STUB` geklärt.
- **Phase 3 (Technik):** aktiver Cross-Partner-API-Angriff (Kunden/Orders/Commission/Export); `active`-Flag je Endpoint; Geld-Grenzfälle (krumme Summen × custom %, Integer-Cent vs. Backend byte-genau); Quantity-Clamp client+server; Dormant-Query (N+1, 60-Tage-Grenze); Bild-Upload (Magic Bytes, SVG, Größe, Serving-Content-Type); Print-Pagination mehrseitig; `auth.js`-Refresh-Fehlerpfade; Import-Parser-Kanten (leere Header-Zelle ist gefixt — Regression prüfen).
- **Phase 4 (UX):** neue Screens (Katalog+Warenkorb, Meine Endkunden, Order-Filter, Bildergalerie, Beleg) auf Desktop **1920×1080 + 1440×900** UND Mobile **390×844**; Umlaut-Rendering; Verständlichkeit für nicht-technikaffine Partner; Token-Konformität gegen `Design/variables.css` + `base.css`.
- **Phase 5:** Register konsolidieren, P0–P4/REMOVE/DEFER/DECISION, „3 Dinge vor dem Termin".

## 7. Offene Entscheidungen (nicht blockierend, nicht selbst entscheiden)

| # | Frage | Wer |
|---|---|---|
| 1 | Dormanz-Schwelle 60 Tage bestätigen | Kunde (nächster Termin) |
| 2 | Import-Spaltensets / echte Daten | Kunde (überfällig-nahe) |
| 3 | B-08-Entscheidung | Erledigt 2026-08-24: Partner-Trend bleibt als STUB |
| 4 | Forschungsfragen-Sign-off | Erledigt: Forschungsfrage vom Betreuer bestätigt |
| 5 | Firefox-Print-Check (Task 4.5) | Chris (eigene Maschine) |
| 6 | Domain für Deployment | Bachl (bei Deployment-Start) |

## 8. Evidenz & Limitationen

- **Gelesen (vollständig):** CONTINUATION_PROMPT.md, CLAUDE.md, FSD v2, phase2_backlog.md, Besprechungsprotokoll 10.07, Proposals von `add-order-document-print` / `self-host-fonts` / `order-correctness-hardening` / `improve-order-form` / `add-product-image-upload` (archiviert).
- **Ausgeführt:** `git status`/`git log -60`, `openspec list` (11 Changes) + `openspec validate --all` (22/22 ✓), `./mvnw test` (**exit 0, 76 Tests, 14 Klassen, kein Keycloak-Flake** — `netty-resolver-dns-native-macos` inzwischen in `pom.xml`), Struktur-Inventar Backend/Frontend/Tests, gezielte Greps (Import-Mapping, B-08-Trend inkl. `git log -S trendInfo` → `229a93e`, Autor Thuemer).
- **Limitationen:** Kein Tiefen-Code-Audit, keine Security-Tests, kein UX-Durchlauf in dieser Phase (bewusst — Phasen 3/4). Status-Matrix-Einträge mit Konfidenz M beruhen auf Code-Existenz + früheren Session-Verifikationen, nicht auf frischer Prüfung. `ci-cd-deployment-with-gh-actions` bewusst nicht inspiziert (Tobias-Regel). Dev-Stack läuft (Postgres+Keycloak+Quarkus:dev+Frontend:3000) — bereit für Phase 3/4.
