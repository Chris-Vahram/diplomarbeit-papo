## Context

All five items are defects in shipped MVP code found by the 2026-07-09 review. The frontend already ships an `escapeHtml()` helper in `lib/html.js` that `data-table` uses correctly; the affected renderers simply bypass it. The backend already uses opencsv with a German-locale writer. No new dependencies or schema changes are needed — this is hardening of existing behavior.

## Goals / Non-Goals

**Goals:**
- Close the stored XSS across the partner→company privilege boundary.
- Prevent CSV formula injection in the Excel-targeted export.
- Keep internal/library detail out of client-facing error messages.
- Remove hardcoded production secrets.

**Non-Goals:**
- No redesign of the dashboards, export, or import flows.
- No change to the product-image access model (documented as accepted risk only).
- No new authentication/authorization mechanics — role scoping is already sound.

## Decisions

- **Reuse `escapeHtml()` from `lib/html.js`** for the three unescaped sinks rather than introducing a templating layer. Rationale: the project already standardized on this helper; the fix is to route the three renderers through it (labels and any `title="..."` attributes), matching `data-table`.
- **Neutralize CSV cells with a leading apostrophe** when the value starts with `= + - @`, applied in one shared helper in `CsvExportService` used by every user-controlled cell. Rationale: apostrophe-prefixing is the standard, locale-safe neutralization and does not depend on opencsv quoting. Alternative considered: stripping/escaping the leading character — rejected because it alters the displayed name.
- **Fixed German error message + server-side logging** for import parse failures. Rationale: matches the "German, plain language, no internals" convention in `docs/CONVENTIONS.md`; the exception detail stays available in logs for debugging.
- **Env-var overrides with defaults** for `%prod` DB credentials and OIDC issuer (`${DB_PASSWORD:...}` style). Rationale: keeps the demo target runnable out-of-the-box while ensuring a real deployment can override without code changes.
- **Product-image endpoint: comment only.** Rationale: product images are non-sensitive; an enablement check adds cost with no real confidentiality gain for the MVP. The decision is recorded so it is a conscious one.

## Risks / Trade-offs

- [Escaping a value that was already safe HTML] → No visible regression; all affected sinks render plain names/usernames, never intentional markup.
- [Apostrophe-prefixing changes the raw CSV byte for formula-like names] → Acceptable and expected; the displayed text in Excel is unchanged for normal names and correct (literal) for formula-like ones.
- [Env-var defaults could still be weak if a deployer ignores them] → Mitigated by documenting the variables; out of scope to enforce secret strength for the MVP demo.

## Migration Plan

No data migration. Changes are code-only and backward compatible. Deploy is a normal rebuild; rollback is reverting the change. The `%prod` env vars fall back to current defaults, so existing demo runs are unaffected.
