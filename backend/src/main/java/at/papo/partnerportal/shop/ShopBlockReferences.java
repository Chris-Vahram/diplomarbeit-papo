package at.papo.partnerportal.shop;

// Shop-specific checks for ids and segments inside block configurations.
public interface ShopBlockReferences {

    // Image fields reference the shop's own uploaded ShopAssets.
    boolean isOwnAsset(long assetId);

    boolean categoryExists(long categoryId);

    boolean isCatalogueArticle(long articleId);

    // Link target: an online designable page other than the article-detail template.
    boolean isLinkablePage(String segment);
}
