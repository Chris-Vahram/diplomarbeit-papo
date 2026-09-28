# PAPO – Next-Session-Context

**Stand:** 28.09.2026. Operative Übergabe; volatile Angaben beim Start prüfen.
Stabile Regeln stehen in `AGENTS.md` und `CLAUDE.md`. Der beschlossene/noch zu
beschließende Ausbau ist in [PRODUCT_COMPLETION_PLAN.md](PRODUCT_COMPLETION_PLAN.md)
geordnet. Dieses Dokument bleibt der einzige operative Session-Handoff.

## Wichtiger Wiedereinstieg — Deploy-Health/Rollback-Fix

Aktueller lokaler Branch: `fix/deploy-health-rollback`, Basis `origin/main`
`075bd93`. Deployment-Code, Gateway-Harness, Doku, OpenSpec-Changes, B-07-Plan
und KI-Nachweis wurden geprüft; der Harness hat 30/30 Prüfungen bestanden.
Chris autorisierte in dieser Sitzung Commit, Push, Merge und Demo-Deploy.

Der neue OpenSpec-Change validierte mit `openspec validate
verify-deploy-health-and-rollback --strict`. Gateway/Harness `sh -n` bestanden;
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
löscht kein aktives Release. OpenSpec Tasks 1.1, 1.2, 2.1, 2.2 und 3.1 sind
erledigt. Task 3.1 wurde am 25.09.2026 zuerst auf der Demo-VM abgeschlossen;
das ursprüngliche Backup bleibt unter
`/usr/local/libexec/papo-deploy-gateway.backup-20260925T144611Z`. Nach dem
TERM-Rollback-Fix wurde die vorige VM-Version als
`/usr/local/libexec/papo-deploy-gateway.backup-20260928T122840Z` gesichert; die
neue Installation `root:root 755` hat SHA-256
`e967b77f2499edd99a74edeafd8534d96e02948b2e67a29d6a0dc6ef8d79481a` (identisch
zum lokalen Stand), Remote-`sh -n` erfolgreich. Sudoers und SSH-Schlüssel wurden
nicht geändert. Eine nicht geheime Staging-Kopie liegt unter
`/tmp/papo-deploy-gateway.new-20260928T122840Z`. Offen ist Task 3.2:
Feature-Branch push/PR/CI, danach Merge in `main`
(dessen Produktions-Deploy Chris ausdrücklich akzeptiert hat) und
Live-Verifikation. Chris bestätigte am 28.09.2026 den Widerruf des zuvor
sichtbaren GitHub-PAT. Damit ist diese Sperre aufgehoben; den Token niemals
anfordern oder verwenden. Chris hat Commit, Push, Merge und den Demo-Deploy für
diesen Branch ausdrücklich autorisiert.

Die lokale Datei `~/Desktop/papo-deploy-key` ist parsebar, aber ihr
Fingerprint passt **nicht** zum einzigen Key in
`/home/papo-deploy/.ssh/authorized_keys`; Anmeldung mit `IdentitiesOnly=yes`
wurde abgewiesen. Auch die einzige SSH-Agent-Identität passt nicht. Chris
bestätigt, dass das passende Schlüsselpaar derzeit nicht verfügbar ist. Es wird
nur für die externe SSH-Negativmatrix benötigt; lokale Implementierung, PR-CI
und das normale Actions-Deployment können ohne diese Datei weiterlaufen, sofern
das nicht rücklesbare GitHub-Secret `VM_SSH_KEY` stimmt. Keine VM-Autorisierung
ändern und die Matrix erst nach Fingerprint-Abgleich ausführen. Keine
kostenpflichtige Bestellung mit späterer Abwicklung, **keine bestätigte
Online-Zahlung**. Ziel ist eine sicher deploybare Staging-Version bis Herbstferien,
nicht die schriftliche Thesis.

**Nicht entschieden:** verbindliche Herbstferien-Abnahmeliste; Firmen-/Schulfreigabe
für B-06 und B-07; BMD-Produkt/API und Export-/Rückkanal; Provisionsformel/
Auszahlungsart; B-03-Schwellen; Subdomains; Shop-Layouts/Anfragefelder; Staging-
Infrastruktur. Der Plan dokumentiert Empfehlungen, nicht stillschweigende
Entscheidungen. Herr Hammer ist Verbindung zur Firma; nach dem nächsten Termin
liefert Chris ein neues Besprechungsprotokoll. Die öffentliche Website ersetzt
keinen technischen BMD-Vertrag.

## Git/Deployment-Grenze

`main` enthält PR #27 (`5fff3b0`), Migrationsfix aus PR #28 (`b5ab7fb`) und
Post-Deploy-Handoff aus PR #29 (`19fa02b`) und Präsentationsänderung aus PR #30
(`075bd93`); Branch `fix/deploy-health-rollback` wurde auf `origin/main`
`075bd93` fast-forwarded. Archivdateien
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

**Deploy-Risiko:** Der Gateway meldete beim ersten Versuch „Release aktiviert“,
obwohl der Backend-Prozess danach abstürzte; GitHub Actions zeigte Erfolg.
Post-Activation-Health-Check und automatischer Rollback sind ein separater,
prioritärer Infrastruktur-Fix vor der nächsten größeren `main`-Änderung.

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
