# Continuation Prompt — Partnerportal (Diplomarbeit)

Copy the block below into a fresh session to resume with full context.

```text
You are continuing work on an HTL Diplomarbeit software project. Treat this as authoritative context. Do not re-derive or re-litigate settled decisions. If something is unclear, ask targeted questions instead of assuming.

# PROJECT
Web-based internal B2B Partner Portal. Model: Company -> Partner -> End Customer.
"PAPO" (= PartnerPortal) is the NAME OF THIS THESIS/PRODUCT itself, NOT a client. The real
reference client is unnamed (~200 partners, ~800 articles; orders arrive manually by e-mail/phone
today). Do not reintroduce "PAPO" as a client name. HTL diploma demo prototype, NOT enterprise —
calibrate all review/scope decisions to a 2-person school project with no production deployment
and manually-provisioned Keycloak users.

# TEAM / SPLIT
Chris = implementation (backend + functional frontend). Tobias Huemer = frontend visual design.
Julian left the project. Chris runs all git commit/push/merge himself — hand him branch name +
a ready-to-paste commit message, do not commit unless he explicitly says so. **Commit message
handoff MUST use the heredoc form** (`git commit -m "$(cat <<'EOF' ... EOF)"`), never a bare
`-m "..."` — a multi-line message with an embedded `"` broke in his interactive zsh shell this
session (quote closed early, git silently failed, no obvious error) — see DEV GOTCHAS.

# REPO
Working repo (git): /Users/mitgl/Desktop/Schule/4CHITM/Diplomarbeit/diplomarbeit-papo
Branch: main (== origin/main except one uncommitted local edit — a `.gitignore` addition for
`.playwright-cli/`, left for Chris to commit; check `git status`/`git log` to see if it landed).
Also untracked: `openspec/changes/partner-order-cart/` (a proposed-but-not-yet-implemented
change — this is expected, not stray). Read order below for deep work; STATUS section has the
full narrative of the session that produced this file.

# READ ORDER FOR DEEP WORK (authoritative docs)
1. CLAUDE.md (repo root) — project memory, locked scope, key decisions, entities, roles. READ FIRST.
2. Specification/functional_specification_document_partnerportal_final.md — FSD v2, authoritative MVP (FR-01..FR-25).
3. Specification/phase2_backlog.md — deferred features B-01..B-08 + append-only decisions log.
4. Specification/IMPLEMENTATION_PLAN.md — narrative plan (WP1-WP13).
5. Documentation/Besprechungsprotokolle/ — client meeting notes; NEWEST = Besprechungsprotokoll10_07_2026.txt (authoritative, overrides FSD on conflict). Its asks ("Suchleiste, Filter" + "Filter bei Bestellungen nach Endkunde/Partner" + dormant-Endkunde visibility + good mobile) are the throughline behind most of this session's work.
6. docs/CONVENTIONS.md (package layout, REST paths, frontend structure) + docs/SETUP.md (dev/demo stack, demo users) + docs/DATA_MODEL.md (entities; STALE re ProductImage + the keycloak-username removal — not-yet-applied change).
7. openspec/changes/<name>/ — per-change proposal.md + design.md + specs/ + tasks.md (tasks.md = progress source of truth). Run `openspec list` to see live status of every change.
8. Memory files (auto-loaded via MEMORY.md) — see MEMORY-RELEVANT below.

# STATUS (2026-07-14, end of a long implementation session). Verify with `openspec list` and `git status` before trusting.

