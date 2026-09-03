# Deployment — Partnerportal

Produktionstopologie, CI/CD-Pipeline und Rollback-Prozedur. Setup/Dev-Umgebung siehe
`docs/SETUP.md`. Hintergrund und Entscheidungen: `openspec/changes/ci-cd-deployment-with-gh-actions/design.md`.

## Zielsystem

- VM `papo-server-01` (`papo@178.104.109.45`), Ubuntu, Docker + Docker Compose.
- Vier Container über `docker-compose.prod.yml`: `backend` (Quarkus, aus GHCR), `postgres`
  (eine Instanz, zwei Datenbanken `partnerportal` + `keycloak`), `keycloak`, `caddy`
  (Reverse Proxy + automatisches HTTPS).
- Ein Domain, pfadbasiertes Routing (Caddy): `/` → Frontend (statisch), `/api/*` → Backend,
  `/auth/*` → Keycloak. Dadurch **kein CORS** nötig — Frontend und API sind same-origin.
- UFW ist seit 03.09.2026 aktiv: eingehend standardmäßig gesperrt, TCP 22/80/443 für
  SSH, HTTP und HTTPS erlaubt. Externe Erreichbarkeit von 80/443 wurde mit kurzlebigen
  Test-Listenern geprüft; der echte Caddy-/TLS-Test ist noch offen.

## Pipeline (`.github/workflows/ci-cd.yml`)

1. **`test`** — `./mvnw test` bei jedem Push nach `main` und jedem Pull Request.
2. **`build-and-push`** (nur bei Push nach `main`, nach erfolgreichem `test`) — baut das
   Backend-Image (`Dockerfile.jvm`, `linux/amd64`) und pusht es nach GHCR, getaggt
   `latest` und `<git-sha>`.
3. **`deploy`** (nach erfolgreichem `build-and-push`) — verbindet sich per SSH mit der VM
   und führt dort `docker compose pull && docker compose up -d` aus. Nur der `backend`-
   Container wird dabei ersetzt; Postgres und Keycloak laufen unverändert weiter.

Ein fehlgeschlagener Test-Job verhindert Build und Deploy vollständig.

## Benötigte Secrets / Env-Variablen

**GitHub Actions Secrets** (Repo-Settings → Secrets and variables → Actions):

| Secret | Zweck |
|---|---|
| `VM_HOST` | IP/Hostname der VM (`178.104.109.45`) für den `deploy`-Job |
| `VM_SSH_KEY` | Privater Schlüssel des dedizierten Deploy-Keypairs (nicht der persönliche Login-Key) |
| `DB_PASSWORD` | Postgres-Passwort (Referenz — der eigentliche Wert lebt in `.env.prod` auf der VM) |
| `KC_ADMIN_PASSWORD` | Keycloak-Admin-Passwort (Referenz — Wert lebt in `.env.prod` auf der VM) |

`GITHUB_TOKEN` (automatisch von Actions bereitgestellt) authentifiziert den GHCR-Push —
kein zusätzliches Secret nötig.

**VM-lokal, nicht committed** (`.env.prod`, Vorlage: `.env.prod.example`):

| Variable | Zweck |
|---|---|
| `DB_USER`, `DB_PASSWORD` | Postgres-Zugangsdaten (Datasource + Keycloak-DB) |
| `KC_ADMIN_PASSWORD` | Keycloak-Admin-Passwort |
| `DOMAIN` | Produktionsdomain — steuert `KC_HOSTNAME`, Caddy-TLS, Keycloak `redirectUris`/`webOrigins` (siehe `deploy/keycloak/render-and-start.sh`) |
| `IMAGE_TAG` | Welches Image-Tag `docker-compose.prod.yml` zieht (normal `latest`, siehe Rollback) |

Das GHCR-Package ist **privat**; die VM authentifiziert sich einmalig per
`docker login ghcr.io` mit einem VM-lokalen, read-only PAT (nicht in GitHub Secrets und
nicht im Repo). Chris erstellt den `read:packages`-PAT interaktiv; der Agent darf ihn nie
in Ausgaben oder Dokumentation übernehmen.

## Verzeichnis auf der VM

Der `deploy`-Job erwartet `docker-compose.prod.yml`, `deploy/` (Caddyfile, Postgres-Init-
Skript, Keycloak-Realm-Template, Backup-Skript) und `.env.prod` unter `~/papo-deploy` auf
der VM (einmalig kopiert/geklont, nicht Teil der Pipeline selbst).

## Rollback (manuell)

Kein automatisches Rollback in dieser Pipeline. Bei einem fehlerhaften Deploy:

```bash
ssh papo@<VM_HOST>
cd ~/papo-deploy
# IMAGE_TAG in .env.prod auf eine bekannt-gute Commit-SHA setzen, z. B.:
sed -i 's/^IMAGE_TAG=.*/IMAGE_TAG=<bekannt-gute-sha>/' .env.prod
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d
```

Jedes Image wird sowohl mit `latest` als auch mit der Commit-SHA getaggt — die SHA bleibt
also für einen manuellen Rollback erreichbar, auch wenn `latest` inzwischen weitergezogen ist.

## Backups

`deploy/backup/backup.sh` dumpt beide Datenbanken (`partnerportal`, `keycloak`) lokal auf
die VM und behält die letzten 14 Dumps pro Datenbank. Cron-Installation (VM-seitig, noch
offen):

```
0 3 * * * /home/papo/papo-deploy/deploy/backup/backup.sh >> /home/papo/papo-deploy/deploy/backup/backup.log 2>&1
```

Offsite-Kopie erfolgt in der nächsten Deployment-Phase über einen S3-kompatiblen Bucket
und VM-lokales `rclone`. Zugangsdaten wurden privat bereitgestellt. Vor der Einrichtung
müssen Endpoint, Bucket und Schreibrecht geprüft werden; Werte gehören weder ins Repo
noch in Terminal-/KI-Ausgaben. Danach ist ein Upload- und Restore-Test erforderlich.

## CORS

CORS ist bewusst nur im `%dev`-Profil aktiv (`application.properties`, für den separat
laufenden lokalen Frontend-Devserver auf Port 3000). Im `%prod`-Profil ist keine CORS-
Konfiguration gesetzt — Caddy serviert Frontend und Backend same-origin (siehe oben), daher
ist CORS dort schlicht nicht nötig, nicht nur deaktiviert.
