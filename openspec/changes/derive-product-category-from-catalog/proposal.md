## Why

An article's category is stored twice: as text `Product.category` and as foreign key `Product.articleCategory`. Renaming a category therefore locks and rewrites every assigned article (`CategoryService.update`), the demo seeder writes only the text and needs `CategoryMigrationService` on every start to create the catalogue rows, and two values can drift. Found by the ponytail audit (15.10.2026) and confirmed by the Codex adversarial review.

## What Changes

- The foreign key is the only source: API, list views, filters and CSV export read the name from `articleCategory`.
- `Product.category` is no longer written; the column stays in the database (Hibernate `update` never drops columns, no manual DDL) and is ignored.
- Renaming a category no longer touches articles.
- The demo seeder creates categories and assigns the foreign key directly.
- Requests and imports keep accepting a category **name**; it is still resolved through the catalogue (`findForProductInput`), unchanged for users.
- `CategoryMigrationService` stays until a SQL check proves no legacy rows remain on the VM; its removal is a separate follow-up task.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `article-categories`: adds "Category stored once per article".

## Impact

Backend: `Product`, `ProductDto`, `ProductService`, `CategoryService`, `DemoDataSeeder`, `CsvExportService` and import if they read the text field; tests. Frontend unchanged (`category` stays in the DTO). No schema change.
