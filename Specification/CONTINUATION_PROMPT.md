# PAPO – Next-Session-Context

**Stand:** 28.09.2026. Operative Übergabe; volatile Angaben beim Start prüfen.
Stabile Regeln stehen in `AGENTS.md` und `CLAUDE.md`. Der beschlossene/noch zu
beschließende Ausbau ist in [PRODUCT_COMPLETION_PLAN.md](PRODUCT_COMPLETION_PLAN.md)
geordnet. Dieses Dokument bleibt der einzige operative Session-Handoff.

## Wichtiger Wiedereinstieg — Deploy-Health/Rollback-Fix

Aktueller lokaler Branch: `fix/deploy-health-rollback`; PR #31 wurde gemergt,
Merge-Commit `6853e78`. Deployment-Code, Gateway-Harness, Doku, OpenSpec-Changes,
B-07-Plan und KI-Nachweis sind geprüft; der Harness hat 30/30 Prüfungen
bestanden. PR, `main`-Deploy und Live-Prüfung sind erfolgreich.

Der OpenSpec-Change `verify-deploy-health-and-rollback` wurde nach dem
erfolgreichen Live-Deploy archiviert. Gateway/Harness `sh -n` bestanden;
der lokale Harness bestand mit **30 Prüfungen**; `git diff --check` sauber.
Unabhängiger finaler Security-Review fand und half beheben: aktiven Frontend-
Release durch Signal-Trap löschen; Curl-Konfig via `.curlrc`; Frontend-Rollback-
Fehler verschlucken; Recovery-Status bis zum Ende eines expliziten Rollbacks
aktiv lassen, damit TERM/HUP/INT den Rollback erneut anstößt. Wiederholte Signale
werden während der Wiederherstellung ignoriert, damit sie den Rollback nicht
abbrechen. Der Harness prüft zwei TERM-Signale bis zur wiederhergestellten
Backend-Bereitschaft; finaler Review bestätigte **keine Blocker**.
Gateway verlangt zwei stabile HTTPS-401-Antworten mit gleicher Container-ID,
Image und Restart-Zahl, nutzt `curl -q --noproxy '*' --resolve ...` mit TLS-
Validierung, rollt Backend/Frontend bei Compose-/Health-/Signalfehler zurück und
löscht kein aktives Release. OpenSpec-Change `verify-deploy-health-and-rollback`
ist nach erfolgreichem Live-Deploy archiviert unter
`openspec/changes/archive/2026-09-28-verify-deploy-health-and-rollback/`.
Tasks 1.1–3.2 sind erledigt. PR #31 wurde als `6853e78` gemergt; `main`-Run
`36422792734` bestand Tests (2:26), Image-Build/Push (1:13) und VM-Deploy (0:43).
Live ist Backend-Image exakt `6853e78e59f8c3c756bdea7f28ef5e02101b4ca3`,
`running`, RestartCount 0. Postgres, Keycloak und Caddy laufen mit RestartCount 0;
ihre Container wurden vor dem Deploy erstellt. VM-lokale HTTPS-Prüfung: Frontend
200, geschützte API 401, OIDC Discovery 200. Die Deployment-Credential aus
GitHub Actions wurde damit praktisch bestätigt.

Task 3.1: Das ursprüngliche Backup bleibt unter
`/usr/local/libexec/papo-deploy-gateway.backup-20260925T144611Z`; vorige
Gateway-Version `577c21d…` wurde vor der finalen Installation als
`/usr/local/libexec/papo-deploy-gateway.backup-20260928T122840Z` gesichert.
Aktuelle VM-Installation `root:root 755`, SHA-256
`e967b77f2499edd99a74edeafd8534d96e02948b2e67a29d6a0dc6ef8d79481a` (identisch
zum lokalen Stand), Remote-`sh -n` erfolgreich. Sudoers und SSH-Schlüssel wurden
nicht geändert. Die nicht geheime Staging-Kopie liegt unter
`/tmp/papo-deploy-gateway.new-20260928T122840Z`.

