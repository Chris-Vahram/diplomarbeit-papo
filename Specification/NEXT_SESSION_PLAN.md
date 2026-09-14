# PAPO-Planungs- und Umsetzungshandoff – 13.09.2026

## Vorrangiger nächster-session Start — nach dem Handoff vom 14.09.2026

- Branch `docs/openspec-planning-2026-09-11`, HEAD/Remote `38cd932`; vor dem aktuellen lokalen Fix war der Arbeitsbaum sauber. `origin/main` ist `05e0bdd`; lokales `main` liegt vier Commits zurück.
- `harden-deployment-gateway` **2/4**. Zuerst die öffentliche VM auf den aktuellsten lokalen Stand deployen. Danach einen autorisierten Security-/Deployment-Subagenten mit VM-, Git- und Securityrechten die Tasks 2.1/2.2 vollständig erledigen lassen.
- Lokal belegt: Gateway-Harness 18/18, Backend 100/100, Package, JS-Syntax, OpenSpec 2/2, Präsentation 15 Folien, Diff-Check.
- Public-Live-Test am 14.09. war erfolgreich: Partner-Login, Endkunde, zwei Artikel, Bestellung #52 über 218,90 €, Company-Status auf `Geprüft`; der Live-Quellstand muss trotzdem zuerst aktualisiert werden.
- `complete-thesis-documentation` bleibt 0/6. Fünf zusätzliche Planungschanges sind 4/4 artefaktvollständig; noch keine Produktimplementierung. Reihenfolge, Abhängigkeiten und Stopbedingungen stehen in `Specification/WORK_PLAN_2026-09-14.md`.
- Clockify über den angemeldeten Zen-Browser im Workspace `DA` tatsächlich erfassen: Datum, Start-/Endzeit, Dauer und Beschreibung. Zeiten dürfen retrospektiv geschätzt/ergänzt werden, müssen als Schätzung markiert werden und dürfen nicht als technische Evidenz dienen.

### Aktueller Aufgabenfilter

- Erledigt/archiviert nicht erneut beginnen: technischer MVP (53/53) sowie die archivierten Changes in `openspec/changes/archive/`.
- Wirklich offen: `complete-thesis-documentation` 0/6, Gateway-Nachweise 2.1/2.2, Partnerstammdaten/Gewerbeschein, BMD-Artikelnummer/Importregel und die Lehrerabnahme.
- Next-session Planungsblock mit höherem Modell, hohem Reasoning, OpenSpec und Caveman: BMD-Partnernummer übernehmen statt PAPO-Auto-Nummer; generische internationale Steuerkennung mit österreichischer Pflicht-/Formatprüfung; Company-only-Notizfeld mit Prüfwert 2.000 Zeichen; Gewerbeschein als sicherer aktueller Nachweis mit rechtlicher Prüfung; eindeutige BMD-Artikelnummer mit Import-/Export-/Duplikatregel.
- Aufwand als Planungsrahmen speichern: Partnernummer/Steuerkennung 0,5–2 Tage, Gewerbeschein 1–2 Tage, BMD-Artikelnummer 1–2 Tage nach Beispieldaten, zusammen minimal 3–5 Entwicklertage plus Dokumentation. Historie, Ablaufdatum, OCR, mehrere Dateien und direkte BMD-Synchronisation separat ausweisen.

### Verbindlicher Copy-paste-Auftrag für die nächste Session

```text
Arbeite mit einem höheren Modell und hohem/xhigh Reasoning. Nutze OpenSpec und Caveman für die Planung. Implementiere und plane in dieser Session zuerst nichts außer dem ausdrücklich nötigen Live-Deployment und den VM-Nachweisen:

1. Bringe paportal.at auf den aktuellsten lokalen Stand. Prüfe danach den Live-Flow erneut.
2. Beauftrage einen autorisierten Security-/Deployment-Subagenten mit den nötigen VM-, Git- und Securityrechten. Erledige mit ihm vollständig: root-eigener Gateway und Dateirechte; visudo; eingeschränkter Deploy-Account und Read-only-Repository-Key; Host-Key-Pinning; kein allgemeines sudo, Docker-Zugriff oder Lesen von .env.prod; negative SSH-Tests für beliebige Befehle, PTY, Forwarding, SCP/SFTP; side-effect-freier Verify-/Upload-Ablauf; HTTPS/OIDC/Login/Bestellung; gekoppelter Rollback ohne PostgreSQL-/Keycloak-/Caddy-Schaden. Belege alles sanitisiert und setze den Gateway-Blocker erst nach echten Nachweisen auf erledigt.
3. Danach mit OpenSpec/Caveman die weiteren Changes planen, noch nicht implementieren: BMD-geführte Partnernummer statt PAPO-Auto-Nummer; generische internationale UID-/Steuerkennung mit verifizierter österreichischer Pflicht-/Formatlogik; Company-only-Notizfeld, Prüfwert 2.000 Zeichen; Gewerbeschein als sicherer aktueller Nachweis mit Vorschlag zu Dateiarten, ca. 10 MB, Zugriff, Ersetzdatum, optionalem Ablaufdatum und rechtlicher Prüfung; eindeutige BMD-Artikelnummer mit Import-, Unique-, Duplikat- und sinnvoller Exportregel. Direkte BMD-Schnittstelle nur planen, wenn die neue Anforderung das ausdrücklich meint; nicht stillschweigend bauen.
4. Clockify diesmal konkret erledigen: rückwirkend geschätzte Einträge mit Datum, Uhrzeit und Beschreibung erstellen/erfassen. Jede Zeit als Schätzung markieren; Clockify-Einträge sind keine technischen Nachweise. Nicht erneut verschieben.
5. Keine Secrets ausgeben, keine Volumes löschen/zurücksetzen, keine fertige Implementierung behaupten. Danach Context-/Handoff-Dateien mit echten Ergebnissen aktualisieren.
```

Die älteren Abschnitte darunter sind historische Sitzungsnotizen und gelten nicht als aktueller Status.

## Aktueller Startpunkt — 13.09.2026

### Verbindlicher nächster Arbeitsauftrag

