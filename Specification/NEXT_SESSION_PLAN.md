# PAPO — Gesamtplan bis Sonntag, 06.09.2026

**Stand:** 05.09.2026
**Nächster Termin:** Sonntag, 06.09.2026, 19:00 Uhr
**Arbeitsgrenze:** Bestehendes MVP veröffentlichen, prüfen, dokumentieren und präsentierbar machen. Keine neue Produktfunktion beginnen.

## Quellen und Priorität

1. `AGENTS.md` und für Tobias zusätzlich `CLAUDE.md`;
2. `Documentation/Besprechungsprotokolle/Besprechungsprotokoll31_08_2026.md` vor dem Protokoll vom 25.08.;
3. die Tasks des jeweils aktiven OpenSpec-Changes;
4. dieses Dokument als change-übergreifende Reihenfolge bis Sonntag;
5. `docs/AI_WORKFLOW_EFFICIENCY.md` für Reviews, Checks und Handoffs.

Die Deployment- und MVP-Tasks bleiben getrennte Quellen der Wahrheit. Präsentation, Clockify und KI-Nachweise sind Liefergegenstände für den Termin, aber keine neuen Produktfeatures.

## Definition of Done für Sonntag

- `https://paportal.at` ist mit ausschließlich synthetischen Daten erreichbar; HTTPS, OIDC und Rollen funktionieren.
- Eine Company- und mindestens zwei Partner-Identitäten erlauben den kompletten Bestell-/Statusablauf sowie negative Cross-Role- und Cross-Partner-Prüfungen.
- CI testet vor Build/Deploy. Ein Release besteht kohärent aus Commit-SHA-Backend, Frontend, Compose, Deploy-Dateien und Markern. Ein normaler Release sowie SHA-Rollback/Forward-Recovery sind belegt, ohne Postgres oder Keycloak neu zu erstellen.
- GHCR bleibt privat; Secrets liegen nur in GitHub Actions bzw. VM-lokal. Lokale mode-600-Dumps, verschlüsselte Offsite-Backups und ein isolierter Restore beider Datenbanken sind nachgewiesen.
- Die einzige offene MVP-Aufgabe 8.4 ist nach echter Deployment-/Restore-Evidenz erledigt oder mit einem konkret dokumentierten externen Blocker offen gelassen.
- Die Reveal-Präsentation entspricht den Beschlüssen vom 25.08. und 31.08., die Live-Demo wurde einmal vollständig geprobt, Clockify und KI-Nachweise sind aktuell.
- Beide Changes sind validiert; archiviert wird nur nach vollständigem Nachweis und Chris' ausdrücklicher Freigabe.

## Verifizierter Ausgangsstand

### Git und OpenSpec

- Aktiver lokaler Branch: `docs/next-session-plan`; PR #5 gegen `main` ist offen. Der Worktree enthält fremde/geschützte Änderungen. Nicht wechseln, verwerfen oder pauschal stagen.
- `ci-cd-deployment-with-gh-actions`: **41/69** erledigt. 5.9 widerrief den breiten Interimsschlüssel; 5.10a implementierte den deaktivierten Forced-Command-Transport; 5.10b pushte ihn in PR #5. `Test backend` ist grün, Build und Deploy sind im PR übersprungen. Nächster Task ist 5.11.
- `build-partnerportal-mvp`: **52/53** erledigt. 8.3 und FD.4 sind abgeschlossen; nur 8.4 bleibt bis zu echter Deployment-/Restore-Evidenz offen.
- `automate-ai-evidence-log` ist umgesetzt und archiviert. Es ist nicht nochmals zu implementieren; pro substantivem Arbeitsblock nur `sync` und `enrich` ausführen.

### GitHub Actions und GitHub

- Tobias hat die ursprüngliche Pipeline implementiert (`47ff292`, `7fd2ff2`, `0f55eab`).
- Der letzte geprüfte `main`-Lauf für `22c14c3` war grün: `Test backend` und `Build and push image to GHCR` erfolgreich, `Deploy to production VM` absichtlich übersprungen.
- Damit sind Tests, amd64-Build und GHCR-Push bereits praktisch bewiesen. Sie werden nicht neu gebaut, sondern durch die lokalen Härtungen aus Tasks 3.6/5.7 ersetzt und nach dem Push nochmals als Regression geprüft.
- Remote-`main` enthält bis zu einem ausdrücklich freigegebenen Merge noch den alten Workflow. PR #5 enthält den gehärteten, weiter deaktivierten Workflow.
- Der interimistische Actions-Key und das zugehörige `VM_SSH_KEY` wurden in 5.9 widerrufen. Branch Protection verlangt `Test backend`, Pull Requests und gilt für Administratoren; PR #5 liefert den praktischen Nachweis.
- Das GHCR-Package `diplomarbeit-papo-backend` zeigt in GitHub `Private`; ein anonymer Manifest-Request für den veröffentlichten SHA-Tag lieferte 401. Task 3.5 ist nachgewiesen. Ein zusätzlich angeforderter `read:packages`-Scope ist für diesen Nachweis nicht mehr erforderlich.

