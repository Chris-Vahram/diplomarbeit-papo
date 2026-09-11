## ADDED Requirements

### Requirement: Controlled article category assignment
Company article creation and editing SHALL support one optional category identifier. Article responses SHALL retain the category display name and add its identifier. Legacy category-name writes SHALL resolve only known categories; simultaneous nonnull category name and identifier SHALL be rejected with 400. Omitting or clearing both SHALL remove the optional assignment under the existing full-update contract.

#### Scenario: Category selected by identifier
- **WHEN** the company saves an article with a known active category identifier
- **THEN** the article response contains that identifier and the current category name

#### Scenario: Legacy name compatibility
- **WHEN** a legacy client submits only a known category name
- **THEN** the same controlled assignment rules apply without creating a category

#### Scenario: Conflicting or unknown input
- **WHEN** both category inputs are nonnull or the selected category is unknown
- **THEN** the API rejects the request with 400 and no article change

#### Scenario: Category removed
- **WHEN** the company submits an article without a category
- **THEN** the article remains valid and its category fields are null

#### Scenario: Concurrent deactivation
- **WHEN** article assignment races with deactivation of the selected category
- **THEN** assignment either commits while the category is active or is rejected after deactivation, never bypassing the active-state rule
