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
   exakten `main`-SHA auf. Der Gateway ersetzt nur `backend`, prüft dessen Start über
   die lokale HTTPS-Route und hängt erst danach `frontend-live/current` atomar um.
   Postgres, Keycloak und Caddy bleiben unverändert.

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
| `SHOP_PUBLIC_ENABLED` | Öffentliche Partner-Shops (`/shop/*`, `/api/shop/*`) erreichbar machen; Default `false`. Erst einschalten, wenn die Shop-Rechtstexte (`add-shop-legal-consent`) live sind. Shop-Verwaltung im Portal funktioniert auch ausgeschaltet |
| `PAPO_ENVIRONMENT` | `staging` (Default) oder `production`. Nur `production` erzwingt die Rechtstext-Sperre: Shops lassen sich nicht freigeben und sind offline, solange ein Rechtstext im Firmenprofil fehlt. Staging zeigt markierte Platzhalter. Ungültige Werte verhindern den Start |

Das GHCR-Package ist **privat**; die VM authentifiziert sich einmalig per
`docker login ghcr.io` mit einem VM-lokalen, read-only PAT (nicht in GitHub Secrets, nicht
im Repo — von Tobias direkt auf der VM eingerichtet).

## Verzeichnis auf der VM

Der `deploy`-Job erwartet `docker-compose.prod.yml`, `deploy/` (Caddyfile, Postgres-Init-
Skript, Keycloak-Realm-Template, Backup-Skript) und `.env.prod` unter `~/papo-deploy` auf
der VM (einmalig kopiert/geklont, nicht Teil der Pipeline selbst).

> **Achtung – manuell nachziehen:** Die Pipeline aktualisiert nur das Backend-Image und
> `frontend-live/`. Ändert ein Merge `docker-compose.prod.yml` oder etwas unter `deploy/`
> (z. B. `deploy/caddy/Caddyfile`), wirkt das auf der VM **erst nach manuellem Kopieren**.
> Ein grüner Deploy-Job beweist das nicht. Vorfall 19.10.2026: Nach PR #50 lieferte
> `/shop/<kennung>` das Portal statt `shop.html`, bis das neue Caddyfile auf der VM lag.
>
> Ablauf mit Admin-Zugang (nicht `papo-deploy`), vom aktuellen `main` aus:
>
> ```bash
> scp deploy/caddy/Caddyfile <vm>:~/papo-deploy/deploy/caddy/Caddyfile
> scp docker-compose.prod.yml <vm>:~/papo-deploy/docker-compose.prod.yml
>
> # auf der VM
> cd ~/papo-deploy
> docker exec papo-prod-caddy caddy validate --config /etc/caddy/Caddyfile --adapter caddyfile
> docker exec papo-prod-caddy caddy reload --config /etc/caddy/Caddyfile --adapter caddyfile
> ```
>
> `caddy reload` übernimmt das Caddyfile ohne Unterbrechung. Geänderte Umgebungsvariablen
> in `docker-compose.prod.yml` greifen erst, wenn der jeweilige Container neu erstellt wird
> (nächster Deploy bzw. `docker compose -f docker-compose.prod.yml --env-file .env.prod up -d <dienst>`).
> Danach live prüfen, z. B. `curl -sI https://<domain>/shop/test` → `X-Robots-Tag: noindex`.
> Andere Dateien unter `deploy/` (Postgres-Init, Keycloak-Template, Backup) gelten ebenso;
> deren Wirkung hängt vom jeweiligen Dienst ab.

Frontend-Dateien werden vom `deploy`-Job selbst nach `~/papo-deploy/frontend-live/releases/<sha>`
synchronisiert; `frontend-live/current` ist ein Symlink auf die jeweils aktive Release. Caddy
bindet `frontend-live/` (nicht die einzelne Release) - der Symlink wird pro Deploy atomar
umgehängt, ohne dass der Caddy-Container neu erstellt werden muss. Ältere Reste aus dem
früheren Verzeichnis-Swap-Schema (`frontend/`, `frontend-old/`, `frontend-new/`) können von
Hand entfernt werden, sobald der erste Deploy mit diesem Schema durchgelaufen ist.

Der eingeschränkte SSH-Account `papo-deploy` erhält keine Shell und keine Docker-Gruppe.
Sein erzwungener Befehl ruft ausschließlich das root-owned
`/usr/local/libexec/papo-deploy-gateway` über die enge Regel aus
`deploy/ssh/papo-deploy.sudoers` auf. Installation und externe SSH-Negativtests sind
im OpenSpec-Change `harden-deployment-gateway` dokumentiert; die Negativtests mit
dem eingeschränkten Deploy-Key sind noch offen.

