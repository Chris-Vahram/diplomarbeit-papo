## ADDED Requirements

### Requirement: Public landing page at the root URL
The system SHALL serve a static, public landing page at `/` that explains PAPO to prospective users in German with „Sie“-Anrede. The page SHALL address companies first (problem with orders by e-mail and telephone, the chain Unternehmen → Partner → Endkunde, company functions such as order handling with status, partner and commission management, article enablement, Excel/CSV import and dashboard), then partners (selecting or creating an end customer, ordering several articles, own orders and commission), and SHALL present „Mein Webshop“ as an existing function. The page SHALL be a single page with in-page anchors and no sub-pages.

#### Scenario: First visit to the domain
- **WHEN** a visitor without a portal session opens `/`
- **THEN** the landing page is shown instead of the login form
- **AND** it contains sections for companies, partners and „Mein Webshop“, in that order

#### Scenario: Readable without JavaScript
- **WHEN** a visitor opens `/` with JavaScript disabled
- **THEN** all texts, images and links of the landing page are visible and usable

### Requirement: Login is the only call to action
The landing page SHALL offer „Anmelden“ as its only account action, linking to `/portal/#/login`. It SHALL NOT offer registration, an access request or demo credentials.

#### Scenario: Visitor wants to log in
- **WHEN** a visitor activates „Anmelden“ on the landing page
- **THEN** the browser opens the portal login at `/portal/#/login`

#### Scenario: No self-service account creation
- **WHEN** a visitor reads the landing page
- **THEN** no registration form, „Registrieren“ link or demo login name or password is shown

### Requirement: Existing portal session skips the landing page
When the visitor's browser tab holds a stored portal session, the landing page SHALL forward to `/portal/` so that daily users are not held up; the portal then routes to the role's dashboard or, if the session can no longer be refreshed, to the login.

#### Scenario: Logged-in user returns to the domain in the same tab
- **WHEN** a user with a stored portal session in the current tab opens `/`
- **THEN** the browser is forwarded to `/portal/` and the portal shows the user's dashboard

#### Scenario: Expired session
- **WHEN** a user whose stored session can no longer be refreshed opens `/`
- **THEN** the browser is forwarded to `/portal/` and the portal shows the login

### Requirement: Only real product content
The landing page SHALL show only real content: screenshots SHALL be generated from the demo data of the actual application, and the page SHALL NOT show invented customers, testimonials, figures, references or legal statements. Screenshots SHALL be reproducible by a versioned script.

#### Scenario: Screenshots are regenerated
- **WHEN** a developer runs the screenshot script against a local environment with demo data
- **THEN** the landing images under `frontend/assets/landing/` are recreated from the running application

#### Scenario: No invented social proof
- **WHEN** the landing page is reviewed
- **THEN** it contains no customer logos, testimonials, usage numbers or ratings

### Requirement: Hint for end customers
The landing page SHALL contain a short hint that end customers order in their partner's shop and receive the shop link from their partner.

#### Scenario: End customer lands on the domain
- **WHEN** an end customer reads the landing page
- **THEN** a short hint explains that the shop link is provided by the partner and that the portal login is not meant for end customers

### Requirement: Footer with project note and legal links
The landing page, the legal pages and the not-found page SHALL share a footer containing a note that PAPO is an HTL diploma project, a contact e-mail address and links to „Impressum“ and „Datenschutz“.

#### Scenario: Visitor looks for the operator
- **WHEN** a visitor scrolls to the footer of the landing page
- **THEN** the diploma-project note, the contact address and links to `/impressum` and `/datenschutz` are shown

### Requirement: Imprint and privacy pages
The system SHALL serve static pages `/impressum` (Austrian disclosure under § 5 ECG and § 25 MedienG) and `/datenschutz` (information under Art. 13 DSGVO) for the landing page and the portal. The privacy page SHALL describe at least the reverse-proxy access logs, the self-hosted fonts without third-party requests, the browser storage of the login session and of the dismissed bookmark hint, the Keycloak login, the data-subject rights and the right to complain to the Austrian data protection authority. Facts that are not yet known (media owner, controller, postal address, contact e-mail, hosting provider and location, log retention period) SHALL appear as clearly marked placeholders in the form `[[NAME]]`. The shops' own legal pages are not affected.

#### Scenario: Visitor opens the imprint
- **WHEN** a visitor opens `/impressum`
- **THEN** a German imprint page with the PAPO footer is shown

#### Scenario: Placeholders are recognisable
- **WHEN** a required fact has not been provided yet
- **THEN** the page shows the marked placeholder instead of an invented value

#### Scenario: Placeholders block release
- **WHEN** the change is prepared for production release
- **THEN** a check lists every remaining `[[…]]` placeholder in the public pages so none goes live unnoticed

### Requirement: Not-found page
Requests for unknown paths outside the portal, the shops, the API and Keycloak SHALL receive a static German not-found page with HTTP status 404 and a link to the landing page.

#### Scenario: Unknown path
- **WHEN** a visitor opens `/gibt-es-nicht`
- **THEN** the response has status 404 and shows the not-found page with a link to `/`

#### Scenario: Shop paths keep their own handling
- **WHEN** a visitor opens `/shop/unbekannt`
- **THEN** the shop entry handles the request as before and the PAPO not-found page is not shown

### Requirement: Search-engine indexing rules
The landing page and the legal pages SHALL be indexable; the portal, the shops and the not-found page SHALL carry `noindex`. A `robots.txt` SHALL be served at `/robots.txt`.

#### Scenario: Crawler visits the domain
- **WHEN** a search-engine crawler requests `/`, `/impressum` and `/portal/`
- **THEN** the landing and imprint pages carry no `noindex`, while the portal page does

### Requirement: Public pages follow the design system
The public pages SHALL use the PAPO design tokens with a neutral brand accent documented in `DESIGN.md` instead of the company green or partner blue, use only self-hosted fonts, use the word mark „PAPO“ and the page title prefix „PAPO“, work at phone width without horizontal scrolling and meet the accessibility floor of the portal (WCAG 2.2 AA contrast, visible focus, 44 px touch targets, `prefers-reduced-motion`). Light mode only, like the portal.

#### Scenario: Phone visitor
- **WHEN** the landing page is opened at 360 px width
- **THEN** all content is readable without horizontal scrolling and „Anmelden“ is a touch target of at least 44 px

#### Scenario: Reduced motion
- **WHEN** the visitor's system requests reduced motion
- **THEN** the landing page shows no non-essential animation
