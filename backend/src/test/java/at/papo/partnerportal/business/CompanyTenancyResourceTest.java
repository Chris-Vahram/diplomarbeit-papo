package at.papo.partnerportal.business;

import at.papo.partnerportal.category.Category;
import at.papo.partnerportal.order.Order;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.partner.PartnerLogin;
import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.product.ProductImage;
import at.papo.partnerportal.seed.DemoDataSeeder;
import at.papo.partnerportal.support.KeycloakTestTokens;
import at.papo.partnerportal.support.TestCompanies;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

// Cross-tenant isolation (add-company-tenancy): company-zwei / partner-zwei must never see,
// change or reference data of company 1 (company-demo / partner-demo), and the company is
// taken only from the verified login binding.
@QuarkusTest
class CompanyTenancyResourceTest {

    @Inject
    KeycloakTestTokens tokens;

    String zwei;
    String partnerZwei;
    String demo;

    long companyOneId;
    long foreignPartnerId;
    long foreignProductId;
    long foreignCategoryId;
    long foreignImageId;
    long foreignOrderId;

    @BeforeEach
    void setUp() {
        zwei = tokens.tokenFor("company-zwei", "company-zwei");
        partnerZwei = tokens.tokenFor("partner-zwei", "partner-zwei");
        demo = tokens.tokenFor("company-demo", "company-demo");
        QuarkusTransaction.requiringNew().run(() -> {
            CompanyProfile one = TestCompanies.one();
            companyOneId = one.id;
            foreignPartnerId = Partner.<Partner>find("partnerNumber", "PARTNER-DEMO").firstResult().id;
            foreignProductId = Product.<Product>find("company = ?1 order by id", one).firstResult().id;
            foreignCategoryId = Category.<Category>find("company = ?1 order by id", one).firstResult().id;
            foreignImageId = ProductImage.<ProductImage>find("company = ?1 order by id", one).firstResult().id;
        });
        // Complete orders (items + commission record) in both companies, so unscoped list or
        // export queries would really return foreign rows.
        foreignOrderId = order(tokens.tokenFor("partner-demo", "partner-demo"), "Bohrmaschine Profi 750W");
        ownOrderId = order(partnerZwei, "Testartikel Zwei A");
    }

    long ownOrderId;

    private long order(String partnerToken, String productName) {
        long productId = QuarkusTransaction.requiringNew().call(() ->
                Product.<Product>find("name", productName).firstResult().id);
        return given().auth().oauth2(partnerToken).contentType(ContentType.JSON)
                .body(Map.of("newKunde", Map.of("lastName", "Isolationstest", "address", Map.of("street", "Weg",
                                "houseNumber", "1", "postalCode", "1010", "city", "Wien", "country", "Österreich")),
                        "items", java.util.List.of(Map.of("productId", productId, "quantity", 1))))
                .when().post("/api/partner/orders").then().statusCode(200).extract().jsonPath().getLong("id");
    }

    @Test
    void companyUserSeesOnlyOwnProfile() {
        given().auth().oauth2(zwei).when().get("/api/company/profile")
                .then().statusCode(200).body("businessName", is("Testfirma Zwei GmbH"));
    }

    @Test
    void headersNamingAnotherCompanyAreIgnored() {
        given().auth().oauth2(zwei)
                .header("X-Company-Id", String.valueOf(companyOneId))
                .header("X-Forwarded-Host", "huemer.paportal.at")
                .queryParam("companyId", companyOneId)
                .when().get("/api/company/profile")
                .then().statusCode(200).body("businessName", is("Testfirma Zwei GmbH"));
    }

    @Test
    void revokedCompanyLoginIsDenied() {
        setCompanyLoginRevoked(true);
        try {
            given().auth().oauth2(zwei).when().get("/api/company/partners")
                    .then().statusCode(403).body("message", is("Kein Firmenkonto mit diesem Login verknüpft."));
        } finally {
            setCompanyLoginRevoked(false);
        }
    }