## Bereitschaft und Rollback

Der Gateway akzeptiert das neue Backend erst, wenn das exakte SHA-Image zweimal
hintereinander ohne Container-Neustart über
`https://<DOMAIN>/api/company/profile` per lokalem Loopback mit gültigem TLS-Zertifikat
HTTP 401 liefert. Die Prüfung ist zeitlich begrenzt. Bei einem Start- oder
Frontend-Umschaltfehler bleibt das bisherige Frontend aktiv. Der Gateway versucht,
das zuvor laufende, unveränderliche SHA-Image nur für `backend` wiederherzustellen
und ebenfalls zu prüfen. Der Deploy-Job bleibt auch nach erfolgreichem Rollback
fehlgeschlagen. Ohne bekanntes SHA-Image oder bei fehlgeschlagenem Rollback ist ein
manueller Eingriff nötig. Bei HUP, INT oder TERM während der Aktivierung versucht
der Gateway ebenfalls den alten Frontend-Zeiger und das vorige Backend
wiederherzustellen. Die Datenbank wird nie automatisch zurückgerollt.
Wiederholte HUP-, INT- und TERM-Signale werden während der Wiederherstellung
ignoriert, damit sie den Rollback nicht abbrechen. Hängt der Docker-Daemon dabei,
ist ein Eingriff über den VM-Adminzugang nötig.

Für einen manuellen Backend-Rollback zuerst Image-SHA, Logs und Datenbank-
Kompatibilität prüfen. Danach mit einer bekannt guten SHA gezielt nur `backend`
starten; `.env.prod` muss dafür nicht geändert werden:

```bash
ssh papo@<VM_HOST>
cd ~/papo-deploy
IMAGE_TAG=<bekannt-gute-sha> docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --no-deps backend
```

Bei einem fehlgeschlagenen Gateway-Deploy zeigt das Frontend weiter auf die vorige
SHA. Bei einem später bemerkten Fehler nach einem abgeschlossenen Deploy muss dessen
Symlink separat auf ein bekannt gutes Frontend-Release gesetzt werden. Jedes Image
wird mit `latest` und der Commit-SHA getaggt; `latest` ist kein sicheres Rollback-Ziel.

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

## Partner-Login binden (`bind-partner-login-to-oidc-subject`)

Release A löst Partner über die verifizierte Keycloak-Benutzer-ID auf; Partner ohne
jede Bindung melden sich übergangsweise noch per Benutzername = Partnernummer an.
Nach dem Deploy von Release A einmalig auf der VM:

1. In der Keycloak-Administration (`https://<DOMAIN>/auth/admin`, Realm `partnerportal`)
   den Benutzer `partner-demo` öffnen und die **ID** kopieren (keine Tokens/Passwörter
   notieren).
2. Als `company-demo` in der Partnerverwaltung beim Partner `PARTNER-DEMO` auf „Logins“
   klicken, die ID eintragen, „Verknüpfen“.
3. Als `partner-demo` anmelden und prüfen, dass das Dashboard lädt.

Erst danach Release B (Fallback entfernt) deployen. Rollback B → A ist sicher; A → davor
nur, solange kein Login entfernt, kein Keycloak-Benutzer umbenannt und keine
Partnernummer geändert wurde.
## Mandantenfähigkeit (`add-company-tenancy`)

Ausgeliefert in zwei Releases, damit der automatische Gateway-Restore nie ein Image startet,
das nicht zum Schema passt (der Gateway setzt SQL nie zurück):

- **Release A (expand):** ordnet beim Start alle Partner, Artikel, Kategorien und Bilder
  ohne Firma der einzigen Firma zu (`CompanyTenancyMigrationService.expand()`); `company_id`
  bleibt optional, Singleton-Schlüssel `CURRENT` und alte Unique-Constraints bleiben. Das
  vorige Image läuft auf diesem Schema weiter (lokal geprüft mit beiden JARs).
- **Release B (contract):** erst nach A und gebundenem `company-demo`
  (`CompanyTenancyMigrationService.contract()`): setzt `company_id` auf NOT NULL, entfernt die
  globalen Unique-Constraints auf `partner.partnernumber`, `article_categories.normalized_name`
  und `companyprofile.singletonkey` und macht `singletonkey` optional. Partnernummer und
  Kategoriename sind danach nur noch je Firma eindeutig. Das A-Image läuft auf diesem Schema
  weiter. Erst danach Firma 2 anlegen.

