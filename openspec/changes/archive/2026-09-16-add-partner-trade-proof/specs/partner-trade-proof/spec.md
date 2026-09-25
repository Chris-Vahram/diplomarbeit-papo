## Purpose

Defines private submission and review of one current partner-company authorization proof without exposing sensitive documents through public asset routes.

## ADDED Requirements

### Requirement: Partner submits one current proof
An active authenticated partner SHALL provide one trade-register identifier and upload or replace exactly one current proof for its own Partner record. The identifier SHALL contain ISO country, register type and a 1–64 character value; Austria SHALL use register type `GISA`, while other countries SHALL use `OTHER_TRADE_REGISTER`. Accepted files SHALL be PDF, JPEG or PNG with verified content and at most 10 MiB. Failed validation or storage SHALL leave the previous proof unchanged.

#### Scenario: Valid first upload
- **WHEN** a partner supplies a valid register identifier and uploads an allowed file within the limit
- **THEN** it becomes the current proof with upload timestamp and pending review state

#### Scenario: Valid replacement
- **WHEN** a partner replaces an existing proof
- **THEN** the new proof atomically replaces it, review returns to pending and no history remains available

#### Scenario: Invalid replacement
- **WHEN** size, declared type and verified content do not meet the rules
- **THEN** upload is rejected and the prior proof remains unchanged

#### Scenario: Partner targets another record
- **WHEN** a partner attempts to upload or download proof for another partner
- **THEN** access is denied without revealing whether that proof exists

#### Scenario: Invalid register identifier
- **WHEN** the register country, type or value is missing or Austria uses a type other than `GISA`
- **THEN** submission is rejected and the previous proof remains unchanged

### Requirement: Company reviews proof
Company-role users SHALL view proof metadata, download the current document, and accept or reject that exact version with review timestamp. A review request for a replaced version SHALL fail with a conflict. Partners SHALL see their own review state and rejection reason but SHALL NOT access company-only review administration.

#### Scenario: Company accepts current proof
- **WHEN** a company user accepts the pending current version
- **THEN** that version becomes accepted with reviewer timestamp

#### Scenario: Replacement after acceptance
- **WHEN** a partner replaces an accepted proof
- **THEN** the replacement is pending and the previous acceptance no longer applies

#### Scenario: Stale review after replacement
- **WHEN** company downloaded one version but partner replaced it before the review was submitted
- **THEN** the stale accept or reject request is rejected and the replacement remains pending

#### Scenario: Private download
- **WHEN** an authorized user downloads the current proof
- **THEN** it is returned as an attachment with `Cache-Control: private, no-store` and `X-Content-Type-Options: nosniff`

### Requirement: Proof review does not silently change activation
Proof state SHALL NOT automatically activate or deactivate a partner in this change.

#### Scenario: Proof accepted or rejected
- **WHEN** company changes proof review state
- **THEN** `Partner.active` remains unchanged
