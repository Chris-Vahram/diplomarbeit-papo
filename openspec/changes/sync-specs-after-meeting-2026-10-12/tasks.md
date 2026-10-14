## 1. Specification documents — essential

- [ ] 1.1 Update `Specification/functional_specification_document_full_vision.md`: BMD reopened (no "permanently excluded"), UC-A with guest order plus optional customer account and no online payment, shop as homepage builder "Mein Webshop" (path or subdomain, details open), multi-company/personal accounts/audit, B-08/B-09, data model §7, roadmap §8, header B-01…B-09; verify with `grep -n -i "permanently excluded\|no login"` returning nothing outdated.
- [ ] 1.2 Add a dated "Stand 10/2026" note to `functional_specification_document_partnerportal_final.md` and correct BMD exclusion lines 75/78/91; mark reference scale as to be verified; verify FR-01…FR-25 numbering unchanged.
- [ ] 1.3 Update `phase2_backlog.md` B-01/B-02 status and add the 2026-10-12 decisions-log entry.

## 2. Operative context — essential

- [ ] 2.1 Update `CONTINUATION_PROMPT.md`, `ROADMAP_2026-08-25.md` and `PRODUCT_COMPLETION_PLAN.md` to the 12.10 meeting and the new next block; keep open company questions open.
- [ ] 2.2 Update `CLAUDE.md`, `AGENTS.md` and `openspec/config.yaml`: newest meeting 12.10, current active changes, BMD reopened, consistent "Locked direction"; keep rules and Tobias' Claude files.

## 3. Completion

- [ ] 3.1 Run `git diff --check`, check changed Markdown links, `openspec validate sync-specs-after-meeting-2026-10-12 --strict`; record results.
