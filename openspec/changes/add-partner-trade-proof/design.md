## Context

Article images are stored in PostgreSQL but served to partners with public immutable caching. Global request-body limit is 2,200 KiB. Trade proofs need a separate private boundary and a deliberate 10 MiB limit.

## Goals / Non-Goals

**Goals:** One current proof, partner submission, company review, atomic replacement, private download.

**Non-Goals:** History, OCR, malware-clean guarantee, public links, automatic activation, multiple files, legal retention claim.

## Decisions

- Create separate proof entity with one-to-one unique Partner relation, opaque version, bytes, verified media type, display filename, size, uploadedAt, status, reviewedAt and optional rejection reason. PostgreSQL `bytea` is smallest existing storage path and joins current DB backups.
- Partner endpoints derive owner through `CurrentPartner`; company endpoints use path id with `@RolesAllowed("company")`. Never accept partner id from partner payload.
- Verify extension/MIME and file signatures; perform bounded streaming before full persistence. A file passing signatures is not claimed malware-free.
- Raise global multipart/body ceiling only enough for a 10 MiB file plus measured multipart overhead (target 11 MiB). Before that, deliberately define a 2 MiB import-file business limit and enforce it before `readAllBytes`; this closely preserves the former 2,200 KiB whole-request ceiling after multipart overhead. Keep the existing 2 MiB article-image validator and add pre-read rejection. Trade proof enforces 10 MiB. Tests assert oversized proof/import/image requests fail with 413/established German handling and no persistence.
- Replacement uses one transaction and locking/version check. Old bytes are deleted only when new proof is valid and stored.
- Company accept/reject requests must carry the opaque version they inspected; stale review returns 409. Rejection reason max 1,000 characters. Partner sees own status/reason; proof bytes remain authenticated and uncached.
- Reuse CompanyProfile from `add-business-tax-identifiers` for the portal company's required register identifier. Partner proof stores the same country/type/value group with its current file. This change depends on that change.

## Risks / Trade-offs

- [Risk] Malicious PDF. Mitigation: attachment disposition, nosniff, no inline rendering; antivirus is separate future work.
- [Risk] DB/backups grow. Mitigation: one file per partner, 10 MiB cap, no history.
- [Risk] Legal retention unclear. Mitigation: no historical retention promise; document company policy before production real-data onboarding.

## Migration Plan

Implement after company profile. Add nullable proof storage and UI without requiring existing demo proof. Rollback disables upload/download first; data deletion requires separate approved retention action.
