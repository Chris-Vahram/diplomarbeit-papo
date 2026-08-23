## Context

Kunde (Endkunde) records are strictly scoped to the creating partner (`Kunde.ownerPartner`); the partner-facing endpoint `GET /api/partner/customers` (`customer/KundeResource.java`) resolves the caller via `CurrentPartner.resolve()` and returns `KundeDto` records via `KundeService.listForPartner()` — currently a plain `Kunde.list("ownerPartner", partner)`. Orders carry `Order.orderDate` (`LocalDate`, table `orders`) and reference both `partner` and `kunde`.

On the frontend there is no Endkunden list page at all: `/partner/customers` is consumed only by the Endkunde picker inside order-creation forms (`lib/order-form.js`, used by the dashboard quick-order, the order list and `partner-order-new-page.js`). The shared `data-table` component supports client-side search, select/daterange filters, pagination, row classes and HTML-escapes cell values by default — but has no column sorting. `lib/format.js` provides `formatDate()` (ISO → `dd.MM.yyyy`, de-AT) and `formatAddress()`. `styles/base.css` already has a row-state precedent (`.data-table__row--unseen`).

The project just went through an efficiency review removing N+1 queries; this change must not reintroduce any.

## Goals / Non-Goals

**Goals:**
- A partner sees, per own Endkunde, the date of the most recent order (or that none exists) and can quickly isolate Endkunden without recent orders.
- `lastOrderDate` is computed in exactly one partner-scoped SQL query (left join + aggregation), never per row.
- Kunde scoping stays airtight: a partner never sees another partner's Endkunden, before or after this change.
- German UI throughout, umlauts (ä/ö/ü/ß) render correctly, Kunde names pass through the existing `escapeHtml`.

**Non-Goals:**
- No company-side dormant view (partner-side only, per the 10.07.2026 meeting).
- No reminders or e-mails to dormant Endkunden (real e-mail stays deferred, B-04).
- No server-side dormancy flag, no threshold configuration UI — the threshold is a frontend constant.
- No Kunde editing/deleting on the new page; it is a read-only visibility feature.

## Decisions

**1. Extend the existing list endpoint instead of adding a new one.**
`GET /api/partner/customers` already returns everything the page needs except the date; the pickers that consume it today simply ignore the extra field. A separate `/dormant` endpoint would duplicate scoping logic and query code for no benefit. `KundeDto` gains a nullable `LocalDate lastOrderDate`; the existing `KundeDto.from(Kunde)` factory (used by order details and the create response) passes `null`, keeping every current caller compiling and behaving identically.

**2. One grouped left-join query in `KundeService.listForPartner()`.**
Replace the plain Panache list with a single JPQL query along the lines of

```
select k, max(o.orderDate)
from Kunde k left join Order o on o.kunde = k
where k.ownerPartner = :partner
group by k
```

mapping each result row to `KundeDto` with the aggregated date. Left join keeps Endkunden without any order in the result with `null` — arguably the most dormant case, and required by the meeting requirement. Scoping stays on `k.ownerPartner`; no join condition on `o.partner` is needed because a Kunde's orders can only ever belong to its owning partner (orders are created via `CurrentPartner.resolve()` or, on-behalf, for that same partner). Alternative considered: a second aggregate query keyed by kunde id merged in Java — more code, same single-query property, no advantage. Per-row `max()` lookups are ruled out (N+1). The company on-behalf picker (`/api/company/partners/{id}/customers` in `PartnerResource`) calls the same service method and simply carries the extra field; the grouped query costs the same order of magnitude as the plain list at this scale (~200 partners, Kunden per partner in the tens/hundreds).

**3. New page "Meine Endkunden" rather than bolting a table onto an existing page.**
No partner Endkunden page exists today, and the order pages are already dense. New `pages/partner-customer-list-page.js` (`area--partner`, same skeleton as `partner-product-list-page.js`), route `"/partner/customers": { tag: "partner-customer-list-page", role: "partner", title: "Meine Endkunden" }`, sidebar link "Endkunden" (lucide icon `contact`, `NAV_ICONS` entry) between "Meine Bestellungen" and "Produkte", lazy import in the router's `DOMContentLoaded` block. User-facing terminology is "Endkunde" (established by the 2026-07-08 change), not "Kunde".

