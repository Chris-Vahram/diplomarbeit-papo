## Context

Company-side lists (orders, commissions) live in the portal; the client's accounting workflow lives in Excel. opencsv is already a dependency (import). `apiFetch` (`lib/api.js`) attaches the Keycloak Bearer token and only handles JSON responses. Frontend (:3000) calls the backend (:8080) cross-origin with CORS.

## Goals / Non-Goals

**Goals:**
- Downloadable CSV of all orders and of all commission records, company role only.
- Correct ä/ö/ü/ß rendering when opened in Excel (Austrian client — hard requirement).
- Downloads work with Bearer-token auth (no 401/logout on click).

**Non-Goals:**
- No e-mail delivery (B-04 deferred), no scheduled exports, no XLSX output (CSV opens in Excel fine), no partner-side export.
- No commission *settlement/payout* logic (B-07) — flat record export only.

## Decisions

**opencsv `CSVWriter` for output (already a dependency).** Handles quoting/escaping edge cases (commas, quotes, line breaks in Kunde/partner names) correctly — safer than hand-rolled string joining. Wrap an `OutputStreamWriter` (UTF-8); write the BOM (`﻿`) to the stream **before** the `CSVWriter` so Excel detects UTF-8 and renders umlauts instead of mojibake. Semicolon vs comma separator: use `;` — German-locale Excel expects it (same convention the import parser sniffs for).

**Two distinct exports, not one.** `OrderDto` already carries `commissionPercentUsed`/`commissionValue`, so the order CSV includes commission columns. The commission CSV is deliberately commission-centric: only orders that *have* a `CommissionRecord`, columns Partner, Bestell-Nr, Basiswert, %-Satz, Provision — a settlement-ready sheet rather than an order log. (Decided with Chris 2026-07-04.)

**Endpoints:** `GET /api/company/orders/export` and `GET /api/company/commissions/export`, both `@RolesAllowed("company")`, `@Produces("text/csv;charset=UTF-8")`. Commission export fetches all `CommissionRecord`s in one query (no N+1).

**Authenticated blob download client-side.** A bare `<a href>`/`window.location` sends no Authorization header → 401 → forced logout; `apiFetch` can't return CSV (JSON-only). New `apiDownload(path, filename)` in `lib/api.js`: authenticated `fetch` → `response.blob()` → `URL.createObjectURL` → temporary `<a download=filename>` → click → `revokeObjectURL`; 401 routes through the existing logout path. Filename set client-side (`bestellungen.csv`, `provisionen.csv`) — no server `Content-Disposition` needed, which also sidesteps CORS-exposing that header cross-origin.

## Risks / Trade-offs

- **[Risk]** Excel mangles umlauts without BOM → **Mitigation**: BOM written before the writer; round-trip test asserts ä/ö/ü/ß survive; browser-verify opens the file.
- **[Risk]** Semicolon separator surprises non-German tooling → **Mitigation**: accepted — target consumer is the Austrian client's Excel; documented in the endpoint comment.
- **[Trade-off]** Whole export built in memory (no streaming) — fine at demo scale, consistent with the rest of the app.

## Migration Plan

No migration. Feature branch after `add-company-dashboard` merges (settlement button placement). Additive only.

## Open Questions

None.