    @Test
    void listsContainOnlyOwnCompanyRows() {
        given().auth().oauth2(zwei).when().get("/api/company/partners")
                .then().statusCode(200).body("partnerNumber", everyItem(is("Z-0001")));
        given().auth().oauth2(zwei).when().get("/api/company/products")
                .then().statusCode(200).body("name", everyItem(containsString("Testartikel Zwei")));
        given().auth().oauth2(zwei).when().get("/api/company/categories")
                .then().statusCode(200).body("id", not(hasItem((int) foreignCategoryId)));
        given().auth().oauth2(zwei).when().get("/api/product-images")
                .then().statusCode(200).body("id", not(hasItem((int) foreignImageId)));
        given().auth().oauth2(zwei).when().get("/api/company/orders")
                .then().statusCode(200).body("id", hasItem((int) ownOrderId))
                .body("id", not(hasItem((int) foreignOrderId)));
        long ownUnseen = QuarkusTransaction.requiringNew().call(() ->
                Order.count("seenByCompany = false and partner.company = ?1", TestCompanies.two()));
        given().auth().oauth2(zwei).when().get("/api/company/orders/unseen-count")
                .then().statusCode(200).body(is(String.valueOf(ownUnseen)));
        given().auth().oauth2(zwei).when().get("/api/company/dashboard")
                .then().statusCode(200).body("settlement.partnerName", everyItem(is("Zweitpartner Test OG")));
        given().auth().oauth2(zwei).when().get("/api/company/shops")
                .then().statusCode(200).body("partnerId", not(hasItem((int) foreignPartnerId)));
    }

    @Test
    void exportsContainNoForeignRows() {
        String orders = given().auth().oauth2(zwei).when().get("/api/company/orders/export")
                .then().statusCode(200).extract().asString();
        String commissions = given().auth().oauth2(zwei).when().get("/api/company/commissions/export")
                .then().statusCode(200).extract().asString();
        assertEquals(true, orders.contains("Zweitpartner Test OG"), "own order present");
        assertEquals(true, commissions.contains("Zweitpartner Test OG"), "own commission present");
        assertEquals(false, orders.contains("Huemer") || orders.contains("Sanitär"));
        assertEquals(false, commissions.contains("Huemer") || commissions.contains("Sanitär"));
    }

    @Test
    void foreignIdsBehaveLikeMissingOnes() {
        String partnerPath = "/api/company/partners/" + foreignPartnerId;
        given().auth().oauth2(zwei).when().get(partnerPath).then().statusCode(404);
        given().auth().oauth2(zwei).when().get(partnerPath + "/customers").then().statusCode(404);
        given().auth().oauth2(zwei).when().get(partnerPath + "/logins").then().statusCode(404);
        given().auth().oauth2(zwei).when().get(partnerPath + "/trade-proof").then().statusCode(404);
        given().auth().oauth2(zwei).when().get(partnerPath + "/products").then().statusCode(404);
        given().auth().oauth2(zwei).when().get(partnerPath + "/shop").then().statusCode(404);
        given().auth().oauth2(zwei).when().get("/api/company/products/" + foreignProductId).then().statusCode(404);
        given().auth().oauth2(zwei).contentType(ContentType.JSON)
                .body(Map.of("name", "Übernahme", "active", true))
                .when().put("/api/company/categories/" + foreignCategoryId).then().statusCode(404);
        given().auth().oauth2(zwei).contentType(ContentType.JSON).body(Map.of("status", "REVIEWED"))
                .when().put("/api/company/orders/" + foreignOrderId + "/status").then().statusCode(404);
        given().auth().oauth2(zwei).when().put("/api/company/orders/" + foreignOrderId + "/seen")
                .then().statusCode(404);
        given().auth().oauth2(zwei).when().get("/api/company/orders/" + foreignOrderId + "/emails")
                .then().statusCode(404);
        given().auth().oauth2(zwei).when().get("/api/product-images/" + foreignImageId).then().statusCode(404);
        given().auth().oauth2(partnerZwei).when().get("/api/product-images/" + foreignImageId).then().statusCode(404);
    }

    @Test
    void foreignReferencesInPayloadsAreRejected() {
        long ownPartnerId = ownPartnerId();
        given().auth().oauth2(zwei).contentType(ContentType.JSON)
                .body(Map.of("name", "Fremdkategorie", "price", 1, "categoryId", foreignCategoryId))
                .when().post("/api/company/products").then().statusCode(400);
        given().auth().oauth2(zwei).contentType(ContentType.JSON)
                .body(Map.of("name", "Fremdbild", "price", 1, "imageUrl", "/product-images/" + foreignImageId))
                .when().post("/api/company/products").then().statusCode(404);
        given().auth().oauth2(zwei)
                .when().put("/api/company/partners/" + ownPartnerId + "/products/" + foreignProductId)
                .then().statusCode(404);
        given().auth().oauth2(zwei).contentType(ContentType.JSON)
                .body(Map.of("partnerId", foreignPartnerId,
                        "newKunde", Map.of("lastName", "Test", "address", Map.of("street", "Weg", "houseNumber", "1",
                                "postalCode", "1010", "city", "Wien", "country", "Österreich")),
                        "items", java.util.List.of(Map.of("productId", foreignProductId, "quantity", 1))))
                .when().post("/api/company/orders").then().statusCode(404);
    }