Addon: Je nach 5.9-Befund entweder einen konkreten Folgeprompt an den Gateway-Security-Agenten senden oder den fehlenden Tobias-Nachweis als Blocker festhalten. Danach Dirty-Worktree-Aufräumtask ohne OpenSpec-Change durchführen: Änderungen einzeln prüfen, sauber committen oder nach Freigabe entfernen und den Status bereinigen. Push, Pull, Merge, Deployment, VM-Zugriff und Commits mit sauberer Commitmessage sind für diesen Auftrag ausdrücklich freigegeben; keine Secrets oder Datenvolumes ausgeben/löschen.

1. 5.9 zuerst anhand sanitiserter VM-/Key-/Host-Key-/sudoers-/Remote-Negativnachweise prüfen. Der Agent soll mit den freigegebenen VM-, Git-Pull/Push/Merge- und Deployment-Rechten selbst entscheiden, ob er 5.9 ausführen darf; fehlende Sicherheitsbelege bleiben ein Stop.
2. Bei ausreichender Evidenz 5.9 selbst umsetzen und Subagents für unabhängige Remote-Security-, Browser-, Live- und gekoppelten Rollbacktests einsetzen. Keine Secrets ausgeben; laufende Daten/Volumes schützen.
3. Danach finale Security-/OpenSpec-/Live-Abnahme und Präsentationsfinalisierung nach Lehreranforderungen und neuesten Protokollen. Präsentation: durchgehend **Artikel**.
4. Clockify über mehrere belegte Arbeitstage verteilen: letzte Protokolle und OpenSpec-Archive als Tätigkeitsquelle prüfen, leere Screenshot-Blöcke beschreiben, sinnvolle Zwischenblöcke ergänzen; nur realistisch belegbare Zeiten verwenden, Unsicherheiten offen markieren.

Fortschreibung nach Integration 14.09.2026: `origin/main` `05e0bdd` ist kontrolliert übernommen. Der Basis-CI/CD-Change bleibt wie von Tobias geliefert archiviert; offene Gateway-Arbeit liegt in `harden-deployment-gateway` (**2/4**). Demo-Seeding ist übernommen, standardmäßig aus und mit der lokalen Kategorie-Migration kombiniert. Fokus 1/1, Suite 100/100 und Package 100/100 erfolgreich. Nächster Schritt: Tobias' VM-/Key-/sudoers-/Remote-Nachweise für Tasks 2.1/2.2; erst danach Aktivierung und finale Livechecks.

- MVP: `build-partnerportal-mvp` **53/53**, technisch abgeschlossen, nicht archiviert.
- Apply-ready: `complete-thesis-documentation` **0/6** und `integrate-category-management-into-article-management` **0/4**.
- GitHub belegt: GHCR privat, `main` geschützt. Julian besitzt laut Screenshot noch Write-Zugriff; klären.
- Reihenfolge: Gateway-Remote-Nachweise, Security-/Livechecks, Cron/Backup, danach Dokumentation und Präsentation. Kein Volume löschen, keine Secrets ausgeben.

## Tatsächlicher Abschlussstand — 12.09.2026

`define-mvp-traceability-format` (**3/3**) und `manage-article-categories` (**6/6**) sind nach fokussierter Prüfung, Strict-Validate, passenden Gesamtprüfungen, Browserprüfung bzw. Dokumentationscheck sowie `partnerportal-quality` und den erforderlichen Reviews archiviert. Archive: `openspec/changes/archive/2026-09-12-define-mvp-traceability-format` und `openspec/changes/archive/2026-09-12-manage-article-categories`.

Aktiv sind nur `build-partnerportal-mvp` (**50/53**) und `ci-cd-deployment-with-gh-actions` (**30/41**). Nicht starten: Der MVP-Change hat die offenen Aufgaben 8.3 (Tobias UI/UX-Doku), 8.4 (gemeinsame Traceability-/Diplomarbeit) und FD.4 (Tobias-Polish neuer Screens). CI/CD ist weiter **BLOCKIERT**; offen sind 3.5/3.6, 6.1–6.4, 7.5/7.6a sowie post-MVP 7.2/7.3/7.6b. Die getrennten Evidenzblocker P0-Laufzeitdiagnose, Frontend-Archiv-/Recoverypfad und PR-#5-Gateway-/Schlüsselgrenzen bleiben ungeklärt. Nächster Schritt nur nach Tobias-/gemeinsamer Freigabe bzw. neuen Nachweisen. Kein Commit/Push/Merge/Deployment.

## Neuer tatsächlicher Startpunkt — define-mvp-traceability-format abgeschlossen

`define-mvp-traceability-format` ist **3/3 fertig**, nicht archiviert. Die neue Matrix `docs/REQUIREMENTS_TRACEABILITY.md` hat 33 eindeutige Quellenzeilen, acht Spalten, Legende und funktionierende relative Links; offene Zuordnungen und der historische/aktuelle Prüfstatus sind transparent. `docs/DOCUMENTATION_TODO.md` verweist auf das Format, benennt die Verantwortlichkeiten und hält 8.3/8.4/FD.4 offen. Strict-Validate, Quellen-/Link-/Strukturchecks, Diff-Check sowie Funktions-/Evidenz-/Securityreview waren ohne Findings.

```text
/goal PAPO weiterführen. GPT-5.6 Terra medium. Der aktuelle Stand ist: search-order-customers, clarify-order-customer-context, show-partner-company-name und use-huemer-demo-partner sind fertig geprüft und archiviert; manage-article-categories und define-mvp-traceability-format sind jeweils vollständig, aber gemäß Auftrag nicht archiviert. Vorhandene Dirty-Worktree-Änderungen und die untracked Playwright-Screenshots bewahren; kein Fetch/Pull, Commit, Push, Merge, Archivieren, Deployment, VM-, Secret-, SSH-, Backup- oder GitHub-Secrets-Zugriff.

Vor jedem neuen Change ausschließlich nach dem bestehenden PAPO-Ablauf vorgehen: fehlende Context-Dateien genau einmal lesen, git status --short --branch und openspec list --json, dann status/instructions apply des konkret freigegebenen Changes und nur dessen Context-Files. Tasks strikt sequenziell; Checkboxen nur nach echten Akzeptanz-, Daten-/Security-, Test- und DoD-Nachweisen. Nach Abschluss Strict-Validate, passende Prüfung, partnerportal-quality, Funktions-/Test-/Sinnhaftigkeitsreview und bei Bedarf Securityreview. KI-Nachweise nur mit tatsächlich ausgeführten Checks synchronisieren. CI/CD, build-partnerportal-mvp und ausgeschlossene Themen nicht spekulativ beginnen. Keine Archivierung ohne ausdrücklichen Auftrag. Am Ende nur kurze Commit-Vorschläge und exakte git-add-/Push-Befehle ausgeben, niemals ausführen.
```

