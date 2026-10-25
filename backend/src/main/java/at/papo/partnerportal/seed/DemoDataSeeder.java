package at.papo.partnerportal.seed;

import at.papo.partnerportal.category.Category;
import at.papo.partnerportal.category.CategoryNames;
import at.papo.partnerportal.commission.CommissionService;
import at.papo.partnerportal.business.BusinessTaxIdentifier;
import at.papo.partnerportal.business.BusinessTaxIdentifierMigrationService;
import at.papo.partnerportal.business.BusinessTaxIdentifierType;
import at.papo.partnerportal.business.CompanyLogin;
import at.papo.partnerportal.business.CompanyProfile;
import at.papo.partnerportal.business.CompanyTenancyMigrationService;
import at.papo.partnerportal.business.TradeRegisterIdentifier;
import at.papo.partnerportal.business.TradeRegisterIdentifierType;
import at.papo.partnerportal.common.Address;
import at.papo.partnerportal.customer.Kunde;
import at.papo.partnerportal.enablement.ProductEnablement;
import at.papo.partnerportal.order.Order;
import at.papo.partnerportal.order.OrderItem;
import at.papo.partnerportal.order.OrderStatus;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.partner.PartnerLogin;
import at.papo.partnerportal.product.DemoPngGenerator;
import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.product.ProductImage;
import at.papo.partnerportal.product.ProductImageDto;
import at.papo.partnerportal.product.ProductImageService;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Dev/test always seed (LaunchMode DEVELOPMENT/TEST), matching the drop-and-create
// schema strategy for those profiles (docs/CONVENTIONS.md, docs/SETUP.md). Under
// LaunchMode.NORMAL (packaged/deployed runs, e.g. the VM) seeding only happens when
// explicitly opted into via papo.seed-demo-data=true (SEED_DEMO_DATA env var, VM-local
// .env.prod only - see docs/DEPLOYMENT.md); it must be turned off again before real
// customer partners/articles are onboarded. Not a raw import.sql: Hibernate's default
// pooled sequence optimizer (block size 50) would collide hand-picked seed ids with the
// first id the app allocates at runtime, so seed rows are persisted through Panache
// like any other entity and let Hibernate assign ids itself.
@ApplicationScoped
public class DemoDataSeeder {

    @Inject
    CommissionService commissionService;

    @Inject
    ProductImageService productImageService;


    @Inject
    BusinessTaxIdentifierMigrationService businessTaxIdentifierMigrationService;

    @Inject
    CompanyTenancyMigrationService companyTenancyMigrationService;

    // Company 1 of the current seed run (set by seedCompanyProfile).
    private CompanyProfile company;

    @ConfigProperty(name = "papo.seed-demo-data", defaultValue = "false")
    boolean seedDemoData;

    // Tests of pre-tenancy migrations switch the second dev/test company off.
    @ConfigProperty(name = "papo.seed.second-company", defaultValue = "true")
    boolean seedSecondCompany;

    @Transactional
    void onStart(@Observes StartupEvent event) {
        LaunchMode launchMode = LaunchMode.current();
        businessTaxIdentifierMigrationService.lockPartnerTable();
        if (shouldSeed(launchMode, Partner.count(), seedDemoData)) {
            seedCompanyProfile();
            seedPartners();
            // Fixed Keycloak user id of partner-demo exists only in the committed dev/test realms;
            // a deployed Keycloak generates its own id, so NORMAL mode never seeds a binding
            // (bind-partner-login-to-oidc-subject design "Dev/test identity").
            if (launchMode != LaunchMode.NORMAL) {
                seedPartnerDemoLogin();
                seedCompanyLogin(COMPANY_DEMO_SUBJECT, company);
            }
            seedProducts();
            seedProductImages();
            seedEnablements();
            // A second, clearly fictitious company for cross-tenant checks; dev/test only
            // (add-company-tenancy D7: real companies are provisioned by the operator).
            if (launchMode != LaunchMode.NORMAL && seedSecondCompany) {
                seedCompanyTwo();
            }
            // Tests assume a clean order baseline, so seed orders only in dev or
            // in the explicit NORMAL+flag demo path.
            if (launchMode == LaunchMode.DEVELOPMENT || launchMode == LaunchMode.NORMAL) {
                seedOrders();
            }
        }
        businessTaxIdentifierMigrationService.migrate(launchMode != LaunchMode.NORMAL || seedDemoData);
        companyTenancyMigrationService.expand();
    }

