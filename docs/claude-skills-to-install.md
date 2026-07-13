# Claude Skills To Install Later

Ziel: nur die fehlenden und fuer die Diplomarbeit sinnvollen Skills nachinstallieren. Keine grossen Skill-Bundles blind installieren.

## Bereits abgedeckt

- `frontend-design` ist in Claude bereits installiert.
- Browser/E2E ist in Claude bereits durch das offizielle `playwright` Plugin abgedeckt.
- OpenSpec-Skills sind bereits installiert: `openspec-propose`, `openspec-apply-change`, `openspec-explore`, `openspec-archive-change`.
- `context7` und `typescript-lsp` sind als Claude-Plugins installiert.
- Code- und Security-Review sind als Harness-Built-ins vorhanden: `code-review`, `simplify`, `security-review`. Nichts nachzuinstallieren.

## Installiert am 2026-07-09 (global in ~/.claude/skills)

- `huashu-design` — fuer die MVP-Praesentationsfolien (siehe Abschnitt Praesentation).
- `excalidraw-diagram` — Diplomarbeits-Diagramme; Renderer (uv + Chromium) ist eingerichtet.
- `last30days` — On-demand-Recherche der letzten 30 Tage.

## Praesentation (MVP) — TODO

- Es ist spaeter eine Praesentation ueber den MVP zu halten.
- Werkzeug: `huashu-design` (installiert). Gibt HTML-Decks aus + Export nach editierbarem PPTX (`html2pptx.js`).
- Reveal.js: huashu erzeugt **kein** reveal.js nativ, sondern eine eigene HTML-Deck-Engine (`deck_stage.js`).
- ENTSCHEIDUNG 2026-07-09: reveal.js ist das Ziel. Weg = `frontend-design`-Skill fuer Design-Richtung +
  handgeschriebenes reveal.js-Deck (huashu erzeugt kein reveal.js). huashu bleibt Fallback/visuelle Referenz.
  Folienbau selbst spaeter (TODO), nicht jetzt.
- Hinweis: huashu SKILL-Beschreibung ist auf Chinesisch, funktioniert aber agent-agnostisch.

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

4. `partnerportal-quality` Custom Skill — ERLEDIGT 2026-07-13
   - Angelegt unter `.claude/skills/partnerportal-quality/SKILL.md` (im Repo, damit Tobias/Claude denselben Kontext nutzen).
   - Aufruf: `/partnerportal-quality` nach einer Implementierung, vor Commit/Merge; prueft nur den Diff.
   - Design: zeigerbasiert — liest `docs/CONVENTIONS.md` + `CLAUDE.md` zur Review-Zeit frisch, inline nur die
     Hard-Rules-Checkliste (Rollen/Scoping, BigDecimal-Geld, deutsche Texte + Umlaute, escapeHtml,
     REST-Prefixe, Backlog-Scope-Disziplin, Testkonventionen). Ergaenzt die Built-in-Reviews, ersetzt sie nicht.

## Optional spaeter

- Google Workspace (`gws`) ist interessant, aber nur wenn wirklich Docs/Sheets/Drive/Gmail/Calendar automatisiert werden sollen.
- Empfehlung fuer Start: wenn ueberhaupt, nur gezielt `gws-shared`, `gws-drive`, `gws-docs`, `gws-sheets`, eventuell `gws-calendar`.
- Gmail erst installieren/aktivieren, wenn ein konkreter Workflow existiert.
- Auth/Login fuer Google Workspace separat und bewusst machen; keine Tokens oder Credentials in Projektdateien speichern.

## GitHub-Skills geprueft (2026-07-09)

Fuenf angefragte Drittanbieter-Skills von GitHub, bewertet gegen das bestehende Setup
(kuratiertes Datei-Gedaechtnis vorhanden, Caveman-Mode fuer Tokens, `frontend-design` bereits da,
Tobias macht Visual Design, OpenSpec-Workflow, Solo-Studentenprojekt, eigene Simplicity-first-Regel).

1. `last30days-skill` (mvanhorn/last30days-skill) — INSTALLIERT 2026-07-09
   - Zweck: Recherche zu einem Thema ueber Reddit/X/YT/HN/TikTok/HN/GitHub der letzten 30 Tage, synthetisierte Zusammenfassung.
   - Bewertung: einzelner On-demand-Skill, keine Infrastruktur, geringes Risiko. Kostenlos, self-contained (nutzt Web-Tools).
   - Valyu-Vergleich (siehe unten): last30days = kostenlose Social-Signal-Recherche (Trends, was Leute sagen),
     keine akademischen Zitate. Valyu = kuratierte/autoritative Quellen mit Zitaten, aber API-Key noetig.
     Fuer schnelles Trend-Scouting last30days; fuer zitierfaehige Thesis-Quellen eher Valyu.
   - Status: installiert.

2. `huashu-design` (alchaincyf/huashu-design) — INSTALLIERT 2026-07-09
   - Zweck: HTML-native Design-Skill: Prototypen/Slides/Animationen/MP4, 20 Design-Philosophien, 5-Dim-Review.
   - Bewertung: fuer den Build ueberschneidet es sich mit `frontend-design` (behalten). Konkreter Nutzen fuer die
     **MVP-Praesentation**: HTML-Deck + PPTX-Export. Kein reveal.js nativ (eigene Deck-Engine).
   - Status: installiert, global, fuer die Praesentation reserviert. Nicht fuer den Produktivcode verwenden.

