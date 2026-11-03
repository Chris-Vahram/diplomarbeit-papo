# Setup & Run — Partnerportal

Wie man die Entwicklungs-/Demo-Umgebung startet. Konventionen siehe `docs/CONVENTIONS.md`.

## Voraussetzungen

- Docker (Desktop) — für PostgreSQL + Keycloak
- Java 21 (JDK)
- Kein Node/npm nötig — das Frontend ist statisches HTML/JS ohne Build-Schritt

## Ports (fix)

| Dienst | URL |
|---|---|
| Quarkus-Backend | http://localhost:8080 |
| Keycloak | http://localhost:8081 (Admin: `admin` / `admin`) |
| PostgreSQL | localhost:5432 (`partnerportal` / `partnerportal`, DB `partnerportal`) |
| Frontend (statisch) | http://localhost:3000 (siehe unten) |
| Mailpit (Bestell-E-Mails, nur dev) | http://localhost:8025 (SMTP `127.0.0.1:1025`) |

Im Dev-Profil werden Bestell-E-Mails echt an Mailpit gesendet (Container `mailpit` aus
`docker-compose.yml`); dort lassen sich alle Mails ansehen, nichts verlässt den Rechner.
Tests nutzen den Quarkus-`MockMailbox`, auf der VM ist der Versand standardmäßig aus
(`docs/DEPLOYMENT.md`, Abschnitt Bestell-E-Mails).

## 1. Container starten (Postgres + Keycloak)

```bash
docker compose up -d
```

Keycloak importiert beim Start automatisch den Realm `partnerportal`
(`backend/src/main/resources/keycloak/partnerportal-realm.json`) mit den Rollen
`company` / `partner` und den Demo-Usern.

**Demo-Logins:**

| Benutzer | Passwort | Rolle |
|---|---|---|
| `company-demo` | `company-demo` | company (Firma 1 „Huemer GmbH“) |
| `partner-demo` | `partner-demo` | partner (Firma 1) |
| `company-zwei` | `company-zwei` | company (Firma 2 „Testfirma Zwei GmbH“, nur dev/test) |
| `partner-zwei` | `partner-zwei` | partner (Firma 2, nur dev/test) |

**Provisionierungs-Konvention:** Keycloak-User werden manuell angelegt und von
der Firma im Dialog „Logins“ (Partnerverwaltung) über ihre Keycloak-Benutzer-ID
mit dem Partner verknüpft. Firmenbenutzer werden über `company_login` an ihre Firma
gebunden (Mandantenfähigkeit). Alle vier Demo-User haben in beiden Realm-Dateien feste
IDs (`partner-demo` `7c1d9a2e-…`, `company-demo`/`company-zwei`/`partner-zwei`
`c0a1d2e3-0000-4000-8000-00000000000{1,2,3}`); der `DemoDataSeeder` legt Firma 2 und alle
Bindungen in dev/test an. Ein älterer lokaler Keycloak-Container hat noch eine zufällige ID —
dann einmal neu erstellen (kein Datenvolume, der Realm wird neu importiert), sonst
scheitert der Login mit „Kein Partnerkonto …“ bzw. „Kein Firmenkonto mit diesem Login verknüpft.“:

```bash
docker compose up -d --force-recreate keycloak
```

## 2. Backend starten (Dev-Modus)

```bash
cd backend
./mvnw quarkus:dev
```

Der `%dev`-Modus verbindet sich mit den Compose-Containern (Keycloak :8081, Postgres :5432).
**Wichtig:** `docker compose up` muss vorher laufen — Dev Services werden im Dev-Modus
bewusst NICHT genutzt, damit Frontend und Backend denselben Keycloak (gleicher Token-Issuer)
verwenden. Schema: `drop-and-create` bei jedem Start; Demodaten (Partner, Artikel) werden
danach programmatisch von `DemoDataSeeder` eingespielt (Dev-Daten sind Wegwerf-Daten).
Das Seed-Firmenprofil und die Steueridentifikatoren sind synthetische Demo-Werte
und keine echten Registerdaten.

## 3. Frontend starten

Mitgelieferter statischer Server (nur Python-Standardbibliothek), der wie Caddy in
Produktion `/shop/<kennung>` auf `shop.html` umleitet:

```bash
python3 tools/dev_frontend_server.py
```

Die Startmeldung nennt die Adressen. Lokal gilt dieselbe Zuordnung wie in Produktion
(`deploy/caddy/Caddyfile`):

| Pfad | Inhalt |
|---|---|
| `/` | öffentliche Landing-Page (`landing.html`); mit offener Portal-Sitzung im selben Tab Weiterleitung nach `/portal/` |
| `/portal/` | Portal (`index.html`, Hash-Routen wie `/portal/#/login`); `/portal` leitet um |
| `/impressum`, `/datenschutz` | statische Rechtsseiten |
| `/shop/<kennung>` | öffentlicher Partner-Shop (`shop.html`) |
| andere Pfade | vorhandene Datei oder echte 404 mit `404.html` |

Zum Anmelden http://localhost:3000/portal/ öffnen und einen Demo-User verwenden.
Das Frontend spricht das Backend über `http://localhost:8080/api` an (CORS ist im
Backend aktiviert); Login-Tokens holt es direkt vom Keycloak auf :8081.

