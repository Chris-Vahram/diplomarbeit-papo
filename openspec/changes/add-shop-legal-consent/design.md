## Context

Seller = company; legal texts belong to the company profile (`CompanyProfile` exists, singleton until multi-tenancy). Decisions: `Specification/ENDKUNDENSHOP_DECISIONS.md` §2, §5. No legal advice is given by the project; texts come from the company.

## Goals / Non-Goals

**Goals:** legal structure complete and enforced before production release; a real, revocable consent mechanism.

**Non-Goals:** writing legal texts, legal review, consent logging on the server, third-party consent tools.

## Decisions

1. **Legal texts as restricted Markdown subset rendered by a tiny safe renderer** (paragraphs, `#` headings, lists, links). Alternative: rich-text HTML — rejected (XSS, stack).
2. **Release gate via `papo.environment=production|staging`** (default staging); checked in release action and on public reads (production shops with missing texts are offline).
3. **Consent stored in `localStorage` (`papo.consent.<version>`)**; consent component exposes `hasConsent('statistik')` used by the statistics change. No server-side consent records (no personal identifier exists without consent).
4. **Banner as own custom element in `src/shop/`**, focus-trapped, keyboard operable.

## Risks / Trade-offs

- [Placeholder texts reach production] → release gate + offline rule.
- [Consent UX too intrusive] → small bottom banner, equal options, remembered decision.

## Migration Plan

Additive fields; existing profile gets empty texts (placeholders shown).

## Open Questions

- Actual legal texts and payment terms from the company.
