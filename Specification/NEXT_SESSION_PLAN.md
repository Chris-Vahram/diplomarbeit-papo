# PAPO — Plan für die nächste Session

**Stand:** 03.09.2026

**Zieltermin:** Sonntag, 06.09.2026

**Arbeitsgrenze:** Bestehendes MVP veröffentlichen, prüfen und dokumentieren. Kein neues, noch nicht geplantes Produktfeature beginnen.

## Ziel und Definition of Done

PAPO ist unter `https://paportal.at` mit synthetischen Demodaten erreichbar. Login, Company-/Partner-Rollen, Bestellung und Bestellstatus funktionieren. CI testet vor Build/Deploy, GHCR bleibt privat, Secrets stehen nur in GitHub Actions bzw. VM-lokal, Datenbanken besitzen lokale und S3-Offsite-Backups, und ein Rollback ist geprüft oder exakt dokumentiert. Danach werden MVP-OpenSpec und Traceability-Matrix aktualisiert. Erst dann endet die Session vor einem neuen Feature.

## Verifizierter Ausgangsstand

- Git bei Erstellung: `main == origin/main == 22c14c3`; die Handoff-Dokumente entstehen auf `docs/next-session-plan`. `tmp/` ist fremder untracked Bestand und bleibt unberührt.
- Der frühere CI/CD-Branch und Tobias' `feature/partner-new-order-redesign` besitzen keine Commits außerhalb von `main`; beide sind bereits vollständig integriert.
- Offene Changes: `build-partnerportal-mvp` 50/53 und `ci-cd-deployment-with-gh-actions` 26/41.
- Letzte Backend-Vollprüfung: 77/77 Tests und Package erfolgreich; für neue Änderungen erneut ausführen.
- `paportal.at` zeigt per A-Record auf `178.104.109.45`.
- VM-Zugriff als `papo` und passwortloses `sudo` funktionieren. Docker/Compose sind installiert.
- Externer Ingress auf 80/443 wurde am 03.09. mit kurzlebigen Listenern geprüft. UFW ist aktiv und erlaubt nur 22/80/443 eingehend; SSH wurde danach erneut erfolgreich verbunden.
- `~/papo-deploy` existiert auf der VM; es fehlen noch `.env.prod`, gestartete Container und Cron.
- Frontend verwendet noch feste localhost-Adressen in `frontend/src/lib/api.js` und `frontend/src/lib/auth.js`; das blockiert Produktion.
- Die letzten vier CI/CD-Läufe waren grün. Der Deploy-Job ist absichtlich mit `if: false` deaktiviert. Es gibt noch keine Actions Secrets und `main` ist nicht geschützt. GHCR-Sichtbarkeit ist mit dem aktuellen `gh`-Token nicht prüfbar, weil `read:packages` fehlt.
- S3- und SMTP-Zugangsdaten wurden über private Vaultwarden-Sends geliefert. Keine Werte in Git, Dokumentation, Prompt oder Tool-Ausgabe kopieren. SMTP ist kein aktuelles Feature und wird nicht eingerichtet.
- Die vereinfachte Firmenmail zu Artikeldaten fehlt. Deshalb kein Artikelstammdaten-, Mandanten-, Subdomain- oder E-Mail-Feature beginnen.

## Reihenfolge der nächsten Session

### 1. Sicherer Start und Remote-Abgleich

```bash
git status --short --branch
git fetch origin --prune
git switch main
git pull --ff-only origin main
git log --oneline --decorate -5
openspec list --json
```

Prüfen, ob Tobias seit diesem Handoff neue UI-Arbeit gepusht hat. Der bisherige `feature/partner-new-order-redesign`-Stand ist bereits in `main`; ihn nicht nochmals mergen. Danach einen eigenen Branch vom aktuellen `main` erstellen:

```bash
git switch -c feature/finish-production-deployment
```

### 2. Aktiven Deployment-Change fertigstellen

