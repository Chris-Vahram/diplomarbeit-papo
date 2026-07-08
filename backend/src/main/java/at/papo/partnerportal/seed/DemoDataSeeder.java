package at.papo.partnerportal.seed;

import at.papo.partnerportal.commission.CommissionService;
import at.papo.partnerportal.common.Address;
import at.papo.partnerportal.customer.Kunde;
import at.papo.partnerportal.enablement.ProductEnablement;
import at.papo.partnerportal.order.Order;
import at.papo.partnerportal.order.OrderItem;
import at.papo.partnerportal.order.OrderStatus;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.Product;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Dev/test only (never in the packaged demo run — LaunchMode.NORMAL), matching
// the drop-and-create schema strategy for those profiles (docs/CONVENTIONS.md,
// docs/SETUP.md). Not a raw import.sql: Hibernate's default pooled sequence
// optimizer (block size 50) would collide hand-picked seed ids with the first
// id the app allocates at runtime, so seed rows are persisted through Panache
// like any other entity and let Hibernate assign ids itself.
@ApplicationScoped
public class DemoDataSeeder {

    @Inject
    CommissionService commissionService;

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
        seedEnablements();
        // Demo orders only in dev/demo — tests assume a clean order baseline
        // (specific partners with zero orders), so don't seed them under %test.
        if (LaunchMode.current() == LaunchMode.DEVELOPMENT) {
            seedOrders();
        }
    }

    private void seedPartners() {
        // Matches the Keycloak demo user "partner-demo" so login -> dashboard works out of the box.
        persistPartner("P-1001", "Elektro Huber GmbH", address("Hauptstraße", "12", "4020", "Linz"),
                "partner-demo@example.at", "+43 660 1234567", "10.00", true, "partner-demo");
        // No linked Keycloak user yet — demonstrates the manual-provisioning gap (design.md).
        persistPartner("P-1002", "Sanitär Bauer KG", address("Ringstraße", "5", "8010", "Graz"),
                "office@sanitaer-bauer.example.at", "+43 664 2345678", "5.00", true, null);
        // Inactive — for FR-23 inactive-partner testing (Phase 2).
        persistPartner("P-1003", "Baumarkt Weber e.U.", address("Industriezeile", "3", "6020", "Innsbruck"),
                "info@weber-baumarkt.example.at", "+43 512 3456789", "15.00", false, null);
    }

    // Every seed address is in Austria; the country part is fixed here.
    private Address address(String street, String houseNumber, String postalCode, String city) {
        return new Address(street, houseNumber, postalCode, city, "Österreich");
    }

    private void persistPartner(String partnerNumber, String name, Address address, String email,
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
                "Schlagbohrmaschine mit 750 Watt, inkl. Koffer.", true, "Werkzeug",
                "/assets/products/werkzeug.svg");
        persistProduct("Akkuschrauber 18V Set", "129.00",
                "Akkuschrauber mit zwei Akkus und Ladegerät.", true, "Werkzeug",
                "/assets/products/werkzeug.svg");
        persistProduct("Malerpinsel-Set 5-teilig", "14.50",
                "Fünf Pinselgrößen für Innen- und Außenanstrich.", true, "Malerbedarf",
                "/assets/products/malerbedarf.svg");
        persistProduct("Silikon transparent 300ml", "6.90",
                "Sanitärsilikon, schimmelresistent.", true, "Baustoffe",
                "/assets/products/baustoffe.svg");
        persistProduct("Schutzhelm gelb", "12.30",
                "Bauhelm nach EN 397.", true, "Arbeitsschutz",
                "/assets/products/arbeitsschutz.svg");
        // Unavailable — for FR-24 (unavailable-product) testing (Phase 3).
        // Also the deliberate no-image product, exercising the thumbnail placeholder path.
        persistProduct("Wasserwaage 100cm", "24.90",
                "Aluminium-Wasserwaage, 3 Libellen.", false, "Werkzeug", null);
        persistProduct("Handschuhe Gr. L (Set 10 Paar)", "18.00",
                "Arbeitshandschuhe, beschichtet.", true, "Arbeitsschutz",
                "/assets/products/arbeitsschutz.svg");
        persistProduct("Kabeltrommel 25m", "39.90",
                "Kabeltrommel mit Überlastschutz.", true, "Elektro",
                "/assets/products/elektro.svg");
    }

    private void persistProduct(String name, String price, String description, boolean available,
            String category, String imageUrl) {
        Product product = new Product();
        product.name = name;
        product.price = new BigDecimal(price);
        product.description = description;
        product.available = available;
        product.category = category;
        product.imageUrl = imageUrl;
        product.persist();
    }

    // A few enabled products for "partner-demo" (P-1001) so the enabled-products
    // view (product-enablement spec) has demo data out of the box.
    private void seedEnablements() {
        Partner partnerDemo = Partner.find("partnerNumber", "P-1001").firstResult();
        for (String productName : List.of("Bohrmaschine Profi 750W", "Akkuschrauber 18V Set", "Schutzhelm gelb")) {
            Product product = Product.find("name", productName).firstResult();
            ProductEnablement enablement = new ProductEnablement();
            enablement.partner = partnerDemo;
            enablement.product = product;
            enablement.persist();
        }
    }

    // Historical orders so the dashboards, charts, and settlement demo with real
    // data out of the box (dashboards aggregate orders client-side, and orders
    // otherwise all carry today's date). Dev/test only, like the rest of this seed.
    private void seedOrders() {
        Partner p1 = Partner.find("partnerNumber", "P-1001").firstResult();
        Partner p2 = Partner.find("partnerNumber", "P-1002").firstResult();
        Partner p3 = Partner.find("partnerNumber", "P-1003").firstResult();

        // Kunde names mirror Tobias's dashboard mockup. Scoped to the creating partner.
        Kunde fischer = persistKunde(p1, "Fischer GmbH", null, address("Marktplatz", "1", "4020", "Linz"));
        Kunde berger = persistKunde(p1, "Berger", "Anna", address("Lindengasse", "8", "4040", "Linz"));
        Kunde mueller = persistKunde(p1, "Müller", "Max", address("Bahnhofstraße", "22", "4600", "Wels"));
        Kunde hofer = persistKunde(p1, "Hofer", "Thomas", address("Feldweg", "3", "4030", "Linz"));
        Kunde koller = persistKunde(p1, "Koller", "Sabine", address("Seestraße", "14", "4810", "Gmunden"));
        Kunde gruber = persistKunde(p2, "Gruber & Söhne GmbH", null, address("Industriestraße", "7", "8010", "Graz"));
        Kunde steiner = persistKunde(p2, "Steiner", "Julia", address("Murgasse", "5", "8020", "Graz"));
        Kunde leitner = persistKunde(p3, "Leitner Vertrieb GmbH", null, address("Dorfplatz", "2", "6020", "Innsbruck"));

        Product bohr = product("Bohrmaschine Profi 750W");
        Product akku = product("Akkuschrauber 18V Set");
        Product pinsel = product("Malerpinsel-Set 5-teilig");
        Product silikon = product("Silikon transparent 300ml");
        Product helm = product("Schutzhelm gelb");
        Product handschuh = product("Handschuhe Gr. L (Set 10 Paar)");
        Product kabel = product("Kabeltrommel 25m");

        // P-1001 (partner-demo): spread over ~5 months, mostly fulfilled, mixed Kunden.
        order(p1, fischer, 158, OrderStatus.FULFILLED, line(akku, 2), line(bohr, 1));
        order(p1, mueller, 142, OrderStatus.FULFILLED, line(kabel, 3));
        order(p1, koller, 120, OrderStatus.FULFILLED, line(helm, 5), line(handschuh, 5));
        order(p1, berger, 111, OrderStatus.FULFILLED, line(bohr, 2));
        order(p1, fischer, 96, OrderStatus.FULFILLED, line(akku, 4));
        order(p1, hofer, 88, OrderStatus.FULFILLED, line(kabel, 2), line(helm, 3));
        order(p1, mueller, 74, OrderStatus.FULFILLED, line(akku, 1), line(handschuh, 2));
        order(p1, fischer, 63, OrderStatus.FULFILLED, line(bohr, 3));
        order(p1, berger, 51, OrderStatus.FULFILLED, line(kabel, 4));
        order(p1, koller, 40, OrderStatus.FULFILLED, line(helm, 8));
        order(p1, fischer, 28, OrderStatus.FULFILLED, line(akku, 2), line(bohr, 1));
        order(p1, hofer, 16, OrderStatus.REVIEWED, line(kabel, 2));
        order(p1, mueller, 9, OrderStatus.REVIEWED, line(handschuh, 4));
        order(p1, berger, 4, OrderStatus.SUBMITTED, line(bohr, 1));
        order(p1, fischer, 1, OrderStatus.SUBMITTED, line(akku, 1));

        // P-1002: fewer, for company-side variety.
        order(p2, gruber, 130, OrderStatus.FULFILLED, line(silikon, 20));
        order(p2, steiner, 97, OrderStatus.FULFILLED, line(pinsel, 10), line(silikon, 12));
        order(p2, gruber, 55, OrderStatus.FULFILLED, line(silikon, 30));
        order(p2, steiner, 22, OrderStatus.REVIEWED, line(pinsel, 6));

        // P-1003 (inactive): a couple of older fulfilled orders.
        order(p3, leitner, 140, OrderStatus.FULFILLED, line(kabel, 5));
        order(p3, leitner, 70, OrderStatus.FULFILLED, line(bohr, 2), line(helm, 4));
    }

    private Kunde persistKunde(Partner owner, String lastName, String firstName, Address address) {
        Kunde kunde = new Kunde();
        kunde.ownerPartner = owner;
        kunde.lastName = lastName;
        kunde.firstName = firstName;
        kunde.address = address;
        kunde.persist();
        return kunde;
    }

    private Product product(String name) {
        return Product.find("name", name).firstResult();
    }

    private record OrderLine(Product product, int quantity) {}

    private OrderLine line(Product product, int quantity) {
        return new OrderLine(product, quantity);
    }

    private void order(Partner partner, Kunde kunde, int daysAgo, OrderStatus status, OrderLine... lines) {
        Order order = new Order();
        order.partner = partner;
        order.kunde = kunde;
        order.orderDate = LocalDate.now().minusDays(daysAgo);
        order.status = status;

        BigDecimal total = BigDecimal.ZERO;
        for (OrderLine l : lines) {
            OrderItem item = new OrderItem();
            item.order = order;
            item.product = l.product();
            item.quantity = l.quantity();
            item.unitPriceSnapshot = l.product().price;
            order.items.add(item);
            total = total.add(l.product().price.multiply(BigDecimal.valueOf(l.quantity())));
        }
        order.orderValue = total;
        order.persist();

        // Commission is snapshotted at order creation in the real flow — mirror that
        // so settlement/commission views have data for the fulfilled orders.
        commissionService.createForOrder(order, partner);
    }
}