## Neuer tatsächlicher Startpunkt — 12.09.2026

`manage-article-categories` ist **6/6 fertig**, nicht archiviert. Belegt: fokussiert 27/27, Full-Test 79/79, Package, Strict-Validate, Diff-Check, Browser 1440/390 und alle geforderten Qualitäts-/Funktions-/Daten-/Securityreviews ohne Findings. Die letzten Änderungen umfassten Kategorieverwaltung, Migration, FK/Legacy-Synchronisierung, Importregeln, Company-UI und Artikel-Select; bestehende inaktive Zuordnung wird im Edit-Select als `(deaktiviert)` angezeigt. Nächster zulässiger Change: `define-mvp-traceability-format`. CI/CD/MVP bleiben ohne neue Entscheidung/Nachweise außen vor.

```text
/goal PAPO weiterführen. GPT-5.6 Terra medium. Setze nach dem fertig implementierten, aber nicht archivierten Change manage-article-categories mit dem nächsten zulässigen genehmigten Change define-mvp-traceability-format an. Prüfe zuerst nur, falls im aktuellen Kontext fehlend, AGENTS.md, CLAUDE.md, Specification/CONTINUATION_PROMPT.md, Specification/NEXT_SESSION_PLAN.md und openspec/PLANNING_REVIEW_2026-09-11.md genau einmal. Danach git status --short --branch und openspec list --json; dann status --change define-mvp-traceability-format --json und instructions apply --change define-mvp-traceability-format --json, anschließend ausschließlich dessen Context-Files lesen. Dirty Worktree bewahren. Kein Fetch/Pull, Commit, Push, Merge, Archivieren, Deployment, VM-, Secret-, SSH-, Backup- oder GitHub-Secrets-Zugriff. Tasks strikt nacheinander, Checkboxen erst nach echten Akzeptanz-, Test- und DoD-Nachweisen. Keine Subagents ohne klar unabhängigen Diagnose-/Reviewnutzen.

Bereits fertig und archiviert: align-visible-article-terminology, fix-partner-create-commission-default, add-standalone-customer-creation, search-order-customers, clarify-order-customer-context, show-partner-company-name, use-huemer-demo-partner. manage-article-categories ist 6/6 fertig, aber nicht archiviert. Bestehenden Vorlauf in frontend/src/pages/partner-order-new-page.js und alle untracked Playwright-Screenshots bewahren.

Für define-mvp-traceability-format: nur dessen Scope bearbeiten; keine Produkt- oder MVP-Abnahme erfinden. Nach jeder Task fokussiert prüfen, danach Strict-Validate und die in dessen Tasks genannten Reviews/Nachweise ausführen. Am Ende die vier Handoff-Dateien ausschließlich mit echten Ergebnissen aktualisieren. CI/CD, build-partnerportal-mvp und ausdrücklich ausgeschlossene Themen nicht einplanen. Kurze Commit-Vorschläge und exakte git-add-/Push-Befehle nur ausgeben, niemals ausführen.
```

## Exakter Startprompt nach Auto-Compact — 12.09.2026

```text
/goal PAPO weiterführen. GPT-5.6 Terra medium. Setze beim aktuellen Arbeitsstopp in manage-article-categories an; nicht von vorne beginnen und keine bereits archivierten Changes erneut bearbeiten. Arbeite immer genau einen OpenSpec-Change gleichzeitig und Tasks strikt nach Abhängigkeit nacheinander. Hauptagent sequenziell; Subagents nur für klar unabhängige Diagnose/Recherche/Review, nicht blind pro Task und nie für dieselben Dateien. Token/Credits sparsam.

Vor dem nächsten Change nur falls diese Informationen im aktuellen Kontext fehlen: AGENTS.md, CLAUDE.md, Specification/CONTINUATION_PROMPT.md, Specification/NEXT_SESSION_PLAN.md und openspec/PLANNING_REVIEW_2026-09-11.md genau einmal lesen. Danach git status --short --branch und openspec list --json; für den aktiven Change status --change und instructions apply ausführen und ausschließlich dessen Context-Files lesen. Dirty Worktree vollständig bewahren. Kein Fetch/Pull, Commit, Push, Merge, Archivieren, Deployment, VM-, Secret-, SSH-, Backup- oder GitHub-Secrets-Zugriff. Frontend-/Präsentationsänderungen und die untracked Playwright-Screenshots nicht überschreiben/löschen.

Aktueller Stand: search-order-customers, clarify-order-customer-context, show-partner-company-name und use-huemer-demo-partner sind fertig geprüft und archiviert. manage-article-categories ist 3/6: 1.1, 1.2 und 2.1 abgehakt; die letzte Import-Erweiterung wurde angewendet, aber nach dem Auto-Compact noch nicht fokussiert getestet. 2.2, 3.1 und 4.1 sind offen. Nicht archivieren.

Wiederaufnahme: prüfe zuerst den letzten Diff in ImportDefinition.java, ImportService.java und ProductImportDefinition.java. Behebe davor den dokumentierten stale-Entity-Punkt, falls der Code ihn noch nicht erfüllt: nach Kategorie-Lock Entity-Zustand refreshen und Aktivstatus/Legacyname erneut prüfen; Rename/Deactivate zwischen Vorauflösung und Lock darf nicht unbemerkt durchgehen. Dann 2.2 vollständig und fokussiert testen: bekannte/optionale Kategorien, unbekannte/inaktive Kategorien, gemischte Datei, Preview→Rename/Deactivate, Parallelcommit, Rollback, keine Kategorienanlage und korrekter Zeilenreport. Erst bei erfüllter DoD Checkbox setzen. Danach 3.1 Company-Kategorienseite plus Artikelform-Select implementieren und mit Browser 1440/390, Tastatur, Escaping, Pending, GET-/Save-Fehler/Retry, deaktivierter Bestandsoption und Partnerisolation belegen; Partner darf keinen globalen Kategorienabruf erhalten. Danach 4.1 inklusive docs/DATA_MODEL.md.

Nach jedem Task gezielt testen. Nach manage-article-categories: openspec validate manage-article-categories --strict, relevante Backendtests, ./mvnw test, ./mvnw package unter JAVA_HOME=/Users/mitgl/Library/Java/JavaVirtualMachines/temurin-21.0.9/Contents/Home, Browserprüfung, partnerportal-quality sowie Funktions-/Test-/Sinnhaftigkeits- und wegen Daten/Rollen/Input Securityreview. Findings beheben und erneut prüfen. KI-Nachweis nur mit real ausgeführten Checks synchronisieren/anreichern. Am Ende dieses Changes die vier Handoff-Dateien mit dem echten Stand aktualisieren. Nicht spekulieren und keine Prüfungen erfinden. Danach mit dem nächsten nicht blockierten freigegebenen Change fortfahren; CI/CD/MVP und ausdrücklich ausgeschlossene Themen bleiben außen vor.
```