Die lokale Datei `~/Desktop/papo-deploy-key` ist parsebar, aber ihr
Fingerprint passt **nicht** zum einzigen Key in
`/home/papo-deploy/.ssh/authorized_keys`; Anmeldung mit `IdentitiesOnly=yes`
wurde abgewiesen. Auch die einzige SSH-Agent-Identität passt nicht. Chris
bestätigt, dass das passende Schlüsselpaar derzeit nicht verfügbar ist. Es wird
nur für die externe SSH-Negativmatrix benötigt; lokale Implementierung, PR-CI
und das normale Actions-Deployment laufen ohne diese Datei. Der erfolgreiche
`main`-Run `36422792734` bestätigt das GitHub-Secret `VM_SSH_KEY`, dessen Wert
nicht rücklesbar ist. Keine VM-Autorisierung ändern und die Matrix erst nach
Fingerprint-Abgleich ausführen. Keine kostenpflichtige Bestellung mit späterer
Abwicklung, **keine bestätigte Online-Zahlung**. Ziel ist eine sicher deploybare
Staging-Version bis Herbstferien, nicht die schriftliche Thesis.

## Konkreter nächster Schritt

- Deployment: Es ist keine weitere Gateway-Implementierung für diesen Change
  offen. Sobald das passende Schlüsselpaar verfügbar ist, Fingerprint vergleichen
  und nur dann die externe SSH-Negativmatrix aus Task 2.2 in
  `harden-deployment-gateway` ausführen. Die GitHub-Actions-Deploy-Credential
  wurde bereits erfolgreich bestätigt.
- Weitere Changes: `complete-thesis-documentation` wartet auf Tobias' Screenshot-
  Prüfung und die gemeinsame Abnahme von Chris/Tobias. `decouple-partner-number-from-login`
  und `add-unique-bmd-article-number` warten auf verbindliche Daten-/Fachregeln;
  aktuell ist dort nichts sicher implementierungsbereit.

**Nicht entschieden:** verbindliche Herbstferien-Abnahmeliste; Firmen-/Schulfreigabe
für B-06 und B-07; BMD-Produkt/API und Export-/Rückkanal; Provisionsformel/
Auszahlungsart; B-03-Schwellen; Subdomains; Shop-Layouts/Anfragefelder; Staging-
Infrastruktur. Der Plan dokumentiert Empfehlungen, nicht stillschweigende
Entscheidungen. Herr Hammer ist Verbindung zur Firma; nach dem nächsten Termin
liefert Chris ein neues Besprechungsprotokoll. Die öffentliche Website ersetzt
keinen technischen BMD-Vertrag.

## Git/Deployment-Grenze

`main` enthält PR #27 (`5fff3b0`), Migrationsfix aus PR #28 (`b5ab7fb`),
Post-Deploy-Handoff aus PR #29 (`19fa02b`), Präsentationsänderung aus PR #30
(`075bd93`) und Deploy-Health/Rollback aus PR #31 (`6853e78`). Die archivierte
Deployment-Spezifikation ist mit dem Code synchronisiert. Archivdateien
und neuere Spezifikationen aus dem früheren lokalen `main` blieben erhalten.
Der erste Deploy von `5fff3b0` startete das Backend nicht: Die VM hatte den
historischen Demo-Namen „Elektro Huber GmbH“, die Migration erkannte nur
„Huemer GmbH“. Die VM wurde ohne Datenbank-/Volume-Löschung auf das vorige
Image `e63a81d` zurückgestellt. Der enge Fix akzeptiert beide bekannten
Demo-Namen, unbekannte Daten weiter nicht. Der Regressionstest reproduzierte
den Fehler zuerst; danach `./mvnw test` und `./mvnw package` jeweils
**130/130** grün, unabhängiger Java-Review ohne Blocker. `main`-Run
`36115368910` bestand. Live nach Deploy von `b5ab7fb`: Backend `running`,
RestartCount 0, Frontend-Link auf denselben SHA, 3/3 Partner mit Steuerkennung,
1 Firmenprofil; Frontend HTTP 200, geschützte API ohne Login HTTP 401, OIDC
HTTP 200. Noch keine angemeldete End-to-End-Browserabnahme.

