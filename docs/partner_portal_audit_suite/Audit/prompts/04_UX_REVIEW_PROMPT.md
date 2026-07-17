# Partner Portal - UX, Desktop, Mobile, Accessibility, and Print Review

## Objective

Review the usability and visual quality of the Partner Portal features that are expected to remain in the MVP.

Focus on practical usability for non-technical partners.

This is a read-only review. Do not modify production code.

## Required Context

Read:

1. `Specification/CONTINUATION_PROMPT.md`
2. `CLAUDE.md`
3. the latest meeting protocol
4. `Audit/01_PROJECT_OVERVIEW_REPORT.md`
5. `Audit/02_SCOPE_REVIEW_REPORT.md`
6. `Audit/03_TECHNICAL_REVIEW_REPORT.md`
7. `Design/variables.css`
8. `frontend/src/styles/base.css`
9. relevant frontend files and recent diffs

Treat previous reports as structured input, not as unquestionable truth.

Do not spend deep review time on features clearly marked for removal or deferral unless they affect shared components.

## Local Review Setup

Start the local environment using documented project instructions.

Respect known local constraints:

- port `5432` conflicts
- Keycloak startup behavior
- Playwright bfcache issues
- use `about:blank` before revisiting the same page where necessary

Use documented local demo accounts only.

Do not expose credentials in the report.

## Review These Screens and Flows

At minimum:

- login and authentication-related screens affected by recent changes
- customer list
- dormant customer state
- product catalog
- cart
- `/partner/orders/new`
- order submission feedback
- order history
- order filters
- order document or print view
- product image gallery

## Viewports

Test at:

- 1920 x 1080
- 1440 x 900
- 390 x 844

Desktop is the primary business usage mode and must receive equal or greater attention than mobile.

## Check For

### Desktop

- excessive stretching
- missing max-width constraints
- unusable empty space
- over-wide forms
- over-wide tables
- broken card grids
- poor alignment
- awkward centering
- inconsistent spacing
- poor information hierarchy

### Mobile

- horizontal scrolling
- clipped values
- awkward text wrapping
- small touch targets
- controls that are difficult to use
- hidden or inaccessible actions
- amounts or commission previews being cut off
- broken forms
- modal or drawer problems

### General UX

- unclear labels
- hidden actions
- unnecessary steps
- missing loading feedback
- missing success feedback
- weak error messages
- inconsistent button hierarchy
- poor empty states
- confusing validation
- actions that are risky or easy to trigger accidentally
- confusing behavior for non-technical users

### Design Consistency

- hardcoded colors
- hardcoded spacing
- design-token violations
- inconsistent typography
- inconsistent button styles
- inconsistent card styles
- inconsistent form controls
- visual regressions introduced by recent changes

### Accessibility

- missing labels
- keyboard navigation problems
- weak focus behavior
- contrast concerns
- inaccessible modals
- inaccessible controls
- unclear validation associations

### Print

- multi-page behavior
- page breaks
- totals separated from labels
- long customer names
- long product names
- umlauts
- hidden interactive elements
- duplicate or broken cloned elements

## Finding Classification

Classify every UX finding as one of:

- blocks task completion
- creates confusion
- causes avoidable friction
- visual inconsistency
- accessibility issue
- optional polish

For every finding include:

- ID
- severity
- confidence
- viewport
- page or flow
- evidence
- user impact
- recommended direction
- suggested owner
- whether it should be fixed before the next meeting, before feature freeze, before submission, or after MVP

## Output

Write the report to:

`Audit/04_UX_REVIEW_REPORT.md`

Use this structure:

1. UX verdict
2. Desktop findings
3. Mobile findings
4. Accessibility findings
5. Print findings
6. Design-system inconsistencies
7. Highest-value UX improvements
8. Evidence and limitations

Do not fix code.
