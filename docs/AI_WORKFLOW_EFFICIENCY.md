# Effizienter Spec-driven Workflow für PAPO

## Grundsatz

PAPO ist Brownfield. Pro Arbeitsblock nur betroffene Quellen lesen. Bereits belegte Prüfungen nicht unter anderem Namen wiederholen.

```text
klein:      prüfen -> ändern -> fokussierter Check
normal:     planen -> apply -> fokussierte Checks -> validate/archive
hohes Risiko: planen -> unabhängiger Plan- und Readiness-Review
             -> apply/tests -> finaler Security-/Diff-Review -> validate/archive
```

Hohes Risiko: Authentifizierung/Autorisierung, Partner- oder Tenant-Isolation, Geld, Datenverlust, Migrationen, Uploads, Secrets, externe Integrationen und produktives Deployment.

## Normaler Change

1. Relevante Besprechungsnotiz, FSD, bestehende Spec sowie Code/Testpfad lesen.
2. Einen kleinen OpenSpec-Change anlegen oder vorhandenen Change aktualisieren.
3. Proposal, bei echten Entscheidungen Design, Delta-Spec und ausführbare `tasks.md` erstellen.
4. Jede Task enthält Ziel, Voraussetzungen, betroffene Systeme, Verhalten, Test, DoD und Stopbedingung.
5. `openspec validate <change-id>` ausführen.
6. Erst implementieren, wenn alle fachlichen und externen Voraussetzungen geschlossen sind.

## Hochrisiko-Change

1. Normalen Plan erstellen.
2. Genau einen unabhängigen Review zu Scope, Architektur, Security, Testbarkeit, Rollback und Stopbedingungen durchführen.
3. Findings sichtbar ergänzen; erst danach `READY` aussprechen.
4. Nach Umsetzung genau einen finalen Review durchführen: `partnerportal-quality`, generischer Korrektheitsreview und Security-Review.
5. Tests, OpenSpec-Validierung und Freigabegates dokumentieren.

## Terra-Readiness

Ein Change ist erst für `GPT-5.6 Terra medium` bereit, wenn Proposal, Design, Delta-Spec und Tasks keine offene fachliche, Sicherheits-, Daten-, Migrations-, Upload-, E-Mail-, Infrastruktur- oder Zugriffsentscheidung mehr enthalten. Jede Task muss konkrete Dateien/Komponenten, Akzeptanzkriterien und Prüfungen nennen. Externe Blocker bleiben als präzise Frage dokumentiert; sie werden nicht durch Annahmen ersetzt.

## Grenzen

- Eine Capability pro Change, keine Sammelchanges.
- Kein Deployment, VM-, Secret-, SSH-, Backup- oder GitHub-Secrets-Change ohne Tobias und explizite Freigabe.
- Mandantenfähigkeit, Subdomains, Partnershop, direkte BMD-Schnittstelle, Fachbereiche, Produktgruppen, Paketlogik und KI-Funktion bleiben außerhalb aktueller Changes, bis eine verbindliche neuere Quelle sie freigibt.
- Vor jeder Übergabe nur echte Checks dokumentieren; keine Erfolgsmeldung ohne Beleg.
