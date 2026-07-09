package at.papo.partnerportal.product;

import java.time.Instant;

public record ProductImageDto(
        Long id,
        String filename,
        String contentType,
        long sizeBytes,
        Instant uploadedAt,
        String url
) {
    // "/product-images/{id}" - NOT the full "/api/product-images/{id}" REST path. Every
    // frontend fetch helper (apiFetch/apiFetchForm/apiFetchBlob, lib/api.js) prepends its
    // own API_BASE, which already ends in "/api"; a caller passes paths like "/company/products",
    // never "/api/company/products". This value round-trips through Product.imageUrl and
    // lib/product-images.js's resolveImageUrl, so it must follow the same convention or those
    // helpers double up the prefix ("/api/api/...", a 404).
    public static ProductImageDto from(ProductImage image) {
        return new ProductImageDto(image.id, image.filename, image.contentType, image.sizeBytes,
                image.uploadedAt, "/product-images/" + image.id);
    }
}
