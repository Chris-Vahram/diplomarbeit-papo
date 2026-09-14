## Context

Product has no business number. Import currently treats normalized article name as identity, and accepted rows persist in one transaction. No anonymized BMD sample exists.

## Goals / Non-Goals

**Goals:** Unique visible textual number, shared API/import canonicalization, safe demo migration and concurrency behavior.

**Non-Goals:** Direct BMD connector, upsert/deactivation, reverse master export, pricing groups, bundles.

## Decisions

- Add textual `articleNumber`, never numeric. Trim surrounding whitespace; preserve internal characters, case and leading zeros until sample data defines stricter canonicalization.
- Add DB unique constraint as final race guard and a shared service-level duplicate check for German errors.
- Because production contains demo data only, assign explicit synthetic BMD-style numbers to seeds and verify all rows before making column required.
- Change import identity from name to article number. Do not use the generic unconditional lowercasing path for this identifier; ImportDefinition supplies its canonicalization.
- Keep create-only import. Existing number produces row rejection; no silent update.
- Show/search number in company article management and existing partner-visible enabled article data. Technical DB id remains internal.
- Do not build article-master export. Official BMD material confirms exported data can be handled through Excel; concrete interface work waits for product/API contract and sample.

## Risks / Trade-offs

- [Risk] Unknown BMD formatting invalidates canonicalization. Mitigation: textual preservation and explicit sample-data gate before implementation starts.
- [Risk] Concurrent imports return success after DB collision. Mitigation: translate unique violation, roll back full commit, test race.

## Migration Plan

Blocked until sample column rules exist. Then add nullable column, backfill demo and verified mappings, detect duplicates, enable unique/not-null, switch API/import/UI. Rollback keeps additive number while reverting import identity only before real BMD data enters.
