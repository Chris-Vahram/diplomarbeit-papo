## 1. Rollout safety — essential

- [x] 1.1 Add a nullable-first, transactional tax-identifier migration that backfills only verified demo partners, initializes the missing demo company profile, rejects unknown incomplete rows, and restores all database `NOT NULL` constraints; verify first run, repeated run, rollback, constraints and existing-value preservation against PostgreSQL.
- [x] 1.2 Align company-profile business-name validation with the 255-character column and verify 255 succeeds while 256 is rejected without mutation.

## 2. Completion — essential

- [x] 2.1 Run focused tests, full backend test/package, strict OpenSpec validation, diff check, partnerportal-quality and an independent correctness/database review; fix findings and update the continuation gate only after all checks pass.