3. `context-mode` (mksglu/context-mode)
   - Zweck: MCP + Hooks, sandboxt Tool-Output (98% Reduktion), Session-Memory, Routing ueber 17 Plattformen.
   - Bewertung: schwere MCP/Hooks-Infrastruktur, invasiv, ueberschneidet sich mit Caveman-Intention.
     Hooks koennen mit dem Caveman-Hook kollidieren. Overkill fuer Coding-Arbeit.
   - Empfehlung: **nicht installieren**.

4. `everything-claude-code` (affaan-m/everything-claude-code)
   - Zweck: Kitchen-Sink-Framework, 66 Agents / 268 Skills / Memory / Security / research-first, Paid-Pro-Tier.
   - Bewertung (ehrlich, "Bloat oder Optimierung?"): Es ist modular, man **koennte** einzelne Agents/Skills
     gezielt uebernehmen — es ist nicht alles-oder-nichts. Der Kern (research-first, Memory, Security-Instincts)
     kann echten Nutzen bringen. ABER: fuer ein Solo-Studentenprojekt auf festem, sauberem Stack ist der Grossteil
     redundant zu dem, was schon da ist (OpenSpec, Built-in-Review, kuratiertes Memory). Blindes Bundle-Install
     bringt 268 Skills in den Namespace -> Kontext-/Auswahl-Rauschen, verletzt Simplicity-first.
   - Empfehlung: **nicht als Bundle installieren**. Wenn ein konkreter Bedarf auftaucht, gezielt EINEN Skill/Agent
     aus dem Repo kopieren statt das Ganze. Aktuell kein solcher Bedarf.

5. `claude-mem` (thedotmack/claude-mem)
   - Zweck: Auto-Capture aller Session-Aktivitaet, AI-Kompression, Re-Injektion in spaetere Sessions.
   - Bewertung: **kollidiert** mit dem bereits vorhandenen kuratierten Datei-Gedaechtnis (`~/.claude/.../memory` + MEMORY.md).
     Zwei Memory-Systeme = Chaos; das bestehende ist bewusst kuratiert und lesbar, claude-mem ist automatisch und opak.
   - Empfehlung: **nicht installieren** (aktiv schaedlich fuer das bestehende Setup).

## Gesamt-Ranking (was wirklich installieren)

Von hoch nach niedrig, ueber alle Kandidaten:

1. `excalidraw-diagram` — INSTALLIERT. Diplomarbeits-Diagramme, kein Ersatz vorhanden. Renderer eingerichtet.
2. `partnerportal-quality` (custom) — ANGELEGT 2026-07-13 in `.claude/skills/partnerportal-quality/`. Projektspezifisch, hoher Nutzen.
3. Code-Review / Security-Review — **bereits im Harness** als `code-review`, `simplify`, `security-review`. Nichts nachzuinstallieren; ggf. nur eigenen Projekt-Wrapper.
4. `huashu-design` — INSTALLIERT. Fuer die MVP-Praesentation, nicht fuer den Build.
5. `last30days` — INSTALLIERT. Optional/On-demand, geringes Risiko, nur bei Recherchebedarf.
6. Google Workspace (`gws`) — nur bei konkretem Docs/Sheets-Workflow (offen).
7. Valyu — nur wenn zitierfaehige, autoritative Recherche gebraucht wird (API-Key noetig); sonst reicht last30days.
8. Rest **nicht**: `claude-mem` (Memory-Konflikt), `context-mode` (Infra-Overkill), `everything-claude-code` (Bundle-Bloat), PlanetScale, Remotion, Shannon.

Bester Review-Skill (Frage geklaert): Du hast schon alle drei als Built-in. Aufteilung:
`code-review` = Bugs + Qualitaet (Effort-Level low..max, `ultra` = Cloud-Multi-Agent), `simplify` = nur Qualitaet/Vereinfachung,
`security-review` = Rollenrechte/Scoping/OIDC/IDOR. Fuer den Partnerportal-Kontext: `code-review high` fuer Korrektheit,
`security-review` gezielt fuer Partner/Kunde-Scoping + Keycloak-Rollen. Kein Drittanbieter-Reviewer noetig.

## Nicht priorisieren

- PlanetScale: passt nicht zum Stack, weil das Projekt PostgreSQL + Hibernate Panache nutzt.
- Remotion: nur relevant, wenn ein Produktvideo gebraucht wird.
- Valyu: Enterprise-Such-/Recherche-API fuer Agents; ein Aufruf durchsucht Web + 36+ kuratierte Quellen
  (SEC-Filings, PubMed, akademische Verlage, Patente, Wirtschaftsdaten) und liefert AI-Antworten mit Zitaten +
  DeepResearch. Braucht API-Key. Relevant fuer zitierfaehige Thesis-Recherche, nicht fuer die Implementierung.
  Unterschied zu last30days: Valyu = autoritativ/akademisch mit Zitaten (Key noetig); last30days = kostenlos, Social-Signal/Trends.
- Shannon: erst spaeter gegen eine stabile lokale/staging Umgebung und nur mit ausdruecklicher Autorisierung.
- `claude-mem`: Konflikt mit bestehendem kuratiertem Datei-Gedaechtnis.
- `context-mode`: schwere MCP/Hooks-Infrastruktur, kollidiert mit Caveman-Hook.
- `everything-claude-code`: 268-Skills-Bundle, verletzt Simplicity-first.
