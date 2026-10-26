package at.papo.partnerportal.shop;

import at.papo.partnerportal.business.BusinessTaxIdentifier;
import at.papo.partnerportal.business.BusinessTaxIdentifierType;
import at.papo.partnerportal.common.Address;
import at.papo.partnerportal.enablement.ProductEnablement;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.product.ProductImage;
import at.papo.partnerportal.support.KeycloakTestTokens;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

@QuarkusTest
class PublicShopContentTest {

    private static final String WITH_UPLOAD = "Akkuschrauber 18V Set";
    private static final String WITH_ASSET = "Kabeltrommel 25m";
    private static final String UNAVAILABLE = "Wasserwaage 100cm";
    private static final String NOT_ENABLED = "Bohrmaschine Profi 750W";

    // Own client address per request block, so the public rate limit never interferes.
    private static final AtomicInteger CLIENT = new AtomicInteger(1);

    @Inject
    KeycloakTestTokens tokens;

    @Inject
    ShopPageService pageService;

    private static RequestSpecification visitor() {
        return given().header("X-Forwarded-For", "198.18.0." + (CLIENT.getAndIncrement() % 250 + 1));
    }

    private long createPartner(String... enabledProducts) {
        return QuarkusTransaction.requiringNew().call(() -> {
            Partner partner = new Partner();
            partner.partnerNumber = "CAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            partner.name = "Katalog " + partner.partnerNumber;
            partner.commissionPercent = new BigDecimal("10.00");
            partner.address = new Address("Teststraße", "1", "4020", "Linz", "Österreich");
            partner.taxIdentifier = new BusinessTaxIdentifier("AT", BusinessTaxIdentifierType.VAT_ID, "ATU12345675");
            partner.persist();
            for (String productName : enabledProducts) {
                ProductEnablement enablement = new ProductEnablement();
                enablement.partner = partner;
                enablement.product = Product.find("name", productName).firstResult();
                enablement.persist();
            }
            return partner.id;
        });
    }

    // Creates and releases the shop, returns its kennung.
    private String releasedShop(long partnerId) {
        String token = tokens.tokenFor("company-demo", "company-demo");
        given().auth().oauth2(token).when().post("/api/company/partners/" + partnerId + "/shop/release")
                .then().statusCode(200);
        return given().auth().oauth2(token).when().get("/api/company/partners/" + partnerId + "/shop")
                .then().statusCode(200).extract().path("kennung");
    }

    private static long productId(String name) {
        return QuarkusTransaction.requiringNew().call(() -> Product.<Product>find("name", name).firstResult().id);
    }

    private static long uploadedImageId(String productName) {
        return QuarkusTransaction.requiringNew().call(() -> Product.<Product>find("name", productName).firstResult().image.id);
    }

    @Test
    void themeNavigationAndStartPageArePublished() {
        String kennung = releasedShop(createPartner(WITH_UPLOAD));

        visitor().when().get("/api/shop/" + kennung + "/theme")
                .then().statusCode(200)
                .header("X-Robots-Tag", "noindex")
                .body("colors.primary", is("#1B3A57"))
                .body("headingFont", is("work-sans"))
                .body("form", is("klar"));

        visitor().when().get("/api/shop/" + kennung + "/navigation")
                .then().statusCode(200)
                .body("items.segment", contains("startseite", "artikel"))
                .body("items[0].path", is(""))
                .body("legal.segment", contains("impressum", "agb", "datenschutz", "widerruf"));

        visitor().when().get("/api/shop/" + kennung + "/pages/startseite")
                .then().statusCode(200)
                .body("slot", is("HOME"))
                .body("blocks.type", contains("hero", "article-grid", "image-text"))
                .body("blocks[0].config.buttonPage", is("artikel"));

        visitor().when().get("/api/shop/" + kennung + "/pages/artikeldetail")
                .then().statusCode(200)
                .body("blocks.type", contains("article-detail", "article-grid"));
    }

