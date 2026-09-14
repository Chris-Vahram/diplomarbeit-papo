## 1. Company-only data boundary — essential

- [ ] 1.1 Add nullable 4,000-character Partner note and separate company versus partner-self response mapping; verify 4,000 accepted, 4,001 rejected, clearing, line breaks/umlauts and absence from every partner/order/export response.
- [ ] 1.2 Add company request round-trip handling while ignoring/rejecting manipulated partner-self note input; verify unrelated company edits preserve note and partner edits cannot read or change it.

## 2. UI and completion — essential

- [ ] 2.1 Add plain `<textarea maxlength="4000">` labeled “Interne Notiz – nur für das Unternehmen” to existing company partner form; verify create/edit/cancel, escaping, desktop and mobile without rich-text dependencies.
- [ ] 2.2 Run focused resource tests, full `./mvnw test`, `./mvnw package`, JS syntax, `openspec validate add-company-internal-partner-note --strict`, diff check and partnerportal-quality/security review; update data docs and fix findings.

## Implementation handoff — 14.09.2026

The implementation is deployed through PR #16 (`486ca54`), but checkboxes remain open until acceptance criteria are complete. Existing evidence: focused `PartnerResourceTest` 13/13, full suite 101/101, package, JavaScript syntax, strict validation and diff check. Remaining: preserve an existing note on company PUT requests that omit the field, add negative assertions for relevant partner/order/dashboard/product/export responses, perform desktop/mobile browser verification, rerun reviews, then mark tasks complete before archiving.