1. Same-origin-Produktionskonfiguration für API und Keycloak planen, minimal implementieren und lokal in Dev sowie im Produktions-Compose testen.
2. Dediziertes Deploy-Keypair erzeugen; Public Key append-only auf der VM installieren; Private Key ausschließlich als GitHub Actions Secret hinterlegen.
3. `~/papo-deploy` mit Compose-/Deploy-Dateien bestücken und eine nicht committed `.env.prod` anlegen. Vorhandene Demo-Zugänge dürfen laut Chris für die synthetische Sonntagsdemo weiterverwendet werden; Datenbank-/Admin-Secrets bleiben trotzdem nicht öffentlich.
4. GHCR-Package auf privat prüfen. Chris erstellt bei Bedarf interaktiv einen klassischen PAT nur mit `read:packages`; diesen per `docker login ghcr.io --password-stdin` VM-lokal verwenden.
5. GitHub Actions Secrets setzen, Deploy-Job freischalten und zunächst über einen kontrollierten Commit/Workflow-Lauf deployen.
6. Containerzustand, Health, Logs ohne Secrets, Ressourcenlimits und Persistenz prüfen.
7. Caddy-Zertifikat, HTTPS und Keycloak-Flow über `/auth` vollständig prüfen.
8. Synthetische Demodaten anlegen; keine echten Firmen-/Personendaten verwenden.
9. Lokalen Backup-Cron installieren und Dump/Retention/Restore prüfen.
10. Den privaten S3-Send erst in diesem Schritt sicher öffnen. Endpoint/Bucket/Schreibrecht validieren, VM-lokales `rclone` konfigurieren und Upload plus Restore testen. Nur bei einem konkret fehlenden Wert Herrn Bachl fragen.
11. Branch Protection für `main`: Pull Request und erfolgreicher Statuscheck `Test backend` verpflichtend, zunächst null verpflichtende Reviews für das Zweierteam; Admin-Bypass nur für Notfälle.
12. Vollständigen Company- und Partner-Smoke-Test auf Desktop und Mobile durchführen. Cross-Role-/Cross-Partner-Zugriffe negativ prüfen.

### 3. Abschluss ohne neues Feature

- `cd backend && ./mvnw test && ./mvnw package`
- `openspec validate ci-cd-deployment-with-gh-actions`
- Offene Deployment-Tasks nur mit tatsächlichem Nachweis abhaken; Change nach Abschluss archivieren.
- Die drei verbleibenden MVP-Tasks prüfen: manueller kompletter Bestellflow, OpenSpec-Hygiene, Abschlussreview. Bei belegtem Erfolg abschließen und `build-partnerportal-mvp` archivieren.
- Traceability-Matrix aus FSD/OpenSpec, Code-Modul und konkretem Test/Nachweis erstellen. Sie ist Dokumentationsarbeit, kein neues Produktfeature.
- `python3 tools/ai_evidence_log.py sync` und einmal passend für den Arbeitsblock `enrich` ausführen.
- Finalen Diff mit `partnerportal-quality` und einem separaten Korrektheits-/Sicherheitsreview prüfen.

## Externe oder interaktive Restpunkte

- Chris muss einen GHCR-PAT interaktiv erstellen, falls der vorhandene GitHub-Zugang kein `read:packages` besitzt. Der Agent darf den geheimen Wert anschließend nur per verdeckter Eingabe verwenden.
- Nur falls der S3-Send Endpoint oder Bucket nicht nennt oder der Schreibtest scheitert, ist eine präzise Rückfrage an Herrn Bachl nötig.
- Die angekündigte vereinfachte Artikeldaten-Mail bleibt extern offen, blockiert aber weder Demo noch Deployment.
- Tobias' UI-Dokumentation bleibt sein eigener Backlog und blockiert den technischen Go-live nicht.

## Stoppregel

Nach Deployment, Smoke-Test, Backups, OpenSpec-Abschluss und Traceability-Matrix stoppen. Vor Partner-Stammdaten, Artikelnummer/Kategorie, echtem SMTP, Mandantenfähigkeit oder einem anderen neuen Feature zuerst einen eigenen OpenSpec-Vorschlag mit Ziel, Scope, Sicherheitsauswirkungen, Tests und Definition of Done erstellen und mit einem starken Modell prüfen.

## Knappe Mail an Herrn Bachl

> Hallo Herr Bachl,
>
> vielen Dank, die S3- und SMTP-Zugangsdaten sind angekommen. Wir richten die Zugänge selbst ein und melden uns nur, falls dabei eine konkrete technische Angabe fehlt. Die Firmen-Subdomains stimmen wir später separat mit Herrn Hammer ab.
>
> Liebe Grüße
> Chris
