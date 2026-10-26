package at.papo.partnerportal.shop;

import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.common.UploadLimits;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.ProductImageValidator;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// Partner-uploaded shop images (shop-assets spec): verified formats, 5 MB per file,
// 50 MB per shop, rights confirmation, and deletion only while no page uses the image.
@ApplicationScoped
public class ShopAssetService {

    public static final long QUOTA_BYTES = 50L * 1024 * 1024;
    static final int FILENAME_MAX_LENGTH = 255;

    @Inject
    PartnerShopService shopService;

    @Inject
    ShopPageService pageService;

    public ShopAssetListDto list(Partner partner) {
        PartnerShop shop = shopService.findOrCreate(partner);
        // Same rule as delete: drafts and the restore point count as use.
        Map<Long, Set<String>> usage = usage(shop);
        List<ShopAssetDto> assets = ShopAsset.getEntityManager().createQuery(
                        "select new at.papo.partnerportal.shop.ShopAssetDto(a.id, a.filename, a.contentType, "
                                + "a.sizeBytes, a.uploadedAt) from ShopAsset a where a.shop = ?1 order by a.uploadedAt desc, a.id desc",
                        ShopAssetDto.class)
                .setParameter(1, shop)
                .getResultList().stream()
                .map(asset -> asset.withUsedOn(List.copyOf(usage.getOrDefault(asset.id(), Set.of()))))
                .toList();
        return new ShopAssetListDto(assets, usedBytes(shop), QUOTA_BYTES);
    }

    @Transactional
    public ShopAssetDto upload(Partner partner, byte[] data, String filename, boolean rightsConfirmed) {
        if (!rightsConfirmed) {
            throw new ApiException(400, "Bitte bestätigen Sie, dass Sie die Rechte an diesem Bild haben.");
        }
        if (data.length == 0) {
            throw new ApiException(400, "Die Datei ist leer.");
        }
        if (data.length > UploadLimits.SHOP_ASSET_BYTES) {
            throw new ApiException(413, "Das Bild ist zu groß (maximal 5 MB).");
        }
        String contentType = ProductImageValidator.sniffContentType(data);
        if (contentType == null) {
            throw new ApiException(400, "Bitte PNG, JPG oder WebP verwenden.");
        }
        PartnerShop shop = shopService.findOrCreate(partner);
        // Serialises uploads per shop, so two parallel uploads cannot both slip under the quota.
        PartnerShop.getEntityManager().lock(shop, LockModeType.PESSIMISTIC_WRITE);
        if (usedBytes(shop) + data.length > QUOTA_BYTES) {
            throw new ApiException(409, "Speicherplatz voll – bitte nicht verwendete Bilder löschen.");
        }
        ShopAsset asset = new ShopAsset();
        asset.shop = shop;
        asset.data = data;
        asset.contentType = contentType;
        asset.filename = cleanFilename(filename);
        asset.sizeBytes = data.length;
        asset.uploadedAt = Instant.now();
        asset.rightsConfirmed = true;
        asset.persist();
        return new ShopAssetDto(asset.id, asset.filename, asset.contentType, asset.sizeBytes, asset.uploadedAt);
    }

    // Bundled sample photo as the shop's own image (shop-templates "Bundled sample images"):
    // copied once per shop and found again by its file name, so applying several templates
    // never duplicates it. The licence (CC0) allows the use; the partner can replace or delete it.
    @Transactional
    public long sampleAsset(PartnerShop shop, ShopTemplateImage image, byte[] data) {
        String filename = "Beispielbild " + image.file();
        Optional<ShopAsset> existing = ShopAsset.<ShopAsset>find("shop = ?1 and filename = ?2", shop, filename)
                .firstResultOptional();
        if (existing.isPresent()) {
            return existing.get().id;
        }
        PartnerShop.getEntityManager().lock(shop, LockModeType.PESSIMISTIC_WRITE);
        if (usedBytes(shop) + data.length > QUOTA_BYTES) {
            throw new ApiException(409, "Speicherplatz voll – bitte nicht verwendete Bilder löschen.");
        }
        ShopAsset asset = new ShopAsset();
        asset.shop = shop;
        asset.data = data;
        asset.contentType = ProductImageValidator.sniffContentType(data);
        asset.filename = filename;
        asset.sizeBytes = data.length;
        asset.uploadedAt = Instant.now();
        asset.rightsConfirmed = true;
        asset.persist();
        return asset.id;
    }

