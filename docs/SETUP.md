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
| `company-demo` | `company-demo` | company |
| `partner-demo` | `partner-demo` | partner |

## 2. Backend starten (Dev-Modus)

```bash
cd backend
./mvnw quarkus:dev
```

Der `%dev`-Modus verbindet sich mit den Compose-Containern (Keycloak :8081, Postgres :5432).
**Wichtig:** `docker compose up` muss vorher laufen — Dev Services werden im Dev-Modus
bewusst NICHT genutzt, damit Frontend und Backend denselben Keycloak (gleicher Token-Issuer)
verwenden. Schema: `drop-and-create` bei jedem Start; Demodaten (Partner, Produkte) werden
danach programmatisch von `DemoDataSeeder` eingespielt (Dev-Daten sind Wegwerf-Daten).

## 3. Frontend starten

Beliebiger statischer Server im Ordner `frontend/`, z. B.:

```bash
cd frontend
python3 -m http.server 3000
```

Dann http://localhost:3000 öffnen und mit einem Demo-User anmelden.
Das Frontend spricht das Backend über `http://localhost:8080/api` an (CORS ist im
Backend aktiviert); Login-Tokens holt es direkt vom Keycloak auf :8081.

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