## Arbeitsstopp – tatsächlicher Wiederaufnahmepunkt 12.09.2026

Dieser Abschnitt ersetzt die älteren Startanweisungen darunter.

1. `git status --short --branch` ausführen und den schmutzigen Arbeitsbaum vollständig bewahren. Branch: `docs/openspec-planning-2026-09-11`. Kein Fetch/Pull, Stash, Commit, Push, Merge oder Deployment.
2. `search-order-customers` ist mit 3/3 Tasks fertig und als `openspec/changes/archive/2026-09-12-search-order-customers` archiviert. Die lokale ID-Mitgliedschaftsprüfung wurde nach dem Review ergänzt; Browser 1440/390, Java-21-Package 79/79, Strict-Validate, Diff-Check, Projekt-/Funktions-/Securityreview und Abschlussreview sind ohne Blocker belegt.
3. `clarify-order-customer-context` ist mit 3/3 Tasks fertig und als `openspec/changes/archive/2026-09-12-clarify-order-customer-context` archiviert. Kundenüberschriften, exakter Prüfhint, Katalog-vor-Bestellpanel, responsive Reihenfolge, unveränderter Ordervertrag und Reset sind mit Browser 1440/390, Java-21-Package 79/79, Strict-Validate, Diff-Check und den geforderten Reviews ohne Blocker belegt.
4. `show-partner-company-name` ist mit 3/3 Tasks fertig und als `openspec/changes/archive/2026-09-12-show-partner-company-name` archiviert. Huemer-Anzeige, Retry, Logout-/Navigations-Races, Sitzungsrotation, Mobile-Wrap und Profilnavigation sind mit Browser 1440/390, Java-21-Package 79/79, Strict-Validate, Diff-Check und den geforderten Reviews ohne Blocker belegt.
5. `use-huemer-demo-partner` ist mit 2/2 Tasks fertig und als `openspec/changes/archive/2026-09-12-use-huemer-demo-partner` archiviert. Der bestehende `PARTNER-DEMO`-Seed nennt `Huemer GmbH`; Partnernummer, Beziehungen und Seed-Gating blieben unverändert. Fokus 12/12, Suite/Package 79/79, Strict-Validate, Diff-Check, lokaler Browsernachweis und geforderte Reviews sind ohne Blocker belegt. Als Nächstes `manage-article-categories`; CI/CD und MVP bleiben ohne neue Entscheidung/Nachweise außen vor.

Belegte letzte Checks: `./mvnw test` und `./mvnw package` unter Java 21 erfolgreich mit 79/79 Tests; für die vier Changes außerdem passende `node --check`-Läufe, Strict-Validate und `git diff --check` sowie Playwright 1440/390 mit temporären Kunden-/Order-/Profil-Mocks und echtem Huemer-Namen. Der nächste Change ist `manage-article-categories`.

Untracked lokale Screenshots bewahren: `output/playwright/show-partner-company-name-desktop.png`, `output/playwright/show-partner-company-name-mobile.png`. KI-Evidence-Sync/Enrich wurde mit den tatsächlich ausgeführten Checks synchronisiert; keine fiktiven Nachweise anlegen.

## Fortschreibung — 11.09.2026

Maßgeblich: [vollständiger Ergebnisreport](../openspec/PLANNING_REVIEW_2026-09-11.md). Der genehmigte Plan wurde in sechs getrennten neuen OpenSpec-Changes umgesetzt; elf aktive Changes insgesamt. Der Endkundenplan erhält einen präzisen UI-Submit-/HTTP-401-Retry-Vertrag. Vorherige Kandidaten-/READY-Tabellen unten sind historisch.

## Nächster Start — kurz

Die vier aktuellen Changes sind unter `openspec/changes/archive/2026-09-12-*` archiviert und weiterhin nicht committed. `add-standalone-customer-creation` ist **6/6 fertig**, Java-21-Fokuslauf 9/9, vollständige Suite und Package jeweils 79/79; Strict-Validate und Diff-Check grün. Browser 1440/390 prüfte Anlage, Filterreset, Text-Escaping, Pending-/Fehler-/Refreshpfade, Wizard-Wiederverwendung und Rollenrouten. Nächster nicht blockierter Change: `manage-article-categories`.

`align-visible-article-terminology` ist mit 2/2 Tasks fertig und archiviert. Sichtbare Begriffe lauten Artikel/Artikelkategorie; technische Namen, IDs, APIs und Importaliasnamen blieben kompatibel. Java-21-Fokuslauf: 26/26. `./mvnw test` und `./mvnw package`: jeweils 77/77, 0 Fehler. Browserprüfung 1440/390 für Company-/Partner-Artikel, Import und Bestellung einschließlich Fehlerpfaden; Projekt-, Funktions- und Securityreview ohne Blocker.

