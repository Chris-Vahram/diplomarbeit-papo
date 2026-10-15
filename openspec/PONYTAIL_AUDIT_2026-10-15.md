# Ponytail-Audit PAPO — Plan (15.10.2026)

Scope: Überkomplexität im ganzen Repo (Stand Branch `feat/bind-partner-login-to-oidc-subject`,
inkl. B-04/B-08 und Tobias' Shop-Specs). Bugs/Security/Performance sind nicht Teil davon.
**Regel:** Es darf keine wichtige Funktion verloren gehen. Jede Kürzung bekommt einen Test
oder eine Browserprüfung, die das unveränderte Verhalten zeigt. Noch nichts umgesetzt.

## Befunde, nach Einsparung sortiert

| # | Tag | Was | Ersatz | Pfad | ~Zeilen | Risiko / Prüfung |
|---|---|---|---|---|---|---|
| 1 | delete | Ungenutzter CSS-Block „Design-Konflikte“ (`.conflict-*`, `.conflicts-*`), kein JS/HTML nutzt ihn | nichts | `frontend/src/styles/base.css` ~3431–3570 | −140 | Tobias' CSS → nur mit seinem OK; grep + Browser-Smoke |
| 2 | stdlib | Handgeschriebener PNG-Encoder (Chunks, CRC, Deflate) für zwei einfarbige Demo-Bilder | `javax.imageio.ImageIO.write(BufferedImage, "png", out)` | `product/DemoPngGenerator.java` | −65 | Demo-Seed-Bilder; Test: Bild ist gültiges PNG, gleiche Größe/Farbe |
| 3 | shrink | `Product.category` (Text) dupliziert `articleCategory.name`; `CategoryService.update()` sperrt und überschreibt dafür alle zugeordneten Artikel; der Seeder schreibt nur den Text und braucht deshalb `CategoryMigrationService` | Seeder legt Kategorien + Fremdschlüssel direkt an, danach Textspalte nur noch lesen/auflösen und später entfernen | `seed/DemoDataSeeder.java`, `category/CategoryService.java`, `product/*`, `CategoryMigrationService` | −60…−120 | Eigener OpenSpec-Change (Datenmodell!); Upgrade- und Rollback-Pfad prüfen, frische DB: Kategorienliste + Zuordnungen |
| 4 | shrink | Doppelt kopiert in beiden Dashboards: `eur`, `dateLabel`, `formatCompactEur`, `renderMonthChart`, `wirePeriodPicker`, `updatePeriodUi` | ein gemeinsames Modul `lib/dashboard-ui.js` | `pages/company-dashboard-page.js`, `pages/partner-dashboard-page.js` | −110 | Tobias' Markup bleibt identisch; Playwright-Vergleich beider Dashboards |
| 6 | delete | Getrackter Müll: `tmp/pdfs/` (gerenderte KI-Handreichung, 2,1 MB), `.scratch/current/*` | nichts (Git-Historie) | Repo-Wurzel | −12 Dateien | Nicht referenziert; `.gitignore` ergänzen |
| 7 | delete | `CategoryService.resolveForProduct` ohne Aufrufer | nichts | `category/CategoryService.java` | −8 | Compile + Tests |
| 8 | delete | `hasRole` ohne Aufrufer | nichts | `frontend/src/lib/auth.js` | −5 | node --check |
| 9 | delete | `Address.toDisplayString` ohne Aufrufer | nichts | `common/Address.java` | −5 | Compile |
| 10 | yagni | Redundante Abhängigkeiten `quarkus-hibernate-orm` und `quarkus-arc` (von Panache/REST transitiv geliefert) | entfernen | `backend/pom.xml` | −8, −2 deps | `./mvnw package` + Tests |
| 11 | delete | Kyrillische/griechische Font-Subsets (Seiten sind deutsch; per `unicode-range` nie geladen) | nichts | `frontend/vendor/fonts/*cyrillic*`, `*greek*` + `@font-face` | −68 KB, −~30 CSS | Umlaute/ß bleiben in latin/latin-ext; Browser-Check |
| 12 | shrink | `inPeriod` muss nicht exportiert sein | `function` statt `export function` | `lib/dashboard-data.js` | 0 | — |

## Bewusst **nicht** kürzen (Funktion/Nachweis)

- `PartnerRequest` als JavaBean: unterscheidet „Notiz nicht gesendet“ von „Notiz geleert“ (`internalNoteProvided`).
- `output/playwright/*`, `docs/partner_portal_audit_suite/*`: von `docs/UI_UX_DOCUMENTATION.md`,
  `docs/REQUIREMENTS_TRACEABILITY.md` und der Präsentation als Nachweis referenziert.
- opencsv/POI: festgelegter Stack für den Import.
- Deploy-Gateway/Rollback, Outbox/Retry (B-04), Fallback (OIDC Release A, kommt mit Release B weg).
- `netty-resolver-dns-native-macos` (test-scope, behebt lokale Testinstabilität).
- Shop-Specs von Tobias: noch nicht implementiert, kein Audit-Gegenstand.
- `BusinessTaxIdentifierMigrationService`: setzt die einzigen NOT-NULL-Constraints der
  Steuerfelder und liefert `lockPartnerTable()` für den Seeder (Codex-Review 15.10.).
- `CategoryMigrationService`: erzeugt auch auf frischen DBs die Kategorien aus dem Seeder
  (bis #3 umgesetzt ist).
- `HelloResource`: drei Testklassen (Rollen, Paket-Smoke) und der FR-20-Nachweis in
  `docs/REQUIREMENTS_TRACEABILITY.md` hängen daran — Aufwand größer als Gewinn.

net: ca. −380…−440 Zeilen (davon #3 als eigener Change), −2 Dependencies, −14 Dateien möglich.

**Codex-Adversarial-Review 15.10.:** „needs-attention“ für die erste Fassung (alte #3 Migrationen löschen, alte #5 `/hello` löschen); beide Punkte gestrichen bzw. als Datenmodell-Change neu gefasst. Übrige Kürzungen ohne Blocker.

## Umsetzung (nach Freigabe)

Ein Branch `chore/ponytail-audit`, ein Commit je Befund (Regel aus CLAUDE.md), nach jedem
Commit relevante Tests. #1 nur mit Tobias' OK, #3 nur als eigener OpenSpec-Change.
