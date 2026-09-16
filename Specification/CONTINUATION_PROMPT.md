# PAPO — Next-Session-Context

**Stand:** 16.09.2026
**Zweck:** einzige operative Source of Truth für den nächsten Arbeitsblock. Stabile
Projekt-, Stack-, Sicherheits- und Workflowregeln stehen in `AGENTS.md` und `CLAUDE.md`.
Fachdetails stehen im neuesten Besprechungsprotokoll, in FSD/Backlog und den jeweils
relevanten OpenSpec-Artefakten.

## Session-Start

1. `AGENTS.md` und diese Datei vollständig lesen.
2. `CLAUDE.md` sowie das neueste einschlägige Besprechungsprotokoll lesen.
3. Neu prüfen, nie diesen Snapshot ungeprüft übernehmen:

   ```bash
   git status --short --branch
   openspec list --json
   ```

4. Nur Artefakte des nächsten zulässigen Changes und dessen Primärquellen lesen.
5. Fremde oder uncommittete Änderungen bewahren. Kein Commit, Push, Merge,
   Deployment, VM-, SSH-, Secret- oder GitHub-Secrets-Zugriff ohne aktuellen Auftrag.

Für `add-partner-trade-proof` danach exakt den OpenSpec-Kontext laden:

```bash
openspec status --change add-partner-trade-proof --json
openspec instructions apply --change add-partner-trade-proof --json
```

Anschließend nur die von `openspec instructions apply` genannten Proposal-, Design-,
Spec- und Task-Dateien dieses Changes lesen. Keine alten Planungsdateien wiederherstellen.

## Volatiler Snapshot

Beim Context-Review am 16.09.2026:

- Branch: `codex/archive-business-tax-identifiers`, Tracking
  `origin/codex/archive-business-tax-identifiers`.
- Arbeitsbaum: zahlreiche untracked, offenbar duplizierte Dateien mit Namenssuffix
  ` 2`; nicht löschen, verschieben oder ungeprüft stagen.
- Aktive OpenSpec-Changes:
  - `harden-deployment-gateway`: **3/4**
  - `complete-thesis-documentation`: **3/6**
  - `harden-tax-identifier-rollout`: **3/3**
  - `add-partner-trade-proof`: **0/6**
  - `decouple-partner-number-from-login`: **0/6**
- `add-unique-bmd-article-number`: **0/6**

Verifizierter OpenSpec-Stand am 16.09.2026:

- `harden-tax-identifier-rollout`: **3/3**, Status `complete`.
- `add-partner-trade-proof`: **0/6**, Status `in-progress`, Planung vollständig,
  `state: ready`.

## Nächste zulässige Aktion

1. `add-partner-trade-proof` implementieren. Der Tax-Identifier-Vorblocker ist nach
   verifizierter Migration, fokussierten PostgreSQL-Tests, vollständigem Backend-Test/
   Package und unabhängigen Reviews geschlossen.
2. Vor Apply dennoch den aktuellen Status, die Apply-Instructions, Tasks und Stopbedingungen
   erneut prüfen; die sechs Tasks strikt in Reihenfolge bearbeiten.
3. Unabhängig davon kann ein autorisierter Betreiber den fehlenden externen
   SSH-Nachweis für `harden-deployment-gateway` erbringen.
4. Tobias muss `complete-thesis-documentation` visuell abnehmen.
5. Nummernchanges bleiben bis zu anonymisierten BMD-Regeln und Beispieldaten blockiert.

Keine Produkt-/OpenSpec-Tasks allein aufgrund dieser Übergabe anwenden. Vor jedem Apply
aktuellen Auftrag, Status, Instructions, Tasks und Stopbedingungen prüfen.

## Offene Blocker

### Deployment-Gateway — 3/4

- Externe SSH-Negativmatrix fehlt wegen fehlendem autorisiertem Deploy-Key.
- GitHub Secret `VM_SSH_KEY` ist write-only und nicht aus GitHub auslesbar.
- Niemals private Schlüssel in Chat, Git, Logs oder Dokumente kopieren.
- Benötigt: dediziertes Deploy-Keypair; Public Key beim autorisierten Deploy-Account
  auf der VM hinterlegen, privaten Schlüssel lokal beziehungsweise als GitHub Secret
  setzen.
- Danach echte Negativtests für beliebige Befehle, PTY, Forwarding und SCP/SFTP sowie
  den vorgesehenen Deploypfad ausführen. Ohne echten Key keinen Erfolg behaupten.

### Thesis-Dokumentation — 3/6

- Technikdokumentation, Traceability und UI/UX-Dokumentation sind erstellt.
- Tobias' menschliche UI-/Screenshot-Abnahme fehlt. Nicht vorweg abhaken.

### Partnernummer vom Login entkoppeln — blockiert

Firma/BMD muss anonymisiert liefern: Feldname, Datentyp, maximale Länge,
Pflichtfeldstatus, führende Nullen, Groß-/Kleinschreibung, Whitespace, erlaubte Zeichen,
Änderungsregeln und bestehende Zuordnungen. Lehrkraft kann höchstens fachlichen Scope
bestätigen. Ohne diese Daten keine Migration oder Loginentkopplung.

### Eindeutige BMD-Artikelnummer — blockiert

