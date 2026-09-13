## Context

`DemoDataSeeder` (backend/.../seed/DemoDataSeeder.java) seeds partners, products, product images, enablements, and (dev-only) order history via Panache `persist()` — deliberately not a raw `import.sql`, because Hibernate's pooled sequence optimizer (block size 50) would collide hand-picked seed ids with runtime-allocated ids. It currently gates on `LaunchMode.current() == LaunchMode.NORMAL` (return early) — the packaged jar deployed to the VM always runs in `NORMAL` mode, so seeding never happens there. `%prod` uses `quarkus.hibernate-orm.schema-management.strategy=update` (never drop-and-create), and the VM's Keycloak realm template (`deploy/keycloak/partnerportal-realm.prod.json.template`) already provisions `company-demo`/`partner-demo` logins — so today those logins hit a fully empty database.

The VM is currently the customer's only route to test the application; it is not yet carrying real partner/article/order data, but it is the same environment (`docker-compose.prod.yml`, `%prod` profile) that will later become real production.

## Goals / Non-Goals

**Goals:**
- Let the VM be populated with the same demo dataset dev already has (partners, products, enablements, orders), so a customer login shows a working, populated portal.
- Make seeding an explicit opt-in that leaves `%dev`/`%test` behavior and existing test assumptions untouched.
- Keep the existing idempotency guard so re-running (redeploys, container restarts) never duplicates or overwrites data.

**Non-Goals:**
- No new admin UI or REST endpoint for triggering seeding.
- No change to schema management strategy (`update` stays; no drop-and-create in `%prod`).
- No automatic wipe/reset mechanism for clearing demo data before real onboarding — that is a manual, documented step for later.
- No change to how real Partner/Product data will eventually be imported (CSV/Excel import flow untouched).

## Decisions

**Decouple the seed trigger from `LaunchMode`, add a config flag instead.**
`DemoDataSeeder.onStart()` currently checks `LaunchMode.current() == LaunchMode.NORMAL`. Replace/extend this with an injected `@ConfigProperty(name = "papo.seed-demo-data", defaultValue = "false") boolean seedDemoData`, so the effective condition becomes: seed if (`LaunchMode` is DEVELOPMENT/TEST) OR (`seedDemoData` is true). This keeps existing dev/test behavior identical (flag defaults false and is never set in those profiles) and adds exactly one new path for NORMAL+flag=true.

*Alternative considered*: a separate one-time admin REST endpoint or CLI command to trigger seeding manually. Rejected for this change — more code (new endpoint, auth wiring) for a single-VM, single-customer situation where the risk of accidental re-seeding is already covered by the existing `Partner.count() > 0` guard. Revisit if a second environment or a real production cutover needs a safer, explicit trigger.

**Wire the flag through `docker-compose.prod.yml` / `.env.prod`, not the image.**
`SEED_DEMO_DATA` becomes a new environment variable passed to the `backend` service in `docker-compose.prod.yml`, sourced from `.env.prod` (and documented in `.env.prod.example` with a default of `false`/commented out). It must never be baked into the GHCR image default — the same image is used for the eventual real-production deploy.

**Seed the full existing dataset, including order history.**
`seedOrders()` is currently gated to `LaunchMode.DEVELOPMENT` only (tests need a clean order baseline). For the VM path, call it whenever seeding runs under the new flag too, so dashboards/commission views/status-transition testing all have realistic data instead of zeroes. `%test` stays excluded exactly as today (it doesn't set the new flag).

**Keep the existing `Partner.count() > 0` short-circuit as the sole idempotency guard.**
No new "already seeded" marker table. This mirrors the current dev/test behavior exactly and means: once any partner exists (seeded or from a real import), seeding never runs again regardless of the flag's value.

## Risks / Trade-offs

- **[Risk]** Flag is accidentally left `true` in `.env.prod` when the VM transitions to real production with an empty DB → fake demo partners get seeded into what should become the live environment. **Mitigation**: document explicitly in `docs/DEPLOYMENT.md` that `SEED_DEMO_DATA` must be unset/`false` before real customer onboarding; since it's a manual VM-local `.env.prod` edit (never committed), this is a documented operational step, not a code safeguard — acceptable given the single-VM, single-operator (Tobias/Chris) scale of this project.
- **[Risk]** Seeded order history (dated up to ~5 months in the past) sitting in the same environment where the customer is also creating live test orders could look confusing in a screen-share. **Mitigation**: accepted trade-off per explicit decision — customer wants working dashboards, not an empty one; call out clearly to the customer during the demo that historical orders are seed data.
- **[Trade-off]** No dedicated "clear demo data" tooling. Wiping before go-live means a manual `docker compose down -v` (destroys the Postgres volume) or manual `DELETE` statements. Acceptable for now since real production cutover is not yet scheduled (per CLAUDE.md, thesis deadline for that is still open); revisit if a concrete cutover date is set.

## Migration Plan

1. Add `papo.seed-demo-data` config property (backend/application.properties), default `false` for all profiles.
2. Update `DemoDataSeeder.onStart()` condition as described above; extend `seedOrders()` call to also fire under the flag path.
3. Add `SEED_DEMO_DATA` to `docker-compose.prod.yml`'s `backend` service environment block and to `.env.prod.example` (documented, default off).
4. On the actual VM: set `SEED_DEMO_DATA=true` in the real (gitignored) `.env.prod`, `docker compose up -d` to pick it up, verify seed ran (partners/products/orders present), confirm `company-demo`/`partner-demo` logins now show populated screens.
5. Document the flag and the pre-go-live "turn it off" step in `docs/DEPLOYMENT.md`.

Rollback: set `SEED_DEMO_DATA=false` (or remove the var) and redeploy — no seeding on future restarts, but does not retroactively remove already-seeded rows (manual cleanup if ever needed, see Risks).

## Open Questions

- Exact point at which `SEED_DEMO_DATA` must be flipped off / data cleared is tied to the still-open real-production-cutover timing (CLAUDE.md: "exact thesis deadline/scope for multi-company tenancy... remains open") — no action needed now, just tracked as a future manual step.
