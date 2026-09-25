# Prompt-Register

## Register

| ID | Datum | Person | Werkzeug | Thema/Zweck | Rohbeleg | Status | Ergebnis/Prüfung |
|---|---|---|---|---|---|---|---|
| KI-2026-08-24-01 | 24.-25.08.2026 | Chris | Codex | Projektstand seit letzter Besprechung, Reveal-Präsentation, Forschungsfragen, Deployment und neue Meetingentscheidungen analysieren | lokale Codex-Sitzung `01a0345a-4164-7941-bbb5-282970d96fe7` | `EXAKT` | Abgleich mit Git, FSD, Backlog, OpenSpec, Tests und Besprechungsprotokollen; Ergebnisse in Protokoll und Roadmap festgehalten |
| KI-2026-08-25-02 | 25.08.2026 | Chris | Codex | Mehr-Unternehmen-Zielbild, Keycloak Organizations, Partnernummern, gemeinsamer Partnerzugang gegenüber persönlichen Mitarbeiterzugängen und Subdomains bewerten | lokale Codex-Sitzung `01a0345a-4164-7941-bbb5-282970d96fe7` | `EXAKT` | Quellprüfung gegen aktuellen Code, Keycloak-/Quarkus-Dokumentation und Datenmodell; Zugangsmodell bleibt bis zur Empfehlung des Betreuers offen; noch keine Implementierung |
| KI-2026-08-25-03 | 25.08.2026 | Chris | Codex | BMBWF-Handreichung und Vergleichsdokument zur KI-Nutzung analysieren und ein zweistufiges Nachweiskonzept planen | lokale Codex-Sitzung `01a0345a-4164-7941-bbb5-282970d96fe7` | `EXAKT` | Beide PDFs textuell und visuell geprüft; Nachweisstruktur und ehrlicher Hinweistext erstellt |




| AUSSTEHEND-01a08640 | 2026-09-09 | Chris | Codex | Astra-Handoff korrigieren | lokale Sitzung `01a08640-2b93-7ac1-9cc4-e82d5b782de9` | `EXAKT` | Vorzeitigen OpenSpec-Entwurf als unreviewt markiert; Astra muss ihn zuerst unabhängig prüfen und erst danach weitere Changes planen.; Prüfungen: git diff --check |





| AUSSTEHEND-01a0897e | 2026-09-10 | Chris | Codex | PAPO-OpenSpec-Planung abschließen und Kontext-Handoff aktualisieren | lokale Sitzung `01a0897e-af71-7b53-9ffd-949fa51204bf` | `EXAKT` | Drei kleine Changes vollständig geplant; Endkundenentwurf korrigiert; MVP-/CI-CD-Status und Kontextdateien synchronisiert.; Prüfungen: Fünf aktive Changes mit openspec validate --strict geprüft; git diff --check erfolgreich. |

| AUSSTEHEND-01a090bb | 2026-09-11 | Chris | Codex | OpenSpec-Changes archivieren und Handoffs aktualisieren | lokale Sitzung `01a090bb-36cf-7863-abd7-8124845fc62f` | `EXAKT` | Drei abgeschlossene Changes archiviert, Spezifikationen synchronisiert und Next-Session-Goal aktualisiert; Prüfungen: openspec validate --changes --strict: 8/8 gültig; git diff --check erfolgreich |




| AUSSTEHEND-01a09252 | 2026-09-11 | Chris | Codex | search-order-customers finalen Reviewstand dokumentiert | lokale Sitzung `01a09252-c148-7fc2-946c-7757df61684e` | `EXAKT` | Gate-Fix geprüft; Such-, Draft-, Partner- und Company-Flows erneut verifiziert; 3 Tasks fertig; Prüfungen: node --check frontend/src/pages/partner-order-new-page.js; cd backend && ./mvnw package; openspec validate search-order-customers --strict; git diff --check |



| AUSSTEHEND-01a0953c | 2026-09-12 | Chris | Codex | define-mvp-traceability-format implementieren und abschließend prüfen | lokale Sitzung `01a0953c-57b0-7ae1-8ed1-313137c1ffdc` | `EXAKT` | 3 von 3 Tasks fertig; 33-zeilige Requirements-Matrix und Dokumentationsverantwortlichkeiten erstellt; Prüfungen: Quellen-/ID-/Link-/Tabellenstrukturabgleich, Strict-Validate, git diff --check sowie Funktions-/Evidenz-/Securityreview |






