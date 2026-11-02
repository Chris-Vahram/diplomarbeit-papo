package at.papo.partnerportal.order;

import at.papo.partnerportal.customer.Kunde;
import at.papo.partnerportal.customer.KundeOrigin;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

// add-shop-guest-checkout migration: rows from before the origin columns existed were all entered
// in the portal. Idempotent: only null columns are filled.
@ApplicationScoped
public class OriginMigrationService {

    @Transactional
    public void migrate() {
        Order.update("origin = ?1 where origin is null", OrderOrigin.INTERNAL);
        Kunde.update("origin = ?1 where origin is null", KundeOrigin.PORTAL);
    }
}
