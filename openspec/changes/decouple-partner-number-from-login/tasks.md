## 1. Inputs — essential

- [ ] 1.1 Obtain anonymized BMD partner-number sample or binding rule sheet covering column, length, leading zeros, case, whitespace, allowed characters and corrections; add fixtures. Stop while absent.
- [ ] 1.2 Confirm `bind-partner-login-to-oidc-subject` Release B is deployed and the VM partner login resolves by subject.

## 2. Business number — essential

- [ ] 2.1 Update partner validation, API/UI and import mapping/dedupe to the confirmed canonical rules; verify leading zeros, duplicates (manual and import), German messages and that renumbering keeps bindings and relations.

## 3. Completion

- [ ] 3.1 Run focused partner/import tests, full `./mvnw test`, `./mvnw package`, relevant `node --check`, `openspec validate decouple-partner-number-from-login --strict`, `git diff --check`, partnerportal-quality and an independent data review; fix findings before completion.