## What got MERGED to main this session (all Playwright-verified)
- `self-host-fonts` — vendored Plus Jakarta Sans + Fira Code (frontend/vendor/fonts/), dropped the Google Fonts CDN @import. Offline-demo precondition, unblocked the next two.
- `add-keycloak-session-refresh` — frontend now uses the Keycloak refresh_token (ensureValidSession/refreshSession in auth.js, refresh-and-retry-once in api.js, async router guard). Fixes "man wird schnell ausgeloggt". accessTokenLifespan bumped to 900s in the realm export as defence-in-depth. Two real bugs found+fixed during review: a render-generation race in router.js (rapid navigation could show a stale page) and a logout-during-inflight-refresh resurrection race in auth.js.
- `improve-order-form` — replaced the plain product `<select>` with a searchable combobox (frontend/src/lib/order-form.js), added live line/order totals + a commission preview that mirrors CommissionService's HALF_UP arithmetic exactly (new frontend/src/lib/money.js: sumCents + commissionCentsHalfUp, verified byte-for-byte against a real stored CommissionRecord). Shipped to ALL THREE partner+company order-creation surfaces that existed at the time (order-new page, company manual-order modal, dashboard quick-order modal — the last one added after Chris pushed back on my initial "shared parts only" scope-down; full parity was the right call, cost was small). Two bugs found+fixed: Enter-key stray form submit when no suggestion selectable, and a document click-listener leak across form re-renders (verified via real CDP listener counts). Codex cross-review was attempted (this was flagged a "meaty" change) but hung 25+ min with zero output and was killed — proceeded on Claude's own review + extensive Playwright verification alone.
- `add-order-list-filters` — Partner dropdown + Endkunde text filter on the company order list, Endkunde filter on the partner order list (deliberately no Partner filter there). New `"text"` filter type + a "Filter zurücksetzen" reset button added to the shared data-table.js component, so EVERY table using search/filters (partners, both product lists) got the reset for free. No backend change (client-side only, per the existing order-list-efficiency decision).

