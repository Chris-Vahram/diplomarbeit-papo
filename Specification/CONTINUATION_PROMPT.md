# Continuation Prompt — PAPO Partnerportal

**Dokumentstand:** 03.09.2026

**Repository:** `/Users/mitgl/Desktop/Schule/4CHITM/Diplomarbeit/diplomarbeit-papo`

**Zweck:** kurzer operativer Einstieg für eine frische Codex-/Claude-Session

```text
Du setzt die Arbeit am PAPO-Partnerportal fort.

Lies zuerst AGENTS.md und diese Datei. Lies danach Specification/NEXT_SESSION_PLAN.md, wenn der dort beschriebene Sonntags-/Deployment-Block noch offen ist. Lade nicht pauschal alle Projektunterlagen. Nutze das aufgabenbezogene Quellen-Routing in AGENTS.md und verifiziere Git, OpenSpec, Tests und Infrastruktur aktuell mit kurzen Befehlen.

## Aktuelles Ziel

Bis Sonntag, 06.09.2026, soll das vorhandene Partnerportal als sichere, synthetische Demo unter https://paportal.at laufen. Schließe zuerst den bereits geplanten Deployment-Change, den MVP-Smoke-Test, Backups, OpenSpec-Hygiene und die Traceability-Matrix ab. Beginne danach kein neues Produktfeature ohne eigenen geprüften OpenSpec-Vorschlag.

## Produktgrenzen

- PAPO ist eine erweiterbare B2B-Partnerbestellplattform; die aktuelle Demo hat ein Referenzunternehmen.
- Kernkette: Unternehmen -> Partnerfirma -> Endkunde.
- Aktueller Kern: gemeinsamer organisatorischer Login je Partnerfirma, freigeschaltete Artikel, Endkunden, Bestellung, Status, Provision und Dashboards.
- In UI und fachlicher Dokumentation heißt es „Artikel“; interne Namen wie `Product` bleiben vorerst.
- BMD ist führend für Artikelstammdaten; PAPO nutzt aktuell Excel/CSV-Import.
- Nicht jetzt implementieren: Mandantenfähigkeit/Firmensubdomains, öffentlicher Partnershop, persönliche Partner-Mitarbeiterkonten, echte SMTP-Mail, direkte BMD-Schnittstelle, Fachbereich/Produktgruppen/Pakete oder neue Provisionslogik.
- Die vereinfachte Firmenmail zu Artikeldaten fehlt weiterhin. Daraus darf kein spekulativer Change entstehen.

## Harte technische und Sicherheitsregeln

- Frontend: statisches HTML/CSS/JavaScript und native Web Components; kein Node, npm, Framework oder Bundler.
- Backend: Java 21, Quarkus REST, Panache, PostgreSQL und Keycloak/OIDC.
- Geld: Backend `BigDecimal`, Frontend Integer-Cents.
- Protected REST methods brauchen die richtige `@RolesAllowed`-Rolle.
- Partneridentität ausschließlich über `security/CurrentPartner.resolve()`, nie aus einer Client-Partner-ID.
- Kunden, Orders, Provisionen und Freischaltungen partner-spezifisch halten und Cross-Partner-Zugriff negativ testen.
- Inaktive Partner serverseitig vor Schreibzugriffen blockieren.
- Keine Secrets, Tokens, Vaultwarden-Inhalte oder echten Personen-/Firmendaten ausgeben oder committen.
- Tobias' `CLAUDE.md`, `.claude/` und Designarbeit nicht löschen, umbenennen oder ungefragt überschreiben.

## Git- und Workflow-Regeln

- Chris besitzt Implementierung und Deployment; Tobias besitzt visuelles Frontenddesign.
- Vor Arbeit: `git status --short --branch`, `git fetch origin --prune`, dann `git pull --ff-only` auf `main`.
- Ein Branch je Change. Fremde/untracked Dateien – derzeit insbesondere `tmp/` – nicht anfassen.
- Nicht committen, pushen oder mergen, solange Chris den konkreten Schritt nicht ausdrücklich beauftragt.
- Nicht triviale Änderungen: OpenSpec propose -> review -> apply -> validate -> archive.
- Nach Implementierung `partnerportal-quality` plus separaten Korrektheitsreview ausführen.
- KI-Nachweis pro sinnvollem Arbeitsblock einmal mit `sync` und `enrich` aktualisieren.

## Verifizierter Snapshot vom 03.09.2026

- Bei Erstellung war `main == origin/main == 22c14c3`; Handoff-Änderungen liegen auf `docs/next-session-plan`.
- OpenSpec: `build-partnerportal-mvp` 50/53; `ci-cd-deployment-with-gh-actions` 26/41.
- Letzter belegter Backend-Gesamtstand: 77/77 Tests und Package erfolgreich. Nach Codeänderungen neu prüfen.
- GitHub CI testet und baut erfolgreich; Deploy ist noch mit `if: false` deaktiviert.
- `paportal.at` zeigt auf `178.104.109.45`.
- VM-SSH und passwortloses `sudo` funktionieren. Externe Ports 80/443 wurden geprüft. UFW ist aktiv und erlaubt eingehend nur 22/80/443; SSH funktionierte danach weiter.
- Produktive Container, `.env.prod`, Backup-Cron, GHCR-Login und Deploy-Secrets sind noch offen.
- `frontend/src/lib/api.js` und `frontend/src/lib/auth.js` enthalten noch localhost-Basen und blockieren den echten Produktionsflow.
- S3- und SMTP-Zugangsdaten wurden privat geliefert. SMTP bleibt außerhalb des aktuellen Changes. S3 wird ohne Ausgabe der Secrets während der Backup-Konfiguration geprüft.

## Exakte nächste Aktion

Arbeite Specification/NEXT_SESSION_PLAN.md der Reihe nach ab. Nutze für die bereits entschiedene, mechanische Implementierung ein kostengünstiges Modell. Wechsle für neue Architektur-/Sicherheitsentscheidungen, einen ungelösten Fehler nach zwei fokussierten Versuchen oder den finalen Go-live-Review auf ein starkes Modell.

Stoppe vor dem nächsten ungeplanten Feature und berichte:

1. aktiver Branch und Git-Diff,
2. erledigte/offene OpenSpec-Tasks,
3. exakt ausgeführte Tests und Ergebnisse,
4. externe oder interaktive Restpunkte,
5. fertige Commit-Nachricht, aber kein Commit/Push ohne konkrete Freigabe.
```

## Quellen nur bei Bedarf

- Aktueller Arbeitsblock: `Specification/NEXT_SESSION_PLAN.md`
- Stabiles Produkt/Scope: `CLAUDE.md`
- Neueste Fachentscheidungen: `Documentation/Besprechungsprotokolle/Besprechungsprotokoll31_08_2026.md`
- Deployment: `docs/DEPLOYMENT.md` und `openspec/changes/ci-cd-deployment-with-gh-actions/`
- MVP-Abschluss: `openspec/changes/build-partnerportal-mvp/tasks.md`
- Anforderungen: `Specification/functional_specification_document_partnerportal_final.md`
- Zukunft/Out of scope: `Specification/ROADMAP_2026-08-25.md` und `Specification/phase2_backlog.md`
- Daten/Runtime-Konventionen: `docs/DATA_MODEL.md`, `docs/CONVENTIONS.md`, `docs/SETUP.md`
- Effiziente KI-Arbeit: `docs/AI_WORKFLOW_EFFICIENCY.md`
