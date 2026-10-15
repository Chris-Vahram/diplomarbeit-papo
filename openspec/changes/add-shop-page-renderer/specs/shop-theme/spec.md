## ADDED Requirements

### Requirement: Theme colours
Each shop SHALL have one theme with freely chosen colours for primary, accent, background, surface and text (any valid hex colour). The theme SHALL apply to all blocks by default.

#### Scenario: Theme applied
- **WHEN** the published theme has primary colour #0A5C36
- **THEN** all blocks without override render their primary elements in #0A5C36

### Requirement: Block colour override and reset
A block SHALL be able to override individual theme colours. Removing the override SHALL make the block follow the theme again.

#### Scenario: Reset to theme
- **WHEN** a block's override is removed
- **THEN** the block renders with the current theme colours

### Requirement: Curated self-hosted fonts
Fonts SHALL be selectable only from a curated list of self-hosted fonts with open licences (heading font and body font). The shop SHALL NOT load fonts or other resources from third-party servers.

#### Scenario: No external font request
- **WHEN** a visitor opens any shop page
- **THEN** no request is made to a font or CDN host outside the PAPO domain

#### Scenario: Unknown font rejected
- **WHEN** a theme save references a font not in the curated list
- **THEN** the system rejects the save

### Requirement: Theme draft and publication
The theme SHALL have a draft and a published version. Publishing the theme SHALL apply it to all online pages at once; block overrides SHALL be preserved.

#### Scenario: Publishing theme updates all pages
- **WHEN** the theme draft is published
- **THEN** every online page renders with the new theme, and blocks with overrides keep their overridden colours
