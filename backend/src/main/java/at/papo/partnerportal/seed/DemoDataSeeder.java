package at.papo.partnerportal.seed;

import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.Product;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;

// Dev/test only (never in the packaged demo run — LaunchMode.NORMAL), matching
// the drop-and-create schema strategy for those profiles (docs/CONVENTIONS.md,
// docs/SETUP.md). Not a raw import.sql: Hibernate's default pooled sequence
// optimizer (block size 50) would collide hand-picked seed ids with the first
// id the app allocates at runtime, so seed rows are persisted through Panache
// like any other entity and let Hibernate assign ids itself.
@ApplicationScoped
public class DemoDataSeeder {

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (LaunchMode.current() == LaunchMode.NORMAL) {
            return;
        }
        if (Partner.count() > 0) {
            return;
        }
        seedPartners();
        seedProducts();
    }

    private void seedPartners() {
        // Matches the Keycloak demo user "partner-demo" so login -> dashboard works out of the box.
        persistPartner("P-1001", "Elektro Huber GmbH", "Hauptstraße 12, 4020 Linz",
                "partner-demo@example.at", "+43 660 1234567", "10.00", true, "partner-demo");
        // No linked Keycloak user yet — demonstrates the manual-provisioning gap (design.md).
        persistPartner("P-1002", "Sanitär Bauer KG", "Ringstraße 5, 8010 Graz",
                "office@sanitaer-bauer.example.at", "+43 664 2345678", "5.00", true, null);
        // Inactive — for FR-23 inactive-partner testing (Phase 2).
        persistPartner("P-1003", "Baumarkt Weber e.U.", "Industriezeile 3, 6020 Innsbruck",
                "info@weber-baumarkt.example.at", "+43 512 3456789", "15.00", false, null);
    }

    private void persistPartner(String partnerNumber, String name, String address, String email,
            String phone, String commissionPercent, boolean active, String keycloakUsername) {
        Partner partner = new Partner();
        partner.partnerNumber = partnerNumber;
        partner.name = name;
        partner.address = address;
        partner.email = email;
        partner.phone = phone;
        partner.commissionPercent = new BigDecimal(commissionPercent);
        partner.active = active;
        partner.keycloakUsername = keycloakUsername;
        partner.persist();
    }

    private void seedProducts() {
        persistProduct("Bohrmaschine Profi 750W", "89.90",
                "Schlagbohrmaschine mit 750 Watt, inkl. Koffer.", true, "Werkzeug");
        persistProduct("Akkuschrauber 18V Set", "129.00",
                "Akkuschrauber mit zwei Akkus und Ladegerät.", true, "Werkzeug");
        persistProduct("Malerpinsel-Set 5-teilig", "14.50",
                "Fünf Pinselgrößen für Innen- und Außenanstrich.", true, "Malerbedarf");
        persistProduct("Silikon transparent 300ml", "6.90",
                "Sanitärsilikon, schimmelresistent.", true, "Baustoffe");
        persistProduct("Schutzhelm gelb", "12.30",
                "Bauhelm nach EN 397.", true, "Arbeitsschutz");
        // Unavailable — for FR-24 (unavailable-product) testing (Phase 3).
        persistProduct("Wasserwaage 100cm", "24.90",
                "Aluminium-Wasserwaage, 3 Libellen.", false, "Werkzeug");
        persistProduct("Handschuhe Gr. L (Set 10 Paar)", "18.00",
                "Arbeitshandschuhe, beschichtet.", true, "Arbeitsschutz");
        persistProduct("Kabeltrommel 25m", "39.90",
                "Kabeltrommel mit Überlastschutz.", true, "Elektro");
    }

    private void persistProduct(String name, String price, String description, boolean available,
            String category) {
        Product product = new Product();
        product.name = name;
        product.price = new BigDecimal(price);
        product.description = description;
        product.available = available;
        product.category = category;
        product.persist();
    }
}