**4. Table shape and null rendering.**
Columns: "Name" (last name + first name, default-escaped — this is where umlaut names like "Müller" or "Größbacher" must survive), "Adresse" (`formatAddress`), "Letzte Bestellung". The date column is an `html: true` column whose cell is built by the page from computed-only parts: `formatDate(lastOrderDate)` or the literal text "Noch keine Bestellung" for `null`, plus (when dormant) a small badge `<span class="dormant-badge">Inaktiv</span>`. No user-entered text enters this HTML cell, so `html: true` is safe; the Name column stays on the default-escape path. Search (`searchKeys`) covers the name; `pageSize` 20 like the other list pages.

**5. Dormancy filter via the existing `data-table` select-filter mechanism — no component change for filtering.**
The page derives a per-row field from `lastOrderDate` and today's date: `aktivitaet = "inaktiv"` when the last order is older than the threshold **or `null`**, else `"aktiv"`. A standard select filter (`label: "Aktivität"`, options "Länger keine Bestellung" / "Kürzlich bestellt", plus the built-in "Alle") then does the filtering with zero changes to `data-table`. Threshold: module constant `DORMANT_THRESHOLD_DAYS = 60` in the page file — **decided (Chris, 2026-07-13): 60 days for now, revisit with the client at the next Termin.** "Older than 60 days" is a strict boundary (exactly 60 days is not yet dormant). Deliberately client-side and trivial to change.

**5b. Dormancy computed on whole calendar days, not elapsed milliseconds.**
`lastOrderDate` is a date-only value (`LocalDate` → ISO `YYYY-MM-DD`). Compute dormancy by comparing **calendar days** — parse the ISO date to a date-only value and diff against today's date-only value (e.g. `Date.UTC(y,m,d)` day numbers), not `Date.now() - Date.parse(iso)` in milliseconds. Millisecond/elapsed math misclassifies boundary dates across timezones and DST (Codex finding). The same date-only computation drives both the filter and the badge, so they can never disagree.

**6. Sorting: page-local oldest-first default, no shared-component change (review-scoped down).**
The client ask is "see Endkunden who haven't ordered in a while" — satisfied by the dormant filter + the badge + an **oldest-first default order**. The page pre-sorts its rows ascending by `lastOrderDate` (null/never-ordered first) before handing them to `data-table`, so dormant Endkunden are on top immediately. Generalized clickable column sorting in the shared `data-table` was considered and **deferred** (Codex + Chris, 2026-07-13): it's broader than this requirement and adds regression surface to every table. If reusable sorting is wanted later, it's its own change (and would then need `aria-sort` + keyboard-activatable headers).

**7. Subtle dormant marker following the existing row-state pattern.**
Dormant rows get `rowClass: "data-table__row--dormant"` (muted background tint, styled next to `.data-table__row--unseen` in `base.css`) plus the "Inaktiv" badge in the date cell. Both are computed client-side from the same threshold as the filter, so marker and filter can never disagree.

## Risks / Trade-offs

- **[Grouped query changes the list's row mapping]** → The service method's return type/mapping changes from entity list to tuple mapping; covered by re-running the existing customer/order flows (picker still populates, create-Kunde response unchanged) in Verify.
- **[`KundeDto` gains a field visible to all consumers (order details, company picker)]** → Additive and nullable; JSON consumers ignore unknown/null fields. Explicitly checked in Verify that order details still render.
- **[Client-side "today" determines dormancy]** → A wrong client clock shifts the badge/filter, not the data. Accepted for a prototype; the authoritative fact (`lastOrderDate`) comes from the server.
- **[Threshold hardcoded at 60 days]** → Decided for now (Chris, 2026-07-13); changing it is a one-constant edit. Confirm the value with the client at the next Termin.
- **[Boundary/timezone misclassification]** → Mitigated by the date-only calendar-day computation (Decision 5b); Verify includes 59/60/61-day boundary cases.

## Open Questions

- **Exact dormancy threshold** — 60 days is the working default (decided 2026-07-13, not blocking). Confirm with the client whether their reorder cycle suggests 30/90 at the next Termin — one-line change.
- Should the company later get the same view per partner (explicitly out of scope now, would be a small follow-up change)?
