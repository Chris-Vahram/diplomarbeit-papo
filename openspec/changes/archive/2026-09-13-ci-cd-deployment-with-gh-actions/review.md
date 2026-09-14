# Planreview — 10.09.2026

## Fortschreibung 14.09.2026

Task 5.8 ist lokal umgesetzt: native SSH-Übertragung, gepinnte Actions, kanonischer Frontend-Archivvergleich, exakter `origin/main`-SHA, direkter Releasepfad `frontend-live/releases/<sha>/`, backend-only Compose mit `IMAGE_TAG=<sha>` und atomarer `current`-Wechsel. `sh -n` und der isolierte Harness bestanden mit 18 Prüfungen. Task 5.9 bleibt bis zu Tobias' sanitisierten VM-, Key-, Host-Key-, Eigentümer-, sudoers- und Remote-Negativnachweisen blockiert. Es gab keinen VM-/SSH-/Secret-Zugriff und keinen Pull/Merge/Push.

## Fortschreibung 13.09.2026

P0-Ursache belegt: PostgreSQL-Volume gesund und persistent, aber gespeichertes Rollenpasswort `partnerportal` stimmt nicht mit aktuellem VM-lokalem `.env.prod` überein. Keycloak und Backend scheitern beide mit `FATAL: password authentication failed for user "partnerportal"`; Caddy/Netzwerk funktionieren. `/` liefert 200, `/api/*` und `/auth/*` 502. Recovery: zuerst lokale Dumps, dann DB-Rollenpasswort ohne Ausgabe an `.env.prod` angleichen, Keycloak/Backend neu starten und vollständigen Login-/Bestellflow prüfen. Nie Volume löschen oder DB neu initialisieren.

Nachweise 3.5/3.6 liegen vor: GHCR privat; `main` verlangt PR, aktuellen Branch und `Test backend`, Bypass deaktiviert. Screenshot zeigt jedoch weiterhin Julian mit geerbtem Write-Zugriff, obwohl aktuelles Team Chris/Tobias ist; Zugriff entfernen oder ausdrücklich neu autorisieren.

DNS `paportal.at` auf `178.104.109.45` sowie gültiges Let's-Encrypt-HTTPS mit Root-200 sind öffentlich belegt. Frontend-404/Archivpfad gilt damit als gelöst. OIDC/API/Login/Bestellung bleiben bis P0-Recovery offen.

Gateway-Code aus `48ad4d8` ist wiederverwendbar, aber nicht blind cherry-pickbar: damaliger Workflow/Releasepfad ist älter als `1513f9a`. Planung portiert Gateway, Tests und sudoers-Grenze gezielt auf aktuellen `frontend-live/releases/<sha>/current`-Stand.

## Fortschreibung 11.09.2026

Lokales Git enthält `fea0e5b` (Merge PR #10) und `1513f9a` (Frontendziel abflachen/Mount-Swap korrigieren). Damit ist die frühere reine Frontendpfad-Vermutung durch vorhandenen Reparaturcode überholt. Es wurde kein neuer Remote-/Betriebscheck ausgeführt. Fehlend bleibt Tobias' datierter Nachweis der tatsächlich ausgelieferten Revision und Frontend-/Recoveryfunktion; keine zweite Reparatur blind planen. P0-Betrieb, Frontendnachweis und PR-5-Zugriffsgrenzen bleiben getrennt. 30/41 historische Tasks unverändert; kein Terra-Apply. Vollständiger aktueller Report: `../../PLANNING_REVIEW_2026-09-11.md`.

**BLOCKIERT.** Verantwortlich: Tobias. Keine Terra-Freigabe und kein neuer Infrastruktur-Implementierungsplan.

Geprüft wurden ausschließlich lokale OpenSpec-Artefakte, Handoff/Restarbeitsplan und die lokalen Frontend-Sync-Zeilen des Workflows. Kein Live-, VM-, SSH-, Secret-, Backup- oder GitHub-Secrets-Zugriff; PR-/GitHub-/Live-Angaben sind historische Quellenbehauptungen.

## Getrennte offene Nachweise

| Strang | Konkreter fehlender Nachweis / Entscheidung | Stopbedingung |
|---|---|---|
| P0-Ausfall | Tobias liefert sanitierte Diagnose zu laufender Revision, betroffenen Services/Netzwerk/DB und notwendigen Konfigurationsnamen sowie datierte HTTPS-/OIDC-/API-/Login-/Bestellnachweise. | Kein Reparaturplan ohne aktuelle Ursachenklärung. |
| Frontend-Sync | Tatsächliche Archivstruktur des verwendeten Action-Tags, erwartete index.html-Lage, Swap-Voraussetzungen und Wiederherstellungsverhalten bei fehlendem Index klären. Lokales source/target ist ein Verdacht, kein VM-Beweis. | Kein automatisches Übernehmen eines strip_components-Fixes ohne Nachweis. |
| Geschlossener PR #5 | Tobias bestätigt relevante Revision, konkrete Gateway-/Befehls-/sudoers-/Schlüsselgrenzen, negative Boundary-Tests und Wiederherstellung. Referenzen 5.11/5.12 müssen dem tatsächlichen Fremdplan zugeordnet werden. | Kein Merge, Cherry-pick oder Gatewayplan aus der alten PR-Beschreibung. |

## Unabhängige Findings und Dokumentationskorrekturen

- Securityreview: ein dauerhaft autorisierter Deploy-Key ist nicht durch Secret-Aufbewahrung zeitlich oder funktional begrenzt. Die falsche Sicherheitsgarantie wurde entfernt; tatsächliche Grenze bleibt offen.
- Funktions-/Testreview: Action-Erfolg beweist weder Erreichbarkeit noch ausschließliches Ersetzen von Quarkus. Historische Häkchen bleiben erhalten; unterbrechungsfreie Persistenz und Wiederherstellung sind nicht frisch belegt.
- JVM wurde am 23.08 bereits bestätigt, `paportal.at` am 25.08 genannt. Diese Entscheidungen sind synchronisiert; DNS-/TLS-/OIDC-Funktion bleibt separat offen.
- Offsite ist nach dokumentierter Entscheidung verschoben. Lokale Kopien schützen nicht gegen Verlust von VM und Datenträger. Nur falsche Textaussage korrigiert; keine neue Backupplanung. Verantwortlicher muss akzeptierte Demo-Daten-/Verlustgrenze bestätigen, bevor Betriebsbereitschaft behauptet wird.
- 3.5/3.6 sind laut Restplan historisch erfüllt; ohne aktuellen datierten Nachweis keine neuen Häkchen. Die Quelle wird nicht als neuer Remotecheck ausgegeben.
- Referenzkunde wird nicht mehr als PAPO bezeichnet; PAPO ist das Produkt.

Unabhängige Reviews: `customer_security_review` und `functional_plan_review` in dieser Sitzung. Prüfkommandos/abschließende Ergebnisse stehen in `../../PLANNING_REVIEW_2026-09-10.md`. Formale Validität hebt keine der obigen Sperren auf.
