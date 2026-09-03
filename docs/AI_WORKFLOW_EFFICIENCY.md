# Effizienter KI-Workflow für PAPO

**Analyse:** 03.09.2026

## Befund

Der hohe Verbrauch kommt überwiegend von langen, wiederholt fortgesetzten Sitzungen, nicht von der eigentlichen Antwortlänge. Repräsentative lokale Codex-Protokolle zeigen:

- 39,15 Mio. Input-Tokens, davon 37,59 Mio. gecacht, 171.757 Output-Tokens und drei Compactions;
- 38,70 Mio. Input-Tokens, davon 36,96 Mio. gecacht, 118.650 Output-Tokens und drei Compactions;
- 27,96 Mio. Input-Tokens, davon 25,70 Mio. gecacht, 213.043 Output-Tokens und drei Compactions.

Rund 92–96 % des Inputs waren gecacht. Das reduziert Rechenkosten gegenüber neuem Input, verhindert aber nicht, dass ein großer Kontext bei sehr vielen Agenten-/Tool-Runden immer wieder verarbeitet wird. Die zuletzt protokollierte Plus-Auslastung lag bei 49 % im Fünf-Stunden-Fenster und 68 % im Wochenfenster; zusätzliche Credits waren nicht vorhanden. Diese Anzeigen sind Momentaufnahmen, keine Abrechnung und keine verlässliche Zuordnung zu einem einzelnen Plugin.

Konkrete Ursachen im bisherigen PAPO-Workflow:

1. Der alte Continuation Prompt hatte 486 Zeilen und verlangte danach zehn weitere, teilweise inhaltlich überlappende Dokumente.
2. Mehrere große Arbeitsziele – Planung, neun Change-Reviews, Implementierung, Deployment und Dokumentation – liefen über Tage in denselben Sessions.
3. Jeder Tool-Aufruf führt zu einer weiteren Modellrunde. Breite Ausgaben wie vollständige Logs, Verzeichnisbäume oder Toolkataloge vervielfachen den Kontext.
4. Global sind zwölf Plugins plus mehrere MCP-Server aktiviert. Ihre verfügbaren Skills/Tools vergrößern die Instruktions- und Tooloberfläche, auch wenn sie für PAPO meist irrelevant sind.
5. Wiederholte Benutzeranweisungen wie „continue“ erhalten den gesamten Alt-Kontext, definieren aber kein neues begrenztes Ergebnis.

## Ponytail und andere Skills

Ponytail ist nicht der Hauptverursacher. Es fügt Hooks und Skill-Anweisungen hinzu, kann bei Code aber Ausgabe und Implementierungsumfang reduzieren. Weil es für jede Coding-Aufgabe automatisch triggert, ist sein Overhead dauerhaft. Für PAPO ist die sparsamste Einstellung: standardmäßig deaktivieren oder nur ausdrücklich für Simplification/YAGNI-Reviews aktivieren. Ein exakter Tokenanteil pro Plugin ist aus den Sitzungslogs nicht messbar; belastbar wäre nur ein A/B-Test mit derselben kleinen Aufgabe in zwei frischen Sessions.

Für normale PAPO-Arbeit werden typischerweise nur benötigt:

- OpenSpec-Propose/Apply/Archive für nicht triviale Changes;
- `partnerportal-quality` beim Abschluss;
- Browser/Playwright nur bei UI- oder End-to-End-Prüfungen;
- GitHub nur bei CI/PR/Repository-Arbeit.

Für PAPO standardmäßig entbehrlich sind Blender, Node-REPL, Context7, Sites, Visualize, Documents, Spreadsheets, Presentations, PDF, Template Creator und Unified Computer Use. Diese globalen Plugins wurden nicht automatisch deaktiviert, weil das andere Projekte beeinflusst. Sinnvoll ist ein schlankes PAPO-Profil oder das manuelle Deaktivieren dieser Plugins, bevor eine neue PAPO-Session startet.

## Neue Dokumentgrenzen