    @Test
    void partnerNumberIsUniquePerCompanyOnly() {
        Map<String, Object> partner = Map.of(
                "partnerNumber", "P-1002", "name", "Gleiche Nummer GmbH", "commissionPercent", 10,
                "address", Map.of("street", "Weg", "houseNumber", "1", "postalCode", "1010", "city", "Wien",
                        "country", "Österreich"),
                "taxIdentifier", Map.of("country", "AT", "type", "VAT_ID", "value", "ATU44444444"));
        long id = given().auth().oauth2(zwei).contentType(ContentType.JSON).body(partner)
                .when().post("/api/company/partners").then().statusCode(200).extract().jsonPath().getLong("id");
        try {
            given().auth().oauth2(zwei).contentType(ContentType.JSON).body(partner)
                    .when().post("/api/company/partners").then().statusCode(409);
        } finally {
            QuarkusTransaction.requiringNew().run(() -> Partner.deleteById(id));
        }
    }

    @Test
    void companySubjectCannotBeBoundAsPartnerLogin() {
        given().auth().oauth2(demo).contentType(ContentType.JSON)
                .body(Map.of("oidcSubject", DemoDataSeeder.COMPANY_ZWEI_SUBJECT))
                .when().post("/api/company/partners/" + foreignPartnerId + "/logins")
                .then().statusCode(409);
        assertEquals(0L, QuarkusTransaction.requiringNew().call(() ->
                PartnerLogin.count("oidcSubject", DemoDataSeeder.COMPANY_ZWEI_SUBJECT)));
    }

    @Test
    void partnerSeesOnlyOwnCompanyCatalogue() {
        given().auth().oauth2(partnerZwei).when().get("/api/partner/products")
                .then().statusCode(200).body("name", everyItem(containsString("Testartikel Zwei")));
        given().auth().oauth2(partnerZwei).when().get("/api/partner/dashboard")
                .then().statusCode(200).body("totalProductCount", is(2));
    }

    @Test
    void productImagesAreNotCacheableAcrossLogins() {
        long ownImage = QuarkusTransaction.requiringNew().call(() ->
                ProductImage.<ProductImage>find("company = ?1", TestCompanies.two()).firstResult().id);
        given().auth().oauth2(zwei).when().get("/api/product-images/" + ownImage)
                .then().statusCode(200).header("Cache-Control", is("private, no-store"));
    }

    @Test
    void companyMailUsesTheOrdersCompanyAndRecordsMissingRecipient() {
        setZweiRecipient("");
        long withoutRecipient = order(partnerZwei, "Testartikel Zwei A");
        setZweiRecipient("bestellungen@testfirma-zwei.example");
        long withRecipient = order(partnerZwei, "Testartikel Zwei A");
        QuarkusTransaction.requiringNew().run(() -> {
            at.papo.partnerportal.mail.OrderEmail skipped = companyMail(withoutRecipient);
            assertEquals("SKIPPED", skipped.status.name());
            assertEquals("NO_RECIPIENT", skipped.skipReason);
            assertEquals("bestellungen@testfirma-zwei.example", companyMail(withRecipient).recipientAddress);
        });
    }

    private static at.papo.partnerportal.mail.OrderEmail companyMail(long orderId) {
        return at.papo.partnerportal.mail.OrderEmail.find("order.id = ?1 and recipientRole = ?2", orderId,
                at.papo.partnerportal.mail.OrderEmail.RecipientRole.COMPANY).firstResult();
    }

    private void setZweiRecipient(String address) {
        QuarkusTransaction.requiringNew().run(() -> TestCompanies.two().orderNoticeEmail = address);
    }

    private long ownPartnerId() {
        return QuarkusTransaction.requiringNew().call(() ->
                Partner.<Partner>find("partnerNumber", "Z-0001").firstResult().id);
    }

    private void setCompanyLoginRevoked(boolean revoked) {
        QuarkusTransaction.requiringNew().run(() -> {
            CompanyLogin login = CompanyLogin.find("oidcSubject", DemoDataSeeder.COMPANY_ZWEI_SUBJECT).firstResult();
            login.revokedAt = revoked ? Instant.now() : null;
        });
    }

}
