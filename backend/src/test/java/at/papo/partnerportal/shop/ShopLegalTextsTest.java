package at.papo.partnerportal.shop;

import at.papo.partnerportal.business.BusinessTaxIdentifier;
import at.papo.partnerportal.business.BusinessTaxIdentifierType;
import at.papo.partnerportal.common.Address;
import at.papo.partnerportal.enablement.ProductEnablement;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.support.KeycloakTestTokens;
import at.papo.partnerportal.support.TestCompanies;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

// company-profile spec "Company maintains shop legal texts", shop-legal spec "Legal system pages",
// "Fixed legal footer" and the staging half of "Placeholder marking and release gate".
@QuarkusTest
class ShopLegalTextsTest {

    static final String HTML_AGB = "# AGB\n\n<script>alert(1)</script> Es gilt österreichisches Recht.";

    @Inject
    KeycloakTestTokens tokens;

    static long createShopPartner(String email) {
        return QuarkusTransaction.requiringNew().call(() -> {
            Partner partner = new Partner();
            partner.company = TestCompanies.one();
            partner.partnerNumber = "LEG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            partner.name = "Recht " + partner.partnerNumber;
            partner.email = email;
            partner.phone = "+43 732 1234";
            partner.commissionPercent = new BigDecimal("10.00");
            partner.address = new Address("Teststraße", "1", "4020", "Linz", "Österreich");
            partner.taxIdentifier = new BusinessTaxIdentifier("AT", BusinessTaxIdentifierType.VAT_ID, "ATU12345675");
            partner.persist();
            ProductEnablement enablement = new ProductEnablement();
            enablement.partner = partner;
            enablement.product = Product.find("available = true order by id").firstResult();
            enablement.persist();
            return partner.id;
        });
    }

    static void putTexts(String token, Map<String, Object> texts) {
        given().auth().oauth2(token).contentType("application/json").body(texts)
                .when().put("/api/company/profile/legal-texts")
                .then().statusCode(200);
    }

    static Map<String, Object> allTexts() {
        return Map.of("impressum", "Impressum Huemer GmbH", "agb", HTML_AGB,
                "datenschutz", "Datenschutz", "widerruf", "Widerruf\n\nMuster-Widerrufsformular");
    }

    @Test
    void companySavesTextsAndShopsShowThemEscapedOrAsMarkedPlaceholder() {
        String company = tokens.tokenFor("company-demo", "company-demo");
        long partnerId = createShopPartner("kontakt@recht.example");
        try {
            putTexts(company, Map.of("agb", HTML_AGB, "impressum", "  "));
            given().auth().oauth2(company).when().get("/api/company/profile/legal-texts")
                    .then().statusCode(200)
                    .body("agb", is(HTML_AGB))
                    .body("impressum", nullValue())
                    .body("missing", contains("IMPRESSUM", "DATENSCHUTZ", "WIDERRUF"));

            // Staging: release works with placeholders, the dialog learns what is missing.
            String kennung = given().auth().oauth2(company)
                    .when().post("/api/company/partners/" + partnerId + "/shop/release")
                    .then().statusCode(200)
                    .body("status", is("ONLINE"))
                    .body("missingLegalTexts", is(3))
                    .body("legalGate", is(false))
                    .extract().path("kennung");

            // Stored as typed; the shop renderer escapes it.
            given().when().get("/api/shop/" + kennung + "/legal/agb")
                    .then().statusCode(200)
                    .body("title", is("AGB"))
                    .body("text", is(HTML_AGB))
                    .body("placeholder", is(false));
            given().when().get("/api/shop/" + kennung + "/legal/impressum")
                    .then().statusCode(200)
                    .body("placeholder", is(true))
                    .body("text", containsString("Platzhalter – kein gültiger Rechtstext"));
            given().when().get("/api/shop/" + kennung + "/legal/kasse")
                    .then().statusCode(404);

            given().when().get("/api/shop/" + kennung + "/navigation")
                    .then().statusCode(200)
                    .body("sellerName", is("Huemer GmbH"))
                    .body("contact.email", is("kontakt@recht.example"))
                    .body("contact.phone", is("+43 732 1234"));

            putTexts(company, allTexts());
            given().auth().oauth2(company).when().get("/api/company/profile/legal-texts")
                    .then().statusCode(200).body("missing", empty());
        } finally {
            putTexts(company, Map.of());
        }
    }

    @Test
    void textsAreLimitedAndOnlyForCompanyUsers() {
        String company = tokens.tokenFor("company-demo", "company-demo");
        given().auth().oauth2(company).contentType("application/json")
                .body(Map.of("agb", "A".repeat(30_001)))
                .when().put("/api/company/profile/legal-texts")
                .then().statusCode(400);
        given().auth().oauth2(company).contentType("application/json")
                .body(Map.of("agb", "A".repeat(30_000)))
                .when().put("/api/company/profile/legal-texts")
                .then().statusCode(200);
        putTexts(company, Map.of());

        String partner = tokens.tokenFor("partner-demo", "partner-demo");
        given().auth().oauth2(partner).when().get("/api/company/profile/legal-texts").then().statusCode(403);
        given().auth().oauth2(partner).contentType("application/json").body(Map.of("agb", "x"))
                .when().put("/api/company/profile/legal-texts").then().statusCode(403);
    }
}