**Deploy-Risiko behoben:** Der Gateway meldete beim ersten Versuch „Release
aktiviert“, obwohl der Backend-Prozess danach abstürzte. PR #31 ergänzt
Post-Activation-Health-Check und automatischen Backend-/Frontend-Rollback; der
Live-Deploy von `6853e78` bestätigte den Ablauf.

Der bestehende Keycloak-Produktions-Template enthält **aktive Demo-Konten mit
bekannten Passwörtern**. `SEED_DEMO_DATA=false` deaktiviert sie nicht; eine bereits
importierte Realm wird durch bloßes Code-Deployment nicht zuverlässig aktualisiert.
Chris hat bestätigt: Auf der aktuellen VM liegen **nur Demo-Daten**, und die
Demo-Logins sind aktiv. Damit ist der ausdrücklich freigegebene `main`-Deploy ein
Demo-Deployment, **keine Freigabe für echte Firmen-/Kundendaten**. Vor echtem
Onboarding Demo-Logins deaktivieren, echte Zugänge provisionieren, Staging trennen
und erneut prüfen. Bei bestehenden Company-Profilen bleibt die neue echte
Registerkennung leer; ein Firmen-Admin muss sie vor Profiländerung ergänzen.

## Altlasten außerhalb des nächsten Produkt-Agenten

Diese Punkte gehören zu Chris/Tobias bzw. autorisierter VM-Prüfung, **nicht** als
pauschales Aufgabenpaket zum nächsten Feature-Agenten:

1. Tobias liest `docs/UI_UX_DOCUMENTATION.md`, gleicht jeden Screenshot mit UI
   und Beschriftung ab, dokumentiert Abweichungen und bestätigt Task 2.2.
2. Chris + Tobias gehen FR-01–FR-25 und die ergänzenden Changes in
   `docs/REQUIREMENTS_TRACEABILITY.md` gegen FSD, Protokolle und B-01–B-08 durch,
   unterscheiden implementiert/simuliert/offen und bestätigen Task 3.1.
3. Die lokale Datei `~/Desktop/papo-deploy-key` ist parsebar und hat Modus `600`,
   aber ihr Fingerprint weicht vom einzigen `papo-deploy`-Key in der VM-
   `authorized_keys` ab. Keine VM-Schlüssel oder Zugriffsrechte ändern und den
   SSH-Negativtest nicht mit diesem Key ausführen. Tobias muss das korrespondierende
   private Schlüsselpaar sicher bereitstellen. Nach erneut bestätigtem Fingerprint-
   Match die SSH-Negativmatrix aus `openspec/changes/harden-deployment-gateway/tasks.md`
   ausführen. Keine Schlüsseldatei und keinen Schlüsselinhalt im Chat teilen.

## Jev, Evidence und KI-Nachweis

`@wrtnlabs/evidence` ist nicht installiert: es benötigt Node/npm und Annotationen,
konfligiert mit dem festgelegten PAPO-Stack und ersetzt keine fachliche Prüfung.
OpenSpec/Traceability und `tools/ai_evidence_log.py` bleiben die passenden
bestehenden Mittel. Jev schreibt keinen Code, sondern trifft eng begrenzte
Entscheidungen. `TYPESAFE_API_KEY` war in dieser Session nicht gesetzt; ChatGPT
Plus liefert keinen TypeSafe-Schlüssel. Deshalb wurde kein funktionsloser Jev-
Wrapper eingebaut. Bei einem echten Routingbedarf und sicher bereitgestelltem
TypeSafe-Key kann ein optionaler lokaler Wrapper außerhalb der PAPO-Laufzeit
separat geplant werden; Tests/Git bleiben deterministische Evidenz.

## Verifikation beim nächsten Start

```bash
git status --short --branch
git log -5 --oneline --decorate
openspec list --json
gh run view 36115368910 --json status,conclusion
```

Bei Backendänderungen gemäß `AGENTS.md`:

```bash
cd backend
./mvnw test
./mvnw package
```