## A git mishap during this session (RESOLVED, no data lost) — read DEV GOTCHAS for the lesson
The add-order-list-filters commit initially failed silently (embedded `"` in a bare `-m` string, pasted into an interactive zsh shell, split into a `git commit`/`git checkout main`/`git merge --no-ff` sequence that no-op'd because there was nothing to merge). Diagnosed and fixed within the session: unstaged the unrelated files that had gotten swept in, moved back to the feature branch, committed via heredoc, pushed, merged for real. End state on GitHub is correct.

## Discovered mid-session: partner order-creation was THREE separate UIs, not two
While implementing `improve-order-form`'s "full parity" follow-up, found that `partner-order-list-page.js` ("Meine Bestellungen") has its OWN hand-rolled order-creation modal (plain `<select>`, ~130 lines duplicating form/confirmation logic) that never got any of `improve-order-form`'s treatment — it doesn't call the shared order-form.js functions at all, so it was invisible to the grep that found the other two callers. Combined with the dashboard's modal (shared module, now full-parity) and a THIRD, ORPHANED full page at `/partner/orders/new` (partner-order-new-page.js — best implementation, shared module + side-by-side live-summary layout, but nothing in the app navigates to it), that's three UIs for one job.

Chris's reaction to the resulting combobox UI ("looks weird", "can't see the product, just says 'B...'", "I want both — list AND search", proposed a shopping-cart model himself) led to superseding the planned narrow fix:
- Proposed then SHELVED: `consolidate-partner-order-creation` (would have made the full page canonical via simple navigation, killed both modals, kept the combobox). Superseded before any code was written; deleted from openspec/changes/.
- Proposed and CURRENT: **`partner-order-cart`** (0/21 tasks, not yet implemented) — pivots the partner compose UI from combobox to a browsable/searchable product catalog (thumbnails, full names, category, price — reusing `resolveImageUrls` from product-images.js and the category-filter pattern from partner-product-list-page.js) + a cart (add-to-cart or single-item "direct" order, both the same submit — money.js reused verbatim for totals/commission). This absorbs the consolidation: it also kills both partner modals and makes the full page canonical, in one change instead of two. Company manual-order flow explicitly OUT of scope — stays on the combobox (different UX, company picks a partner first). No backend change either way. Full detail in openspec/changes/partner-order-cart/{proposal,design,specs,tasks}.md — read design.md's Decisions section before starting, it maps every reuse point precisely (file:line level was verified this session, but re-verify — code moves).

# WHAT'S LEFT / NEXT MOVES — Chris's call, but here's the reasoning
Sequencing recommendation from this session's wrap-up (not yet acted on):
1. **`add-dormant-kunde-visibility`** (0/18 tasks) and **`add-order-document-print`** (0/10 tasks) — both small, both isolated (new page / CSS-only respectively), both direct 10.07-meeting asks, ZERO file overlap with the cart. Quick clean wins, do these first.
2. **`partner-order-cart`** (0/21 tasks) — give it its own focused session once the above are merged and main is updated. Biggest remaining frontend piece; heavy reuse already mapped in design.md but it's genuinely new UI (catalog+cart), budget real attention, not a quick pass.
3. **`remove-keycloak-username-field`** (0/25 tasks) — LAST, as always planned: biggest blast radius (PartnerService + DTO + ~4 backend test files), explicitly sequenced last so nothing after it can conflict.

Also still open / externally blocked:
- `ci-cd-deployment-with-gh-actions` (0/40 tasks) — planned only, needs Oracle→Hetzner re-scope once Christian Bachl's Hetzner contact happens (company-side, not Chris's blocker to clear).
- Import column-mapping — waiting on real client data (company said 1-2 weeks as of the 10.07 meeting; check if it's arrived).
- `build-partnerportal-mvp` (46/53) and `order-correctness-hardening` — still un-archived; archiving `build-partnerportal-mvp` first would unblock `order-correctness-hardening`'s archive (a spec-sync ordering issue, not a quality issue — see prior session notes if archiving these becomes the task).

Every change above works the same way: branch off an UPDATED main -> `/opsx:apply` -> implement -> `verify` skill + `partnerportal-quality` skill + `/code-review` (Codex cross-review only for genuinely complex ones — `partner-order-cart` likely qualifies given its size, but Codex hung for 25+ min last time it was tried; have a time-box and a fallback plan if it hangs again) -> hand Chris branch + heredoc-safe commit message -> he merges -> next.

# IMPLEMENTATION WORKFLOW (how Chris works — follow it)
- Spec-driven OpenSpec: propose via `openspec-propose` (or /opsx:propose) before non-trivial work, apply via `/opsx:apply`, work tasks.md top-down, check off as done, validate after spec edits, archive via `/opsx:archive` when complete.
- Plan with Opus/Fable, IMPLEMENT with Sonnet (switch model at apply time) — this session's cart proposal was planned on Opus; implement it on Sonnet.
- One branch per change (e.g. `feat/partner-order-cart`). Commit in small logical chunks. Chris runs git himself — end each turn with branch + a heredoc-safe ready commit message, unprompted.
- Before commit/merge: run the `verify` skill (drive the flow end-to-end, incl. unhappy path) + the `partnerportal-quality` skill + built-in `/code-review`. For meaty changes, optionally a Codex cross-review of the diff — budget a hard time limit, it hung to death once already this session.
- Browser-verify UI changes with Playwright (dev stack up). Every text-I/O change needs an explicit umlaut (ä/ö/ü/ß) test — hard requirement (Austrian). This session repeatedly used CDP `Network.clearBrowserCache`/`setCacheDisabled` before every fresh Playwright check — the dev static server + hash router combo goes stale otherwise (see DEV GOTCHAS).
- When something doesn't match what the proposal scoped for (an unlisted file, an unexpected caller, a UX call), STOP and ask via a clarifying question rather than silently deciding — this session's dashboard-full-parity call and the cart pivot both started as exactly this kind of pause, and both times the answer changed the plan meaningfully.

# SKILLS TO USE
- `/opsx:apply`, `/opsx:archive`, `/opsx:propose` (or openspec-* variants) — the OpenSpec workflow.
- `partnerportal-quality` (PROJECT skill, .claude/skills/) — reviews a diff against THIS project's rules (role/scoping, German texts, money=integer-cents/BigDecimal, MVP scope). Run before every commit/merge.
- `verify` — drive a change end-to-end and observe real behavior. `run` — launch/screenshot the app.
- `code-review` / `security-review` / `simplify` — built-in per-diff passes. This session's `/code-review` calls were mostly done as focused self-review (not the full 8-agent spawn) for changes small enough that the implementer already held full context — use judgment on when the lighter pass is enough vs. when the diff genuinely needs the multi-angle treatment (auth/concurrency code got the full treatment; CSS-only and small filter diffs didn't).
- `frontend-design` — for the cart's catalog/card visual design. `dataviz` — if dashboard charts get touched.
- `claude-api` — model IDs / API reference (Opus 4.8 = claude-opus-4-8, Fable 5 = claude-fable-5, Sonnet 5 = claude-sonnet-5).
- Codex (external, via CLI): `codex exec -m gpt-5.6-sol -c model_reasoning_effort="high" "<prompt>"` for cross-model review. Hung 25+ min with zero output once this session (on `improve-order-form`'s diff) — killed it, proceeded without. If retrying, consider a shorter/narrower prompt or a hard timeout via a background task check rather than blocking.

# SUBAGENTS (spawn only if Chris asks or names one)
- For implementation work, prefer inline (this whole session was done inline, no subagent spawns) — the reasoning: re-deriving context in a fresh subagent is expensive when the main thread already holds it from having written the code.
- caveman:cavecrew-investigator (locate code, compressed output), cavecrew-builder (1-2 file surgical edit), cavecrew-reviewer (diff review). general-purpose for multi-step. Explore for read-only lookup.

# MEMORY-RELEVANT (auto-loaded from MEMORY.md, verify against code before trusting)
- git-branching-and-checkpoints — Chris runs commit/push/merge himself; UPDATED this session with the heredoc-commit-message lesson (a bare multi-line `-m "..."` with an embedded `"` silently broke his commit).
- cross-model-review-workflow — Codex cross-review on complex changes; note the hang this session when deciding whether to retry it.
- partnerportal-review-decisions-2026-07 — Direction B "Auftragsbeleg" (still deferred), Q1 order-form money-preview (now shipped via improve-order-form + about to be reshaped again by partner-order-cart).
- austrian-client-umlauts-and-split-changes — umlauts hard requirement; one change per concern (this session's split-then-shelve-then-repropose of the order-creation consolidation is a live example of following this).
- always-state-branch-and-commit-message — branch per change, hand Chris branch+message, he commits.
- partnerportal-dev-environment-gotchas — see DEV GOTCHAS below.

# LOCKED DECISIONS (do not re-litigate)
- Commission = Partner.commissionPercent, dropdown 5/10/15 + custom; stored per order as CommissionRecord; BigDecimal HALF_UP; no CommissionLevel entity. Frontend mirrors this exactly via money.js's commissionCentsHalfUp (integer-cent exact-division, not float Math.round) — this is now the canonical frontend implementation, reuse it, don't reinvent.
- Active status = DB flag Partner.active, enforced server-side. Kunde scoped to creating partner. OrderItem unit-price snapshot; quantity <= 10000.
- Partner<->Keycloak link (current, until remove-keycloak-username-field ships) = keycloakUsername field; that change will switch it to Partner.partnerNumber, canonical uppercase, exact match.
- Dashboard totals: fulfilled-only; all-time by default, integer-cent ranged totals.
- Schema: Hibernate auto-gen, no Flyway (dev/test drop-and-create + DemoDataSeeder; demo/prod update).
- German (de-AT) everywhere. Tech stack FIXED: HTML/CSS/native Web Components (no framework), Quarkus, PostgreSQL, Keycloak, Apache POI + opencsv. No new deps, no CDN (offline demo — self-host-fonts made this fully true).
- Order-form architecture (as of this session): frontend/src/lib/order-form.js holds the shared combobox/Endkunde-picker/confirmation code (still used by the company manual-order flow); frontend/src/lib/money.js holds integer-cent + HALF_UP-commission math (shared by dashboard-data.js and order-form.js); frontend/src/components/data-table.js is the shared list/search/filter/pager component (now with "text" filter type + reset button, used by every list page). partner-order-cart will add frontend/src/lib/order-cart.js alongside these, NOT replace order-form.js (company flow still needs it).

# DEV GOTCHAS (learned the hard way, incl. this session)
- `my_postgres` container squats port 5432 -> `docker stop my_postgres` before `docker compose up -d`.
- Hash-router SPA: edited JS can look stale between routes/reloads -> ALWAYS do a CDP `Network.clearBrowserCache` + `Network.setCacheDisabled(true)` + reload before trusting a fresh Playwright check, especially in a long-lived browser session (this session hit this repeatedly — a "browser tab open for hours across many test cycles" accumulates enough stale state that even navigation-based fixes can look broken when they're actually fine; always verify against a genuinely fresh load before concluding a fix doesn't work).
- Keycloak access token now 900s (15min) by default post add-keycloak-session-refresh; the refresh flow, not the lifespan, is what actually keeps sessions alive.
- Quarkus dev drop-and-creates + reseeds on JVM restart; seeder only runs when Partner.count()==0.
- @Lob byte[] on Postgres throws — use plain byte[] (bytea). RestAssured .multiPart mis-encodes non-ASCII filenames client-side — build multipart by hand for umlaut-filename tests.
- Frontend apiFetch* helpers take paths WITHOUT leading /api; use them (not raw fetch) so German error toasts work (lib/api.js extractErrorMessage).
- zsh: a trailing `# comment` on a one-liner is passed as a literal arg — keep comments on their own line. **New this session: a bare `git commit -m "multi-line string with an embedded \" character"` typed directly into an interactive zsh shell closes the outer quote early and silently corrupts the commit — always hand Chris the heredoc form for anything beyond a single plain-quoted line.**
- Git worktree + uncommitted openspec artifacts authored in main before branching can BLOCK merge - author change files inside the worktree, or rm the untracked main-tree duplicate before merging. (This session didn't use worktrees — sequential branching per the locked workflow — but the gotcha still applies if that changes.)
- `.playwright-cli/` (like the already-ignored `.playwright-mcp/`) is throwaway test-runner output that kept appearing as untracked junk across changes — added to .gitignore this session (verify the commit landed, see REPO section).
- Testing a temporary/edge-case scenario (e.g. an umlaut-username Keycloak user, a forced-expired token, a custom commissionPercent) via the Admin API or direct sessionStorage manipulation is a good pattern for exercising real code paths without waiting on real time or seeding permanent fixtures — used successfully several times this session. Clean up temp Keycloak users/product-test-data afterward if they're not simply "dev DB resets on next restart" throwaway (most are).

# OPEN QUESTIONS (ask, don't assume)
- Import file column formats — waiting on client data (check if the "1-2 weeks" from the 10.07 meeting has elapsed).
- Dormancy threshold (60 days, per add-dormant-kunde-visibility's proposal) — confirm with client at next Termin, not blocking implementation.
- Hetzner provisioning timing (Bachl contact) -> ci-cd re-scope.
- Whether `partner-order-cart`'s open questions (explicit "Sofort bestellen" shortcut vs. relying on a one-item cart; catalog card density/filter-as-chips-vs-dropdown) need Chris's input before/during implementation, or are fine as implementer's judgment call (design.md flags both as UI-polish, not architectural).

# NEXT STEP
Ask Chris which of the two sequencing options he wants (small-wins-first as recommended, or dive straight into partner-order-cart), confirm the `.playwright-cli/` gitignore commit landed, then cut the branch for whichever change is next and `/opsx:apply`. Fill in his answer: <STATE NEXT>.
```
