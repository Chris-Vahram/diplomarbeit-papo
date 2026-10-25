package at.papo.partnerportal.business;

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

// Release A ("expand") of add-company-tenancy on the single-company demo state: unassigned rows
// go to the only company, the schema stays compatible with the previous image (company_id
// nullable), ambiguous data aborts without change, and a repeated run is a no-op.
@QuarkusTest
@TestProfile(BusinessTaxIdentifierMigrationTest.SingleCompany.class)
class CompanyTenancyMigrationTest {

    @Inject
    CompanyTenancyMigrationService migrationService;

    @Inject
    EntityManager entityManager;

    @AfterEach
    void restore() {
        QuarkusTransaction.requiringNew().run(() ->
                CompanyProfile.delete("businessName", "Zweitprofil Migrationstest"));
        migrationService.expand();
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
    void repeatedRunChangesNothingAndCopiesLegacyRecipientOnce() {
        QuarkusTransaction.requiringNew().run(() -> TestCompanies.one().orderNoticeEmail = null);

        migrationService.expand();
        migrationService.expand();

        QuarkusTransaction.requiringNew().run(() -> {
            assertEquals("firma@example.test", TestCompanies.one().orderNoticeEmail);
            for (String table : CompanyTenancyMigrationService.OWNED_TABLES) {
                assertEquals(0L, count("select count(*) from " + table + " where company_id is null"), table);
            }
        });
    }

    @Test
    void deliberatelyClearedRecipientIsNotRestored() {
        QuarkusTransaction.requiringNew().run(() -> TestCompanies.one().orderNoticeEmail = "");

        migrationService.expand();

        QuarkusTransaction.requiringNew().run(() -> assertEquals("", TestCompanies.one().orderNoticeEmail));
        QuarkusTransaction.requiringNew().run(() -> TestCompanies.one().orderNoticeEmail = null);
    }

    private void detachAll() {
        for (String table : CompanyTenancyMigrationService.OWNED_TABLES) {
            entityManager.createNativeQuery("update " + table + " set company_id = null").executeUpdate();
        }
    }

    private long count(String sql) {
        return ((Number) entityManager.createNativeQuery(sql).getSingleResult()).longValue();
    }
}
