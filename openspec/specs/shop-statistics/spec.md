# shop-statistics Specification

## Purpose
In-house, cookieless statistics for partner shops: anonymous daily aggregates of shop events, unique and returning visitors only with consent, and the partner page "Webshop-Statistik".
## Requirements
### Requirement: Anonymous event collection
The shop SHALL send page-view, article-view and add-to-cart events to the server. The server SHALL store only daily aggregated counts per shop, page or article, referrer domain and device class. It SHALL NOT store IP addresses, full user-agent strings, full referrer URLs or any personal data from events.

#### Scenario: Referrer reduced to domain
- **WHEN** a visitor arrives from `https://www.example.com/search?q=lampe`
- **THEN** only "example.com" is counted as referrer

#### Scenario: No IP stored
- **WHEN** events are received
- **THEN** no table or log entry written by the statistics feature contains the client IP

### Requirement: Consent-dependent visitor metrics
Unique and returning visitor counts SHALL only be collected after the visitor consented to the category "Statistik". Without consent, events SHALL carry no identifier. Visitor identifiers SHALL be random, SHALL be used only to compute daily counts and SHALL be deleted after daily aggregation.

#### Scenario: No consent
- **WHEN** a visitor chose "Nur notwendige"
- **THEN** events contain no visitor identifier and the visitor is not part of unique-visitor counts

### Requirement: Partner statistics page
The partner SHALL see a page "Webshop-Statistik" showing for the selected period (Diese Woche, Dieses Monat as default, Dieses Jahr): Besuche, Artikelaufrufe, In den Warenkorb, Bestellungen, Umsatz (brutto), Conversion-Rate, a time chart, top 10 articles, referrer domains, device classes and, where consent-based data exists, unique and returning visitors with the note that they only include consenting visitors. Each KPI SHALL show the change against the previous equal-length period.

#### Scenario: Default period
- **WHEN** the partner opens the statistics page
- **THEN** "Dieses Monat" is selected and the KPIs for the current month are shown

#### Scenario: Partner sees only own shop
- **WHEN** a partner requests statistics
- **THEN** only data of the partner's own shop is returned

### Requirement: Event abuse limits
The event endpoint SHALL be rate-limited, SHALL accept only known event types and only article/page ids of the shop, and SHALL ignore invalid events without error details.

#### Scenario: Unknown article id
- **WHEN** an event references an article not in the shop's catalogue
- **THEN** the event is ignored
