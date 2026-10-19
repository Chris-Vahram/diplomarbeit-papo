## Context

Identity was split out into `bind-partner-login-to-oidc-subject` (15.10.2026). This change only covers the BMD business number. The final canonical rules depend on company data that is not available yet.

## Goals / Non-Goals

**Goals:** BMD-owned visible partner number with confirmed canonical rules; safe renumbering.

**Non-Goals:** Login identity, provisioning, personal accounts, tenancy, direct BMD API.

## Decisions

- Keep `partnerNumber` as text and unique business key; preserve leading zeros.
- Canonical comparison (case, whitespace, allowed characters, length) is taken from the anonymized BMD sample or a written rule sheet; nothing is invented before that.
- Renumbering is allowed only after the subject-binding Release B is deployed, so no login can depend on the old number.

## Risks / Trade-offs

- [Risk] Unknown BMD format. Mitigation: implementation blocked until sample/rules exist (task 1.1).
- [Risk] Renumbering while a username fallback still exists. Mitigation: hard dependency on Release B.

## Migration Plan

Deploy after `bind-partner-login-to-oidc-subject` Release B. Existing numbers stay valid until the company replaces them; rollback keeps subject resolution and needs no number restore.