**Rollout Release B:** wie A Schritt 1–2 (Backup, SHA notieren, Merge → Deploy), danach als
`company-demo` und `partner-demo` anmelden und prüfen. Prüfen des Schemas auf der VM:
`select pg_get_constraintdef(oid) from pg_constraint where conrelid = 'partner'::regclass and contype = 'u';`
zeigt nur noch `UNIQUE (company_id, partnernumber)`.

**Rollout Release A:**

1. Vor dem Merge auf der VM: `deploy/backup/backup.sh`, aktuelle Image-SHA notieren.
2. Merge → Deploy. Firmenbenutzer bekommen jetzt 403 „Kein Firmenkonto mit diesem Login
   verknüpft.“, bis sie gebunden sind (kein Übergangs-Fallback).
3. Sofort `company-demo` binden: In der Keycloak-Administration den Benutzer `company-demo`
   öffnen, die **ID** kopieren, dann auf der VM (Firma 1 hat die ID aus der Ausgabe von
   `select id, businessname from companyprofile;`):

   ```bash
   # lokal: Skript auf die VM kopieren
   scp deploy/tenancy/provision-company.sql papo@<VM_HOST>:~/papo-deploy/
   # auf der VM
   cd ~/papo-deploy
   DBU=$(grep '^DB_USER=' .env.prod | cut -d= -f2)
   docker compose -f docker-compose.prod.yml --env-file .env.prod cp provision-company.sql postgres:/tmp/p.sql
   docker compose -f docker-compose.prod.yml --env-file .env.prod exec postgres \
     psql -U "$DBU" -v ON_ERROR_STOP=1 -v subject="'<ID>'" -v company_id=<Firma-1-ID> \
     -v operator="'chris'" -f /tmp/p.sql partnerportal
   ```
   Das Skript liegt im Repo unter `deploy/tenancy/provision-company.sql`; es bricht ohne
   Änderung ab, wenn das Login schon (als Firma oder Partner) verknüpft ist.
4. Als `company-demo` anmelden und Partner, Artikel, Bestellungen, Dashboard prüfen (der
   Gateway prüft nur eine unauthentifizierte 401, dieser Check ist Pflicht). Unter
   „Firmenprofil“ die „E-Mail für Bestellhinweise“ kontrollieren (seit Release B die einzige
   Quelle; `PAPO_MAIL_COMPANY_RECIPIENT` wird nicht mehr gelesen).

**Rollback:** Nach A jederzeit das vorige Image (Gateway-Restore oder `IMAGE_TAG=<sha>`), ohne
SQL. Sobald nach Release B eine zweite Firma existiert, nie mehr ein Image vor Release A
starten (es würde allen Firmenbenutzern alle Daten zeigen): stattdessen Backup einspielen
oder vorwärts korrigieren.

**Weitere Firma (nur nach Release B):** Keycloak-Benutzer für Firma und Partner anlegen, IDs
kopieren, das Skript mit `company_id=0` und `business_name`, `tax_value`, `register_value`
ausführen (Kopf des Skripts), dann als neuer Firmenbenutzer den Partner anlegen und dessen
Login im Dialog „Logins“ verknüpfen.

## Bestell-E-Mails (`PAPO_MAIL_*`, `SMTP_*`)

Echter Versand von Bestellbestätigungen (B-04). Standard: **aus** (`PAPO_MAIL_ENABLED=false`),
Bestellungen zeigen dann „E-Mail-Versand ist nicht aktiviert“. Einschalten auf der VM:

1. SMTP-Daten der Firma in `.env.prod` eintragen (`SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`,
   `SMTP_PASSWORD`, `SMTP_START_TLS` bzw. `SMTP_TLS`) — nie in Git, nie in Chat/Issues.
   Für paportal.at (geprüft 30.10.2026): `SMTP_HOST=mail.paportal.at`, `SMTP_PORT=465`,
   `SMTP_TLS=true`, `SMTP_START_TLS=DISABLED` (Port 587 ist von der VM aus zu),
   `SMTP_USERNAME=no-reply@paportal.at` — Login und Absender (`PAPO_MAIL_FROM`) müssen dasselbe
   Postfach sein, sonst lehnt der Server ab (`REJECTED`). `*.your-objectstorage.com` ist der
   Hetzner-Objektspeicher, kein SMTP-Server.
2. `PAPO_MAIL_FROM` und **`PAPO_MAIL_REDIRECT_TO`** (Testpostfach) setzen, die Firmenadresse
   im Portal unter „Firmenprofil“ → „E-Mail für Bestellhinweise“ eintragen, dann `PAPO_MAIL_ENABLED=true`. Ohne Umleitung startet das Backend nur mit
   `PAPO_MAIL_ALLOW_REAL_RECIPIENTS=true` — das bleibt aus, solange die VM Demo-Daten und
   bekannte Demo-Logins hat.