`fix-partner-create-commission-default` ist mit 2/2 Tasks fertig und archiviert. `openCreate()` startet bei 5 %; API, Import, Berechnung und bestehende Daten blieben unverändert. Browser 1440/390 prüfte 5-%-POST, Abbruchreset, 10 % beim Bearbeiten und individuellen 7,5-%-Wert; `./mvnw package` 77/77, Strict-Validate und Diff-Check grün; Reviews ohne Findings.

Nächster Change: `manage-article-categories`. Start: `git status --short --branch`, `openspec list --json`, `openspec status --change manage-article-categories --json`, `openspec instructions apply --change manage-article-categories --json`; nur dessen Context-Files lesen. Kein automatisches Archivieren oder Committen.

Bestätigt: bestehender Partnername als Firmenname; synthetische Huemer GmbH; verwaltete Kategorien mit kontrolliertem Import; Kundensuche/Neuanlagebutton; Kundenbezug und links/rechts-Anordnung. Keycloak-ID statt Nummer als künftige Loginbindung; vorhandene Partnernummern bleiben. Kundenbearbeitung nur nach Schutz historischer Kundendaten.

Die Produktumsetzung der drei Changes ist abgeschlossen; diese Sitzung synchronisiert Specs, archiviert die drei Changes und aktualisiert die Handoffs. Neue Changes besitzen Proposal, Design, Delta-Spec und Tasks mit Akzeptanz, Daten/Security, Tests, DoD und Stop. MVP-Abschluss und CI/CD bleiben mit konkreten fremden Nachweisen offen.

**Umsetzungsstand 11.09.2026:** Der Keycloak-Dev-Service-Blocker für `align-visible-article-terminology` ist gelöst. Der Change steht bei 2/2 Tasks, mit Java-21-Gesamttest/Package, Browserprüfung und Reviews ohne Blocker; unter `openspec/changes/archive/2026-09-11-align-visible-article-terminology` archiviert, nicht committed.

### Nächste Sitzung

1. Branch/Worktree prüfen und vorhandene Änderungen bewahren. Aktueller Planungsbranch `docs/openspec-planning-2026-09-11`; kein automatischer Stash/Branchwechsel/Pull.
2. Ergebnisreport und seitdem geänderte Quellen lesen; neuestes Protokoll ist `Besprechungsprotokoll_10.9.md`.
3. Nach dem Commit den Change `show-partner-company-name` beginnen; zuerst Status und Apply-Instructions lesen, dann ausschließlich dessen Context-Files.
4. Snapshot: Chris entscheidet Altbestandsquelle oder Umstellungsstand. Login: verifiziertes issuer/sub-Mapping und Bindungsprozess fehlen. Ohne Antworten keine Migration beginnen.
5. Neue Verifikation exakt dokumentieren; historisches 11/11-Validierungsergebnis oder alte Browserchecks sind keine neue Produktregression.

### Terra-medium-Goal zur taskweisen Umsetzung

```text
/goal Implementiere die bestätigten PAPO-OpenSpec-Changes taskweise mit Terra medium. Lies zuerst vollständig AGENTS.md, CLAUDE.md, Specification/CONTINUATION_PROMPT.md, Specification/NEXT_SESSION_PLAN.md und openspec/PLANNING_REVIEW_2026-09-11.md. Prüfe Branch, git status und openspec list; bewahre alle fremden und uncommitteten Änderungen. Kein Fetch/Pull bei Dirty Worktree. Kein Commit, Push, Merge, Deployment, VM-, Secret-, SSH-, Backup- oder GitHub-Secrets-Zugriff. Nutze für jeden Change einzeln openspec-apply-change: Change ankündigen; openspec status --change <change> --json und openspec instructions apply --change <change> --json ausführen; alle dort genannten Context-Files vollständig lesen; dann offene Tasks der Reihe nach umsetzen und jede Checkbox unmittelbar nach erfüllten Akzeptanzkriterien, Tests und DoD setzen. Bei Stopbedingung, unklarer Anforderung oder Designfehler nicht raten: genauen Blocker im Change dokumentieren und den Change pausieren. Reihenfolge: align-visible-article-terminology, fix-partner-create-commission-default, add-standalone-customer-creation, show-partner-company-name, use-huemer-demo-partner, search-order-customers, clarify-order-customer-context, manage-article-categories, define-mvp-traceability-format. Pro Change nur dessen Scope; keine Mandantenfähigkeit, Subdomains, öffentlichen Partnershop, direkte BMD-Schnittstelle, Fachbereiche, Produktgruppen, Paketlogik oder KI-Features ergänzen. Nach jedem Task passende fokussierte Tests ausführen. Nach jedem fertigen Change openspec validate <change> --strict, relevante Backend-Gesamttests und package bzw. Desktop-/Mobile-Browserprüfung ausführen. Danach partnerportal-quality, unabhängigen Funktions-/Test-/Sinnhaftigkeitsreview und bei Daten-, Auth-, Rollen- oder Eingabeänderungen unabhängigen Securityreview durchführen; Findings beheben, erneut testen und validieren. Einen Change erst als fertig melden, wenn alle Tasks, Akzeptanzkriterien, Daten-/Securityfolgen, Tests und DoD belegt sind. Nicht automatisch archivieren; fertigen Change samt exakten Prüfergebnissen und nächstem Change melden und weiter iterieren. Am Ende CONTINUATION_PROMPT.md, NEXT_SESSION_PLAN.md, RESTARBEITSPLAN_2026-09-09.md und den Planning-Report mit dem tatsächlichen Stand aktualisieren, KI-Nachweis synchronisieren/anreichern und sichere, kurze Commit-Vorschläge mit expliziten git-add-Pfaden liefern. Keine Prüfung erfinden.
```

## Neuer Start-Prompt für die nächste Session

