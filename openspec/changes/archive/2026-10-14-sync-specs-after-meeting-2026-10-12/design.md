## Context

Source order: newest meeting notes override older documents (`Besprechungsprotokoll_12.10.md` is newest), then Chris' confirmed decisions from 25.09 and 06.10 recorded in `PRODUCT_COMPLETION_PLAN.md` and `phase2_backlog.md`. The MVP FSD is the baseline for FR-01…FR-25 and for `docs/REQUIREMENTS_TRACEABILITY.md`.

## Goals / Non-Goals

**Goals:** No document claims BMD is permanently excluded or that the shop has no login; the 12.10 meeting is reflected everywhere agents start; one consistent next-step statement.

**Non-Goals:** Rewriting the MVP FSD requirements, answering the open shop questions (Tobias drafts recommendations; company answers via Prof. Hammer), deciding B-03/B-06/B-07/B-09 rules, reducing all documentation redundancy.

## Decisions

- **Full-vision FSD is rewritten in place** where outdated; it is the whole-product reference, so it describes the current target including open points marked as open.
- **MVP FSD gets a note, not a rewrite**, to keep FR numbering and traceability stable; only the factually wrong BMD exclusion lines are corrected.
- **Homepage builder wording** stays at the level of the meeting: partner-side builder, sidebar item "Mein Webshop", URL by path or subdomain, inspiration from website builders such as Jimdo. Builder scope, layouts and legal pages stay open.
- **Numbers are not invented**: the ~200 partners / ~800 articles reference conflicts with newer notes (~100 partners); mark as to be verified with company data.
- **Agent files**: update volatile facts only; keep rules, stack and roles.

## Risks / Trade-offs

- [Risk] Docs present open questions as decisions. Mitigation: every new shop/BMD statement names its source date or is marked open.
- [Risk] Breaking traceability by renumbering. Mitigation: no FR renumbering in the MVP FSD.

## Migration Plan

Documentation only; revert the commit to roll back.
