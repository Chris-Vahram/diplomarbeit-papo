## ADDED Requirements

### Requirement: Single role accent
Every role-aware element of the internal portal SHALL take its colour from the generic `--color-accent` tokens set once per area (company green, partner blue). No component SHALL reference a role palette directly.

#### Scenario: Partner area colours
- **WHEN** a partner opens any page
- **THEN** primary buttons, active navigation and links use the partner accent without page-specific colour rules

### Requirement: Consistent button system
The portal SHALL offer primary, secondary, ghost and danger buttons with uniform size, radius and focus style, plus one larger hero size for the single main action of a page.

#### Scenario: Main action stands out
- **WHEN** a page has one main action (for example "Neue Bestellung")
- **THEN** it uses the hero size and all other actions use the regular sizes

### Requirement: Readable lists on phones
Below 640 px viewport width the main lists (orders, end customers, partners, articles) SHALL show each row as a stacked card with labelled fields instead of horizontal scrolling. Search, filters, sorting, pagination and row actions SHALL keep working.

#### Scenario: Order list on a phone
- **WHEN** a user opens the order list at 390 px width
- **THEN** each order is a card with labelled date, end customer, value and status, and no horizontal page or table scroll is needed

### Requirement: Shared loading, empty and error states
Pages SHALL show loading, empty and error states with the same visual pattern and German text; error states SHALL offer a retry where the action is safe to repeat.

#### Scenario: List cannot be loaded
- **WHEN** a list request fails
- **THEN** the page shows the shared error state with a German message and a "Erneut laden" action that only repeats the read request

### Requirement: Accessibility floor
The portal SHALL keep visible keyboard focus on all interactive elements, a text contrast of at least 4.5:1, touch targets of at least 44 px on mobile, and SHALL respect `prefers-reduced-motion`.

#### Scenario: Keyboard navigation
- **WHEN** a user tabs through any page
- **THEN** every focused control shows a visible focus indicator

### Requirement: Metric cards without empty decoration
Metric cards SHALL never show an empty icon area: without a trend they show a neutral icon.

#### Scenario: No comparison data
- **WHEN** a metric has no previous-period comparison
- **THEN** the card shows its neutral icon instead of an empty circle

### Requirement: Dashboard and wizard accents
Each dashboard SHALL give one lead metric visible priority, and the order wizard SHALL distinguish completed, current and upcoming steps without relying on colour alone.

#### Scenario: Wizard progress
- **WHEN** a partner is on step 2 of the order wizard
- **THEN** step 1 shows as completed, step 2 as current and the remaining steps as upcoming, each with a non-colour indicator