```text
/goal PAPO weiterführen. GPT-5.6 Terra medium setzt genehmigte OpenSpec-Changes taskweise um; immer nur einen Change gleichzeitig.

Vorstart:
- Lies AGENTS.md, CLAUDE.md, Specification/CONTINUATION_PROMPT.md, Specification/NEXT_SESSION_PLAN.md und openspec/PLANNING_REVIEW_2026-09-11.md genau einmal.
- Prüfe `git status --short --branch` und `openspec list --json`.
- Dirty Worktree bewahren; kein Fetch/Pull bei Dirty Worktree.
- Kein Commit, Push, Merge, Deployment, VM-, Secret-, SSH-, Backup- oder GitHub-Secrets-Zugriff.
- Bestehende Frontend-/Präsentationsänderungen und die untracked Playwright-Screenshots nicht überschreiben oder löschen.

Bereits abgeschlossen und archiviert:
- `align-visible-article-terminology`
- `fix-partner-create-commission-default`
- `add-standalone-customer-creation`

Aktueller Wiederaufnahmepunkt — 12.09.2026:
- `search-order-customers` ist mit 3/3 Tasks fertig und unter `openspec/changes/archive/2026-09-12-search-order-customers` archiviert; nicht erneut umsetzen.
- Belegt: Browser 1440/390 mit >20/0 Treffern, Umlauten, Tastaturauswahl, Filter-/Kundenwechsel, Warenkorb-/Payload-Erhalt, Draft, Abbruch und Partner-/Company-Regression; Erfolgspfade temporär gemockt, keine echte Order persistiert.
- `node --check frontend/src/pages/partner-order-new-page.js`, `cd backend && ./mvnw package` (79/79), `openspec validate search-order-customers --strict`, `git diff --check`, `partnerportal-quality` sowie Funktions-/Sinnhaftigkeits- und unabhängiger Abschlussreview sind ohne Blocker belegt.
- `clarify-order-customer-context` ist mit 3/3 Tasks fertig und unter `openspec/changes/archive/2026-09-12-clarify-order-customer-context` archiviert; seine Änderungen in `frontend/src/pages/partner-order-new-page.js` und `frontend/src/styles/base.css` bleiben erhalten.

Danach:
- Als Nächstes ausschließlich `use-huemer-demo-partner` taskweise fortsetzen: erst Status/Apply, nur dessen Context-Files lesen. Die archivierten Kundenkontext- und Firmenanzeigeänderungen nicht zurücksetzen.
- `show-partner-company-name`: 3/3 Tasks fertig und archiviert.
- `use-huemer-demo-partner`: 2/2 Tasks fertig und archiviert.
- Keine Checkbox ohne Akzeptanzkriterien, Daten-/Securityfolgen, Tests und DoD setzen.

Pro Change:
1. `openspec status --change <name> --json`
2. `openspec instructions apply --change <name> --json`
3. Tasks strikt nacheinander umsetzen; nach jeder Task fokussiert testen.
4. Bei Unklarheit oder Stopbedingung nicht raten, sondern den exakten Blocker dokumentieren.
5. Nach Abschluss `openspec validate <name> --strict`, passende Backendtests/Package oder Browserprüfung ausführen.
6. Danach `partnerportal-quality`, Funktions-/Test-/Sinnhaftigkeitsreview und bei Auth/Daten/Rollen/Input zusätzlich Securityreview durchführen.
7. Nicht automatisch archivieren oder committen.

Java 21:
`JAVA_HOME=/Users/mitgl/Library/Java/JavaVirtualMachines/temurin-21.0.9/Contents/Home`

Nicht einplanen: Mandantenfähigkeit, Subdomains, öffentlicher Partnershop, direkte BMD-Schnittstelle, Fachbereiche, Produktgruppen, Paketlogik, KI-Feature sowie blockierte Snapshot-, Login-, Partnerstammdaten-, Nachweis-, E-Mail- und MVP-/CI/CD-Themen ohne neue Entscheidungen/Nachweise.

Am Ende nur den echten Stand in CONTINUATION_PROMPT.md, NEXT_SESSION_PLAN.md, docs/RESTARBEITSPLAN_2026-09-09.md und openspec/PLANNING_REVIEW_2026-09-11.md nachziehen. Keine Prüfungen erfinden. Kurze Commit-Vorschläge sowie exakte `git add`-/Push-Befehle nur ausgeben, nicht ausführen.
```

Der alte nächste Astra-Goal darunter ist durch diese abgeschlossene Planung ersetzt; nicht dieselben Entwürfe erneut von null erstellen.

> **Fortsetzung nach OpenSpec-Planung, Stand 10.09.2026:** Dieser Abschnitt ersetzt die darunterliegende ursprüngliche Startlage. Nicht wieder bei null beginnen. Drei Changes sind geplant und streng validiert: `add-standalone-customer-creation`, `fix-partner-create-commission-default`, `define-mvp-traceability-format`. Detailstatus: `openspec/PLANNING_REVIEW_2026-09-10.md`. CI/CD und MVP-Abschluss bleiben blockiert. Die ältere Kandidatenliste darunter bleibt als Herleitung erhalten.

## Nächster Astra-Goal

