## Context

`CurrentPartner` currently resolves `jwt.getName()` against canonical `partnerNumber`. Production contains demo data only, allowing an additive demo migration without a real-customer mapping exercise.

## Goals / Non-Goals

**Goals:** Stable identity binding; BMD-owned visible partner number; preserved technical IDs and relations.

**Non-Goals:** User provisioning, personal employee accounts, tenancy, first-login claiming, direct BMD API.

## Decisions

- Add nullable unique `oidcSubject` to Partner. Use `JsonWebToken.getSubject()` only after Quarkus validates issuer/signature. Username is display/login input, never identity. Binding uses compare-and-set: first bind requires empty current value; correction requires the caller to supply the exact current value and a distinct new verified subject.
- Keep `partnerNumber` as text and unique business key. Preserve leading zeros; final length/character canonicalization is gated by sample export.
- Fresh dev/test realms use an explicit stable demo-user ID in the realm export. Existing production Keycloak chooses its own subject, so production binding is never guessed or derived from username.
- Deploy in two releases. Release A adds the nullable column and controlled company-only binding operation while retaining the old resolver. An operator reads the existing demo user's verified subject from Keycloak, binds it and proves the mapping. Release B switches exclusively to subject resolution. New/imported partners remain unbound until the same controlled process assigns a verified subject; ordinary partner forms never accept it.

## Risks / Trade-offs

- [Risk] Wrong subject locks out or cross-links a partner. Mitigation: unique DB constraint, company-only assignment, explicit collision tests.
- [Risk] Unknown BMD format. Mitigation: keep field textual and block final import rules until sample exists.

## Migration Plan

Release A adds nullable column/binding API plus a fixed fresh-realm demo ID, while retaining username resolution. Bind and verify the existing production demo subject with `SEED_DEMO_DATA=false`. Release B switches resolver before any partner-number change. Rollback to Release A is allowed only while every original partner number mapping is unchanged. Release C enables BMD-number changes; after that point auth rollback must keep subject resolution or transactionally restore the exact pre-change number mapping first.
