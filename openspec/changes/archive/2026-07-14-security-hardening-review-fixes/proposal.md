## Why

A Fable code review (2026-07-09) of the already-built MVP found several security defects in shipped code: a stored XSS that crosses the partner→company privilege boundary, CSV formula injection in the Excel export, leaked library internals in client-facing error messages, and hardcoded production secrets. These are correctness/safety bugs in existing behavior and should be fixed before further feature work.

## What Changes

- Escape all partner-/user-controlled strings before they are rendered via `innerHTML`. Stored XSS: a partner sets an arbitrary `name` via `PUT /api/partner/profile`; that name is interpolated unescaped into the **company** dashboard (`company-dashboard-page.js`, "Umsatz nach Partner" chart) and executes in the company user's session. Same unescaped pattern for Kunde `name` (`partner-dashboard-page.js`) and `session.username` (`router.js`). Route all through `escapeHtml()` from `lib/html.js`.
- Neutralize CSV formula injection in the order export (`CsvExportService`). Partner-controlled names (`order.partner.name`, Kunde first/last name) are written into an Excel-targeted CSV (UTF-8 BOM, `;` separator); a value beginning with `= + - @` executes as a formula when the company opens the file. Prefix such cell values with a leading apostrophe.
- Stop leaking internals in import error messages (`ImportedFileParser`): do not concatenate `e.getMessage()` from POI/opencsv into client-facing text. Log the exception internally, return a fixed German message.
- Remove hardcoded production secrets from `application.properties` (`%prod`): DB credentials and the OIDC issuer become environment-variable overrides with defaults, so a packaged/demo run cannot silently ship the defaults.
- Document the product-image access decision (`ProductImageResource`): `GET /api/product-images/{id}` is readable by any authenticated partner for any id. Record the accepted risk (product images are non-sensitive) in a code comment; no behavioral change unless image confidentiality later matters.

No new dependencies. All user-facing text stays German with correct umlauts.

## Capabilities

### New Capabilities
- `security-hardening`: cross-cutting output-safety and configuration requirements — escaping user-controlled data before HTML rendering, keeping internal/library detail out of client-facing error messages, and sourcing secrets from the environment rather than hardcoding them.

### Modified Capabilities
- `data-export`: CSV exports must neutralize spreadsheet formula injection for user-controlled cell values.

## Impact

- Frontend: `frontend/src/pages/company-dashboard-page.js`, `frontend/src/pages/partner-dashboard-page.js`, `frontend/src/lib/router.js` (use existing `lib/html.js#escapeHtml`).
- Backend: `CsvExportService.java`, `ImportedFileParser.java`, `ProductImageResource.java`, `src/main/resources/application.properties` (`%prod`).
- No API contract changes, no schema changes, no new dependencies. Behavior change is limited to escaping/quoting output and reading secrets from env.
