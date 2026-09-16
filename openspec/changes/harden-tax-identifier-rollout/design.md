## Context

Production uses Hibernate schema `update` without Flyway. The tax-identifier mapping currently declares three new partner columns `NOT NULL`, so PostgreSQL can reject their creation before any application startup code can backfill existing rows. The archived tax-identifier design permits migration only for the known synthetic demo dataset and forbids invented real identifiers.

## Goals / Non-Goals

**Goals:** Make schema creation, verified demo backfill, company-profile initialization and final constraints one tested, idempotent startup path.

**Non-Goals:** General-purpose migration framework, migration of unknown real tax data, multi-company onboarding, or changes to tax-identifier syntax.

## Decisions

- Map the three embedded columns as nullable so Hibernate `update` can add them to a populated table. Keep API validation required and let the startup migration establish PostgreSQL `NOT NULL` after backfill. A pre-Hibernate custom migrator was rejected because the project has no migration framework and adding one for three columns would expand scope.
- Reuse one application startup transaction. Permit backfill only when demo seeding is explicitly enabled (or in dev/test) and the complete three-partner fixture, including names, matches exactly. Create the existing synthetic company profile only for that verified dataset, flush, verify every identifier, then execute idempotent `ALTER COLUMN SET NOT NULL` statements.
- Abort on any unknown incomplete partner. PostgreSQL transactional DDL ensures identifier assignments and constraints roll back together.
- Acquire a PostgreSQL table lock before reading partner rows so concurrent instances serialize without a row-lock-to-DDL-lock upgrade.
- Keep company-profile columns non-null in entity metadata; only partner columns need the nullable-first mapping. Profile recovery belongs to migration, not ordinary seeding, so existing products/orders are never duplicated.
- Add Jakarta `@Size(max = 255)` beside `@NotBlank`; the database remains the final constraint while HTTP clients receive a controlled validation response.

## Risks / Trade-offs

- [Risk] A database with real incomplete partner rows cannot be migrated automatically. → Fail with an explicit operator-facing error; obtain real identifiers before retrying.
- [Risk] Partner entity metadata remains nullable while PostgreSQL is stricter after startup. → API validation and the migration test verify the intended two-stage contract; no nullable runtime state is accepted.
- [Risk] Startup instances race on profile creation or constraint DDL. → An up-front exclusive partner-table lock serializes the migration transaction.

## Migration Plan

Deploy the nullable partner mapping and startup migrator together. The demo deployment keeps `SEED_DEMO_DATA=true`; real deployments leave it off. On startup Hibernate adds nullable partner columns, the migrator backfills only an exact verified demo fixture, creates its missing profile, validates all existing identifiers, and sets `NOT NULL`. Missing real profile data, invalid identifiers, or an unverified fixture abort startup without guessed data.
