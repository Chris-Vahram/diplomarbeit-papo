## Purpose

Provide a consistent documentation contract for tracing PAPO requirements to implementation and verification evidence without confusing historical checks, missing evidence and completed product behavior.

## ADDED Requirements

### Requirement: Complete source-indexed traceability format
The project documentation SHALL provide one fillable Markdown matrix containing exactly one source-indexed row for each FR-01 through FR-25 and B-01 through B-08. Each row SHALL distinguish source, behavior, code, test evidence, implementation status, verification status/date/revision and ownership/blockers. Unresearched mappings SHALL be explicitly marked rather than inferred as missing or completed implementation.

#### Scenario: Source coverage
- **WHEN** the traceability format is checked against the FSD and deferred backlog
- **THEN** all 33 IDs occur once with their corresponding source title and link, without missing or duplicate requirements

#### Scenario: Mapping is not researched yet
- **WHEN** a row has no established implementation or test mapping
- **THEN** it explicitly marks that mapping as outstanding and does not claim that the feature is implemented or absent

### Requirement: Verification evidence has provenance
The format SHALL distinguish unverified, historically evidenced and verified results. Verified entries SHALL identify the check, result, date and examined revision or working-diff basis. Deferred features SHALL retain their sourced backlog status. Evidence SHALL exclude credentials, private raw sessions and personal company data.

#### Scenario: Historical test report
- **WHEN** an earlier test report is referenced
- **THEN** its historical provenance is preserved and it is not represented as a current test run

#### Scenario: Missing evidence or external decision
- **WHEN** a result cannot be supported by available evidence
- **THEN** the row records the missing check or decision and its owner without claiming completion

### Requirement: Format completion does not close joint documentation
The format SHALL assign technical mappings to Chris and UI evidence to Tobias, with joint final acceptance recorded separately. Completing the format SHALL NOT mark MVP task 8.4, UI documentation 8.3 or visual polish FD.4 complete.

#### Scenario: Format delivered before joint documentation
- **WHEN** the source-indexed matrix and its usage rules are delivered
- **THEN** the format change can be completed while the joint MVP documentation tasks remain open
