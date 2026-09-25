# PAPO – Next-Session-Context

**Stand:** 25.09.2026. Operative Übergabe; volatile Angaben beim Start prüfen.
Stabile Regeln stehen in `AGENTS.md` und `CLAUDE.md`. Der beschlossene/noch zu
beschließende Ausbau ist in [PRODUCT_COMPLETION_PLAN.md](PRODUCT_COMPLETION_PLAN.md)
geordnet. Dieses Dokument bleibt der einzige operative Session-Handoff.

## Startprompt für den nächsten Agenten

> Lies `AGENTS.md`, `CLAUDE.md`, diese Datei, `PRODUCT_COMPLETION_PLAN.md`, die
> Protokolle vom 31.08., 10.09. und 14.09. sowie `phase2_backlog.md`. Prüfe
> `git status --short --branch`, `git log -5 --oneline --decorate`,
> `openspec list --json` und PR #27. Arbeite an **einem** nächsten freigegebenen
> Produkt-Change mit OpenSpec; keine pauschale Umsetzung von Phase 2. Bewahre
> Tobias-Dateien und fremde Änderungen. BMD-/Auszahlungsregeln nicht erfinden.

## Verifizierter Stand dieser Session

- Chris hat die 122 Dateien mit Suffix ` 2` ausdrücklich zur Bereinigung
  freigegeben. 119 waren bitgleich, eine ältere Frontend-Kopie, zwei enthielten
  den bereits versionierten Inhalt; vier versionierte OpenSpec-Kopien wurden
  ebenfalls entfernt. Kanonische Dateien bleiben erhalten; gelöschte versionierte
  Kopien sind aus Git wiederherstellbar. Keine ` 2`-Datei blieb im Inventar.
- Trade Proof/Registerkennung samt OpenSpec-Archiv und dem Fix für inaktive
  Partner und Partner ohne Nachweis: Commit `7b2d4b7` auf
  `codex/archive-business-tax-identifiers` gepusht. PR #27 existiert. Der
  Review fand und behob den `tradeProof:null`-Listenregressionsfall.
- Backend `./mvnw test`: 128/128 grün vor dem letzten Regressionstest;
  `./mvnw package`: 129/129 grün und Build erfolgreich nach dem Fix.
  Ein paralleler Paketlauf scheiterte zwischenzeitlich nur an Test-Port 8081;
  der anschließende einzelne Lauf war erfolgreich. `git diff --check`,
  JavaScript-`node --check` und `openspec validate
  complete-thesis-documentation --strict` bestanden. PR #27 Backend-CI bestand;
  Deploy-Jobs wurden bei PR erwartungsgemäß übersprungen.
- Thesis-Dokumentation und Matrix wurden gegen FSD, Phase 2, Code und Audit
  unabhängig überprüft und korrigiert. Insbesondere sind FR-03/04 laut geltender
  FSD erfüllt; die BMD-Artikelnummer ist eine separate spätere Erweiterung.
  Der alte Import-Screenshot zeigt einen Fehler, keinen Erfolgsbeleg.
- `complete-thesis-documentation` steht bei **4/6**. Technische Link-/Matrix-/
  Diff-/OpenSpec-Prüfung 3.2 ist erledigt. 2.2 ist Tobias' UI-/Screenshot-
  Abnahme; 3.1 ist die gemeinsame fachliche Text-/Matrixabnahme von Chris und
  Tobias. Kein Agent darf diese menschlichen Unterschriften fingieren.
- `harden-deployment-gateway` bleibt **3/4**. Einzig die externe SSH-Negativmatrix
  in Task 2.2 fehlt. GitHub-Secret `VM_SSH_KEY` ist vorhanden, aber dessen
  privater Wert ist nicht rücklesbar. VM-IP wurde von Chris bestätigt; autorisierte
  Administration liegt bei Chris/Tobias. Private Schlüssel nie in Chat/Git.
- `decouple-partner-number-from-login` und `add-unique-bmd-article-number`
  stehen **0/6**. Die fachlichen BMD-Feldregeln/Beispiele fehlen. Für persönliche
  Konten ist jedoch die **Identitätsentkopplung** vorab nötig; den aktiven
  Partnernummer-Change dafür nach gesonderter Prüfung in einen technischen
  Identitäts- und einen BMD-/Legacy-Nummern-Teil aufteilen.
- Die Firmenmail vom 25.08. ist fachlich bereits in
  `Documentation/Besprechungsprotokolle/Besprechungsprotokoll31_08_2026.md`
  ohne Kontaktdaten erfasst: eindeutige BMD-Artikelnummer; Kategorie,
  Fachbereich und Produktgruppe sind getrennt; Preislistenbereich kann entfallen;
  Pakete/Schulungsrechte/PP-Margen sind Zukunftsrichtungen, keine fertigen Regeln.