Firma/BMD muss anonymisiert liefern: Artikelnummernfeld, Datentyp, maximale Länge,
führende Nullen, Groß-/Kleinschreibung, Whitespace, Pflichtfeldstatus,
Duplikatregeln, Update-/Deaktivierungsregeln und repräsentativen CSV/XLSX-Auszug.
Ohne diese Daten kein Mapping, Dedupe oder Rollout.

### Trade Proof — kein fachlicher Vorblocker, noch nicht implementiert

- Die Implementierung darf starten und bleibt auf `add-partner-trade-proof` begrenzt.
- Reihenfolge: Persistenz/Identifier und Migration → Uploadlimits/Signaturprüfung →
  Rollen-/Partnerendpunkte → UIs → Betriebs-/Aufbewahrungsdoku → Abschlussreviews.
- Pflichtgrenzen: genau ein aktuelles Dokument, PDF/JPEG/PNG, maximal 10 MiB,
  private Downloads, Partnerisolation, inaktive Partner serverseitig blockieren,
  versionierte Company-Prüfung, Ersetzen ohne Historie, keine automatische Aktivierung.
- Nicht ergänzen: OCR, Malware-Garantie, Historie, Mehrfachdateien, öffentliche URLs,
  direkte BMD-Schnittstelle, automatische Registerprüfung oder eigene Zusatzfeatures.

## Erledigt — nicht wieder öffnen

- `add-business-tax-identifiers`: 7/7, nach `main` gemergt. Technische Implementierung
  erledigt. Der Folgechange `harden-tax-identifier-rollout` ist 3/3: Partner-Spalten
  werden nullable-first migriert, synthetische Werte nur bei explizitem Demo-Modus und
  exakt verifiziertem Demo-Datensatz ergänzt, vorhandene IDs validiert, Firmenprofil-
  Constraints erhalten und parallele Starts serialisiert. PostgreSQL-Fokustests 9/9,
  Backend-Test und Package jeweils 115/115, strikte OpenSpec-Validierung und Reviews
  sind grün. Damit ist der Vorblocker für `add-partner-trade-proof` geschlossen.
- `add-company-internal-partner-note`: 4/4, archiviert.
- Präsentation: 22 Folien, Offline-Assets, zwei zusätzliche Screenshots,
  Souveränitätsfolien, Clockify-Übersicht und Demo-Drehbuch fertig. Clockify-Istwert
  Chris + Tobias, 01.03.–30.09.2026: **151:41:38**; `ca. 400 h` bleibt separater
  historischer Zielwert.
- Frühere Produkt-, Kategorie-, Kunden-, Terminologie-, MVP- und CI/CD-Changes stehen
  in `openspec/changes/archive/`; Status nicht aus alten Planungsreports rekonstruieren.

## Nächste Session — Definition of Done für `add-partner-trade-proof`

- Alle sechs Tasks in `openspec/changes/add-partner-trade-proof/tasks.md` erfüllt und
  erst nach Akzeptanzkriterien, Tests und Review abgehakt.
- Backend: fokussierte Upload-/Rollen-/Datenbanktests, danach `./mvnw test` und
  `./mvnw package` erfolgreich.
- Frontend: geänderte JavaScript-Dateien mit `node --check`; Partner-/Company-Flows
  auf Desktop und Mobile geprüft, inklusive Fehler-, Retry-, Tastatur- und Escape-Fällen.
- OpenSpec: `openspec validate add-partner-trade-proof --strict` erfolgreich.
- `git diff --check`, `partnerportal-quality`, Securityreview und unabhängiger
  Datenbank-/Korrektheitsreview erfolgreich.
- `python3 tools/ai_evidence_log.py sync` und ein echter `enrich`-Eintrag mit den
  tatsächlich ausgeführten Checks.
- Kein Commit, Push, Merge, Deployment oder Archivierung ohne neuen ausdrücklichen Auftrag.

## Dauerhafte Grenzen

- BMD führt Partner-/Artikelstammdaten; PAPO importiert Excel/CSV. Keine direkte
  BMD-Schnittstelle ohne Produkt, Vertrag, Zugang und Beispieldaten.
- Mandantenfähigkeit/Subdomains erst bei zweitem Unternehmen oder verbindlichem Termin.
- Öffentlicher Partnershop, persönliche Partnerkonten, Fachbereiche, Produktgruppen,
  Pakete/BOMs, PP-/Margenlogik und KI-Features nicht vorziehen.
- Benutzer-/UI-Terminologie: **Artikel**; interne `Product`-Namen dürfen bleiben.
- Keine Prüfungen, Betriebszustände oder Nachweise erfinden.

## Verifikation vor Handoff

- Geänderte Kernlogik: fokussierte Tests; Backendänderungen zusätzlich
  `cd backend && ./mvnw test && ./mvnw package`.
- UI-Änderungen: Desktop und Mobile im Browser; kein Frontend-Build vorhanden.
- OpenSpec: `openspec validate <change> --strict`.
- Immer: `git diff --check`, passende Qualitäts-/Korrektheitsreviews und exakte
  Prüfergebnisse.
- Relevante KI-Arbeit nur mit echten Angaben über `tools/ai_evidence_log.py`
  synchronisieren/anreichern.
