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

## Pipeline (`.github/workflows/ci-cd.yml`)

1. **`test`** — `./mvnw test` bei jedem Push nach `main` und jedem Pull Request.
2. **`build-and-push`** (nur bei Push nach `main`, nach erfolgreichem `test`) — baut das
   Backend-Image (`Dockerfile.jvm`, `linux/amd64`) und pusht es nach GHCR, getaggt
   `latest` und `<git-sha>`.
3. **`deploy`** (nach erfolgreichem `build-and-push`) — sendet ein kanonisches Frontend-
   Archiv an den Forced-Command-Gateway und ruft `upload`, `verify`, `activate` für den
   exakten `main`-SHA auf. Der Gateway ersetzt nur `backend`, hängt danach
   `frontend-live/current` atomar um und lässt Postgres, Keycloak und Caddy unverändert.

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
| `SEED_DEMO_DATA` | Opt-in: Demo-Daten auf einer leeren DB seeden (siehe Abschnitt "Demo-Daten (`SEED_DEMO_DATA`)"); Default `false`, nie im Image |

Das GHCR-Package ist **privat**; die VM authentifiziert sich einmalig per
`docker login ghcr.io` mit einem VM-lokalen, read-only PAT (nicht in GitHub Secrets, nicht
im Repo — von Tobias direkt auf der VM eingerichtet).

## Verzeichnis auf der VM

Der `deploy`-Job erwartet `docker-compose.prod.yml`, `deploy/` (Caddyfile, Postgres-Init-
Skript, Keycloak-Realm-Template, Backup-Skript) und `.env.prod` unter `~/papo-deploy` auf
der VM (einmalig kopiert/geklont, nicht Teil der Pipeline selbst).

Frontend-Dateien werden vom `deploy`-Job selbst nach `~/papo-deploy/frontend-live/releases/<sha>`
synchronisiert; `frontend-live/current` ist ein Symlink auf die jeweils aktive Release. Caddy
bindet `frontend-live/` (nicht die einzelne Release) - der Symlink wird pro Deploy atomar
umgehängt, ohne dass der Caddy-Container neu erstellt werden muss. Ältere Reste aus dem
früheren Verzeichnis-Swap-Schema (`frontend/`, `frontend-old/`, `frontend-new/`) können von
Hand entfernt werden, sobald der erste Deploy mit diesem Schema durchgelaufen ist.

Der eingeschränkte SSH-Account `papo-deploy` erhält keine Shell und keine Docker-Gruppe.
Sein erzwungener Befehl ruft ausschließlich das root-owned
`/usr/local/libexec/papo-deploy-gateway` über die enge Regel aus
`deploy/ssh/papo-deploy.sudoers` auf. Installation und Remote-Negativtests sind Task 5.9
und dürfen erst nach Tobias' VM-, Key-, Host-Key- und sudoers-Nachweisen erfolgen.

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

Offsite-Kopie (S3-kompatibler Object-Storage-Bucket via `rclone`) ist bewusst auf
"vor echtem Produktivbetrieb" verschoben (siehe design.md Decision 7) — lokale Backups
grenzen das Risiko während der Evaluationsphase (Kunde/Betreuer) ausreichend ein.

## Demo-Daten (`SEED_DEMO_DATA`)

Der `%prod`-Profil-Container läuft standardmäßig als `LaunchMode.NORMAL` und seedet nie
Demo-Daten — die Kunden-Realm-Logins (`company-demo`/`partner-demo`, siehe Keycloak-
Realm-Template) treffen sonst auf eine leere Datenbank.

Die Realm-Vorlage enthält weiterhin aktivierte Demo-Logins mit bekannten Passwörtern.
`SEED_DEMO_DATA=false` deaktiviert diese Logins nicht. Vor echten Firmen- oder
Kundendaten müssen sie in der laufenden Keycloak-Realm deaktiviert oder entfernt,
echte Zugänge angelegt und der Zustand nach einem Neustart geprüft werden. Ein
Code-Deployment allein ändert eine bereits importierte Realm nicht zuverlässig.

