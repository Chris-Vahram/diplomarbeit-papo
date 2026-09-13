## Purpose

Provide a reproducible synthetic partner company for local demonstrations without changing existing accounts or deployed business data.

## ADDED Requirements

### Requirement: Synthetic Huemer demo partner
A fresh local development or test dataset SHALL name the existing demo partner “Huemer GmbH”. Its established partner number, login relationship and related records SHALL remain unchanged. The seed SHALL NOT rename existing normal-mode business data or create a second company or login.

#### Scenario: Fresh local dataset
- **WHEN** a fresh development or test database is seeded
- **THEN** the established demo partner has the name “Huemer GmbH” and retains its existing partner number and relationships

#### Scenario: Existing normal-mode data
- **WHEN** the application starts with an existing normal-mode dataset
- **THEN** this seed change does not rename or reset its partners
