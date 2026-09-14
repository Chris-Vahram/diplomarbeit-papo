# auth-access-control Specification

## Purpose
TBD - created by archiving change build-partnerportal-mvp. Update Purpose after archive.

## Requirements

### Requirement: Role-based authentication
The system SHALL authenticate users via Keycloak and assign exactly one role: `company` or `partner`. End customers do not authenticate in the MVP.

#### Scenario: Company logs in
- **WHEN** a user with the `company` role authenticates via Keycloak
- **THEN** the system grants access to company views (partner management, product management, import, all orders, commission)

#### Scenario: Partner logs in
- **WHEN** a user with the `partner` role authenticates via Keycloak
- **THEN** the system grants access only to that partner's internal area (dashboard, enabled products, own master data, own orders, order creation)

### Requirement: Protected views require authorization
Protected internal views and endpoints SHALL NOT be reachable via direct URL without a valid session and the required role.

#### Scenario: Unauthenticated deep link
- **WHEN** an unauthenticated request targets a protected view or API endpoint
- **THEN** the system denies access and redirects to login / returns an unauthorized response

#### Scenario: Partner accesses company-only view
- **WHEN** a `partner` requests a company-only endpoint (e.g. partner management)
- **THEN** the system denies access with an unauthorized response

### Requirement: Partner login linking
Each partner login SHALL be linked to exactly one Partner record via the Keycloak username claim. Keycloak users are provisioned manually (committed realm export for the demo); the data import SHALL NOT create Keycloak users. A Partner record without a Keycloak user simply cannot log in.

#### Scenario: Partner user resolves to Partner record
- **WHEN** a user with the `partner` role authenticates
- **THEN** the backend resolves the caller's username claim to its linked Partner record and scopes all partner endpoints to that record (never trusting a client-supplied partner id)

#### Scenario: Partner user without linked Partner record
- **WHEN** an authenticated `partner` user has no matching Partner record
- **THEN** the system denies access to partner views with a clear message instead of failing silently

### Requirement: Inactive partner blocking
The system SHALL prevent inactive/disabled partners from logging into the internal area and from creating new orders. The active/inactive state is stored solely as the DB flag `Partner.active` and enforced server-side after authentication (Keycloak does not hold this business state). Existing orders of that partner remain visible to the company.

#### Scenario: Inactive partner login attempt
- **WHEN** a partner whose `Partner.active` is false authenticates
- **THEN** the system blocks access to the internal area with a clear message

#### Scenario: Inactive partner order attempt
- **WHEN** an inactive partner attempts to create an order
- **THEN** the system rejects the action server-side

#### Scenario: Company still sees inactive partner's orders
- **WHEN** the company views all orders
- **THEN** orders previously created by a now-inactive partner remain visible
