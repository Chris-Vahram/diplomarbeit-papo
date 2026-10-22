package at.papo.partnerportal.shop;

import at.papo.partnerportal.product.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PublicArticleDtoTest {

    private static String publicUrl(String imageUrl) {
        Product product = new Product();
        product.imageUrl = imageUrl;
        return PublicArticleDto.imageUrl(product, "test-shop");
    }

    @Test
    void sameOriginAssetsPass() {
        assertEquals("/assets/products/elektro.svg", publicUrl("/assets/products/elektro.svg"));
    }

    @Test
    void externalAndProtectedUrlsAreDropped() {
        assertNull(publicUrl("https://evil.example/x.png"));
        assertNull(publicUrl("//evil.example/x.png"));
        assertNull(publicUrl("/\\evil.example/x.png"));
        assertNull(publicUrl("/api/product-images/1"));
        assertNull(publicUrl(null));
    }

    @Test
    void controlCharactersBrowsersStripAreRejected() {
        assertNull(publicUrl("/\t/evil.example/x.png"));
        assertNull(publicUrl("/\n/evil.example/x.png"));
        assertNull(publicUrl("/\r/evil.example/x.png"));
        assertNull(publicUrl(" //evil.example/x.png"));
    }
}
