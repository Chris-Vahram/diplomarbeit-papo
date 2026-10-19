## ADDED Requirements

### Requirement: BMD partner number integrity
Partner numbers SHALL be imported and stored as text, preserve significant leading zeros, and be unique under the documented canonical comparison. The company SHALL be able to replace a partner number with its BMD number without affecting login bindings or related records. Canonical length, character set and whitespace rules SHALL follow the confirmed BMD sample or rule sheet.

#### Scenario: Duplicate BMD partner number
- **WHEN** manual entry or import supplies a number already assigned to another partner
- **THEN** the operation is rejected with a German duplicate message

#### Scenario: Leading zeros preserved
- **WHEN** an import row contains the partner number `000123`
- **THEN** the stored and displayed number is `000123`

#### Scenario: Renumbering keeps login and relations
- **WHEN** the company replaces a demo partner number with its BMD number
- **THEN** the partner's login bindings, orders, customers, enablements and commission records remain linked to the same Partner record