| AUSSTEHEND-01a0959b | 2026-09-12 | Chris | Codex | Abschlussprüfung und Archivierung von define-mvp-traceability-format und manage-article-categories; Readinessbewertung der zwei verbleibenden OpenSpec-Changes | lokale Sitzung `01a0959b-2b41-7692-9f86-58cf2190a737` | `EXAKT` | Beide Changes geprüft, strict validiert und archiviert; verbleibende build/CI-CD-Changes nicht implementiert und auf offene Tasks/Blocker bewertet.; Prüfungen: openspec validate manage-article-categories --strict; Java-21 fokussierte Tests 27/27; ./mvnw test 100/100; ./mvnw package 100/100; node --check der geänderten Kategorie-Seiten; git diff --check; Playwright Desktop/Mobile-Kategorie-CRUD, Deaktivierung/Reaktivierung, deaktivierte Bestandszuordnung und Partnerisolation |

| AUSSTEHEND-01a09ba6 | 2026-09-13 | Chris | Codex | Orchester-Handoff aktualisiert: Archivierung, P0-Recovery und Remote-Diff-Status dokumentiert | lokale Sitzung `01a09ba6-7972-7e13-936f-38b9c7f96624` | `EXAKT` | Neue Session kann mit selektiver Git-/Gateway-Prüfung fortsetzen; Prüfungen: openspec archive erfolgreich; git fetch origin und Statusprüfung bereits ausgeführt |



| AUSSTEHEND-01a09db0 | 2026-09-14 | Chris | Codex | Kontext-Handoff für nächste Session aktualisieren | lokale Sitzung `01a09db0-547d-70a3-80c3-279b68f996e0` | `EXAKT` | CONTINUATION_PROMPT, NEXT_SESSION_PLAN, CLAUDE, PLANNING_REVIEW und RESTARBEITSPLAN auf Commit eacb8cd, sauberen Worktree und den belegten Gateway-Blocker synchronisiert.; Prüfungen: git diff --check; openspec validate --changes --strict --json --no-interactive: 2/2 gültig |


| AUSSTEHEND-01a09f17 | 2026-09-14 | Chris | Codex | Next-Session-Handoff mit Deployment-, VM-Security-, BMD-Stammdaten- und Clockify-Auftrag aktualisieren | lokale Sitzung `01a09f17-f9eb-79a2-b1a7-d5250bcd1508` | `EXAKT` | Nur Context-/Handoff-Dateien aktualisiert; verbindliche Reihenfolge, Nutzerentscheidungen, Subagent-Nachweise, Aufwandsschätzungen und retrospektiv geschätzte Clockify-Einträge für die nächste Session dokumentiert; keine Implementierung, kein Deployment; Prüfungen: git diff --check |

| AUSSTEHEND-01a09f85 | 2026-09-14 | Chris | Codex | BMD-, Steuerkennungs-, Notiz- und Gewerbenachweis-Changes mit OpenSpec durchplanen | lokale Sitzung `01a09f85-3301-7780-8e60-436a08e81900` | `EXAKT` | Fünf vollständige Proposal-, Design-, Spec- und Taskpakete sowie ein neuer Work Plan erstellt; keine Produktimplementierung; Prüfungen: Alle fünf Changes mit openspec validate --strict sowie git diff --check erfolgreich geprüft |

| AUSSTEHEND-01a09fc5 | 2026-09-14 | Chris | Codex | Besprechungsprotokoll 14.9 übernehmen, fertigen Company-Notiz-Change archivieren und Präsentations-Next-Session vorbereiten | lokale Sitzung `01a09fc5-2281-7373-a172-59cff23873c5` | `EXAKT` | Protokoll vollständig gelesen; add-company-internal-partner-note archiviert und Partner-Spec synchronisiert; Handoff-Dateien, Präsentationsplan und Next-Session-Prompt aktualisiert.; Prüfungen: openspec archive add-company-internal-partner-note -y --json; openspec validate --changes --strict --json --no-interactive; git diff --check. |



| AUSSTEHEND-01a0a15e | 2026-09-14 | Chris | Codex | OpenSpec Apply add-business-tax-identifiers implementieren und technisch abschließen | lokale Sitzung `01a0a15e-c030-7c70-b8b9-b51ea07d0595` | `EXAKT` | CompanyProfile-Singleton, gemeinsame Steuer-ID-Validierung, Partner-REST/Import/UI und BMF-/Datenmodelldoku umgesetzt; Partner-Self-Service bleibt ohne Steuerkennung.; Prüfungen: BusinessTaxIdentifierValidatorTest und CompanyProfileResourceTest; ./mvnw test; ./mvnw package; node --check frontend/src; openspec validate add-business-tax-identifiers --strict; git diff --check; statischer Browser-Shellcheck mit Loginroute/Desktop-Mobile-Resize. |