### Infrastruktur und Inhalt

- `paportal.at` zeigt auf die VM; UFW/Ingress und persönlicher SSH-Zugang wurden geprüft. `~/papo-deploy/.env.prod` und `releases/` sind mit restriktiven Rechten vorbereitet; GHCR-Pull eines exakten SHA-Tags funktioniert. Es gibt noch keinen laufenden Stack und keinen Backup-Cron.
- Der Deploy-Job bleibt `if: false`, bis alle in Task 6.3 genannten Gates erfüllt sind.
- Die Präsentation enthält veraltete Aussagen: gemischtes „Produkt/Artikel“, „Fixer Technologie-Stack“, „Domain fehlt“ und zu weitgehende CI/CD-Fertigbehauptungen.
- Die vereinfachte Firmenmail zu Artikeldaten fehlt. Artikelstammdaten-, Mandanten-, Subdomain-, SMTP-, Shop- und KI-Features werden bis Sonntag nicht begonnen.

## Ausführungsreihenfolge

### Block 0 — Arbeitsstand vor Apply prüfen

1. `git status --short --branch`, begrenzten Diff und `openspec list --json` prüfen.
2. Zusammengehörige Änderungen von fremden/geschützten Änderungen trennen; `tmp/`, `.claude/`, `CLAUDE.md` und Tobias' Designarbeit bewahren.
3. PR #5 weder mergen noch den Deploy aktivieren; als Nächstes nur Task 5.11 nach seinen VM-/GitHub-Voraussetzungen ausführen.

### Block 1 — Deployment lokal fertigstellen — abgeschlossen

1. Task 7.2: Erfolg, Dumpfehler und Uploadfehler bestanden.
2. Task 7.3: 16 synthetische Dumps je Datenbank, mode 600 und Retention 14 bestanden.
3. Deployment-Change validiert; Deploy bleibt deaktiviert.

### Block 2 — Bereits erledigte externe Gates

Abhängigkeiten top-down bearbeiten und Tasks nur mit echtem Nachweis abhaken:

1. **3.5:** GHCR privat und anonyme 401-Ablehnung nachgewiesen.
2. **5.2/5.3:** interimistischer `restrict`-Key eingerichtet und GitHub-Werte gesetzt; der Review vom 05.09. zeigte, dass `restrict` keine Remote-Kommandos sperrt. Dieser Zugang wird in 5.9 widerrufen und nie für Deployment verwendet.
3. **5.4/5.6:** VM-lokale Produktionskonfiguration vorbereitet und exakter privater GHCR-SHA-Pull bewiesen.
4. **3.7:** Branch Protection per API nachgewiesen; praktische PR-Gate-Evidenz folgt in 5.10b.

Stoppen, wenn ein Wert in Shell-History/Toolausgabe gelangen würde, Rechte fehlen, Fingerprints abweichen oder unerwartete VM-Inhalte/Volumes existieren.

### Block 3 — Forced-Command-Transport absichern

1. **5.9:** erledigt — Interimsschlüssel und `VM_SSH_KEY` widerrufen.
2. **5.10a:** erledigt — Gateway, Sudoers, Harness und deaktivierter Workflow lokal geprüft.
3. **5.10b:** erledigt — PR #5 ist offen; `Test backend` grün, Build/Deploy übersprungen. Nicht mergen.
4. **5.11:** dedizierten Benutzer `papo-deploy`, root-owned Forced Command, enges sudoers-Gate und VM-lokalen read-only GitHub-Deploy-Key einrichten.
5. **5.12:** Shell, beliebige Kommandos, Forwarding, Secret-Lesen, Docker, unsichere Archive und Nicht-main-SHAs negativ prüfen; nur den aktuellen geschützten `main`-Commit im No-Activate-Modus akzeptieren.

Stoppen und Ersatzschlüssel widerrufen, sobald eine Berechtigungsgrenze umgangen werden kann. Keine Deployment-Aktivierung in diesem Block.

