# shop-inquiries Specification

## Purpose
Inquiries from the public partner shop (B-05): a fixed "Anfrageformular" block, a path from an article page to it and back, spam protection without external services, and an inbox where only the owning partner reads, completes and deletes inquiries. No IP address and no invented retention period.
## Requirements
### Requirement: Inquiry form block
The builder SHALL offer a block "Anfrageformular" with configurable heading and intro text. The form fields SHALL be fixed: Name (required), E-Mail, Telefon, Nachricht (required, max. 2000 characters), optional article selection from the shop catalogue, and a required consent checkbox linking to the privacy page.

#### Scenario: Neither e-mail nor phone
- **WHEN** a visitor submits without e-mail and without phone
- **THEN** the form shows "Bitte E-Mail oder Telefonnummer angeben."

#### Scenario: Consent missing
- **WHEN** the consent box is not ticked
- **THEN** the form is not sent

### Requirement: Inquiry path from an article and back
When an online page of the shop carries the inquiry form, the article detail SHALL show "Weitere Fragen? Schreiben Sie uns eine Nachricht" linking to that page with the article preselected. After a successful submission the visitor SHALL get a way back: to the article when the form was reached from one, otherwise to the Startseite.

#### Scenario: Question about an article
- **WHEN** a visitor opens the link on an article page
- **THEN** the form opens with that article selected, and after sending, "Zurück zum Artikel" leads back to it

#### Scenario: No form page
- **WHEN** no online page carries the inquiry form
- **THEN** the article detail shows no inquiry link

### Requirement: Spam protection without external services
Inquiry submission SHALL be rate-limited, SHALL be discarded if the honeypot field is filled or the form was submitted less than 3 seconds after rendering, and SHALL NOT use external CAPTCHA services.

#### Scenario: Bot-speed submission
- **WHEN** a form is submitted 1 second after rendering
- **THEN** no inquiry is stored and the visitor sees a generic retry message

### Requirement: Assignment and inbox
A valid inquiry SHALL be stored for the partner owning the shop (resolved server-side) and shown in the partner's inbox "Anfragen" with date, name, contact, message and article reference. The navigation SHALL show the number of unread inquiries. The partner SHALL mark inquiries as done and delete them.

#### Scenario: New inquiry
- **WHEN** a visitor sends a valid inquiry in the partner's shop
- **THEN** the partner sees it as unread and the badge count increases by one

#### Scenario: Other partners
- **WHEN** another partner or the company requests the inquiry
- **THEN** access is denied

### Requirement: Inquiry data handling
Inquiries SHALL be stored only with the submitted fields, the shop and timestamps; no IP address SHALL be stored. Deletion by the partner SHALL remove the inquiry permanently. No automatic retention period SHALL be applied until one is specified.

#### Scenario: Delete
- **WHEN** the partner deletes an inquiry
- **THEN** it is no longer retrievable