| AUSSTEHEND-01a0a41f | 2026-09-15 | Chris | Codex | PAPO-Context- und Planungsdokumente konsolidieren | lokale Sitzung `01a0a41f-8191-7e50-934e-5e18303f648a` | `EXAKT` | CONTINUATION_PROMPT als einzige operative Source of Truth gekürzt; vier redundante Planungsdateien entfernt; AGENTS und Präsentations-README synchronisiert; keine Produktdatei geändert; Prüfungen: git diff --check; git diff --name-status; git status --short --branch; openspec list --json |

| AUSSTEHEND-01a0a51d | 2026-09-15 | Chris | Codex | Next-Session-Kontext für add-partner-trade-proof synchronisieren | lokale Sitzung `01a0a51d-60d0-7461-9df7-0f2f9404192b` | `EXAKT` | CONTINUATION_PROMPT.md und die aktuellen Statusblöcke in CLAUDE.md auf Branch, OpenSpec-Status, abgeschlossenen Tax-Rollout und Trade-Proof-Startreihenfolge aktualisiert.; Prüfungen: openspec status/instructions/list geprüft; git diff --check erfolgreich. |


| AUSSTEHEND-01a0a89c | 2026-09-16 | Chris | Codex | Spec-Sync und Archivierung des abgeschlossenen Tax-Identifier-Rollouts | lokale Sitzung `01a0a89c-8f5f-7db0-acb3-0fd15ca41bd2` | `EXAKT` | harden-tax-identifier-rollout synchronisiert und archiviert; Next-Session-Startprompt aktualisiert; Prüfungen: openspec validate harden-tax-identifier-rollout --strict; git diff --check; openspec list --json und Archivpfad geprüft |













































































| AUSSTEHEND-01a0cf11 | 2026-09-23 | Chris | Codex | Trade-Proof-Review, Dublettenbereinigung, Thesis-Prüfung und Phase-2-Handoff | lokale Sitzung `01a0cf11-c556-7f51-8066-421ae5875b69` | `EXAKT` | 122 Dubletten bereinigt; Trade Proof inklusive Review-Fix auf Feature-Branch gepusht; Dokumentation korrigiert und Produktausbau mit offenen Entscheidungen geplant; Prüfungen: ./mvnw package: 129 Tests, 0 Fehler, Build erfolgreich; node --check frontend/src: bestanden; openspec validate complete-thesis-documentation --strict: bestanden; git diff --check: bestanden; 62 Markdown-Links: 0 fehlende Ziele; PR #27 Backend-CI: bestanden |

