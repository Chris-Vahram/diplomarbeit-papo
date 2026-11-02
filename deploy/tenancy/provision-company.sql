-- add-company-tenancy: operator provisioning of a company login (and optionally a new company).
-- Run only after a database backup (deploy/backup/backup.sh), inside the postgres container
-- (see docs/DEPLOYMENT.md "Firmen bereitstellen"). Physical names follow the default naming:
-- companyprofile, company_login, partner_login.
--
--   Bind an existing company (e.g. company 1 = company-demo right after Release A):
--     psql -v ON_ERROR_STOP=1 -v subject="'<keycloak-user-id>'" -v company_id=<id> \
--          -v operator="'chris'" -f provision-company.sql partnerportal
--
--   Create a new company and bind its first login (ONLY after Release B is live; on a Release A
--   schema the NOT NULL singleton column makes this insert fail on purpose):
--     psql -v ON_ERROR_STOP=1 -v subject="'<keycloak-user-id>'" -v company_id=0 \
--          -v operator="'chris'" -v business_name="'Testfirma Zwei GmbH'" \
--          -v tax_value="'ATU22222222'" -v register_value="'87654321'" -f provision-company.sql partnerportal
--
-- Aborts without change if the subject is already bound as company or partner login. The
-- advisory lock key is the one PartnerLoginService uses (LoginSubjectLock), so a concurrent
-- partner binding of the same subject cannot slip in between check and insert.

BEGIN;

SELECT pg_advisory_xact_lock(hashtext('login_subject:' || :subject));

SELECT (EXISTS (SELECT 1 FROM company_login WHERE oidcsubject = :subject)
        OR EXISTS (SELECT 1 FROM partner_login WHERE oidcsubject = :subject)) AS already_bound \gset
\if :already_bound
  \echo 'Abbruch: Dieses Login ist bereits verknüpft. Keine Änderung.'
  ROLLBACK;
  \quit
\endif

SELECT (:company_id = 0) AS create_company \gset
\if :create_company
  INSERT INTO companyprofile (id, businessname, tax_country, tax_identifier_type, tax_identifier_value,
                              register_country, register_type, register_value)
  VALUES (nextval('companyprofile_seq'), :business_name, 'AT', 'VAT_ID', :tax_value, 'AT', 'GISA', :register_value)
  RETURNING id AS company_id \gset
\else
  SELECT (NOT EXISTS (SELECT 1 FROM companyprofile WHERE id = :company_id)) AS unknown_company \gset
  \if :unknown_company
    \echo 'Abbruch: Firma nicht gefunden. Keine Änderung.'
    ROLLBACK;
    \quit
  \endif
\endif

INSERT INTO company_login (id, oidcsubject, company_id, createdat, createdby)
VALUES (nextval('company_login_seq'), :subject, :company_id, now(), :operator);

SELECT c.id AS company_id, c.businessname, l.oidcsubject
FROM company_login l JOIN companyprofile c ON c.id = l.company_id
WHERE l.oidcsubject = :subject;

COMMIT;
