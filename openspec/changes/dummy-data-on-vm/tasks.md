## 1. Backend: config-gated seed trigger

- [x] 1.1 Add `papo.seed-demo-data` config property (backend/src/main/resources/application.properties), default `false`, no profile prefix (so it can be set true under `%prod` via env var `SEED_DEMO_DATA`)
- [x] 1.2 Inject the property into `DemoDataSeeder` and change `onStart()`'s guard from `LaunchMode.current() == LaunchMode.NORMAL -> return` to: proceed if `LaunchMode.current() != LaunchMode.NORMAL` OR the flag is `true`
- [x] 1.3 Extend the `seedOrders()` call condition so it also runs when seeding via the flag path (not only `LaunchMode.DEVELOPMENT`), keeping `%test` excluded exactly as today
- [x] 1.4 Confirm the existing `Partner.count() > 0` guard still short-circuits the whole seeder regardless of trigger path (no code change expected here, verify by reading)

## 2. Local verification

- [x] 2.1 Run `./mvnw test` — confirm dev/test seeding and the 77-test suite are unaffected by the new flag (default `false`)
- [x] 2.2 Build the backend image and run it locally via `docker-compose.prod.yml` with `SEED_DEMO_DATA=true` set in a local `.env.prod`; confirm partners/products/enablements/orders are created on a fresh Postgres volume
- [x] 2.3 Restart the same local stack (flag still `true`, data already seeded); confirm no duplicate rows are created

## 3. Deploy wiring

- [x] 3.1 Add `SEED_DEMO_DATA` to the `backend` service's `environment:` block in `docker-compose.prod.yml`, sourced from `.env.prod`
- [x] 3.2 Document `SEED_DEMO_DATA` in `.env.prod.example` (commented out / default `false`, with a one-line explanation)
- [ ] 3.3 (Tobias/Chris) On the real VM: set `SEED_DEMO_DATA=true` in the VM's `.env.prod`, `docker compose -f docker-compose.prod.yml pull && up -d`, confirm seed ran (check partner/product counts)
- [ ] 3.4 (Tobias/Chris) Verify `company-demo`/`partner-demo` Keycloak logins on the VM now show populated dashboards/order lists/article catalog

## 4. Documentation

- [x] 4.1 Update `docs/DEPLOYMENT.md`: document the `SEED_DEMO_DATA` flag, what it seeds, and that it must be turned off (and demo data cleared, e.g. via volume reset) before real customer partners/articles are onboarded
