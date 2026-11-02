package at.papo.partnerportal.business;

import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.support.TestCompanies;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// add-company-tenancy migration on the single-company demo state. expand() (Release A): unassigned
// rows go to the only company, ambiguous data aborts without change. contract() (Release B): from
// the Release A schema to NOT NULL company_id without the global uniques; a repeated run is a no-op.
@QuarkusTest
@TestProfile(BusinessTaxIdentifierMigrationTest.SingleCompany.class)
class CompanyTenancyMigrationTest {

    @Inject
    CompanyTenancyMigrationService migrationService;

    @Inject
    EntityManager entityManager;

    @AfterEach
    void restore() {
        QuarkusTransaction.requiringNew().run(() -> {
            Partner.delete("company.id in (select id from CompanyProfile where businessName = ?1)",
                    "Zweitprofil Migrationstest");
            CompanyProfile.delete("businessName", "Zweitprofil Migrationstest");
        });
        migrationService.contract();
        QuarkusTransaction.requiringNew().run(() -> entityManager.createNativeQuery(
                "alter table companyprofile drop column if exists singletonkey").executeUpdate());
    }

    @Test
    void assignsUnassignedRowsToTheOnlyCompanyAndKeepsColumnsNullable() {
        QuarkusTransaction.requiringNew().run(this::detachAll);

        migrationService.expand();

        QuarkusTransaction.requiringNew().run(() -> {
            long companyId = TestCompanies.one().id;
            for (String table : CompanyTenancyMigrationService.OWNED_TABLES) {
                assertEquals(0L, count("select count(*) from " + table + " where company_id is null"), table);
                assertEquals(0L, count("select count(*) from " + table + " where company_id <> " + companyId), table);
                assertEquals("YES", entityManager.createNativeQuery("select is_nullable from information_schema.columns "
                        + "where table_name = ?1 and column_name = 'company_id'").setParameter(1, table)
                        .getSingleResult(), table);
            }
        });
    }

    @Test
    void unassignedRowsWithTwoCompaniesAbortWithoutChange() {
        QuarkusTransaction.requiringNew().run(() -> {
            detachAll();
            CompanyProfile second = new CompanyProfile();
            second.businessName = "Zweitprofil Migrationstest";
            second.taxIdentifier = new BusinessTaxIdentifier("AT", BusinessTaxIdentifierType.VAT_ID, "ATU55555555");
            second.persist();
        });

        IllegalStateException failure = assertThrows(IllegalStateException.class, () -> migrationService.expand());

        assertEquals("Mandanten-Übernahme abgebrochen: mehrere Firmen, nicht zuordenbare Daten.", failure.getMessage());
        QuarkusTransaction.requiringNew().run(() ->
                assertEquals(0L, count("select count(*) from partner where company_id is not null")));
        QuarkusTransaction.requiringNew().run(() -> CompanyProfile.delete("businessName", "Zweitprofil Migrationstest"));
    }

    @Test
    void contractTurnsReleaseASchemaIntoPerCompanySchemaIdempotently() {
        QuarkusTransaction.requiringNew().run(() -> {
            detachAll();
            // Release A schema of a migrated VM database: global uniques and the singleton key.
            sql("alter table companyprofile add column singletonkey varchar(16)");
            sql("update companyprofile set singletonkey = 'CURRENT'");
            sql("alter table companyprofile alter column singletonkey set not null");
            sql("alter table companyprofile add constraint uk_legacy_singleton unique (singletonkey)");
            sql("alter table partner add constraint uk_legacy_partnernumber unique (partnernumber)");
            sql("alter table article_categories add constraint uk_legacy_category unique (normalized_name)");
        });

        migrationService.contract();
        migrationService.contract();

        QuarkusTransaction.requiringNew().run(() -> {
            for (String table : CompanyTenancyMigrationService.OWNED_TABLES) {
                assertEquals(0L, count("select count(*) from " + table + " where company_id is null"), table);
                assertEquals("NO", nullable(table, "company_id"), table);
            }
            assertEquals(0L, count("select count(*) from pg_constraint where conname like 'uk_legacy_%'"));
            assertEquals("YES", nullable("companyprofile", "singletonkey"));
            assertEquals("CURRENT", entityManager.createNativeQuery("select singletonkey from companyprofile")
                    .getSingleResult());
        });
    }

    @Test
    void contractAllowsSamePartnerNumberInAnotherCompany() {
        migrationService.contract();

        QuarkusTransaction.requiringNew().run(() -> {
            CompanyProfile second = new CompanyProfile();
            second.businessName = "Zweitprofil Migrationstest";
            second.taxIdentifier = new BusinessTaxIdentifier("AT", BusinessTaxIdentifierType.VAT_ID, "ATU55555555");
            second.persist();
            Partner demo = Partner.find("partnerNumber", "PARTNER-DEMO").firstResult();
            Partner copy = new Partner();
            copy.company = second;
            copy.partnerNumber = demo.partnerNumber;
            copy.name = demo.name;
            copy.address = demo.address;
            copy.taxIdentifier = demo.taxIdentifier;
            copy.commissionPercent = demo.commissionPercent;
            copy.persist();
        });
        QuarkusTransaction.requiringNew().run(() -> {
            assertEquals(2L, count("select count(*) from partner where partnernumber = 'PARTNER-DEMO'"));
        });
    }

    private void detachAll() {
        for (String table : CompanyTenancyMigrationService.OWNED_TABLES) {
            sql("alter table " + table + " alter column company_id drop not null");
            sql("update " + table + " set company_id = null");
        }
    }

    private void sql(String statement) {
        entityManager.createNativeQuery(statement).executeUpdate();
    }

    private Object nullable(String table, String column) {
        return entityManager.createNativeQuery("select is_nullable from information_schema.columns "
                + "where table_name = ?1 and column_name = ?2").setParameter(1, table).setParameter(2, column)
                .getSingleResult();
    }

    private long count(String sql) {
        return ((Number) entityManager.createNativeQuery(sql).getSingleResult()).longValue();
    }
}
