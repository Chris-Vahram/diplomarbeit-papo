## ADDED Requirements

### Requirement: Import file size remains bounded after proof upload expansion
Partner and article import files SHALL be limited to 2 MiB and rejected before their complete contents are read into application memory. Raising the global multipart ceiling for 10 MiB trade proofs SHALL NOT permit larger import files.

#### Scenario: Oversized partner or article import
- **WHEN** a company uploads an import file larger than 2 MiB
- **THEN** the request is rejected without parsing or persisting rows

#### Scenario: Import within limit
- **WHEN** a valid import file is at most 2 MiB
- **THEN** existing preview, mapping, validation and commit behavior remains available
