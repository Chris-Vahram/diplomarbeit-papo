# Claude Skills To Install Later

Ziel: nur die fehlenden und fuer die Diplomarbeit sinnvollen Skills nachinstallieren. Keine grossen Skill-Bundles blind installieren.

## Bereits abgedeckt

- `frontend-design` ist in Claude bereits installiert.
- Browser/E2E ist in Claude bereits durch das offizielle `playwright` Plugin abgedeckt.
- OpenSpec-Skills sind bereits installiert: `openspec-propose`, `openspec-apply-change`, `openspec-explore`, `openspec-archive-change`.
- `context7` und `typescript-lsp` sind als Claude-Plugins installiert.

## Sinnvoll nachzuinstallieren oder selbst anzulegen

1. `code-reviewer` oder `simplify`-aehnlicher Skill
   - Hohe Prioritaet.
   - Zweck: Agent-Code nach Implementierung kritisch pruefen: Wartbarkeit, Duplikate, Fehlerfaelle, Tests, Security, Rollen-/Scoping-Fehler.
   - Empfehlung: lieber eigenen Skill erstellen als eine unklare Drittquelle installieren.

2. `excalidraw-diagram`
   - Hohe Prioritaet fuer Diplomarbeits-Diagramme.
   - Zweck: Architekturdiagramme, Auth-Flow, Bestellprozess, Import-Flow, Partner-Unternehmen-Kunde-Beziehung.
   - Quelle: `coleam00/excalidraw-diagram-skill`
   - Vor Installation kurz Repo/SKILL.md pruefen.

3. Security Review / Security Auditor
   - Mittlere bis hohe Prioritaet.
   - Zweck: Rollenrechte, Partnerdaten-Scoping, Keycloak/OIDC, Imports, CSV/XLSX-Validierung, IDOR-Risiken, unsichere Fehlermeldungen pruefen.
   - Empfehlung: leichter Review-Skill zuerst, kein aggressiver Pentest-Skill wie Shannon in frueher Projektphase.

4. `partnerportal-quality` Custom Skill
   - Sehr sinnvoll, projektspezifisch.
   - Inhalt aus `CLAUDE.md`, `AGENTS.md`, `docs/CONVENTIONS.md` und FSD ableiten.
   - Muss beachten: Quarkus, Panache, PostgreSQL, Keycloak-Rollen `company`/`partner`, Partner/Kunde-Scoping, deutsche UI-Texte, Tests, Validierung, keine MVP-Scope-Erweiterungen.
   - Empfehlung: ins Repo legen, z.B. `diplomarbeit-papo/.claude/skills/partnerportal-quality/`, damit Tobias/Claude denselben Kontext nutzen kann.

## Optional spaeter

- Google Workspace (`gws`) ist interessant, aber nur wenn wirklich Docs/Sheets/Drive/Gmail/Calendar automatisiert werden sollen.
- Empfehlung fuer Start: wenn ueberhaupt, nur gezielt `gws-shared`, `gws-drive`, `gws-docs`, `gws-sheets`, eventuell `gws-calendar`.
- Gmail erst installieren/aktivieren, wenn ein konkreter Workflow existiert.
- Auth/Login fuer Google Workspace separat und bewusst machen; keine Tokens oder Credentials in Projektdateien speichern.

## Nicht priorisieren

- PlanetScale: passt nicht zum Stack, weil das Projekt PostgreSQL + Hibernate Panache nutzt.
- Remotion: nur relevant, wenn ein Produktvideo gebraucht wird.
- Valyu: nur relevant fuer externe Recherche mit aktuellen Quellen, nicht fuer die Implementierung.
- Shannon: erst spaeter gegen eine stabile lokale/staging Umgebung und nur mit ausdruecklicher Autorisierung.
