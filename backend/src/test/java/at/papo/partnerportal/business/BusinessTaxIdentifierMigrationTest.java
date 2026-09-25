package at.papo.partnerportal.business;

import at.papo.partnerportal.partner.Partner;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class BusinessTaxIdentifierMigrationTest {

    private static final String UNKNOWN_PARTNER_NUMBER = "MIGRATION-UNKNOWN";

    @Inject
    BusinessTaxIdentifierMigrationService migrationService;

    @Inject
    EntityManager entityManager;

    @AfterEach
    void restoreDemoData() {
        QuarkusTransaction.requiringNew().run(() -> {
            dropNotNullConstraints();
            Partner unknown = Partner.find("partnerNumber", UNKNOWN_PARTNER_NUMBER).firstResult();
            if (unknown != null) {
                unknown.partnerNumber = "P-1003";
            }
            partner("PARTNER-DEMO").name = "Huemer GmbH";
            setIdentifier("PARTNER-DEMO", "ATU12345678");
            setIdentifier("P-1002", "ATU23456789");
            setIdentifier("P-1003", "ATU34567890");
        });
        migrationService.migrate(true);
    }

    @Test
    void migratesKnownDemoRowsAndMissingProfileIdempotently() {
        QuarkusTransaction.requiringNew().run(() -> {
            dropNotNullConstraints();
            partner("PARTNER-DEMO").taxIdentifier = null;
            partner("P-1002").taxIdentifier = new BusinessTaxIdentifier(
                    "DE", BusinessTaxIdentifierType.VAT_ID, "DE123456789");
            partner("P-1003").taxIdentifier = null;
            CompanyProfile.deleteAll();
        });

        migrationService.migrate(true);
        migrationService.migrate(true);

        QuarkusTransaction.requiringNew().run(() -> {
            assertEquals("ATU12345678", partner("PARTNER-DEMO").taxIdentifier.value);
            assertEquals("DE123456789", partner("P-1002").taxIdentifier.value);
            assertEquals("ATU34567890", partner("P-1003").taxIdentifier.value);
            assertEquals(1, CompanyProfile.count("singletonKey", CompanyProfile.CURRENT_KEY));
            assertEquals(3, requiredTaxColumnCount("partner"));
            assertEquals(3, requiredTaxColumnCount("companyprofile"));
        });
    }

    @Test
    void migratesOlderVmDemoNameWithoutRenamingPartner() {
        QuarkusTransaction.requiringNew().run(() -> {
            dropNotNullConstraints();
            Partner legacyDemo = partner("PARTNER-DEMO");
            legacyDemo.name = "Elektro Huber GmbH";
            legacyDemo.taxIdentifier = null;
            CompanyProfile.deleteAll();
        });

        migrationService.migrate(true);

        QuarkusTransaction.requiringNew().run(() -> {
            assertEquals("Elektro Huber GmbH", partner("PARTNER-DEMO").name);
            assertEquals("ATU12345678", partner("PARTNER-DEMO").taxIdentifier.value);
            assertEquals(1, CompanyProfile.count("singletonKey", CompanyProfile.CURRENT_KEY));
        });
    }

    @Test
    void unknownIncompletePartnerRollsBackTheWholeMigration() {
        QuarkusTransaction.requiringNew().run(() -> {
            dropNotNullConstraints();
            partner("PARTNER-DEMO").taxIdentifier = null;
            partner("P-1002").taxIdentifier = null;
            Partner unknown = partner("P-1003");
            unknown.partnerNumber = UNKNOWN_PARTNER_NUMBER;
            unknown.taxIdentifier = null;
            CompanyProfile.deleteAll();
        });

        assertThrows(IllegalStateException.class, () -> migrationService.migrate(true));

        QuarkusTransaction.requiringNew().run(() -> {
            assertEquals(0, completeIdentifierCount());
            assertEquals(0, CompanyProfile.count());
            assertEquals(0, requiredTaxColumnCount("partner"));
        });
    }

    @Test
    void lookalikePartnerNumberIsNeverBackfilled() {
        QuarkusTransaction.requiringNew().run(() -> {
            dropNotNullConstraints();
            partner("P-1002").name = "Echter Betrieb GmbH";
            partner("P-1002").taxIdentifier = null;
        });

        assertThrows(IllegalStateException.class, () -> migrationService.migrate(true));

        QuarkusTransaction.requiringNew().run(() -> {
            assertEquals(null, partner("P-1002").taxIdentifier);
            partner("P-1002").name = "Sanitär Bauer KG";
        });
    }

    @Test
    void missingProfileOutsideVerifiedDemoDataAborts() {
        QuarkusTransaction.requiringNew().run(() -> CompanyProfile.deleteAll());

        assertThrows(IllegalStateException.class, () -> migrationService.migrate(false));
    }

    @Test
    void invalidExistingIdentifierAbortsWithoutRewritingIt() {
        QuarkusTransaction.requiringNew().run(() -> partner("P-1002").taxIdentifier =
                new BusinessTaxIdentifier("AT", BusinessTaxIdentifierType.VAT_ID, "ungueltig"));

        assertThrows(IllegalStateException.class, () -> migrationService.migrate(true));

        QuarkusTransaction.requiringNew().run(() ->
                assertEquals("ungueltig", partner("P-1002").taxIdentifier.value));
    }

    @Test
    void concurrentMigrationsCompleteWithoutLockUpgradeDeadlock() throws Exception {
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> migrationService.migrate(true));
            var second = executor.submit(() -> migrationService.migrate(true));
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
        }
    }

    private Partner partner(String partnerNumber) {
        return Partner.find("partnerNumber", partnerNumber).firstResult();
    }

    private void setIdentifier(String partnerNumber, String value) {
        Partner partner = partner(partnerNumber);
        if (partner != null) {
            partner.taxIdentifier = new BusinessTaxIdentifier("AT", BusinessTaxIdentifierType.VAT_ID, value);
        }
    }

    private void dropNotNullConstraints() {
        for (String column : new String[] { "tax_country", "tax_identifier_type", "tax_identifier_value" }) {
            entityManager.createNativeQuery("ALTER TABLE partner ALTER COLUMN " + column + " DROP NOT NULL")
                    .executeUpdate();
        }
    }

    private long requiredTaxColumnCount(String tableName) {
        return ((Number) entityManager.createNativeQuery("""
                SELECT count(*)
                FROM information_schema.columns
                WHERE table_name = :tableName
                  AND column_name IN ('tax_country', 'tax_identifier_type', 'tax_identifier_value')
                  AND is_nullable = 'NO'
                """).setParameter("tableName", tableName).getSingleResult()).longValue();
    }

    private long completeIdentifierCount() {
        return ((Number) entityManager.createNativeQuery("""
                SELECT count(*) FROM partner
                WHERE tax_country IS NOT NULL
                  AND tax_identifier_type IS NOT NULL
                  AND tax_identifier_value IS NOT NULL
                """).getSingleResult()).longValue();
    }
}
