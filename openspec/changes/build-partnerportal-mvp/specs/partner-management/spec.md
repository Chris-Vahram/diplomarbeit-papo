## ADDED Requirements

### Requirement: Company manages partners
The company SHALL be able to create, view, edit, and manage partner records centrally, including partner master data, a commission percentage (`Partner.commissionPercent`), and an active/inactive status (`Partner.active`).

#### Scenario: Create partner
- **WHEN** the company submits a new partner with valid master data and a commission percentage
- **THEN** the system stores the partner and shows a success confirmation

#### Scenario: Set commission percentage
- **WHEN** the company selects a commission percentage from the dropdown (5/10/15) or enters a custom value
- **THEN** the system saves it as `Partner.commissionPercent` and applies it to future commission calculations

#### Scenario: Deactivate partner
- **WHEN** the company sets `Partner.active` to false
- **THEN** the partner can no longer log in or create orders (see auth-access-control)

### Requirement: Partner limited self-edit
A partner SHALL be able to edit limited own master data (e.g. contact data) but SHALL NOT change commission percentage, role, active status, or other central system data.

#### Scenario: Partner edits contact data
- **WHEN** a partner updates its own contact fields
- **THEN** the system saves the change

#### Scenario: Partner cannot change commission percentage
- **WHEN** a partner attempts to change its commission percentage
- **THEN** the system rejects the action (field not editable / unauthorized)

### Requirement: Partner validation
The system SHALL validate required partner fields before saving and prevent duplicate partner identifiers.

#### Scenario: Missing required field
- **WHEN** the company saves a partner with a required field empty
- **THEN** the system rejects the save and shows an understandable validation message

#### Scenario: Duplicate partner identifier
- **WHEN** the company saves a partner with an already-used identifier
- **THEN** the system rejects the save and reports the duplicate
