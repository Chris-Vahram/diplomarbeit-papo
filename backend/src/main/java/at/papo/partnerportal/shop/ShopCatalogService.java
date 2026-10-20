package at.papo.partnerportal.shop;

import at.papo.partnerportal.category.Category;
import at.papo.partnerportal.enablement.ProductEnablement;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.product.ProductImage;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

// The shop's public catalogue: articles enabled for the shop's partner and available
// (shop-public-catalog spec). Same rule as the portal and PartnerShopService's status check;
// partner activity is already guaranteed by PartnerShopService.findOnline.
@ApplicationScoped
public class ShopCatalogService {

    // p.image is deliberately not fetched: the DTO only needs its id, which the lazy proxy
    // carries without loading the image bytes.
    private static final String CATALOGUE = "select p from ProductEnablement e join e.product p "
            + "left join fetch p.articleCategory "
            + "where e.partner = ?1 and p.available = true";

    public List<Product> articles(Partner partner) {
        return Product.<Product>find(CATALOGUE + " order by p.name, p.id", partner).list();
    }

    public Optional<Product> article(Partner partner, long articleId) {
        return Product.<Product>find(CATALOGUE + " and p.id = ?2", partner, articleId).firstResultOptional();
    }

    public boolean isCatalogueArticle(Partner partner, long articleId) {
        return ProductEnablement.count("partner = ?1 and product.id = ?2 and product.available = true",
                partner, articleId) > 0;
    }

    // Categories that have at least one catalogue article.
    public List<PublicCategoryDto> categories(Partner partner) {
        return Category.getEntityManager().createQuery(
                        "select distinct new at.papo.partnerportal.shop.PublicCategoryDto(c.id, c.name) "
                                + "from ProductEnablement e join e.product p join p.articleCategory c "
                                + "where e.partner = ?1 and p.available = true order by c.name", PublicCategoryDto.class)
                .setParameter(1, partner)
                .getResultList();
    }

    // Article images are public only as the image of a catalogue article. Shop-owned uploads
    // have their own id space and endpoint (ShopAssetService.publicAsset).
    public Optional<ProductImage> publicImage(Partner partner, long imageId) {
        return ProductEnablement.count("partner = ?1 and product.available = true and product.image.id = ?2",
                partner, imageId) > 0
                ? Optional.ofNullable(ProductImage.findById(imageId))
                : Optional.empty();
    }
}
