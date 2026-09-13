## Why

The deployed VM (`docker-compose.prod.yml`, `%prod` profile) never runs `DemoDataSeeder` — it is guarded by `LaunchMode.current() == LaunchMode.NORMAL`, which is always true for the packaged jar. The Keycloak realm template already provisions `company-demo`/`partner-demo` logins on the VM, so the customer can log in today but lands on a completely empty portal (0 partners, 0 articles, 0 orders) — worse than not having access at all, since it looks broken rather than unfinished. The customer needs to test the application on the VM; this requires the same realistic demo dataset that already exists in dev.

## What Changes

- Add a `SEED_DEMO_DATA` env var (default off) that, when true, runs the same seed logic as the existing `DemoDataSeeder` (partners, products, product enablements, and order history) against the `%prod`-profile VM database.
- Decouple the seeding condition from `LaunchMode` so it can run under `LaunchMode.NORMAL` when explicitly enabled, while leaving the existing idempotency guard (`Partner.count() > 0`) in place so it never re-seeds or overwrites real data.
- Set `SEED_DEMO_DATA=true` in the VM's `.env.prod` only (never as an image default).
- Document that the flag must be turned off (and the demo data cleared) before real customer partners/articles are onboarded, since `%prod` uses `update` schema strategy (no destructive reset) and seeded rows would otherwise sit alongside real data.

## Capabilities

### New Capabilities
- `demo-data-seeding`: controls whether/how the existing demo dataset (partners, articles, enablements, orders) is populated into a non-dev database via an explicit opt-in flag, independent of Quarkus launch mode.

### Modified Capabilities
(none — no existing merged spec covers seeding; `deployment-pipeline` in `ci-cd-deployment-with-gh-actions` covers build/deploy only, not data)

## Impact

- `backend/src/main/java/at/papo/partnerportal/seed/DemoDataSeeder.java`: seed-trigger condition.
- `backend/src/main/resources/application.properties`: new config property for the flag.
- `docker-compose.prod.yml` / `.env.prod.example`: new `SEED_DEMO_DATA` env var wiring.
- `docs/DEPLOYMENT.md`: document the flag and the "turn off before real onboarding" caveat.
- No schema change, no API change, no effect on `%dev`/`%test` behavior.