    // Drafts and the restorable previous version count as use too, so no page can ever
    // reference a missing image.
    @Transactional
    public void delete(Partner partner, long assetId) {
        PartnerShop shop = shopService.findOrCreate(partner);
        ShopAsset asset = own(shop, assetId);
        Set<String> pages = usage(shop).get(asset.id);
        if (pages != null) {
            throw new ApiException(409, "Das Bild wird noch verwendet auf: " + String.join(", ", pages) + ".");
        }
        asset.delete();
    }

    public ShopAsset find(Partner partner, long assetId) {
        return own(shopService.findOrCreate(partner), assetId);
    }

    // Public only while a published online page of this shop shows it; drafts stay private.
    public Optional<ShopAsset> publicAsset(PartnerShop shop, long assetId) {
        if (!isPublished(shop, assetId)) {
            return Optional.empty();
        }
        return ShopAsset.<ShopAsset>find("id = ?1 and shop = ?2", assetId, shop).firstResultOptional();
    }

    private ShopAsset own(PartnerShop shop, long assetId) {
        return ShopAsset.<ShopAsset>find("id = ?1 and shop = ?2", assetId, shop).firstResultOptional()
                .orElseThrow(() -> new ApiException(404, "Bild nicht gefunden."));
    }

    private long usedBytes(PartnerShop shop) {
        Long sum = ShopAsset.getEntityManager()
                .createQuery("select sum(a.sizeBytes) from ShopAsset a where a.shop = ?1", Long.class)
                .setParameter(1, shop).getSingleResult();
        return sum == null ? 0 : sum;
    }

    // Asset id -> menu names of the pages using it in any version (draft, published, restore
    // point), so no page can ever reference a missing image.
    Map<Long, Set<String>> usage(PartnerShop shop) {
        Map<Long, Set<String>> usage = new LinkedHashMap<>();
        for (ShopPage page : ShopPage.<ShopPage>list("shop = ?1 and kind <> ?2 order by position, id",
                shop, ShopPageKind.SYSTEM)) {
            for (String content : new String[] { page.publishedContent, page.draftContent, page.previousPublishedContent }) {
                if (content == null) {
                    continue;
                }
                ShopBlockContent.values(pageService.parse(content), ShopBlockField.Kind.IMAGE).stream()
                        .filter(JsonNode::canConvertToExactIntegral)
                        .forEach(id -> usage.computeIfAbsent(id.asLong(), key -> new LinkedHashSet<>()).add(page.menuName));
            }
        }
        return usage;
    }

    // Public delivery check in the database (jsonb containment) instead of parsing every
    // online page on each image request. Image fields are all named "imageId".
    private boolean isPublished(PartnerShop shop, long assetId) {
        Number count = (Number) ShopAsset.getEntityManager().createNativeQuery(
                        "select count(*) from shop_page where shop_id = ?1 and online = true and kind <> 'SYSTEM' "
                                + "and publishedcontent @> cast(?2 as jsonb)")
                .setParameter(1, shop.id)
                .setParameter(2, "{\"blocks\":[{\"config\":{\"imageId\":" + assetId + "}}]}")
                .getSingleResult();
        return count.longValue() > 0;
    }

    private static String cleanFilename(String filename) {
        String name = filename == null ? "" : filename.replaceAll(".*[/\\\\]", "").strip();
        if (name.isEmpty()) {
            return "bild";
        }
        return name.length() > FILENAME_MAX_LENGTH ? name.substring(0, FILENAME_MAX_LENGTH) : name;
    }
}
