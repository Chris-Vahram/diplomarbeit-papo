## ADDED Requirements

### Requirement: Last order date per end customer
The partner's end-customer list (`GET /api/partner/customers`) SHALL include, per Endkunde, the date of that Endkunde's most recent order as a nullable `lastOrderDate`. An Endkunde with no orders SHALL have `lastOrderDate = null`. The value SHALL be produced by a single bounded, partner-scoped query with no per-Endkunde lookups (no N+1), and the endpoint SHALL continue to resolve the calling partner server-side (`CurrentPartner.resolve()`), never from a client-supplied partner id. (The specific query shape is a design/implementation detail — see design.md.)

#### Scenario: Endkunde with orders
- **WHEN** a partner requests their end-customer list and an Endkunde has one or more orders
- **THEN** that Endkunde's entry carries the date of the most recent of those orders as `lastOrderDate`

#### Scenario: Endkunde without any order
- **WHEN** a partner requests their end-customer list and an Endkunde has never been ordered for
- **THEN** that Endkunde is still included in the response with `lastOrderDate = null`

#### Scenario: Scoping unchanged
- **WHEN** a partner requests their end-customer list
- **THEN** only Endkunden created by that partner are returned, and the last-order dates reflect only that partner's data

### Requirement: Partner end-customer overview page
The portal SHALL provide a partner-only page "Meine Endkunden" (route `#/partner/customers`, sidebar entry "Endkunden") listing the partner's own Endkunden with name, address, and a "Letzte Bestellung" column. The date SHALL be shown in German format (`dd.MM.yyyy`); an Endkunde without orders SHALL show "Noch keine Bestellung" instead of a date. The list SHALL default to oldest-first ordering (Endkunden without any order first, then oldest to newest last order), so dormant Endkunden surface at the top. Endkunden-name text SHALL be HTML-escaped on output.

#### Scenario: Page shows last order dates
- **WHEN** a partner opens "Meine Endkunden"
- **THEN** each own Endkunde appears with its last order date formatted as `dd.MM.yyyy`, and Endkunden without orders show "Noch keine Bestellung"

#### Scenario: Dormant Endkunden surface first
- **WHEN** the partner opens "Meine Endkunden"
- **THEN** Endkunden without any order appear first, followed by the remaining Endkunden from oldest to newest last order

#### Scenario: Umlaut names render correctly
- **WHEN** an Endkunde is named with German special characters (e.g. "Größbacher", "Müller-Lüdenscheidt")
- **THEN** the name renders with correct umlauts/ß in the table, in search results, and in the filtered dormant view, with no broken characters and no HTML injection

#### Scenario: Deep-link protection
- **WHEN** an unauthenticated user or a company user navigates to `#/partner/customers` directly
- **THEN** the router redirects them away, consistent with the existing role-based route protection

### Requirement: Dormant end-customer filter and marker
The "Meine Endkunden" page SHALL let the partner filter to Endkunden without a recent order: an Endkunde counts as dormant when its last order is strictly older than a fixed client-side threshold (default 60 days) or when it has no orders at all. Dormancy SHALL be computed on whole calendar days (date-only), not elapsed milliseconds, so boundary dates are not misclassified by timezone/DST. Dormant rows SHALL additionally carry a subtle visual marker (badge "Inaktiv" and row highlight) that uses the same threshold and computation as the filter.

#### Scenario: Filter shows dormant Endkunden
- **WHEN** the partner activates the dormant filter ("Länger keine Bestellung")
- **THEN** only Endkunden whose last order is older than the threshold, plus Endkunden with no orders at all, remain visible

#### Scenario: Dormant marker matches the filter
- **WHEN** an Endkunde is dormant per the threshold
- **THEN** its row shows the "Inaktiv" badge and the dormant row highlight, and it appears in the dormant filter result; a non-dormant Endkunde shows neither
