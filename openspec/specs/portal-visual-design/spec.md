# portal-visual-design Specification

## Purpose
Visual consistency of the internal portal: one role accent with a strong step for white text, one button system, readable lists on phones and desktop, shared loading/empty/error states, an accessibility floor and accents for dashboards and the order wizard (source: change refine-portal-frontend, decisions in `DESIGN.md`).

## Requirements

### Requirement: Single role accent
Every role-aware element of the internal portal SHALL take its colour from the generic `--color-accent` tokens set once per area (company green, partner blue). No component SHALL reference a role palette directly. Fills behind white text (primary buttons, active navigation item, completed wizard steps) SHALL use the strong accent step so that the text reaches at least 4.5:1.

#### Scenario: Partner area colours
- **WHEN** a partner opens any page
- **THEN** primary buttons, active navigation and links use the partner accent without page-specific colour rules

#### Scenario: Primary button contrast
- **WHEN** a primary button or the active navigation item is shown in either area
- **THEN** its white text has a contrast of at least 4.5:1 against the fill

### Requirement: Consistent button system
The portal SHALL offer primary, secondary, ghost and danger buttons with uniform size, radius and focus style, plus one larger hero size for the single main action of a page.

#### Scenario: Main action stands out
- **WHEN** a page has one main action (for example "Neue Bestellung")
- **THEN** it uses the hero size and all other actions use the regular sizes

### Requirement: Readable lists on phones
Below 640 px viewport width the main lists (orders, end customers, partners, articles) SHALL show each row as a compact card (main field and amount on the first line, date or secondary field and status on the second) instead of horizontal scrolling; fields whose meaning is not obvious from their content SHALL keep a short label. Search, filters, sorting, pagination and row actions SHALL keep working.

#### Scenario: Order list on a phone
- **WHEN** a user opens the order list at 390 px width
- **THEN** each order is a card showing end customer and value on the first line and date and status on the second, and no horizontal page or table scroll is needed

### Requirement: Calm desktop lists
From 640 px viewport width the main lists SHALL keep their table layout but use the same emphasis as the phone cards: a calm header without tint, the main field in bold, amounts right-aligned in bold with their unit, and dates in secondary text. Row actions SHALL wrap instead of overflowing the table.

#### Scenario: Order list on desktop
- **WHEN** a user opens an order list at 1280 px width
- **THEN** the end customer is bold, the value is right-aligned and bold with "€", the date is muted, and the table needs no horizontal scroll

### Requirement: Shared loading, empty and error states
Pages SHALL show loading, empty and error states with the same visual pattern and German text; error states SHALL offer a retry where the action is safe to repeat, and empty states SHALL offer the page's main action where one exists.

#### Scenario: List cannot be loaded
- **WHEN** a list request fails
- **THEN** the page shows the shared error state with a German message and a "Erneut laden" action that only repeats the read request

#### Scenario: Partner has no orders yet
- **WHEN** a partner opens the order list without any orders
- **THEN** the empty state explains that orders appear there and offers "Neue Bestellung"

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
- **THEN** step 1 shows a check mark, step 2 a highlighted ring with a bold label and the text "Schritt 2 von 4", and the remaining steps an outlined number