```text
/goal Setze die PAPO-OpenSpec-Planung am dokumentierten Stand vom 10.09.2026 fort, ohne Produktimplementierung, Commit, Push, Merge, Deployment, VM-, Secret-, SSH-, Backup- oder GitHub-Secrets-Zugriff. Arbeite knapp und quellenbezogen: lies jede relevante Datei einmal, nutze rg und gezielte Ausschnitte statt wiederholter Vollreads. Prüfe zuerst den realen Workspace; bewahre alle uncommitteten Änderungen. Wenn der Arbeitsbaum sicher sauber bzw. durch den Benutzer gesichert ist, führe git fetch --prune und git pull --ff-only auf dem aktuellen Zielbranch aus; bei Dirty Worktree, Divergenz oder Konfliktgefahr nicht pullen, sondern genauen Blocker melden. Ermittle danach das neueste Besprechungsprotokoll in Documentation/Besprechungsprotokolle/, lies und analysiere es vollständig und vergleiche seine Entscheidungen mit AGENTS.md, Specification/CONTINUATION_PROMPT.md, Specification/NEXT_SESSION_PLAN.md, docs/RESTARBEITSPLAN_2026-09-09.md, FSD, Roadmap, Phase-2-Backlog, kanonischen Specs, openspec list --json sowie allen aktiven Proposals/Designs/Specs/Tasks/Reviews. Übernimm neue bestätigte Anforderungen ausschließlich als kleine getrennte OpenSpec-Changes; ändere oder blockiere bestehende Changes, wenn das neueste Protokoll Scope, Daten, Security, UI, Tests oder Abhängigkeiten ändert. Prüfe zuerst die drei als READY dokumentierten Changes add-standalone-customer-creation, fix-partner-create-commission-default und define-mvp-traceability-format gegen den dann aktuellen Git-/Besprechungsstand; keine Umsetzung. Danach prüfe jeden übrigen Chris-Kandidaten einzeln. Lege nur für fachlich bestätigte, nicht extern blockierte Arbeit vollständige proposal.md, design.md, Delta-Spec und tasks.md an. Jede Task braucht Voraussetzungen, Komponenten, Akzeptanzkriterien, Daten-/Securityfolgen, Tests, DoD und Stopbedingung. Extern oder fachlich offene Kandidaten erhalten nur die genaue fehlende Entscheidung, nie Terra-READY. Mandantenfähigkeit, Subdomains, öffentlicher Partnershop, direkte BMD-Schnittstelle, Fachbereiche, Produktgruppen, Paketlogik und KI-Feature nicht einplanen. CI/CD ausschließlich als Tobias-Blocker reviewen; P0-Ausfall, Frontendpfad und PR #5 getrennt halten. Abschließend jeden aktiven/relevanten Change mit openspec validate --strict, unabhängigem Securityreview und unabhängigem Funktions-/Test-/Sinnhaftigkeitsreview prüfen. Zielzustand: jeder bestätigte Chris-Task besitzt einen entscheidungskompletten kleinen OpenSpec-Change und Status READY für Terra medium; jeder andere Kandidat besitzt eine konkrete offene Entscheidung oder Evidenz und Status BLOCKIERT. Aktualisiere danach Continuation Prompt, Next Session Plan, Restarbeitsplan und openspec/PLANNING_REVIEW_2026-09-10.md mit tatsächlichen Ergebnissen. Keine Prüfungen erfinden.
```

## Startreihenfolge nächste Session

1. `git status --short --branch`, `git log -10 --oneline --decorate`, `openspec list --json`.
2. Nur bei sicher sauberem/gesichertem Arbeitsbaum: `git fetch --prune`, dann `git pull --ff-only`. Nie lokale Änderungen überschreiben oder stashen ohne Auftrag.
3. Neuestes Besprechungsprotokoll per Dateiliste bestimmen, vollständig lesen, Änderungen zum Stand 10.09 herausarbeiten.
4. Pflichtkontext und relevante OpenSpec-Artefakte gezielt lesen; keine wiederholten Vollreads ohne neue Revision.
5. Drei READY-Changes zuerst auf Drift prüfen; danach blockierte Kandidaten und neue Besprechungsentscheidungen.
6. Alle Changes streng validieren und unabhängig reviewen; Kontextdateien zuletzt synchronisieren.

## Aktueller Abschlussstatus

- READY: `add-standalone-customer-creation` (0/6), `fix-partner-create-commission-default` (0/2), `define-mvp-traceability-format` (0/3).
- BLOCKIERT: `build-partnerportal-mvp` (50/53: 8.3, 8.4, FD.4), `ci-cd-deployment-with-gh-actions` (30/41 plus aktuelle Tobias-Nachweise).
- Nicht geplant wegen offener Entscheidungen: Partneridentität/Stammdaten außer 5 %, Partnernachweis, Artikel-Minimalmodell, Kategorien, Personalisierung, reale E-Mails, In-App-Benachrichtigungen.
- Aktiver Workspace war bei Abschluss schmutzig; deshalb kein Pull. Branch zum letzten Check: `main`, Tracking `origin/main`; Remote-Inhalt nicht neu abgerufen.

## Ziel

Prüfe zuerst vorhandene, uncommittete Planentwürfe. Plane danach verbleibende bestätigte PAPO-Arbeit einzeln mit OpenSpec. Nach diesem Block ist jeder nicht extern blockierte Chris-Task vollständig für `GPT-5.6 Terra medium` implementierbar. Nicht lösbare externe Abhängigkeiten werden als präzise Entscheidung dokumentiert, nie als fertige Terra-Task ausgegeben.

## Pflichtlektüre

1. `AGENTS.md`
2. `Specification/CONTINUATION_PROMPT.md`
3. dieses Dokument
4. `docs/AI_WORKFLOW_EFFICIENCY.md`
5. Besprechungsprotokolle 25.08. und 31.08.2026
6. `Specification/ROADMAP_2026-08-25.md`, FSD und Phase-2-Backlog
7. `openspec list --json`, dann vollständige Artefakte der aktiven Changes
8. Code und Tests der jeweils betroffenen Capability

## Arbeitsauftrag für Astra