### Block 4 — Erster Release und Identitäten (Chris)

1. **6.3a:** ersten Release read-only prüfen: aktuellen geschützten `main`-SHA als Baseline, grünen ungemergten PR, bewährten GHCR-Pull, leere Produktionsressourcen, `.env.prod`, `verify` und Ports. Der spätere Merge-SHA wird nicht vorweggenommen.
2. **6.3b:** ausschließlich Deploy-Bedingung freischalten und statisch prüfen.
3. **6.3c:** Feature-Branch/PR prüfen; Commit, Push und Merge jeweils nur nach Chris' separater Freigabe. Merge löst ersten Release aus.
4. **6.3d:** Main-, Release-, Marker- und Image-SHA sowie Health und `current` abgleichen.
5. **6.4:** HTTP→HTTPS, Zertifikat, OIDC-Issuer, `/auth` und gerenderte Origins prüfen.
6. **6.5a/6.5b:** `company-verification`, `VERIFY-PARTNER-A/B` sowie den in `tasks.md` festgelegten synthetischen Artikel-/Kundenbestand anlegen.
7. **6.6:** Login und Refresh für alle drei Identitäten ohne CORS-/Redirectfehler prüfen.

Keine realen Firmen-/Personendaten und keine bekannten Demo-Passwörter verwenden.

### Block 5 — Backup und Restore (Chris)

1. **7.5a–7.5c:** vollständigen privaten S3-Eingabevertrag prüfen, `papo-s3` plus `papo-crypt` einrichten, Recovery-Parameter außerhalb der VM sichern und verschlüsselten Probezyklus beweisen.
2. **7.6:** genau einen 03:00-Cron-Eintrag installieren und beide Datenbanken live lokal/offsite sichern.
3. **7.7a–7.7c:** Produktionsbaseline erfassen, beide Offsite-Backups in präfixgeschützter digest-gepinnter PostgreSQL-16-Instanz wiederherstellen und nur exakt diese temporären Ressourcen entfernen.

### Block 6 — Tobias: UI-Dokumentation, Polish und Präsentation

Dieser Block kann parallel zu externen Chris-Gates laufen und blockiert keine VM-Arbeit.

1. **MVP 8.3 abgeschlossen:** `docs/UI_UX.md` deckt Routen, Rollen, Komponenten, Mobilverhalten und Accessibility ab.
2. **MVP FD.4 abgeschlossen:** betroffene Desktop-/Mobile-Flows und einseitige Auftragsbeleg-Druckvorschau geprüft; Modal-Fokus korrigiert.
3. **Reveal-Deck:** `presentation/mvp-deck/` gegen beide August-Protokolle korrigieren:
   - allgemeines Problem und USP vor dem Referenzfall;
   - Referenzunternehmen nicht nennen und ungeprüfte Kennzahlen entfernen oder eindeutig als verifizierten Referenzfall einordnen;
   - sichtbar durchgehend „Artikel“ statt „Produkt“;
   - Company → Partner → Endkunde und Partner-bestellt-für-Endkunde korrekt erklären;
   - „Technologie-Stack“ statt „Fixer Technologie-Stack“;
   - Hostingfolie auf den tatsächlichen Stand bringen: Domain/DNS vorhanden, Test/Build grün, gehärteter Deploy noch deaktiviert;
   - spätere Mandantenfähigkeit/Subdomains und öffentlicher Partnershop klar vom MVP trennen.
4. Deck lokal in Chromium öffnen, jede Folie bei 1280×720 und mindestens einer schmalen Ansicht prüfen; keine Überläufe, abgeschnittenen Texte oder vertraulichen Daten.
5. Wenn nach P0-Arbeit Zeit bleibt: frühere Project-Award-Präsentationen auf Aufbau, Story, Demo und visuelle Verständlichkeit untersuchen und nur konkrete übertragbare Erkenntnisse notieren.

### Block 7 — Gemeinsamer Abschluss (Chris + Tobias)