3. Backend mit dem **aktiven Release** neu starten und mit einer Testbestellung prüfen; der Status
   steht in den Bestelldetails der Firma:

   ```bash
   cd ~/papo-deploy
   SHA=$(basename "$(readlink frontend-live/current)")
   IMAGE_TAG=$SHA docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --no-deps backend
   ```

   Ohne `IMAGE_TAG` startet Compose das lokal gecachte, veraltete `:latest`-Image (der Deploy
   arbeitet nur mit SHA-Tags). Kontrolle: Die Startzeile „Installed features“ enthält `mailer`
   und `scheduler`.

`PAPO_MAIL_SHIPPING_ENABLED=true` schaltet die neutral formulierte Abschlussmail bei
„Abgeschlossen“ ein (Entscheidung 15.10.2026). Absender: `no-reply@paportal.at`. Änderungen an Umleitung/Aktivierung wirken auch auf wartende Mails:
diese werden dann nicht mehr gesendet („Konfiguration geändert“).

## Endkundenkonten im Shop (`add-shop-customer-accounts`)

Optionale Kundenkonten liegen im **eigenen Keycloak-Realm `papo-shop`** derselben Instanz. Das
Backend nimmt Tokens dieses Realms nur unter `/api/shop-customer/*` an (OIDC-Tenant per Pfad);
alle Firmen- und Partner-Endpunkte lehnen sie ab, und Portal-Tokens gelten dort nicht.
Standard: **aus** (`SHOP_ACCOUNTS_ENABLED=false`); die Gastbestellung ist davon unabhängig.

0. Dateien auf die VM kopieren (siehe „Verzeichnis auf der VM“): `docker-compose.prod.yml`,
   `deploy/keycloak/render-and-start.sh`, `deploy/keycloak/papo-shop-realm.prod.json.template`
   und den Theme-Ordner `deploy/keycloak/themes/papo/` (PAPO-Anmeldeseiten; der Realm setzt
   `loginTheme=papo`, ohne den Ordner zeigt Keycloak für diesen Realm keine Anmeldeseite):

   ```bash
   scp -r deploy/keycloak <vm>:~/papo-deploy/deploy/
   scp docker-compose.prod.yml <vm>:~/papo-deploy/docker-compose.prod.yml
   ```
1. SMTP muss funktionieren (siehe „Bestell-E-Mails“): Keycloak verschickt Bestätigungs- und
   Passwort-Mails selbst mit `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`; der
   Absender ist `SMTP_USERNAME`. Für Port 465: `SHOP_SMTP_SSL=true`, `SHOP_SMTP_STARTTLS=false`.
2. `SHOP_ADMIN_CLIENT_SECRET` (mindestens 32 Zeichen, z. B. `openssl rand -hex 32`) in
   `.env.prod` setzen. Nur dann importiert `render-and-start.sh` den Realm; der Service-Account
   `papo-shop-admin` darf ausschließlich Benutzer dieses Realms verwalten (Kontolöschung).
3. Keycloak neu starten (`--import-realm` legt fehlende Realms an, bestehende bleiben
   unverändert — ein später geändertes Secret muss in der Admin-Konsole nachgezogen werden),
   dann `SHOP_ACCOUNTS_ENABLED=true` setzen und das Backend mit dem aktiven Release neu starten
   (Befehl wie oben).
4. Prüfen: Konto im Shop unter „Anmelden“ registrieren, Bestätigungsmail öffnen, bestellen,
   Bestellung unter „Mein Konto“ sehen, Konto löschen.

Ohne importierten Realm startet das Backend trotzdem; `/api/shop-customer/*` antwortet dann 401.
Rückweg: `SHOP_ACCOUNTS_ENABLED=false` blendet die Anmeldung aus; Gastbestellungen laufen weiter.
Aufbewahrung von Kundendaten in Bestellungen nach einer Kontolöschung legt die Firma fest.
Theme-Änderungen: Ordner erneut kopieren und Keycloak neu starten (Produktion cached Themes).
Texte der Anmeldeseiten stehen in `themes/papo/login/messages/messages_de.properties`, die der
Bestätigungsmail in `localizationTexts` des Realm-Templates.

## Rollback nach `derive-product-category-from-catalog`

Ab diesem Release schreibt die App den Kategorietext nicht mehr. Vor einem Rollback auf
einen älteren Release den Text einmal aus dem Katalog nachziehen:

```sql
update product p set category = c.name from article_categories c where c.id = p.article_category_id;
update product set category = null where article_category_id is null;
```

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
