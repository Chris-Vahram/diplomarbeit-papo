## ADDED Requirements

### Requirement: One shop record per partner
The system SHALL hold at most one `PartnerShop` per partner. A shop record SHALL be created on first company action or first partner visit of "Mein Webshop"; a new shop SHALL start as not released.

#### Scenario: First visit creates unreleased shop
- **WHEN** a partner without a shop opens "Mein Webshop"
- **THEN** the system creates one shop record with status "nicht freigegeben" and a suggested identifier

#### Scenario: No second shop
- **WHEN** a second shop would be created for the same partner
- **THEN** the system keeps the existing record and creates no duplicate

### Requirement: Shop identifier
Each shop SHALL have a platform-unique identifier of 3 to 40 characters consisting of lowercase letters a–z, digits and single hyphens, not starting or ending with a hyphen. The system SHALL suggest it from the partner name (umlauts transliterated, e.g. "ä" → "ae") and append a number if taken. Only the company SHALL set or change it. Reserved words (e.g. `api`, `auth`, `admin`, `shop`, `static`) SHALL be rejected. The partner number SHALL NOT be used as identifier source.

#### Scenario: Suggestion from partner name
- **WHEN** a shop is created for partner "Huemer Elektro GmbH"
- **THEN** the suggested identifier is "huemer-elektro-gmbh"

#### Scenario: Collision
- **WHEN** the suggested identifier already exists
- **THEN** the system suggests the identifier with a numeric suffix, e.g. "huemer-elektro-gmbh-2"

#### Scenario: Partner cannot change identifier
- **WHEN** a partner attempts to change the shop identifier
- **THEN** the system rejects the request with 403

#### Scenario: Invalid identifier
- **WHEN** the company enters "Huemer Shop!" or a reserved word
- **THEN** the system rejects it with an understandable German validation message

### Requirement: Company release and takedown
The company SHALL release a shop once before it becomes publicly reachable. The company SHALL be able to take a released shop offline at any time and MUST enter a reason (10–500 characters). The partner SHALL see the reason. The company SHALL be able to release the shop again, which clears the reason.

#### Scenario: Release
- **WHEN** the company releases a not-released shop
- **THEN** the shop status becomes "freigegeben" and the public route becomes reachable, subject to the automatic offline rules

#### Scenario: Takedown requires reason
- **WHEN** the company takes a shop offline without a reason
- **THEN** the system rejects the action

#### Scenario: Partner sees reason
- **WHEN** a partner opens "Mein Webshop" for a shop taken offline
- **THEN** the page shows "Vom Unternehmen offline genommen" and the entered reason

### Requirement: Automatic offline rules
A released shop SHALL be publicly offline while its partner is inactive or has no enabled and available article. It SHALL become reachable again automatically when the condition ends.

#### Scenario: Inactive partner
- **WHEN** the company deactivates a partner with a released shop
- **THEN** the public shop shows the neutral page "Dieser Shop ist derzeit nicht erreichbar." and HTTP 404 for API calls

#### Scenario: No enabled articles
- **WHEN** a released shop's partner has no enabled available article
- **THEN** the public shop is offline and the partner sees the hint "Keine freigeschalteten Artikel – Shop ist offline."

### Requirement: Partner shop status page
The partner SHALL see in "Mein Webshop" the shop identifier, the public link, and the status (nicht freigegeben, online, offline with cause).

#### Scenario: Live shop
- **WHEN** a partner with a released, reachable shop opens "Mein Webshop"
- **THEN** the page shows status "Online" and the clickable public link

### Requirement: Company shop overview per partner
The company SHALL see and manage the shop of each partner in the partner management (identifier, status, release, takedown with reason). The partner table SHALL show each partner's shop status at a glance through a "Webshop" button in the status's signal colour; the status of all partners SHALL be loaded in one request.

#### Scenario: Company manages shop
- **WHEN** the company selects "Webshop" for a partner in the partner management
- **THEN** a "Webshop" dialog shows identifier, status and the actions allowed in that status

#### Scenario: Status at a glance
- **WHEN** the company opens the partner management
- **THEN** every partner row shows a "Webshop" button coloured by that partner's shop status, and a partner without a shop record counts as "Nicht freigegeben"

### Requirement: Shop status signal colours
Shop status SHALL be shown with signal colours consistent with the order status badges: green for "Online" (as "Abgeschlossen"), yellow for the deliberately offline status "Nicht freigegeben" (as "Eingereicht"), red for every unintended offline status (taken offline by the company, partner inactive, no available articles). The colour SHALL never be the only carrier of the status; a German status text SHALL be present.

#### Scenario: Unintended offline is red
- **WHEN** a released shop is offline because its partner has no available article
- **THEN** the partner page, the company dialog and the partner table show the status in red together with the text "Offline – keine Artikel"

#### Scenario: Not released is yellow
- **WHEN** a shop has not been released yet
- **THEN** its status is shown in yellow with the text "Nicht freigegeben"
