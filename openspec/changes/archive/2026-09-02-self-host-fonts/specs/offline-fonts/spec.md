## ADDED Requirements

### Requirement: Fonts load without an external host

The frontend SHALL load all fonts from self-hosted files bundled with the app; no request to an external font CDN may occur when a page is opened. The existing typography (body/display face and the monospace face used for amounts and order numbers) MUST render unchanged.

#### Scenario: Offline render

- **WHEN** the app is opened with no internet access, only the local stack reachable
- **THEN** every page renders with the intended fonts and the browser makes no request to an external font host

#### Scenario: Typography unchanged

- **WHEN** a page that previously used the CDN fonts is opened after the change
- **THEN** the body, headings, and monospace amounts look the same as before (same families, weights)
