package at.papo.partnerportal.shop;

import at.papo.partnerportal.product.Product;

import java.math.BigDecimal;

// Public article (shop-public-catalog spec): gross price plus the article's USt rate; no commission, purchase data or enablement metadata.
// Uploaded images go through the shop's own image endpoint, same-origin frontend assets stay
// as they are; external URLs and protected portal API paths are dropped, so the shop never
// loads third-party resources.
public record PublicArticleDto(
        Long id,
        String name,
        String description,
        Long categoryId,
        String category,
        String imageUrl,
        BigDecimal grossPrice,
        int taxRatePercent
) {
    static PublicArticleDto from(Product product, String kennung) {
        return new PublicArticleDto(product.id, product.name, product.description,
                product.articleCategory == null ? null : product.articleCategory.id,
                product.articleCategory == null ? null : product.articleCategory.name,
                imageUrl(product, kennung), product.price, product.taxRatePercent.intValue());
    }

    // Editor preview of a not yet public shop: uploaded images through the authenticated
    // portal path, which the editor turns into object URLs (lib/product-images.js).
    static PublicArticleDto forPreview(Product product) {
        PublicArticleDto dto = from(product, "");
        return product.image == null ? dto : new PublicArticleDto(dto.id(), dto.name(), dto.description(),
                dto.categoryId(), dto.category(), "/product-images/" + product.image.id, dto.grossPrice(),
                dto.taxRatePercent());
    }

    static String imageUrl(Product product, String kennung) {
        if (product.image != null) {
            return "/api/shop/" + kennung + "/images/" + product.image.id;
        }
        String url = product.imageUrl;
        // Browsers drop tabs/newlines and treat "\" like "/", so "/<TAB>/host" or "/\host" would
        // become a protocol-relative third-party URL; such characters are rejected outright.
        if (url == null || url.chars().anyMatch(c -> c <= 0x20 || c == 0x7f || c == '\\')) {
            return null;
        }
        return url.startsWith("/") && !url.startsWith("//") && !url.startsWith("/api/") ? url : null;
    }
}
