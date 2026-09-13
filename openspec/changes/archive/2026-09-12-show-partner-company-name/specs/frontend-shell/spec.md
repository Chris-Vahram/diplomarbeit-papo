## ADDED Requirements

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