## Neue bestätigte Produktentscheidungen und Grenzen

Chris möchte Phase 2 vollständig und darüber hinaus zwei manuell angelegte
Testunternehmen, persönliche Konten auf Firmen- und Partnerseite sowie Audit mit
Person/Zeitpunkt. Partner- und Firmenorganisationen erhalten Admins und Mitarbeiter;
Admins verwalten Konten, aber es gibt keine Leistungsbewertung. Ein öffentlicher
Partnershop soll Gastbestellungen und optionale Endkundenkonten erlauben;
kostenpflichtige Bestellung mit späterer Abwicklung, **keine bestätigte
Online-Zahlung**. Ziel ist eine sicher deploybare Staging-Version bis Herbstferien,
nicht die schriftliche Thesis. Chris akzeptiert grundsätzlich, dass `main` den
aktuellen Produktions-Deploy auslöst.

**Nicht entschieden:** verbindliche Herbstferien-Abnahmeliste; Firmen-/Schulfreigabe
für B-06 und B-07; BMD-Produkt/API und Export-/Rückkanal; Provisionsformel/
Auszahlungsart; B-03-Schwellen; Subdomains; Shop-Layouts/Anfragefelder; Staging-
Infrastruktur. Der Plan dokumentiert Empfehlungen, nicht stillschweigende
Entscheidungen. Herr Hammer ist Verbindung zur Firma; nach dem nächsten Termin
liefert Chris ein neues Besprechungsprotokoll. Die öffentliche Website ersetzt
keinen technischen BMD-Vertrag.

## Git/Deployment-Grenze

Feature-Branch ist veröffentlicht, `main` wurde in dieser Session bisher **nicht**
aktualisiert. Der bestehende PR #27 trägt noch den alten Tax-Archiv-Titel und muss
vor Merge auf den tatsächlichen Inhalt aktualisiert werden. Lokales `main` enthält
den eigenen Commit `39bad3f`; Feature-Branch enthält die alternative Archivierung
`dd2be09`, den Tax-Härtungscommit `a9c36bd` und Trade Proof `7b2d4b7`.
Vor `main`-Update diese Historien ohne Reset/Force-Push zusammenführen und den
resultierenden Diff/CI prüfen.

Der bestehende Keycloak-Produktions-Template enthält **aktive Demo-Konten mit
bekannten Passwörtern**. `SEED_DEMO_DATA=false` deaktiviert sie nicht; eine bereits
importierte Realm wird durch bloßes Code-Deployment nicht zuverlässig aktualisiert.
Die öffentliche VM darf mit echten Firmen-/Kundendaten nicht als sicher gelten,
solange diese Konten nicht geprüft/deaktiviert und reale Zugänge eingerichtet sind.
Chris wurde gefragt, ob auf der VM nur Demo- oder bereits echte Daten liegen und
ob Demo-Logins aktiv sind. Bis zur Antwort `main`-Merge/Deploy nicht als sicher
freigeben. Bei bestehenden Company-Profilen bleibt die neue echte Registerkennung
leer; ein Firmen-Admin muss sie vor Profiländerung mit echtem Wert ergänzen.

## Altlasten außerhalb des nächsten Produkt-Agenten

Diese Punkte gehören zu Chris/Tobias bzw. autorisierter VM-Prüfung, **nicht** als
pauschales Aufgabenpaket zum nächsten Feature-Agenten:

1. Tobias liest `docs/UI_UX_DOCUMENTATION.md`, gleicht jeden Screenshot mit UI
   und Beschriftung ab, dokumentiert Abweichungen und bestätigt Task 2.2.
2. Chris + Tobias gehen FR-01–FR-25 und die ergänzenden Changes in
   `docs/REQUIREMENTS_TRACEABILITY.md` gegen FSD, Protokolle und B-01–B-08 durch,
   unterscheiden implementiert/simuliert/offen und bestätigen Task 3.1.
3. Chris/Tobias suchen den **lokalen Dateipfad** des privaten Deploy-Keys, der
   für `VM_SSH_KEY` hinterlegt wurde, oder rotieren ein dediziertes Schlüsselpaar
   mit VM-Adminzugang. Danach SSH-Negativmatrix aus
   `openspec/changes/harden-deployment-gateway/tasks.md` autorisiert ausführen;
   keine Schlüsseldatei und keinen Inhalt im Chat teilen.

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
gh pr checks 27
```

Bei Backendänderungen gemäß `AGENTS.md`:

```bash
cd backend
./mvnw test
./mvnw package
```
