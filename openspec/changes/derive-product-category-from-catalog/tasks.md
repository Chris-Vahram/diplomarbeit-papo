## 1. Backend — essential

- [x] 1.1 Tests first: rename shows new name without article writes (Hibernate statistics), import by name links FK, fresh seed has FK-linked categories, article list query count unchanged, CSV export uses catalogue name.
- [x] 1.2 Stop writing the text (field renamed to read-only `legacyCategory`, consumed once by the migration); DTO/export/import/filters read `articleCategory.name`; `CategoryService.update` without article lock/rewrite; seeder assigns FK directly; make 1.1 pass.

## 2. Completion — essential

- [x] 2.1 Full `./mvnw test`, `./mvnw package`, upgrade check against a database from the previous release (schema `update`), Playwright article list/filter/category rename, `openspec validate derive-product-category-from-catalog --strict`, `git diff --check`; document rollback SQL in `docs/DEPLOYMENT.md`.
- [ ] 2.2 (VM check done 16.10.: 0 unlinked rows) Follow-up after deploy: SQL check on the VM (no text-only rows), then remove `CategoryMigrationService` and its tests in a separate commit.
