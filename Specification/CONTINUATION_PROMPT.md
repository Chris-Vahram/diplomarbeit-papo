# Continuation Prompt — Partnerportal (Diplomarbeit)

Copy the block below into a fresh session to resume with full context.

```text
You are continuing work on an HTL Diplomarbeit software project. Treat this as authoritative context. Do not re-derive or re-litigate settled decisions. If something is unclear, ask targeted questions instead of assuming.

# PROJECT
Web-based internal B2B Partner Portal. Model: Company -> Partner -> End Customer.
"PAPO" (= PartnerPortal) is the NAME OF THIS THESIS/PRODUCT itself, NOT a client. This was
corrected across CLAUDE.md + the FSDs on 2026-07-13 (they used to wrongly say "reference client
PAPO"). The real reference client is unnamed (~200 partners, ~800 articles; orders arrive manually
by e-mail/phone today). Do not reintroduce "PAPO" as a client name. HTL diploma demo prototype,
NOT enterprise — calibrate all review/scope decisions to a 2-person school project with no
production deployment and manually-provisioned Keycloak users.

# TEAM / SPLIT
Chris = implementation (backend + functional frontend). Tobias Huemer = frontend visual design.
Julian left the project. Chris runs all git commit/push/merge himself — hand him branch name +
ready commit message, do not commit unless he explicitly says so.

# REPO
Working repo (git): /Users/mitgl/Desktop/Schule/4CHITM/Diplomarbeit/diplomarbeit-papo
Branch: main (== origin/main, clean tree). This session's PLANNING work is committed + pushed
(commits 1fb81c5..36a4b71: meeting+backlog+PAPO fix, the 7 openspec proposals, the quality skill,
deck edits). IMPLEMENTATION of the 7 changes has NOT started. Note: Tobias pushed in parallel this
session (application.properties + his own copy of the 10.07 meeting note) — rebased in, meeting-note
add/add conflict resolved (kept the structured version with Chris's Keycloak Nachtrag).

# READ ORDER FOR DEEP WORK (authoritative docs)
1. CLAUDE.md (repo root) — project memory, locked scope, key decisions, entities, roles. READ FIRST.
2. Specification/functional_specification_document_partnerportal_final.md — FSD v2, authoritative MVP (FR-01..FR-25).
3. Specification/phase2_backlog.md — deferred features B-01..B-08 + append-only decisions log (10.07 meeting synced here).
4. Specification/IMPLEMENTATION_PLAN.md — narrative plan (WP1-WP13).
5. Documentation/Besprechungsprotokolle/ — client meeting notes; NEWEST = Besprechungsprotokoll10_07_2026.txt (authoritative, overrides FSD on conflict).
6. docs/CONVENTIONS.md (package layout, REST paths, frontend structure) + docs/SETUP.md (dev/demo stack, demo users) + docs/DATA_MODEL.md (entities; STALE re ProductImage + the keycloak-username removal).
7. openspec/changes/<name>/ — per-change proposal.md + design.md + specs/ + tasks.md (tasks.md = progress source of truth).
8. Memory files (auto-loaded via MEMORY.md) — see MEMORY-RELEVANT below.

# STATUS (2026-07-13). Verify with `openspec list` and `git status` before trusting.
MVP core + all post-MVP work through the 4 hardening changes + product-image-upload is DONE and MERGED
to main (see git log). This session did NO implementation — it PLANNED. Specifically:

## 1. Captured the 10.07.2026 client meeting (was missing from the repo)
Documentation/Besprechungsprotokolle/Besprechungsprotokoll10_07_2026.txt. Key outcomes (authoritative):
- MVP + live demo well received.
- Priorities: simple Benutzerführung + good MOBILE version (partners often not tech-savvy); partner
  should see Endkunden who haven't ordered in a while.
- Improvements: product-add in order form needs Suchleiste + Filter; order-list filter by Endkunde/Partner.
- HOSTING CHANGED: company provides HETZNER instances (Christian Bachl contacts Chris) — SUPERSEDES the
  Oracle-Cloud assumption in ci-cd-deployment-with-gh-actions. Re-scope that change before building.
- Company delivers real data/Vorgehensweisen within 1-2 weeks -> then implement import mapping -> new Termin.
- Chris's Nachtrag: Keycloak logs you out fast; the "Keycloak-Username" field on partner-create is unclear.
Backlog decisions log (phase2_backlog.md) updated with all of this.

## 2. Proposed 7 new OpenSpec changes (all validate, 0 tasks done, NONE applied)
Driven by the meeting + the 2026-07-09 redesign review. Planned with Opus/Fable, ready for Sonnet apply.
  - add-keycloak-session-refresh (0/14) — fixes the fast logout. auth.js never uses the refresh_token;
    getSession() wipes on access-token expiry (~5min default). Add refresh + retry-once + realm 15min bump.
    RESOLVED: logout is on IN-APP sidebar navigation -> sessionStorage is correct, refresh flow is the full
    fix (no localStorage). Frontend-only + 1 realm value. SINGLE-PASS (no cross-review needed).
  - remove-keycloak-username-field (0/25) — drops the confusing field; link Partner<->Keycloak via
    Partner.partnerNumber instead (LOCKED decision). HARDENED by Codex review: partnerNumber is
    canonicalized to UPPERCASE (Locale.ROOT) on every write, resolve() exact-matches the uppercased
    Keycloak username claim -> closes a real case-variant-duplicate auth-ambiguity bug (DB unique
    constraint is case-sensitive). Backend+frontend+tests. Demo seed partner renamed P-1001 -> PARTNER-DEMO.
  - add-dormant-kunde-visibility (0/18) — new "Meine Endkunden" partner page + lastOrderDate per Kunde
    (one grouped query, no N+1) + "Inaktiv" badge/filter. Threshold 60 days DECIDED (confirm w/ client
    at next Termin). Calendar-day arithmetic (not JS ms). Page-local oldest-first sort (NOT shared
    data-table sorting — scoped down on review). Backend tests added.
  - add-order-list-filters (0/15) — company: Partner dropdown + Endkunde text filter; both lists:
    Endkunde + time. FINDING: time+status filters ALREADY SHIP (frontend-fixes-and-additions); real gap
    is only the Partner dropdown + Endkunde text filter + reset button. NO backend change (OrderDto
    already has partnerId/partnerName/KundeDto). Client-side per order-list-efficiency decision.
  - improve-order-form (0/19) — searchable combobox picker (replaces 800-item <select>) + live line/
    order totals + commission preview (matches backend HALF_UP to the cent). commissionPercent + prices
    already client-side -> NO backend change. TRIMMED on review: ships search+totals; the document/
    letterhead "Belegvorschau" look is DEFERRED to a later change. Company picker must carry price/
    category (currently dropped). Combobox stale-selection guard + category escaping. Depends on
    self-host-fonts landing first.
  - self-host-fonts (0/4) — vendor woff2 + @font-face, drop the Google-Fonts CDN @import in base.css.
    Split out of improve-order-form on review (offline precondition for ALL design work).
  - add-order-document-print (0/10) — "Beleg drucken" via native window.print() + @media print on the
    order confirmation + order-details. No library, offline. NEW feature (Chris greenlit planning it).

## 3. Cross-model review workflow established (used this session)
Chris runs BOTH Claude + Codex subscriptions. New rule in ~/.claude/CLAUDE.md: on genuinely complex
changes, get a second-model review from the other vendor before finalizing (plan+review -> code+review).
This session: ran `codex exec -m gpt-5.6-sol -c model_reasoning_effort="high"` on the 3 meaty changes
(improve-order-form, remove-keycloak-username-field, add-dormant-kunde-visibility). Codex read the actual
backend code and found: the partnerNumber case-normalization bug (fixed), company picker dropping price/
category (fixed), preview!=confirmation on mid-session price change (spec softened to "estimate"),
commission float parity (HALF_UP), threshold contradiction, calendar-day arithmetic, category escaping,
combobox stale-id, over-constrained specs. All RECONCILED into the plans (prototype-calibrated: pushed
back on Codex's enterprise-migration apparatus and frontend-test-runner demand — no production, no JS
test framework). The 3 simple changes (session-refresh, order-filters, print) did NOT need cross-review.

# COMMITTED + PUSHED THIS SESSION (main, commits 1fb81c5..36a4b71)
- docs: 10.07 meeting note + phase2_backlog sync + PAPO naming fix (CLAUDE.md + both FSDs) + this file
- docs(openspec): the 7 change proposals
- chore: .claude/skills/partnerportal-quality (project quality-review skill)
- docs: presentation-deck edits + skills-install log
Working tree is now CLEAN. The next thing that produces uncommitted work is implementing a change.

# ARCHIVING STATUS (updated 2026-07-14 after the hardening QA pass)
ARCHIVED this session (all Verify tasks browser-confirmed via Playwright): add-product-image-upload,
security-hardening-review-fixes, close-fsd-order-gaps, order-list-efficiency. Specs synced to
openspec/specs/ (new: product-images, security-hardening, order-management, order-notifications).
STILL ACTIVE (not archivable yet):
- order-correctness-hardening (✓ all 19 tasks done + verified) — archive ABORTS on a spec-sync mismatch:
  its partner-management delta MODIFIES "### Requirement: Partner validation", which only exists in the
  still-unarchived build-partnerportal-mvp change (so it's not in the published spec). Structural, not a
  QA issue. Fix path: archive build-partnerportal-mvp first (publishes that requirement), then this
  archives cleanly; OR change that one delta from MODIFIED to ADDED; OR archive with --skip-specs (lossy —
  drops this change's other valid deltas too). Left active with a note for now.
- build-partnerportal-mvp (46/53) — remaining 7 are 8.x thesis/UI-doc tasks; not archivable yet. Its
  unarchived state is what blocks order-correctness above.
- ci-cd-deployment-with-gh-actions (0/40) — planned only, re-scope Oracle->Hetzner first.
- the 7 new meeting-driven changes — proposed, not implemented.
HARDENING QA RESULT (2026-07-14): 11/11 Verify items confirmed. One real bug found + fixed during it:
`partner-order-new-page.js` had NO double-submit guard (dashboard quick-order did) — a fast double-submit
created TWO orders; added the `submitButton.disabled=true` guard, re-verified one order. That fix is a
loose commit on top of the archived order-correctness change (see git log / WHAT'S LEFT).
archive/ now holds 13 changes.

# WHAT'S LEFT / NEXT MOVES
- Implement the 7 changes with Sonnet. DECIDED WORKFLOW (2026-07-13, headache-free): SEQUENTIAL, not
  parallel — one change at a time, each on its own branch cut from an UPDATED main, merged before the
  next starts. This way the shared-file edits (base.css touched by fonts/order-form/dormant/print;
  company-order-list-page.js touched by order-form + order-filters) are already in main before the next
  change touches them -> ZERO merge conflicts. No worktrees needed. Loop per change: Chris cuts branch ->
  Sonnet /opsx:apply + implement + check off tasks.md + commit to branch -> verify (verify +
  partnerportal-quality + /code-review; optional Codex diff review on the meaty ones) -> Chris merges to
  main + push -> next.
  ORDER: self-host-fonts (tiny; improve-order-form depends on it) -> add-keycloak-session-refresh
  (isolated) -> improve-order-form -> add-order-list-filters (after order-form, shared file) ->
  add-dormant-kunde-visibility -> add-order-document-print -> remove-keycloak-username-field (LAST,
  biggest blast radius: PartnerService + DTO + ~4 test files, nothing after it to conflict with).
- Waiting on EXTERNALS: client data (1-2 wks) -> finalize import column mapping (FSD §13 open question);
  Christian Bachl's Hetzner contact -> re-scope ci-cd-deployment-with-gh-actions Oracle->Hetzner, then deploy.
- Run VERIFY checklists on the 4 merged hardening changes (dev stack up) then ARCHIVE them + also archive
  add-product-image-upload (already complete) via /opsx:archive. See ARCHIVING STATUS above.
- Remove leftover git worktrees from the prior session if still present:
  /private/tmp/deck-branch-worktree, fsd-gaps-worktree, order-correctness-worktree, order-efficiency-worktree.
- THESIS writeup (8.4, FSD->impl->tests mapping, thorough — Chris's ask). Not started. Longest pole.
  Use both models: Opus 4.8 drafts (consistent voice), GPT-5.6 Sol critiques. Sonnet 5 for cheap drafts.
- Tobias's lane (do NOT implement): mobile/responsive, simplicity polish, visual side of the new features.

# IMPLEMENTATION WORKFLOW (how Chris works — follow it)
- Spec-driven OpenSpec: the 7 changes are already proposed. To build one: /opsx:apply <change-name>,
  work tasks.md top-down by dependency, check off tasks as done. Validate after spec edits.
- Plan with Opus/Fable, IMPLEMENT with Sonnet (switch model at apply time).
- One branch per change (e.g. fix/keycloak-session-refresh, feat/improve-order-form). Commit in small
  logical chunks. Chris runs git himself — end each turn with branch + ready commit message, unprompted.
- After implementing a change, BEFORE commit/merge: run the `verify` skill (drive the flow end-to-end,
  incl. unhappy path) + the `partnerportal-quality` skill + built-in `/code-review`. For the meaty
  changes, optionally a Codex cross-review of the diff (`codex exec -m gpt-5.6-sol -c
  model_reasoning_effort="high" "review the diff on this branch ..."`), paste findings back, reconcile.
- Browser-verify UI changes with Playwright (dev stack). Every text-I/O change needs an explicit
  umlaut (ä/ö/ü/ß) test/scenario — hard requirement (Austrian).

# SKILLS TO USE
- /opsx:apply, /opsx:archive, /opsx:propose (or openspec-* variants) — the OpenSpec workflow.
- partnerportal-quality (PROJECT skill in .claude/skills/) — reviews a diff against THIS project's rules
  (role/scoping, German texts, money=BigDecimal/integer-cents, MVP scope). Run before commit/merge.
  Complements, does not replace, the built-in code-review.
- verify — drive a change end-to-end and observe real behavior before calling it done (Prof asked for
  more self-testing). run — launch/screenshot the app.
- code-review / security-review / simplify — built-in per-diff passes.
- frontend-design — for improve-order-form UI + any Direction-B / mobile design work (loaded this session
  for the order-form direction). dataviz — if dashboard charts get touched.
- claude-api — model IDs / API reference (Opus 4.8 = claude-opus-4-8, Fable 5 = claude-fable-5, Sonnet 5
  = claude-sonnet-5).
- Codex (external, via CLI): `codex exec -m gpt-5.6-sol -c model_reasoning_effort="high" "<prompt>"` for
  cross-model review. GPT-5.6 tiers: Sol (best) / Terra / Luna; efforts include max/ultra. Reads local
  files directly (no repo upload, no copy-paste). sage CLI (`@tigtech/sage`) auto-reviews Claude Code
  live during implementation — optional, currently OpenAI-reviewer only.

# SUBAGENTS (spawn only if Chris asks or names one)
- Chris used background subagents this session to propose 3 changes in parallel (worked well). For
  implementation, prefer inline; delegate to subagents only for mechanical parallel work Chris requests.
- caveman:cavecrew-investigator (locate code, compressed output), cavecrew-builder (1-2 file surgical
  edit), cavecrew-reviewer (diff review). general-purpose for multi-step. Explore for read-only lookup.

# MEMORY-RELEVANT (auto-loaded from MEMORY.md, verify against code before trusting)
- cross-model-review-workflow — Codex cross-review on complex changes; thesis = both models (Opus drafts,
  Sol critiques).
- partnerportal-review-decisions-2026-07 — Direction B "Auftragsbeleg", Q1 order-form money-preview.
- austrian-client-umlauts-and-split-changes — umlauts hard requirement; one change per concern.
- git-branching-and-checkpoints + always-state-branch-and-commit-message — branch per change, hand Chris
  branch+message, he commits.
- partnerportal-dev-environment-gotchas — see DEV GOTCHAS below.

# LOCKED DECISIONS (do not re-litigate)
- Commission = Partner.commissionPercent, dropdown 5/10/15 + custom; stored per order as CommissionRecord;
  BigDecimal HALF_UP; no CommissionLevel entity.
- Active status = DB flag Partner.active, enforced server-side (CurrentPartner.resolve + company manual
  path). Kunde scoped to creating partner. OrderItem unit-price snapshot; quantity <= 10000.
- Partner<->Keycloak link (as of this session's plan) = Partner.partnerNumber, canonical uppercase,
  exact match. Keycloak users provisioned MANUALLY (import creates Partner records only).
- Dashboard totals: fulfilled-only; all-time by default, integer-cent ranged totals.
- Schema: Hibernate auto-gen, no Flyway (dev/test drop-and-create + DemoDataSeeder; demo/prod update).
- German (de-AT) everywhere. Tech stack FIXED: HTML/CSS/native Web Components (no framework), Quarkus,
  PostgreSQL, Keycloak, Apache POI + opencsv. No new deps, no CDN (offline demo).
- Presentation deck = hand-authored reveal.js (offline). Scope-split decisions this session: fonts split
  out; Belegvorschau document-look deferred; dormant sort page-local.

# DEV GOTCHAS (learned the hard way)
- `my_postgres` container squats port 5432 -> `docker stop my_postgres` before `docker compose up -d`.
- Hash-router SPA: edited JS can look stale between routes -> switch origin localhost<->127.0.0.1 (both
  CORS-whitelisted) or CDP Network.clearBrowserCache to force fresh fetches; re-login (sessionStorage).
- Keycloak access token ~5-6min (the very thing add-keycloak-session-refresh fixes).
- Quarkus dev drop-and-creates + reseeds on JVM restart; seeder only runs when Partner.count()==0.
- @Lob byte[] on Postgres throws — use plain byte[] (bytea). RestAssured .multiPart mis-encodes non-ASCII
  filenames client-side — build multipart by hand for umlaut-filename tests.
- Frontend apiFetch* helpers take paths WITHOUT leading /api; use them (not raw fetch) so German error
  toasts work (lib/api.js extractErrorMessage).
- zsh: a trailing `# comment` on a one-liner is passed as a literal arg — keep comments on their own line.
- Git worktree + uncommitted openspec artifacts authored in main before branching can BLOCK merge -
  author change files inside the worktree, or rm the untracked main-tree duplicate before merging.

# OPEN QUESTIONS (ask, don't assume)
- Import file column formats — waiting on client data (1-2 wks).
- Dormancy threshold 60 — confirm with client at next Termin (not blocking).
- Research-question supervisor sign-off (wording approved by Chris 2026-07-07).
- Hetzner provisioning timing (Bachl contact) -> ci-cd re-scope.

# NEXT STEP
Planning is committed + pushed; the immediate next move is IMPLEMENTATION. Most likely: switch to Sonnet
and build `self-host-fonts` on `feat/self-host-fonts` (first in the sequential order above), then walk
the list one change at a time. Alternatives Chris may pick: archive add-product-image-upload now
(/opsx:archive, it's the one clean complete change), or run the hardening Verify checklists with the dev
stack up. Ask, don't assume. Fill in his answer: <STATE NEXT>.
```
