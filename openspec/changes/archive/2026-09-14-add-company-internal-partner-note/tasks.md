## 1. Company-only data boundary — essential

- [x] 1.1 Add nullable 4,000-character Partner note and separate company versus partner-self response mapping; verify 4,000 accepted, 4,001 rejected, clearing, line breaks/umlauts and absence from every partner/order/export response.
- [x] 1.2 Add company request round-trip handling while ignoring/rejecting manipulated partner-self note input; verify unrelated company edits preserve note and partner edits cannot read or change it.

## 2. UI and completion — essential

- [x] 2.1 Add plain `<textarea maxlength="4000">` labeled “Interne Notiz – nur für das Unternehmen” to existing company partner form; verify create/edit/cancel, escaping, desktop and mobile without rich-text dependencies.
- [x] 2.2 Run focused resource tests, full `./mvnw test`, `./mvnw package`, JS syntax, `openspec validate add-company-internal-partner-note --strict`, diff check and partnerportal-quality/security review; update data docs and fix findings.

## Implementation handoff — 14.09.2026

All four tasks are complete. PR #18 (`e061ab8c1e3cc41cae57b7fbe17a8f9e65664b5d`) is merged and CI/CD run `34857012639` deployed it successfully. The final implementation preserves an existing note when `internalNote` is omitted, clears it for explicit `null` or blank input, and keeps it company-only. Evidence: `PartnerResourceTest` 13/13, full suite 101/101, package, JavaScript syntax, strict validation, diff check, desktop/mobile browser verification, partnerportal-quality review and clean Java/security reviews. The next session starts with the remaining VM Task 2.2 SSH negative matrix and then the next explicitly approved OpenSpec change; do not reopen this change.