    static boolean shouldSeed(LaunchMode launchMode, long partnerCount) {
        return shouldSeed(launchMode, partnerCount, false);
    }

    static boolean shouldSeed(LaunchMode launchMode, long partnerCount, boolean seedDemoData) {
        return partnerCount == 0 && (launchMode != LaunchMode.NORMAL || seedDemoData);
    }

    // Must match the "id" of partner-demo in keycloak/partnerportal-realm.json and the test realm.
    public static final String PARTNER_DEMO_SUBJECT = "7c1d9a2e-4b3f-4e5a-9d6c-2f8b1a0e5c01";
    // Fixed ids of company-demo, company-zwei and partner-zwei in both committed realms.
    public static final String COMPANY_DEMO_SUBJECT = "c0a1d2e3-0000-4000-8000-000000000001";
    public static final String COMPANY_ZWEI_SUBJECT = "c0a1d2e3-0000-4000-8000-000000000002";
    public static final String PARTNER_ZWEI_SUBJECT = "c0a1d2e3-0000-4000-8000-000000000003";

    private void seedCompanyLogin(String subject, CompanyProfile owner) {
        CompanyLogin login = new CompanyLogin();
        login.oidcSubject = subject;
        login.company = owner;
        login.createdBy = "demo-seed";
        login.persist();
    }

    private void seedCompanyTwo() {
        CompanyProfile zwei = new CompanyProfile();
        zwei.businessName = "Testfirma Zwei GmbH";
        zwei.taxIdentifier = new BusinessTaxIdentifier("AT", BusinessTaxIdentifierType.VAT_ID, "ATU22222222");
        zwei.tradeRegisterIdentifier = new TradeRegisterIdentifier("AT", TradeRegisterIdentifierType.GISA, "87654321");
        zwei.persist();
        seedCompanyLogin(COMPANY_ZWEI_SUBJECT, zwei);

        CompanyProfile companyOne = company;
        company = zwei;
        persistPartner("Z-0001", "Zweitpartner Test OG", address("Teststraße", "2", "1010", "Wien"),
                "zweitpartner@example.at", "+43 1 2222222", "10.00", true, "ATU33333333");
        Partner partner = Partner.find("partnerNumber", "Z-0001").firstResult();
        PartnerLogin login = new PartnerLogin();
        login.oidcSubject = PARTNER_ZWEI_SUBJECT;
        login.partner = partner;
        login.createdBy = "demo-seed";
        login.persist();
        // Same category name as company 1 on purpose: category names are unique per company.
        persistProduct("Testartikel Zwei A", "10.00", "Artikel der Testfirma Zwei.", true, "Werkzeug", null);
        persistProduct("Testartikel Zwei B", "20.00", "Artikel der Testfirma Zwei.", true, null, null);
        productImageService.upload(zwei, DemoPngGenerator.solidColorPng(64, 64, 160, 60, 60), "Bild Zwei.png");
        for (String name : List.of("Testartikel Zwei A", "Testartikel Zwei B")) {
            ProductEnablement enablement = new ProductEnablement();
            enablement.partner = partner;
            enablement.product = product(name);
            enablement.persist();
        }
        company = companyOne;
    }

    private void seedPartnerDemoLogin() {
        PartnerLogin login = new PartnerLogin();
        login.oidcSubject = PARTNER_DEMO_SUBJECT;
        login.partner = Partner.find("partnerNumber", "PARTNER-DEMO").firstResult();
        login.createdBy = "demo-seed";
        login.persist();
    }

    private void seedPartners() {
        // Canonical (upper-case) demo partner number; the login is linked via PartnerLogin (OIDC subject).
        persistPartner("PARTNER-DEMO", "Huemer GmbH", address("Hauptstraße", "12", "4020", "Linz"),
                "partner-demo@example.at", "+43 660 1234567", "10.00", true, "ATU12345678");
        // No linked Keycloak user yet — demonstrates the manual-provisioning gap (design.md).
        persistPartner("P-1002", "Sanitär Bauer KG", address("Ringstraße", "5", "8010", "Graz"),
                "office@sanitaer-bauer.example.at", "+43 664 2345678", "5.00", true, "ATU23456789");
        // Inactive — for FR-23 inactive-partner testing (Phase 2).
        persistPartner("P-1003", "Baumarkt Weber e.U.", address("Industriezeile", "3", "6020", "Innsbruck"),
                "info@weber-baumarkt.example.at", "+43 512 3456789", "15.00", false, "ATU34567890");
    }

