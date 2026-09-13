## Purpose

Let the company maintain a controlled article category catalog while preserving existing article assignments and partner-specific visibility.

## ADDED Requirements

### Requirement: Company maintains categories
Only the company SHALL list all categories, create categories, rename them and change their active status. Each category SHALL have a stable identifier, a nonblank name of at most 255 characters and an active status. Names SHALL be unique after trimming and case normalization. Invalid input SHALL return 400, duplicate names 409 and unknown update identifiers 404 with German feedback. No deletion operation SHALL be offered.

#### Scenario: Create and rename
- **WHEN** the company creates a category and later renames it
- **THEN** the category is initially active, keeps its identifier and all assigned articles display the new name

#### Scenario: Duplicate or invalid name
- **WHEN** a category name is blank, too long or duplicates another name after normalization
- **THEN** saving is rejected without changing the catalog

#### Scenario: Unauthorized category management
- **WHEN** an anonymous caller or a partner calls any category-management endpoint
- **THEN** the request is rejected with 401 or 403 respectively without changing data

### Requirement: Inactive categories retain existing assignments
Deactivation SHALL preserve existing article assignments and SHALL NOT change article availability or partner enablements. New assignments to inactive categories SHALL be rejected. An unchanged inactive assignment SHALL remain valid when editing another article field. Reactivation SHALL be possible.

#### Scenario: Deactivate assigned category
- **WHEN** the company deactivates a category already assigned to articles
- **THEN** those assignments remain readable and enabled available articles remain orderable

#### Scenario: Preserve or newly assign inactive category
- **WHEN** an article keeps its current inactive category while another field is edited
- **THEN** that edit succeeds
- **AND** assigning the inactive category to another article is rejected

### Requirement: Existing free text is migrated without ambiguous merging
Existing nonblank category text SHALL be mapped to category records and article assignments before the new category behavior becomes available. Empty values SHALL remain unassigned. Ambiguous normalized name collisions or inconsistent existing assignments SHALL stop the migration without partial changes. Repeating a completed migration SHALL preserve identifiers and assignments.

#### Scenario: Existing article categories
- **WHEN** existing articles contain identical category labels and optional empty values
- **THEN** identical trimmed labels share a category and empty values remain unassigned

#### Scenario: Conflicting legacy labels
- **WHEN** distinct legacy spellings normalize to the same category identity
- **THEN** migration stops with a diagnostic instead of silently merging them or partially updating articles

#### Scenario: Repeated migration
- **WHEN** migration runs again after successful completion
- **THEN** no duplicate category or changed assignment is produced

#### Scenario: Removed assignment remains removed
- **WHEN** the company clears an article category and the application subsequently restarts
- **THEN** migration does not restore the removed assignment from legacy data

#### Scenario: Unicode normalization expansion
- **WHEN** a valid category name expands during case normalization
- **THEN** normalization does not cause a storage failure within the accepted name length

### Requirement: Partner visibility stays limited
Partners SHALL derive category choices only from articles enabled for them. The new catalog SHALL NOT expose global categories or counts of another partner's articles to partners.

#### Scenario: Another partner's category
- **WHEN** a category occurs only on articles enabled for a different partner
- **THEN** it is not offered by the caller's article filters through the new catalog
