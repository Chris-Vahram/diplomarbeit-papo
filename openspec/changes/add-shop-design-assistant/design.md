## Context

Free colour choice replaced fixed palettes; guidance must come from an assistant (`Specification/ENDKUNDENSHOP_DECISIONS.md` §4). Rule-based only: no AI service, consistent with the project's data-sovereignty argument and the fixed stack.

## Goals / Non-Goals

**Goals:** immediate, understandable guidance; hard stop only for functional/legal problems; server-enforced blockers.

**Non-Goals:** AI suggestions, automatic fixing, full WCAG audit.

## Decisions

1. **Rules as plain JS modules (`src/shop/assistant/rules/*.js`) with `{id, severity, scope, check(ctx), explain}`**, run on every draft change. Alternative: server-only checks — rejected, feedback would lag.
2. **Server mirrors only blocker rules (contrast < 3:1, required fields, mandatory content)** in Java, executed on publish. Warnings/Tipps stay client-side; the publish request carries `confirmedWarnings: true`.
3. **Contrast via WCAG relative luminance formula**, shared test vectors in both implementations to keep results identical.
4. **Findings keyed by block id** so inline markers and the panel stay in sync.

## Risks / Trade-offs

- [Client and server contrast results differ] → shared test vectors.
- [Too many hints annoy] → Tipps collapsed by default; Blocker and Warnung first.

## Migration Plan

Additive; no data changes.

## Open Questions

None.