    @Test
    void systemAndDraftPagesAreNotPublic() {
        long partnerId = createPartner(WITH_UPLOAD);
        String kennung = releasedShop(partnerId);
        QuarkusTransaction.requiringNew().run(() -> {
            PartnerShop shop = PartnerShop.find("kennung", kennung).firstResult();
            pageService.createFreePage(shop, "Über uns", "ueber-uns");
            ShopPage home = ShopPage.find("shop = ?1 and slot = ?2", shop, ShopPageSlot.HOME).firstResult();
            home.draftContent = "{\"blocks\":[{\"id\":\"" + UUID.randomUUID()
                    + "\",\"type\":\"heading-text\",\"config\":{\"heading\":\"Geheimer Entwurf\"}}]}";
        });

        visitor().when().get("/api/shop/" + kennung + "/navigation")
                .then().statusCode(200)
                .body("items.segment", not(hasItem("ueber-uns")));
        visitor().when().get("/api/shop/" + kennung + "/pages/ueber-uns").then().statusCode(404);
        visitor().when().get("/api/shop/" + kennung + "/pages/warenkorb").then().statusCode(404);
        visitor().when().get("/api/shop/" + kennung + "/pages/startseite")
                .then().statusCode(200)
                .body("blocks.type", contains("hero", "article-grid", "image-text"));
    }

    @Test
    void catalogueHasOnlyEnabledAvailableArticlesWithGrossPrice() {
        String kennung = releasedShop(createPartner(WITH_UPLOAD, WITH_ASSET, UNAVAILABLE));
        long imageId = uploadedImageId(WITH_UPLOAD);

        visitor().when().get("/api/shop/" + kennung + "/articles")
                .then().statusCode(200)
                .body("name", containsInAnyOrder(WITH_UPLOAD, WITH_ASSET))
                .body("find { it.name == '" + WITH_UPLOAD + "' }.grossPrice", is(129.00f))
                .body("find { it.name == '" + WITH_UPLOAD + "' }.taxRatePercent", is(20))
                .body("find { it.name == '" + WITH_UPLOAD + "' }.imageUrl",
                        is("/api/shop/" + kennung + "/images/" + imageId))
                .body("find { it.name == '" + WITH_ASSET + "' }.imageUrl", is("/assets/products/elektro.svg"))
                .body("[0].keySet()", containsInAnyOrder("id", "name", "description", "categoryId", "category",
                        "imageUrl", "grossPrice", "taxRatePercent"));

        visitor().when().get("/api/shop/" + kennung + "/articles/" + productId(WITH_ASSET))
                .then().statusCode(200).body("category", is("Elektro"));
        visitor().when().get("/api/shop/" + kennung + "/articles/" + productId(NOT_ENABLED))
                .then().statusCode(404);
        visitor().when().get("/api/shop/" + kennung + "/articles/" + productId(UNAVAILABLE))
                .then().statusCode(404);

        visitor().when().get("/api/shop/" + kennung + "/categories")
                .then().statusCode(200)
                .body("name", contains("Elektro", "Werkzeug"));
    }

    @Test
    void imagesOnlyForOwnCatalogueArticles() {
        String withImage = releasedShop(createPartner(WITH_UPLOAD));
        String withoutImage = releasedShop(createPartner(WITH_ASSET));
        long imageId = uploadedImageId(WITH_UPLOAD);
        long galleryOnlyId = QuarkusTransaction.requiringNew().call(() -> ProductImage.<ProductImage>find(
                "filename", "Standardbild Grün.png").firstResult().id);

        visitor().when().get("/api/shop/" + withImage + "/images/" + imageId)
                .then().statusCode(200)
                .contentType("image/png")
                .header("Cache-Control", startsWith("private"))
                .header("X-Content-Type-Options", "nosniff");

        // Same image id through a shop whose partner does not have the article enabled.
        visitor().when().get("/api/shop/" + withoutImage + "/images/" + imageId).then().statusCode(404);
        // Gallery image used by no article, and an unknown id.
        visitor().when().get("/api/shop/" + withImage + "/images/" + galleryOnlyId).then().statusCode(404);
        visitor().when().get("/api/shop/" + withImage + "/images/999999").then().statusCode(404);
    }

    @Test
    void unreachableShopHidesAllContent() {
        long partnerId = createPartner(WITH_UPLOAD);
        String kennung = given().auth().oauth2(tokens.tokenFor("company-demo", "company-demo"))
                .when().get("/api/company/partners/" + partnerId + "/shop")
                .then().statusCode(200).extract().path("kennung");

        for (String path : new String[] { "/theme", "/navigation", "/pages/startseite", "/articles",
                "/categories", "/images/" + uploadedImageId(WITH_UPLOAD) }) {
            visitor().when().get("/api/shop/" + kennung + path)
                    .then().statusCode(404)
                    .body("message", is("Dieser Shop ist derzeit nicht erreichbar."))
                    .body("grossPrice", nullValue());
        }
    }
}