```text
Arbeite ausschließlich als Planungs-, OpenSpec- und Review-Session für PAPO. Lies AGENTS.md, CONTINUATION_PROMPT.md, NEXT_SESSION_PLAN.md und docs/AI_WORKFLOW_EFFICIENCY.md vollständig. Prüfe Git/OpenSpec-Stand. Keine Produktimplementierung, kein Commit, Push, Merge, Deployment, VM-, Secret-, SSH-, Backup- oder GitHub-Secrets-Zugriff.

Prüfe zuerst den uncommitteten Entwurf `openspec/changes/add-standalone-customer-creation/`. Er wurde vor dieser Astra-Session erstellt und gilt nicht als freigegeben oder Terra-ready. Prüfe fachliche Grundlage, Scope, Partnerisolation, Tests, Tasks, UI-Verhalten und Abhängigkeiten unabhängig. Korrigiere oder verwerfe ihn, wenn er nicht belastbar ist. Erst nach diesem Review darfst du seinen Status als READY oder BLOCKIERT bewerten.

Gehe Kandidat für Kandidat in dieser Reihenfolge vor. Prüfe jeweils aktuelle Quellen, Code und Tests; kläre Widersprüche als Dokumentationslücke. Lege pro bestätigter Capability genau einen kleinen OpenSpec-Change mit proposal.md, design.md wenn Entscheidungen nötig sind, Delta-Spec und tasks.md an. Jede Task braucht konkrete Akzeptanzkriterien, Sicherheits-/Datenfolgen, Tests, DoD und Stopbedingung. Kein Terra-Ready, solange eine fachliche, externe oder Sicherheitsentscheidung fehlt.

1. Unabhängige Endkundenanlage: Roadmap behauptet sie sei vorhanden, `partner-customer-list-page.js` zeigt nur Liste. Beweise Ist-Zustand und plane nur bei echter Lücke.
2. Partneridentität und Stammdaten: automatische P-Nummer, externe Referenz, Firma/Ansprechpartner, Notiz, Steuerkennung, 5-%-Standard.
3. Partnernachweis: erst planen, wenn Speicher-, Zugriffs-, Lösch- und Nachweisregeln belegbar entschieden sind; sonst exakte externe Fragen dokumentieren.
4. Artikel-Minimalmodell: Artikelnummer, BMD-Import und Dedupe nur nach vereinfachter Firmenmail bzw. Beispieldaten; Fachbereich, Gruppen und Pakete ausschließen.
5. Kategorienverwaltung: globale Kategorieverwaltung ohne Tenant-Vorgriff; Abhängigkeit zu Artikel-Minimalmodell begründen.
6. Kleine Unternehmenspersonalisierung: Name, Logo, Begrüßung; keine Theme-Engine.
7. Reale Bestell-E-Mails: nur mit geklärtem SMTP-/No-reply-Stand, persistierter Bestellung vor Versand, Ereignis-ID, Status und Retry.
8. Wesentliche In-App-Benachrichtigungen: nach E-Mail-Entscheidung; Glocke, Ungelesen-Zähler, letzte 20, Link, gelesen; keine WebSockets.
9. MVP-Dokumentation: 8.4 nur als gemeinsames Traceability-Format planen. 8.3 und FD.4 sind Tobias-Aufgaben, nicht übernehmen.
10. CI/CD: nur Planreview für Tobias. Aktuelle P0-Störung, Frontend-Path-Fehler und Closed PR #5 getrennt bewerten; keine Infrastrukturänderung planen, solange VM-Diagnose fehlt.

Mandantenfähigkeit, Subdomains, öffentlicher Partnershop, direkte BMD-Schnittstelle, echte Produktgruppen/Fachbereiche/Paketlogik und KI-Feature nicht als aktive MVP-Changes planen.

Zum Schluss: jeden Change mit openspec validate prüfen; unabhängigen Security- und Funktions-/Testreview durchführen. Prüfe Scope, Rollen-/Partnerisolation, Datenmodell, Migration, Uploads, Fehlerpfade, Tests, Verifikation und Terra-Readiness. Liefere pro Change READY oder die genaue fehlende Entscheidung. Kein Implementieren.
```

## Aktuelle Planungskandidaten

| Rang | Kandidat | Status für Planung |
|---|---|---|
| P0 | Live-System, Frontend-Sync, Forced-Command-Deploy | Tobias; VM-Diagnose und Freigabe fehlen |
| P0 | MVP 8.3, 8.4, FD.4 | 8.3/FD.4 Tobias; 8.4 gemeinsam |
| P1 | unabhängige Endkundenanlage | unreviewter Entwurf `add-standalone-customer-creation`; Astra prüft ihn zuerst unabhängig |
| P1 | Partneridentität und Stammdaten | Blockiert: aktuelle Partnernummer ist Keycloak-Login; Zielnummer/Legacy-Referenz und Migrations-/Loginablauf müssen zusammen entschieden werden |
| P1 | Partnernachweis | Blockiert: Speicher, autorisierter Download, Lösch-/Aufbewahrung und Nachweisprozess fehlen |
| P1 | Artikel-Minimalmodell | Blockiert: vereinfachte Firmenmail und BMD-Beispieldaten fehlen |
| P2 | Kategorienverwaltung | Blockiert durch Artikel-Minimalmodell; bestehendes Freitextfeld darf nicht ohne bestätigte Migrationsregel ersetzt werden |
| P2 | Unternehmenspersonalisierung | Blockiert: kein `Company`-Datenmodell in Ein-Unternehmen-Architektur; Entscheidung zwischen statischer Deploy-Konfiguration und späterer Tenant-Entität fehlt |
| P2 | reale E-Mails | Blockiert: SMTP-/No-reply-Zugang, Absender, Empfänger- und Retry-Betriebsregeln fehlen |
| P2 | In-App-Benachrichtigungen | Teilweise vorhanden für neue Company-Bestellungen; Erweiterung blockiert durch Company-Mitarbeiter-/Empfängermodell und E-Mail-Entscheidung |

## Bekannte Dokumentationslücken

- `build-partnerportal-mvp` ist 50/53 offen: 8.3, 8.4, FD.4.
- `ci-cd-deployment-with-gh-actions` ist 30/41 offen; einzelne Tasks 3.5/3.6 und Domain-/HTTPS-Angaben sind laut überprüftem GitHub-/Live-Stand veraltet. Nicht still abhaken.
- Roadmap nennt unabhängige Endkundenverwaltung erledigt; aktuelle Listen-Seite besitzt keinen Erstellen-Dialog. Code schlägt hier Dokumentationsbehauptung.
- Der 5-%-Standard ist im aktuellen Formular nicht umgesetzt: `openCreate()` setzt 10 %.
- `add-standalone-customer-creation` ist ein uncommitteter Vorentwurf. Erst Astra-Review entscheidet READY oder BLOCKIERT.
# Nächster Orchester-Start — 14.09.2026

1. Handoff und Gateway-Reviewer-Antwort lesen; `git status --short --branch`, `git fetch origin`, HEAD gegen `origin/main` vergleichen.
2. Nicht automatisch pullen/mergen. Konflikte und Remote-Änderungen (Demo-Seeding, Archive, Präsentation, CI/CD) bewerten; bei Unsicherheit selektiv portieren.
3. CI/CD-Tasks mit P0-Live-Nachweisen synchronisieren. Gateway 5.8 lokal, 5.9 erst nach Tobias-VM-Belegen.
4. Finaler Security-/Quality-/Browser-/OpenSpec-Check.
5. `complete-thesis-documentation` und Präsentation: „Artikel“ primär; Lehreranforderungen einarbeiten.
6. Clockify nur evidenzbasiert ergänzen; Dauer/Uhrzeit aus Logs oder Nutzerangabe.