| AUSSTEHEND-01a06809 | 2026-09-03 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `01a06809-6bda-70c3-922a-59e5eeeeff28` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-01a0676e | 2026-09-03 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `01a0676e-bfa1-7241-89c0-43de935e78ee` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-01a06ab1 | 2026-09-04 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `01a06ab1-5040-7761-b84f-b202b6db6cc8` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-01a090fe | 2026-09-11 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `01a090fe-6739-7501-8459-f9bf920f3bda` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-01a09091 | 2026-09-11 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `01a09091-5137-7b23-a8d6-e4f4d9b828d3` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-01a08c1f | 2026-09-10 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `01a08c1f-1fa4-7e11-badb-e05b02722b26` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-01a07969 | 2026-09-07 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `01a07969-fb4c-7442-b106-45b8188208b5` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-01a0863c | 2026-09-09 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `01a0863c-505a-7f30-b0f4-ac430c1866cb` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-01a05dcf | 2026-09-01 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `01a05dcf-2be8-7021-89cc-07ddb9a4aaca` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-01a0a229 | 2026-09-14 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `01a0a229-f40a-74e3-9d3a-c0e4e56885c5` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-01a09bbc | 2026-09-13 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `01a09bbc-835b-7082-8168-80fdd72001dc` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-01a0991c | 2026-09-13 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `01a0991c-8bfe-7740-acde-03ac93efb975` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-01a09bc1 | 2026-09-13 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `01a09bc1-6421-7b03-81c3-88f51bff5430` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-01a09d84 | 2026-09-14 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `01a09d84-f0df-7e52-9508-b2c829f98aa2` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-019f72d2 | 2026-07-18 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `019f72d2-d09e-7382-ad24-52f329a30e94` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-019f75a8 | 2026-07-18 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `019f75a8-0fb0-7721-9c66-e0a221a69661` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-019f70fe | 2026-07-17 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `019f70fe-8ec1-7a51-85c0-9f029a0a1ec3` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-019f7018 | 2026-07-17 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `019f7018-dd20-7d42-9b5b-926b893b78e0` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-019f62ea | 2026-07-14 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `019f62ea-1909-7862-8b2f-cf2b4298040e` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-019f5c64 | 2026-07-13 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `019f5c64-3a08-77e1-9a96-81c80e769413` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-019f5c5c | 2026-07-13 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `019f5c5c-804f-7ac0-91ee-f052631f8eea` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-019f5e49 | 2026-07-14 | Chris | Codex | Fachliche Einordnung offen | lokale Sitzung `019f5e49-da41-7ba3-bab4-8ee924dc7cb7` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-5a3d5cc8 | 2026-07-14 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `5a3d5cc8-1c89-4ac6-8780-ac9b53b8075f` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-8e6421f3 | 2026-07-15 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `8e6421f3-d7cf-488c-b709-9f5a9da34ee8` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-0a6f9df2 | 2026-07-06 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `0a6f9df2-8b6d-4396-bd90-0dc35d18a5d3` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-1364cd74 | 2026-07-15 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `1364cd74-25fe-42ec-b8dd-e84413529b78` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-9d1c2776 | 2026-07-02 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `9d1c2776-52ac-4e53-bcb3-18bad4ba6239` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-3d97eec7 | 2026-07-04 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `3d97eec7-35d0-4158-a58c-456e518264dc` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-855d66c3 | 2026-07-03 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `855d66c3-f5f3-4079-9c1b-ad8e78a78526` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-132056cb | 2026-07-04 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `132056cb-e0d4-4486-9c28-e08c750f693a` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-6d41f253 | 2026-07-17 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `6d41f253-d1d0-490f-9edc-30390d61f268` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-ebf3ccaf | 2026-07-16 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `ebf3ccaf-bf4a-4de9-b93e-56094b1d935d` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-39e673e0 | 2026-07-09 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `39e673e0-49cb-43c4-a78f-c9dd3ee24511` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-0dd619ff | 2026-07-15 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `0dd619ff-8c3a-41bd-becb-bf074907830f` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-d52ef674 | 2026-07-16 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `d52ef674-fdaf-473a-a6d0-05c60d50b5a9` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-1ba5da19 | 2026-07-16 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `1ba5da19-10e4-49d1-b650-d91d502a8386` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-e51fe421 | 2026-07-14 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `e51fe421-69a4-4e48-9833-d769cd351adf` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-d72b0b9f | 2026-07-16 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `d72b0b9f-b615-4990-ac0c-f98967775075` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-a7034a3f | 2026-07-01 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `a7034a3f-159c-4de3-9193-e647509951fe` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-fd8a1038 | 2026-07-13 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `fd8a1038-ef00-496d-92b5-e1fa2e6f0ba5` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-92ec7bf1 | 2026-07-05 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `92ec7bf1-ba16-4b60-9d0f-6ce355dc6a80` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-fc414896 | 2026-07-14 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `fc414896-9c91-4b57-8bc2-ea2ec337a0f7` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-438af9ec | 2026-07-04 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `438af9ec-64d8-4f0f-a077-980165157ec0` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |
| AUSSTEHEND-1798d34e | 2026-07-01 | Chris | Claude Code | Fachliche Einordnung offen | lokale Sitzung `1798d34e-c42d-4563-9b77-f50fe6084679` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |

## Noch rückwirkend aufzunehmen

- 23 primäre Claude-Code-Sitzungen vom 01.07. bis 17.07.2026;
- 9 ältere PAPO-Codex-Sitzungen vor der aktuellen Sitzung;
- ChatGPT-Verläufe nach Erhalt des Kontodatenexports;
- Claude-Web-/App-Verläufe nach Erhalt des Kontodatenexports;
- wichtige Prompts vor dem 01.07.2026 anhand von Exporten, Git, OpenSpec und vorhandenen Dokumenten.

Die Aufnahme erfolgt nicht als unkommentierter Massendump. Inhaltlich zusammengehörende Folgeprompts dürfen als Dialogeintrag gruppiert werden, solange der genaue Wortlaut und die Reihenfolge im privaten Rohbeleg erhalten bleiben.
