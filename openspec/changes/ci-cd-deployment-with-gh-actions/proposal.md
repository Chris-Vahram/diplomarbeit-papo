## Why

Das Partnerportal läuft bisher nur lokal; für die synthetische Demonstration und Bewertung fehlt ein reproduzierbarer, abgesicherter Weg von einem geprüften Commit zu einer erreichbaren Instanz. Der operative Zielzustand stammt aus `Specification/NEXT_SESSION_PLAN.md`; die FSD enthält keine eigene Deployment-Anforderung, und das Protokoll vom 31.08.2026 bestätigt Deployment und Tests als Teil der Diplomarbeit.

## What Changes

- Eine GitHub-Actions-Pipeline testet das Backend, baut ein Quarkus-JVM-Image für `linux/amd64`, veröffentlicht es privat in GHCR und überträgt danach ein zum auslösenden Commit passendes Release-Bundle per dediziertem SSH-Schlüssel auf die VM.
- Alle verwendeten GitHub Actions werden auf verifizierte vollständige Commit-SHAs gepinnt; der Workflow erhält nur die minimal nötigen Berechtigungen und Secrets.
- Eine getrennte Produktions-Compose-Topologie betreibt Frontend, Backend, Postgres, Keycloak und Caddy auf der vorhandenen VM `papo-server-01`. Zustandsbehaftete Images sind per Digest fixiert; reguläre Releases aktualisieren nur Backend und Caddy/Frontend.
- Produktion nutzt `https://paportal.at` als Same Origin: Frontend-Aufrufe verwenden `/api` und `/auth`; nur der dokumentierte lokale Entwicklungsserver auf Port 3000 verwendet weiterhin die localhost-Ziele.
- Produktionskonfiguration und Zugangsdaten kommen aus GitHub Actions Secrets oder VM-lokalen, nicht versionierten Dateien; echte Werte werden weder committed noch protokolliert.
- Das Produktions-Realm-Template enthält keine Benutzer oder bekannten Passwörter. Chris legt eine synthetische Company-Identität und mindestens zwei synthetische Partner-Identitäten samt passender Anwendungsdaten privat an, damit Rollen- und Partnerisolation prüfbar sind.
- Backups beider Datenbanken werden mit restriktiven Dateirechten lokal gespeichert, nach Retention bereinigt und über ein VM-lokales verschlüsseltes `rclone`-Ziel in den bereitgestellten S3-kompatiblen Speicher kopiert. Ein Restore in einer isolierten, kurzlebigen PostgreSQL-Instanz ist vor dem Status „produktionsbereit“ nachzuweisen.
- Branch Protection verlangt vor einem Merge nach `main` den erfolgreichen Check `Test backend`.

Nicht Teil dieses Changes sind neue Produktfunktionen, SMTP, Mandantenfähigkeit/Subdomains, ein öffentlicher Shop, persönliche Partnerkonten, eine direkte BMD-Schnittstelle, Hochverfügbarkeit, Blue-Green-/Canary-Deployments oder automatisches Rollback.

## Capabilities

### New Capabilities

- `deployment-pipeline`: Test-Gate, unveränderlich referenzierte Build-Actions und Container-Basisimages, privates Image, commit-kohärente und gegen überholte Läufe geschützte Release-Auslieferung, Single-VM-Topologie, Same-Origin-Produktionsbetrieb, synthetische Prüfdaten, persistente Daten sowie lokale und verschlüsselte externe Backups mit isoliertem Restore-Nachweis.

### Modified Capabilities

- Keine.

## Impact

- **Repository:** `.github/workflows/ci-cd.yml`, `docker-compose.prod.yml`, `.env.prod.example`, `backend/src/main/resources/application.properties`, `frontend/src/lib/api.js`, `frontend/src/lib/auth.js`, `deploy/`, `docs/DEPLOYMENT.md` und `docs/SETUP.md`.
- **Externe Systeme:** GitHub Actions/Branch Protection, privates GHCR-Package, VM `papo-server-01`, DNS/TLS für `paportal.at` und der bereits bereitgestellte S3-kompatible Speicher.
- **Zugangsdaten:** `VM_HOST` und der private Deploy-Schlüssel liegen in GitHub Actions; der öffentliche SSH-Host-Fingerprint liegt als Repository-Variable vor. Datenbank-, Keycloak-, GHCR-Lese- und S3-/`rclone`-Zugangsdaten bleiben ausschließlich VM-lokal. Es werden keine vertraulichen Werte in OpenSpec, Git oder Logs aufgenommen.
- **Abhängigkeiten:** keine neue Java- oder Frontend-Abhängigkeit; die Auslieferung verwendet die nativen Runner-Werkzeuge `ssh` und `scp`, `rclone` wird nur auf der VM benötigt. Caddy, Postgres und Keycloak bleiben Produktionscontainer.
- **Kompatibilität:** `%dev`/`%test` und der statische Frontend-Entwicklungsablauf bleiben erhalten; nur das `%prod`-Verhalten wird auf Same Origin und Container-Service-Namen umgestellt.