| Datei | Einziger Zweck |
|---|---|
| `AGENTS.md` | dauerhafte Arbeits-, Sicherheits-, Git- und Verifikationsregeln |
| `CLAUDE.md` | Tobias' stabiler Produktkontext; nicht für Codex-Handoffs umbauen |
| `Specification/CONTINUATION_PROMPT.md` | kurzer volatiler Snapshot und Startauftrag |
| `Specification/NEXT_SESSION_PLAN.md` | aktuelles Arbeitspaket, Reihenfolge, DoD und Blocker |
| Besprechungsprotokolle | Primärbelege neuer fachlicher Entscheidungen |
| OpenSpec-Change | vollständige Planung und Taskfortschritt genau eines Features |
| FSD / kanonische Specs | Anforderungen bzw. implementiertes Sollverhalten |
| Roadmap / Phase-2-Backlog | Zukunft und bewusst aufgeschobener Scope |

Der Continuation Prompt kopiert diese Inhalte künftig nicht mehr. Er verweist auf die jeweils relevante Quelle.

## Arbeitsweise ab der nächsten Session

1. Pro Session genau ein Ergebnis: z. B. „Deployment fertigstellen“ oder „Change X planen“, nicht beides plus neue Features.
2. Frische Session nach jedem abgeschlossenen Change bzw. nach einer großen Planungsentscheidung starten.
3. Zu Beginn nur `AGENTS.md` (automatisch), Continuation Prompt und den aktuellen Plan lesen. Weitere Quellen nur nach dessen Routing.
4. Einen Auftrag in einem Prompt vollständig formulieren: Ziel, Scope, bekannte Entscheidungen, verbotene Änderungen, DoD und gewünschte Verifikation.
5. Bei einem vorhandenen OpenSpec-Plan das günstige Modell implementieren und testen lassen. Nach zwei erfolglosen Fix-Loops oder sobald eine neue Architektur-/Sicherheitsentscheidung entsteht, stoppen und auf ein starkes Modell wechseln.
6. Starkes Modell nur für Anforderungsabgrenzung, OpenSpec-Design, Security/Deployment, schwierige Ursachenanalyse und Abschlussreview einsetzen.
7. Tool-Ausgaben begrenzen: konkrete Dateien/Zeilen, fokussierte Tests und kurze Logs. Keine vollständigen Toollisten oder pauschalen Repository-Dumps.
8. KI-Nachweis einmal pro sinnvollem Arbeitsblock anreichern, nicht nach jeder kleinen Änderung.

## Promptvorlagen

### Planung mit starkem Modell

```text
Lies CONTINUATION_PROMPT.md und NEXT_SESSION_PLAN.md. Plane ausschließlich <Feature> als eigenen OpenSpec-Change. Prüfe relevante aktuelle Quellen und bestehenden Code. Liefere Scope, Nicht-Ziele, Sicherheits-/Datenfolgen, Tasks und DoD. Noch nicht implementieren. Stoppe bei einer Entscheidung, die nur ich treffen kann.
```

### Umsetzung mit kostengünstigem Modell

```text
Wende den bereits geprüften OpenSpec-Change <change-id> taskweise an. Lies nur AGENTS.md, den kurzen Handoff, diesen Change und direkt betroffene Dateien. Keine neuen Produktentscheidungen, kein Refactoring außerhalb des Scopes. Führe fokussierte Tests, danach die vorgeschriebenen Gesamtchecks aus. Stoppe, sobald der Plan unklar oder falsch ist, statt Scope zu erfinden.
```

### Kurzer Folgeauftrag

```text
Setze im selben Change nur bei Task <n> fort. Ergebnis dieser Runde: <konkretes Artefakt/Prüfung>. Gib am Ende erledigte Tasks, Testbefehle und den nächsten offenen Task an.
```

OpenAI empfiehlt ebenfalls schlanke Prompts und Toolsets, ein ausgewogenes Reasoning-Level als Ausgangspunkt und höhere Denkstufen nur bei gemessenem Nutzen: <https://developers.openai.com/api/docs/guides/latest-model>.
