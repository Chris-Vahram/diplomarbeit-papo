package at.papo.partnerportal.shop;

import at.papo.partnerportal.support.TestCompanies;
import at.papo.partnerportal.business.BusinessTaxIdentifier;
import at.papo.partnerportal.business.BusinessTaxIdentifierType;
import at.papo.partnerportal.common.Address;
import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.enablement.ProductEnablement;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.DemoPngGenerator;
import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.support.KeycloakTestTokens;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ShopAssetServiceTest {

    private static final byte[] PNG = DemoPngGenerator.solidColorPng(4, 4, 10, 92, 54);

    @Inject
    ShopAssetService assetService;

    @Inject
    ShopEditorService editorService;

    @Inject
    KeycloakTestTokens tokens;

    @Inject
    ObjectMapper json;

    private long createPartner(String... enabledProducts) {
        return QuarkusTransaction.requiringNew().call(() -> {
            Partner partner = new Partner();
            partner.company = TestCompanies.one();
            partner.partnerNumber = "ASSET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            partner.name = "Bilder Test " + partner.partnerNumber;
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

    private static <T> T tx(Callable<T> action) {
        return QuarkusTransaction.requiringNew().call(action);
    }

    private static String rejection(Callable<?> action) {
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> tx(action));
        for (Throwable cause = thrown; cause != null; cause = cause.getCause()) {
            if (cause instanceof ApiException api) {
                return api.getMessage();
            }
        }
        throw new AssertionError("Expected ApiException", thrown);
    }

    private static Partner partner(long id) {
        return Partner.findById(id);
    }

    private long upload(long partnerId) {
        return tx(() -> assetService.upload(partner(partnerId), PNG, "C:\\Bilder\\laden.png", true)).id();
    }

    // Puts an image block with the asset on the Startseite draft; publishes it if asked.
    private void useOnHome(long partnerId, long assetId, boolean publish) throws Exception {
        ShopPageDto home = tx(() -> editorService.pages(partner(partnerId)).pages().get(0));
        var draft = json.readTree("{\"blocks\":[{\"id\":\"" + UUID.randomUUID()
                + "\",\"type\":\"image\",\"config\":{\"imageId\":" + assetId + ",\"alt\":\"Laden\"}}]}");
        ShopPageDto saved = tx(() -> editorService.saveDraft(partner(partnerId), home.id(),
                new ShopEditorRequests.Draft(draft, home.version())));
        if (publish) {
            tx(() -> editorService.publish(partner(partnerId), saved.id(), saved.version()));
        }
    }

    @Test
    void uploadAcceptsOnlyConfirmedPngJpegWebp() {
        long partnerId = createPartner();
        assertEquals("Bitte bestätigen Sie, dass Sie die Rechte an diesem Bild haben.",
                rejection(() -> assetService.upload(partner(partnerId), PNG, "a.png", false)));
        byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>"
                .getBytes(StandardCharsets.UTF_8);
        assertEquals("Bitte PNG, JPG oder WebP verwenden.",
                rejection(() -> assetService.upload(partner(partnerId), svg, "logo.png", true)));
        byte[] tooLarge = new byte[5 * 1024 * 1024 + 1];
        System.arraycopy(PNG, 0, tooLarge, 0, PNG.length);
        assertEquals("Das Bild ist zu groß (maximal 5 MB).",
                rejection(() -> assetService.upload(partner(partnerId), tooLarge, "gross.png", true)));

        long assetId = upload(partnerId);
        ShopAssetListDto list = tx(() -> assetService.list(partner(partnerId)));
        assertEquals(1, list.assets().size());
        assertEquals(assetId, list.assets().get(0).id());
        assertEquals("laden.png", list.assets().get(0).filename());
        assertEquals("image/png", list.assets().get(0).contentType());
        assertEquals(PNG.length, list.usedBytes());
        assertEquals(50L * 1024 * 1024, list.quotaBytes());
    }

    @Test
    void quotaOfFiftyMegabytesPerShop() {
        long partnerId = createPartner();
        long other = createPartner();
        tx(() -> assetService.list(partner(partnerId)));
        tx(() -> {
            // Size bookkeeping only; the quota sums sizeBytes.
            ShopAsset filler = new ShopAsset();
            filler.shop = PartnerShop.find("partner", partner(partnerId)).firstResult();
            filler.data = PNG;
            filler.contentType = "image/png";
            filler.filename = "gross.png";
            filler.sizeBytes = ShopAssetService.QUOTA_BYTES - PNG.length + 1;
            filler.uploadedAt = Instant.now();
            filler.rightsConfirmed = true;
            filler.persist();
            return null;
        });
        assertEquals("Speicherplatz voll – bitte nicht verwendete Bilder löschen.",
                rejection(() -> assetService.upload(partner(partnerId), PNG, "a.png", true)));
        // The quota is per shop.
        upload(other);
    }

    @Test
    void blocksMayOnlyUseTheShopsOwnAssets() throws Exception {
        long owner = createPartner();
        long stranger = createPartner();
        long assetId = upload(owner);
        assertTrue(rejection(() -> {
            useOnHome(stranger, assetId, false);
            return null;
        }).contains("nicht verfügbar"));
        assertEquals("Bild nicht gefunden.", rejection(() -> assetService.find(partner(stranger), assetId)));
        assertEquals("Bild nicht gefunden.", rejection(() -> {
            assetService.delete(partner(stranger), assetId);
            return null;
        }));
        useOnHome(owner, assetId, false);
        // Draft use counts in the library exactly like it does for deleting.
        assertEquals(List.of("Startseite"), tx(() -> assetService.list(partner(owner))).assets().get(0).usedOn());
    }

    @Test
    void usedImageCannotBeDeletedAndNamesThePage() throws Exception {
        long partnerId = createPartner();
        long used = upload(partnerId);
        long unused = upload(partnerId);
        useOnHome(partnerId, used, true);

        assertEquals("Das Bild wird noch verwendet auf: Startseite.", rejection(() -> {
            assetService.delete(partner(partnerId), used);
            return null;
        }));
        assertEquals(List.of("Startseite"), tx(() -> assetService.list(partner(partnerId))).assets().stream()
                .filter(asset -> asset.id() == used).findFirst().orElseThrow().usedOn());
        tx(() -> {
            assetService.delete(partner(partnerId), unused);
            return null;
        });
        assertEquals(1, tx(() -> assetService.list(partner(partnerId))).assets().size());
    }

    @Test
    void publicAssetOnlyWhilePublishedOnAnOnlinePageOfThatShop() throws Exception {
        long partnerId = createPartner("Kabeltrommel 25m");
        long otherId = createPartner("Kabeltrommel 25m");
        String companyToken = tokens.tokenFor("company-demo", "company-demo");
        String kennung = null;
        String otherKennung = null;
        for (long id : new long[] { partnerId, otherId }) {
            given().auth().oauth2(companyToken).when().post("/api/company/partners/" + id + "/shop/release")
                    .then().statusCode(200);
            String value = given().auth().oauth2(companyToken).when().get("/api/company/partners/" + id + "/shop")
                    .then().statusCode(200).extract().path("kennung");
            if (id == partnerId) {
                kennung = value;
            } else {
                otherKennung = value;
            }
        }
        long assetId = upload(partnerId);
        String path = "/api/shop/" + kennung + "/assets/" + assetId;

        useOnHome(partnerId, assetId, false);
        given().header("X-Forwarded-For", "198.18.1.1").when().get(path).then().statusCode(404);

        useOnHome(partnerId, assetId, true);
        given().header("X-Forwarded-For", "198.18.1.2").when().get(path)
                .then().statusCode(200).contentType("image/png").header("X-Content-Type-Options", "nosniff");
        given().header("X-Forwarded-For", "198.18.1.3").when().get("/api/shop/" + otherKennung + "/assets/" + assetId)
                .then().statusCode(404);
    }
}
