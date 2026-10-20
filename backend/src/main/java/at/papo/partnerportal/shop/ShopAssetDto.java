package at.papo.partnerportal.shop;

import java.time.Instant;
import java.util.List;

// One image of the shop library; usedOn = menu names of the pages referencing it.
public record ShopAssetDto(Long id, String filename, String contentType, long sizeBytes, Instant uploadedAt,
                           List<String> usedOn) {

    // JPQL projection: never loads the image bytes.
    public ShopAssetDto(Long id, String filename, String contentType, long sizeBytes, Instant uploadedAt) {
        this(id, filename, contentType, sizeBytes, uploadedAt, List.of());
    }

    ShopAssetDto withUsedOn(List<String> pages) {
        return new ShopAssetDto(id, filename, contentType, sizeBytes, uploadedAt, pages);
    }
}
