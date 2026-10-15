## ADDED Requirements

### Requirement: Customer origin
Every Kunde SHALL have an origin `PORTAL` or `SHOP`. Existing records SHALL be migrated to `PORTAL`. A shop order SHALL always create a new Kunde owned by the shop's partner with origin `SHOP`, including the e-mail address; it SHALL NOT match or modify existing Kunde records. The partner customer list SHALL show the origin.

#### Scenario: Repeat shop customer
- **WHEN** the same person orders twice in a shop
- **THEN** two Kunde records with origin "Shop" exist and no existing record was changed

#### Scenario: Shop customer stays partner-scoped
- **WHEN** a shop order creates a Kunde
- **THEN** only the shop's partner (and the company via the order) can see it
