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