1. **MVP 8.4:** nach Deployment-8.3 die fest definierte Matrix für FSD FR-01..FR-25, kanonische OpenSpec-Anforderungen, B-01..B-08 und Beschlüsse vom 31.08. erstellen.
2. Live-Demo einmal ohne Unterbrechung proben: Company prüft/freischaltet Artikel → Partner A legt/auswählt Endkunden und bestellt mehrere Artikel → Company setzt Status → Partnerdashboard aktualisiert sich; Partner B und Rollen-/Partnerisolation negativ prüfen.
3. Clockify-Nachtrag anhand `Documentation/Clockify_Nachtrag_2026-09-03.md` prüfen und nur nicht bereits gebuchte, belegbare Zeiten erfassen.
4. Einmal pro substantivem Arbeitsblock `python3 tools/ai_evidence_log.py sync`, dann `Documentation/KI-Nachweise/prompt-register.md` auf die aktuelle Sitzungs-ID prüfen und ausschließlich mit `enrich --session-id <ID>` anreichern. Ohne aktuellen Eintrag bleibt der Backlog offen. Nur belegbare Einträge des aktuellen Blocks kuratieren, keine Rohchats/Secrets committen und keine alten Sitzungen raten oder massenhaft aufarbeiten.

### Block 8 — Release-Nachweis, Dokumentation, Reviews und Abschluss

1. **9.1a/9.1b:** erforderliches Task-/Evidenzupdate als konkreten zweiten Commit wählen und Routine-Release ohne Zustandsverlust beweisen.
2. **9.2a/9.2b:** feste API-Isolationsmatrix und vollständige Browserflows auf Desktop/Mobil durchführen; bei Datenleck sofort stoppen.
3. **9.3a/9.3b:** zwei Releases und Zustandsbaseline read-only prüfen, danach manuellen SHA-Rollback/Forward-Recovery nur für Backend/Caddy mit `--no-deps` beweisen.
4. **Deployment 8.3:** Dokumentation erst jetzt mit erprobtem Forced-Command-, Backup-, Restore- und Rollback-Verhalten synchronisieren.
5. **9.4:** `cd backend && ./mvnw test && ./mvnw package`, gezielte Change-Validierung und `openspec validate --all` ausführen.
6. **9.5:** `partnerportal-quality` plus einen separaten generischen Korrektheits-/Security-Review durchführen. Prüfschwerpunkte: Workflow-Rechte, Forced Command, Quellenprüfung, Credential-Grenzen, Release-Reihenfolge/Atomizität, Partnerisolation und Restore-Sicherheit.
7. MVP 8.3/8.4/FD.4 und Deployment-Tasks gegen Nachweise prüfen. Changes nur nach Chris' Freigabe archivieren.

## Review- und Modellstrategie

- Nach dem Push genau ein kurzer unabhängiger Handoff-Readiness-Check durch Tobias/Claude: `NEXT_SESSION_PLAN.md` gegen beide August-Protokolle, GitHub-Stand und offene OpenSpec-Tasks prüfen; Ergebnis `READY` oder konkrete fehlende/unklare Tasks. Keine Implementierung und kein zweites Deployment-Architekturreview in diesem Schritt.
- Während der Umsetzung fokussierte Checks direkt nach jeder Logikänderung; vollständige Backend-Suite/Package nur einmal vor Abschluss.
- Der große bestehende Audit-Workflow wird vor Sonntag nicht erneut vollständig ausgeführt. Er würde bereits geprüfte Arbeit duplizieren. Stattdessen Task 9.5 als begrenzten finalen Korrektheits-/Security-Review verwenden; den Gesamtaudit nur bei neuen kritischen Findings oder nach dem Termin starten.
- Bei Token-/Kontextdruck Handoff um 75 % aktualisieren und spätestens bei 85 % neu starten. Ein Agent liest nur den aktuellen Taskblock und dessen Quellen.

## Externe Blocker und bewusste Nicht-Aufgaben

- Chris muss GHCR-PAT, Secrets, VM-Konfiguration und produktive Identitäten interaktiv behandeln.
- Nur falls der S3-Send Endpoint/Bucket nicht nennt oder der Schreibtest scheitert, präzise bei Herrn Bachl nachfragen.
- Die vereinfachte Artikeldaten-Mail blockiert weder Demo noch Deployment.
- Nicht bis Sonntag implementieren: Partner-Stammdaten-Erweiterung, Gewerbeschein, neues Artikel-/Kategoriemodell, SMTP, In-App-Benachrichtigungen, Mandantenfähigkeit, Unternehmenssubdomains, öffentliche Registrierung, Partnershop oder KI-Feature.

## Stoppregel

Nach Deployment, Smoke-Test, Backups/Restore, Rollback, MVP-Dokumentation, Präsentationsprüfung, Traceability, KI-/Clockify-Nachweis und finalen Reviews stoppen. Für jedes neue Produktfeature zuerst einen getrennten OpenSpec-Vorschlag mit Scope, Sicherheitsauswirkungen, Tests und Definition of Done erstellen.
