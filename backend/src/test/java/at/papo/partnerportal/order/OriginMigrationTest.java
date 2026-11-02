package at.papo.partnerportal.order;

import at.papo.partnerportal.customer.Kunde;
import at.papo.partnerportal.customer.KundeOrigin;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// add-shop-guest-checkout: rows from before the origin columns become INTERNAL/PORTAL; SHOP stays.
@QuarkusTest
class OriginMigrationTest {

    @Inject
    OriginMigrationService migrationService;

    @Test
    void backfillsMissingOriginsOnly() {
        long shopOrders = QuarkusTransaction.requiringNew().call(() -> Order.count("origin", OrderOrigin.SHOP));
        QuarkusTransaction.requiringNew().run(() -> {
            Order.update("origin = null where origin = ?1", OrderOrigin.INTERNAL);
            Kunde.update("origin = null where origin = ?1", KundeOrigin.PORTAL);
        });

        migrationService.migrate();
        migrationService.migrate();

        QuarkusTransaction.requiringNew().run(() -> {
            assertEquals(0, Order.count("origin is null"));
            assertEquals(0, Kunde.count("origin is null"));
            assertEquals(shopOrders, Order.count("origin", OrderOrigin.SHOP));
        });
    }
}