Setzt `SEED_DEMO_DATA=true` in der VM-lokalen `.env.prod` (nie im Image, nie als
Default), läuft beim nächsten Start derselbe Seed wie in `%dev`/`%test`
(`DemoDataSeeder`): Partner, Artikel, Artikel-Freischaltungen und ~5 Monate
Bestellhistorie (für Dashboards/Provisionsansicht). `docker compose -f
docker-compose.prod.yml --env-file .env.prod up -d` nach dem Setzen genügt, kein
Neu-Build nötig.

Der bestehende Schutz (`Partner.count() > 0`) gilt unverändert: Sobald irgendein
Partner existiert (geseedet oder aus echtem Import), seedet ein Neustart/Redeploy mit
`SEED_DEMO_DATA=true` nicht erneut — keine Duplikate.

**Vor echtem Kunden-Onboarding (reale Partner/Artikel):** `SEED_DEMO_DATA` in
`.env.prod` wieder auf `false` setzen (oder die Zeile entfernen) **und** die geseedeten
Zeilen entfernen — da `%prod` `update` als Schema-Strategie nutzt (kein automatischer
Reset), bleiben sie sonst neben echten Daten stehen. Ohne dedizierte "Clear"-Funktion
manuell per `docker compose -f docker-compose.prod.yml down -v` (löscht das komplette
Postgres-Volume, danach frisch seeden lassen oder `SEED_DEMO_DATA=false` lassen) oder
gezielte `DELETE`-Statements.

## CORS

CORS ist bewusst nur im `%dev`-Profil aktiv (`application.properties`, für den separat
laufenden lokalen Frontend-Devserver auf Port 3000). Im `%prod`-Profil ist keine CORS-
Konfiguration gesetzt — Caddy serviert Frontend und Backend same-origin (siehe oben), daher
ist CORS dort schlicht nicht nötig, nicht nur deaktiviert.

## Gewerbeberechtigungsnachweis

Der Gewerbeberechtigungsnachweis dient der manuellen Prüfung, ob ein Partnerunternehmen
aktuell gewerberechtlich berechtigt ist. Für österreichische Unternehmen wird als
Registerart **GISA** (Gewerbeinformationssystem Austria) mit zweistelligem ISO-Ländercode
`AT` und dem GISA-Kennzeichen verwendet. Für andere Länder steht **Anderes
Handelsregister** zur Verfügung; eine automatische Registerabfrage findet nicht statt.

Es wird pro Partner nur der aktuelle Nachweis gespeichert. Ein Ersatz überschreibt den
bisherigen Nachweis; eine Historie oder OCR-Auswertung gibt es nicht. Erlaubt sind PDF,
JPEG und PNG bis maximal 10 MiB. Dateityp und Dateisignatur werden geprüft, eine
Malware-Freiheit kann PAPO ohne Virenscanner nicht garantieren.

Die Datei liegt als Teil des Partnerdatensatzes in PostgreSQL und ist nur über die
geschützten Partner-/Firmen-Endpunkte mit `private, no-store` abrufbar. Die regulären
PostgreSQL-Backups enthalten daher ebenfalls den aktuellen Nachweis. Ein separates
Lösch- oder Aufbewahrungsdatum ist derzeit nicht festgelegt; vor echtem Produktivbetrieb
müssen Aufbewahrungs- und Löschregeln sowie der Umgang mit Backups fachlich und rechtlich
definiert werden.

Bei bereits vorhandenen Firmenprofilen bleiben die neuen Registerfelder nach dem
Schema-Update leer. PAPO erfindet dafür keine echte Registernummer. Ein berechtigter
Firmenadministrator muss vor der nächsten Profiländerung das tatsächliche
Registerkennzeichen im Firmenprofil eintragen und den Speichervorgang prüfen.
