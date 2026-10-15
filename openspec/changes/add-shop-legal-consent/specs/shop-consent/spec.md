## ADDED Requirements

### Requirement: Consent banner
On the first visit the shop SHALL show a consent banner with the options "Alle akzeptieren" and "Nur notwendige" presented with equal prominence, plus "Einstellungen" listing the categories "Notwendig" (always on: cart, consent decision) and "Statistik" (off by default).

#### Scenario: Equal choice
- **WHEN** the banner is shown
- **THEN** both buttons have the same size and visual weight and neither is preselected

### Requirement: No optional processing before consent
Before consent the shop SHALL NOT store or send any data belonging to optional categories. Technically necessary storage (cart, consent decision) SHALL be allowed.

#### Scenario: Declined
- **WHEN** the visitor chooses "Nur notwendige"
- **THEN** no visitor-recognition identifier is created or sent

### Requirement: Storage and revocation of consent
The consent decision SHALL be stored in the browser with consent version and date. The visitor SHALL be able to change it at any time via "Cookie-Einstellungen" in the footer. A new consent version SHALL show the banner again.

#### Scenario: Revoke
- **WHEN** the visitor withdraws statistics consent
- **THEN** the stored identifier is deleted and no further optional data is sent
