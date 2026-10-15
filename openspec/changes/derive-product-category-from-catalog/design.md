## Context

`Product.category` (text) and `Product.articleCategory` (FK to `Category`) coexist since the category catalogue was added (13.09.2026). `ProductService.applyRequest` writes both; `CategoryService.update` rewrites the text on rename under a pessimistic lock; `DemoDataSeeder` writes only the text and relies on `CategoryMigrationService` to create catalogue rows at startup.

## Goals / Non-Goals

**Goals:** one source of truth, simpler rename, seeder without migration dependency, unchanged API and UI.

**Non-Goals:** dropping the database column, removing `CategoryMigrationService` in this change, changing import column names or the category UI.

## Decisions

- **Read model:** `ProductDto.category` = `articleCategory == null ? null : articleCategory.name`. Lists that need names fetch-join `articleCategory` to avoid N+1 (verify with the existing statistics-based tests).
- **Write model:** remove the text field from the entity (`category` attribute deleted), so nothing can write it by accident; the orphaned column remains in existing databases.
- **Seeder:** create the demo categories first, then assign them; no text-only products.
- **Rename:** only `Category.name`/`normalizedName` change; no article lock or rewrite.
- **Migration service:** kept; it only acts on rows with text but no FK, so it is a no-op on new data.

## Risks / Trade-offs

- [Risk] Rollback to the previous release would read the stale text column. Mitigation: documented one-off SQL in the migration plan to refresh the text from the FK before rolling back.
- [Risk] N+1 when listing articles. Mitigation: fetch join plus query-count test.

## Migration Plan

Deploy normally (no schema change). Before a rollback run:
`update product p set category = c.name from article_category c where c.id = p.article_category_id;`
Follow-up (separate task): on the VM check `select count(*) from product where category is not null and article_category_id is null` = 0, then remove `CategoryMigrationService`.
