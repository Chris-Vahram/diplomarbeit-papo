## Context

Today the Partner↔Keycloak link is an explicit nullable-unique column `Partner.keycloakUsername`, set by the company on the partner create/edit form ("Keycloak-Benutzername" input) and resolved at request time by `CurrentPartner.resolve()` via `Partner.find("keycloakUsername", username)`. The client wants that form field gone (meetings 07.07. + 10.07.2026) — non-technical users don't know what to type into it.

`Partner.partnerNumber` is already `nullable = false, unique = true` and is the identifier the client actually knows (it is also the import duplicate key). Keycloak users are provisioned manually anyway (committed realm export for dev/demo), so the provisioning step can simply adopt the convention *username == partner number* and the extra column becomes redundant.

Relevant surrounding facts found during investigation:

- Published `openspec/specs/partner-management/spec.md` contains only the list-search/pagination requirement — nothing about `keycloakUsername`. The old "Company links Keycloak login to partner" requirement lives only in the *archived* change `2026-07-04-fix-audit-hardening` and was never merged into a published spec; this change supersedes it, so ADDED deltas suffice (no MODIFIED block possible or needed).
- The auth-access-control spec inside the unarchived `build-partnerportal-mvp` change says logins are linked "via the Keycloak username claim" without naming the column — it remains literally true and needs no edit.
- `PartnerService.assertPartnerNumberFree` is already **case-insensitive** (matches the importer's duplicate check), so no two partners can differ only by partner-number casing.
- Backend tests authenticate with `tokens.tokenFor("partner-demo", "partner-demo")` in ~25 places and look partners up as `Partner.find("partnerNumber", "P-1001")` in a handful of places; the seeder links realm user `partner-demo` to seeded partner `P-1001` via the column being dropped.

## Goals / Non-Goals

**Goals:**
- Remove the Keycloak-Benutzername field from form, API, and schema.
- Resolve the authenticated partner via `partnerNumber` with unchanged user-visible behavior: same German 403 texts, same role scoping, same "a partner without a Keycloak user simply cannot log in" semantics.
- Keep demo logins (`partner-demo` / `company-demo`) and the committed realm export unchanged.

**Non-Goals:**
- No Keycloak automation (user provisioning stays manual; import still never creates Keycloak users).
- No address-field split or any other 07.07 meeting item — one change per concern.
- No rename of the demo login or realm edits.

## Decisions

### 1. Link key = `partnerNumber` (locked by Chris, 2026-07-13 — do not re-litigate)
Convention: the manually provisioned Keycloak username **is** the partner's partner number.
- *Rejected:* `Partner.email` — optional and non-unique, cannot be a key.
- *Rejected:* Keycloak `sub` UUID — chicken-egg: partners are imported before their Keycloak users exist, so there is no `sub` to store at import time.

### 2. Canonical-case partner number + exact-match lookup (review-hardened)
Keycloak normalizes usernames to lowercase (login is case-insensitive), while partner numbers are conventionally uppercase (`P-1001`). Naively matching `lower(partnerNumber)` is unsafe: the **DB `unique` constraint on `partnerNumber` is case-sensitive** (`Partner.java`), so `P-1001` and `p-1001` can both exist at the row level, the app-level case-insensitive dedup check is racy and not on every path, and `firstResult()` would then silently pick one — an authentication-correctness hole (Codex finding #3).

Fix: **store one canonical form.** Canonicalize `partnerNumber` to upper-case (`Locale.ROOT`) on *every* write — manual create, manual update, and import — so the existing case-sensitive unique constraint genuinely prevents case-variant duplicates. `CurrentPartner.resolve()` then does an **exact** match on the canonical form: `Partner.find("partnerNumber", jwt.getName().toUpperCase(Locale.ROOT))`. No `lower()` ambiguity, one row or none. `Locale.ROOT` avoids the Turkish-I trap even though numbers are ASCII by convention.

### 2b. Canonicalization lives in one place
Add the upper-casing where partner numbers are set: `PartnerService` (create + update, next to the existing `assertPartnerNumberFree`) and `PartnerImportDefinition`/import mapping (which already trims). Keep the app-level case-insensitive dedup check as a friendly pre-DB error, but correctness now rests on the canonical form + the DB constraint, not the racy check.

### 3. Demo/seed alignment: rename the seeded partner number, not the realm user
After the switch, the realm user `partner-demo` must resolve to a seeded partner. Two options were investigated:

- **Picked — Option A: seed the demo partner with the canonical `partnerNumber = "PARTNER-DEMO"`** (upper-case, per Decision 2; instead of `P-1001`) in `DemoDataSeeder`. The Keycloak realm user is `partner-demo` (lowercase); `resolve()` upper-cases it to `PARTNER-DEMO` and matches the seed exactly — this genuinely exercises the case-folding path (Codex finding: a lowercase seed would only test an exact match, not folding). Churn: the seeder plus roughly ten mechanical `"P-1001"` references in backend tests (`OrderResourceTest`, `PartnerResourceTest`, `EnablementResourceTest`, `PartnerImportResourceTest`). Realm export, `docs/SETUP.md` login table, and every `tokenFor("partner-demo", …)` call stay untouched; the demo login everyone knows keeps working. Trade-off: the demo partner's number no longer follows the `P-XXXX` format of its seed siblings — dev/test-only cosmetics (the seeder never runs in the packaged demo profile).
- **Rejected — Option B: rename the realm user to `p-1001`**. Would keep seed partner numbers pretty, but touches the committed realm export, the `docs/SETUP.md` demo-login table, ~25 `tokenFor("partner-demo", "partner-demo")` test call sites, and everyone's demo-login muscle memory — strictly more churn for a cosmetic gain, and a worse live-demo experience ("p-1001"/"p-1001" as credentials).

### 4. Hard column drop, no deprecation phase
`keycloakUsername` is removed from entity, DTO, and request record in one step. This is a single-team Diplomarbeit prototype with no external API consumers; carrying a deprecated attribute would be pure ceremony. The now-obsolete 409 conflict ("Keycloak-Benutzername ist bereits mit einem anderen Partner verknüpft.") and its test disappear with the field; partner-number uniqueness (which now guards login uniqueness too) is already covered by the existing duplicate test.

### 5. Umlauts: explicitly not a concern here
Login identifiers are ASCII partner numbers by convention, and no German user-facing text changes (the 403 messages "Kein Partnerkonto mit diesem Login verknüpft." and "Ihr Zugang wurde deaktiviert. …" stay byte-identical). Therefore the spec deltas carry no umlaut scenario on purpose.

## Risks / Trade-offs

- [Semantic coupling: renaming a partner number now also relinks/breaks that partner's login] → Accepted for the MVP, but hardened per review: the partner-edit form shows a German warning that changing the Partnernummer changes the partner's login (renaming the Keycloak user must follow). Documented in `docs/DATA_MODEL.md` and `docs/SETUP.md` too. (Making the field immutable-after-provisioning was considered but rejected — the company still needs to fix a typo'd number; a warning is the prototype-appropriate guard.)
- [Non-demo / real deployment] → No production exists yet (Hetzner not provisioned; Keycloak users manually provisioned). At the first real deploy, the provisioning step sets each Keycloak username = that partner's (canonical) partnerNumber; verify **all** seeded/demo users (`partner-demo` and any others), not just one. No migration machinery is in scope for the prototype — there are no live non-demo logins to migrate.
- [Demo profile runs Hibernate `schema-management.strategy=update`, which never drops columns] → An existing demo database keeps a stale, nullable `keycloak_username` column with its unique constraint. Harmless (never read/written; nullable, so inserts succeed — Postgres unique constraints allow multiple NULLs). A fresh demo DB is created clean. No action needed beyond this note.
- [Existing demo-profile data may predate the convention] → Any partner rows in a persistent demo DB must satisfy *Keycloak username == partnerNumber* to log in after the deploy; with the committed realm (`partner-demo`) that means the demo partner row needs partnerNumber `partner-demo`. One-line manual SQL fix at deploy time if such a DB exists; dev/test recreate themselves.
- [Stale code comments referencing the removed spec requirement] → `PartnerRequest`/`CurrentPartner` comments name the old requirement; the tasks update them so code and spec don't drift.

## Open Questions

- None. The link-key decision is locked; everything else follows mechanically.