    private void seedCompanyProfile() {
        if (!shouldSeedCompanyProfile(CompanyProfile.count())) {
            company = CompanyProfile.find("singletonKey", CompanyProfile.CURRENT_KEY).firstResult();
            return;
        }
        CompanyProfile profile = new CompanyProfile();
        profile.singletonKey = CompanyProfile.CURRENT_KEY;
        profile.businessName = "Huemer GmbH";
        profile.taxIdentifier = new BusinessTaxIdentifier("AT", BusinessTaxIdentifierType.VAT_ID, "ATU87654321");
        profile.tradeRegisterIdentifier = new TradeRegisterIdentifier("AT", TradeRegisterIdentifierType.GISA, "12345678");
        profile.persist();
        company = profile;
    }

    static boolean shouldSeedCompanyProfile(long profileCount) {
        return profileCount == 0;
    }

    // Every seed address is in Austria; the country part is fixed here.
    private Address address(String street, String houseNumber, String postalCode, String city) {
        return new Address(street, houseNumber, postalCode, city, "Österreich");
    }

    private void persistPartner(String partnerNumber, String name, Address address, String email,
            String phone, String commissionPercent, boolean active, String taxIdentifierValue) {
        Partner partner = new Partner();
        partner.company = company;
        partner.partnerNumber = partnerNumber;
        partner.name = name;
        partner.address = address;
        partner.taxIdentifier = new BusinessTaxIdentifier("AT", BusinessTaxIdentifierType.VAT_ID, taxIdentifierValue);
        partner.email = email;
        partner.phone = phone;
        partner.commissionPercent = new BigDecimal(commissionPercent);
        partner.active = active;
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
        product.company = company;
        product.name = name;
        product.price = new BigDecimal(price);
        product.description = description;
        product.available = available;
        if (category != null) {
            String normalized = CategoryNames.normalized(category);
            Category existing = Category.find("company = ?1 and normalizedName = ?2", company, normalized).firstResult();
            if (existing == null) {
                existing = new Category();
                existing.company = company;
                existing.name = category;
                existing.normalizedName = normalized;
                existing.active = true;
                existing.persist();
            }
            product.articleCategory = existing;
        }
        product.imageUrl = imageUrl;
        product.persist();
    }

    // Seeds the reusable image gallery with real, validator-passing raster images (see
    // DemoPngGenerator - pure JDK, no binary asset in the repo) and points one product
    // at an uploaded image via the actual upload path, so the internal-image reference
    // (Product.image / imageUrl = "/api/product-images/{id}") demos out of the box.
    // The other seed products keep their external frontend-asset imageUrl untouched;
    // "Wasserwaage 100cm" deliberately keeps no image at all (placeholder-path demo).
    private void seedProductImages() {
        ProductImageDto blau = productImageService.upload(company,
                DemoPngGenerator.solidColorPng(64, 64, 58, 90, 120), "Standardbild Blau.png");
        productImageService.upload(company,
                DemoPngGenerator.solidColorPng(64, 64, 90, 140, 90), "Standardbild Grün.png");

        Product akkuschrauber = product("Akkuschrauber 18V Set");
        akkuschrauber.image = ProductImage.findById(blau.id());
        akkuschrauber.imageUrl = blau.url();
    }

    // A few enabled products for "partner-demo" (PARTNER-DEMO) so the enabled-products
    // view (product-enablement spec) has demo data out of the box.
    private void seedEnablements() {
        Partner partnerDemo = Partner.find("partnerNumber", "PARTNER-DEMO").firstResult();
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
        Partner p1 = Partner.find("partnerNumber", "PARTNER-DEMO").firstResult();
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

        // PARTNER-DEMO (partner-demo): spread over ~5 months, mostly fulfilled, mixed Kunden.
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
