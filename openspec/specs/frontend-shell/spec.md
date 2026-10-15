# frontend-shell Specification

## Purpose
Define frontend bootstrap behavior that keeps shared Web Components reliable across direct loads, reloads, and in-app navigation.

## Requirements

### Requirement: Shared components are registered before any page renders

The frontend SHALL register all shared Web Components (including `<data-table>`) before the router mounts the first page, so that pages always assign properties to upgraded elements and never hit custom-element property shadowing.

#### Scenario: Direct URL load of a list page

- **WHEN** a user opens a list route (e.g. `#/partner/customers` or `#/partner/orders`) via direct URL entry, bookmark, or hard reload (F5)
- **THEN** the page renders its complete table — column headers, cell contents, filters, and search — identical to the result of SPA navigation

#### Scenario: SPA navigation remains unchanged

- **WHEN** a user navigates between pages via the sidebar without a full page load
- **THEN** list pages continue to render completely (no regression from the bootstrap-ordering change)

### Requirement: Partner company name in portal navigation
The partner portal SHALL show “Partnerportal der Firma <name>” in its left portal label using the authenticated partner's own profile name. The name SHALL be rendered as text and SHALL NOT act as an authorization source or a verified legal identity.

#### Scenario: Own partner name is shown
- **WHEN** a partner's profile loads successfully
- **THEN** the left portal label shows that partner's company name

#### Scenario: Logout or stale response
- **WHEN** a profile request completes after logout, a session change or a superseding navigation
- **THEN** the old response does not restore a former partner's name

#### Scenario: Profile cannot be displayed safely
- **WHEN** loading fails or the response belongs to an outdated session
- **THEN** the label stays neutral and a German message offers a read-only retry without showing a previous name

#### Scenario: Name contains markup
- **WHEN** the own profile name contains HTML-like text or German special characters
- **THEN** the characters are displayed as text without execution or broken layout

### Requirement: Company article navigation is grouped

The company area SHALL expose one primary navigation entry named „Artikelverwaltung“. Article-category management SHALL NOT appear as a separate primary navigation entry. Partner navigation SHALL use the possessive entries „Meine Endkunden“ and „Meine Artikel“.

#### Scenario: Company opens navigation
- **WHEN** a company user views the sidebar
- **THEN** one primary entry „Artikelverwaltung“ is shown
- **AND** no separate primary entry „Artikelkategorien“ is shown

#### Scenario: Partner opens navigation
- **WHEN** a partner views the sidebar
- **THEN** the entries „Meine Endkunden“ and „Meine Artikel“ are shown instead of „Endkunden“ and „Artikel“
- **AND** the partner article page title reads „Meine Artikel“