### Öffentlichen Partner-Shop lokal öffnen

Der Shop hat einen eigenen Einstieg `frontend/shop.html`, der keinen Portal-Code lädt.
Caddy (Produktion) und `tools/dev_frontend_server.py` (lokal) beantworten jeden Pfad
`/shop/<kennung>` damit; der Link sieht lokal und live gleich aus.

1. Als `company-demo` anmelden → Partnerverwaltung → beim Partner **Webshop** →
   **Freigeben**. Der Partner braucht mindestens einen freigeschalteten, verfügbaren Artikel.
2. Den angezeigten Link öffnen, lokal `http://localhost:3000/shop/<kennung>`.

Als Partner zeigt **Mein Webshop** denselben Link und den Status. Unbekannte oder
offline geschaltete Shops zeigen „Dieser Shop ist derzeit nicht erreichbar.“
Das Rate-Limit der öffentlichen Shop-API (Standard 300 Anfragen/Minute je IP; eine Shopseite braucht etwa fünf Aufrufe plus einen je Artikelbild) ist über
`SHOP_RATE_LIMIT_PER_MINUTE` einstellbar.

Öffentliche Shops sind lokal (dev) eingeschaltet, in Produktion standardmäßig aus
(`SHOP_PUBLIC_ENABLED`, siehe `docs/DEPLOYMENT.md`). Zum Testen des ausgeschalteten
Zustands lokal: `SHOP_PUBLIC_ENABLED=false ./mvnw quarkus:dev` – Shopseiten zeigen dann
immer „nicht erreichbar“, Portal und Partner sehen „Öffentliche Shops sind auf dieser
Umgebung noch deaktiviert.“

### Screenshots der Landing-Page neu erzeugen

Die Bilder unter `frontend/assets/landing/` sind echte Aufnahmen der lokalen Demo-Daten
(Firmen-Dashboard, Bestellliste, Neue Bestellung, Partner-Dashboard, Shop-Baukasten, Artikelseite des Demo-Shops). Nach
sichtbaren UI-Änderungen bei laufender lokaler Umgebung (Schritte 1–3) neu erzeugen:

```bash
npm i --prefix /tmp/pw playwright-core
PLAYWRIGHT_MODULE=/tmp/pw/node_modules/playwright-core/index.mjs \
  CHROMIUM=<Pfad zu einem Chromium> node tools/render_landing_screenshots.mjs
```

Das Skript läuft nur gegen `localhost`, meldet sich mit `company-demo` und `partner-demo`
an und schreibt je Bild ein `.webp.json` mit der Herkunft. Vor dem Commit die Bilder auf
reine Demo-Daten ansehen.

### Platzhalter in den Rechtsseiten

`tools/check_public_placeholders.sh` listet alle `[[…]]`-Platzhalter in Landing,
Impressum, Datenschutz und 404 und endet mit Fehlercode, solange einer übrig ist.

## 4. Tests

```bash
cd backend
./mvnw test
```

Tests brauchen **kein** laufendes docker compose: der `%test`-Modus nutzt Quarkus Dev Services
(eigene Wegwerf-Container für Postgres + Keycloak, gleicher Realm-Import). Docker muss laufen.

## 5. Demo-/Prod-Profil

Für Demo-Läufe (gepackte App, `%prod`): gleiche Compose-Container, aber Schema-Strategie
`update` statt `drop-and-create` — Demodaten überleben Neustarts. `DemoDataSeeder` läuft im
`%prod`-Modus bewusst NICHT (kein Auto-Seed). Daten kommen über den Excel-Import (Phase 5)
oder werden vorher im Dev-Modus angelegt und bleiben dank `update` erhalten.

```bash
cd backend
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

`%prod`-Zugangsdaten (DB, OIDC-Issuer) sind über Umgebungsvariablen überschreibbar, damit ein
gepackter Lauf nicht stillschweigend die Demo-Standardwerte verwendet:

| Variable | Standardwert |
|---|---|
| `DB_USERNAME` | `partnerportal` |
| `DB_PASSWORD` | `partnerportal` |
| `DB_JDBC_URL` | `jdbc:postgresql://localhost:5432/partnerportal` |
| `OIDC_AUTH_SERVER_URL` | `http://localhost:8081/realms/partnerportal` |

Für den lokalen Demo-Lauf mit den Compose-Containern können die Standardwerte unverändert bleiben.

## Sicherheitshinweise (bewusste Prototyp-Abkürzungen)

Nur für den lokalen Schulprototyp akzeptabel, nicht für echten Betrieb:

- Login per **Direct Access Grant** (Passwort-Formular direkt gegen Keycloak) statt Redirect-Flow.
- Access-Token im `sessionStorage` des Browsers.
- Keycloak-Client `partnerportal-frontend` hat `redirectUris`/`webOrigins` = `*`.
- CORS im Backend offen für lokale Origins.
- Feste Demo-Passwörter (identisch zum Benutzernamen), `admin`/`admin` für Keycloak.
- HTTP statt HTTPS.

Die eigentliche Sicherheitsgrenze (FR-20) ist die serverseitige Rollenprüfung
(`@RolesAllowed`) auf jedem geschützten Endpoint — der Frontend-Router-Guard ist nur UX.
