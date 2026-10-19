package at.papo.partnerportal.shop;

// Shop-specific checks for ids and segments inside block configurations.
public interface ShopBlockReferences {

    boolean isPublicImage(long imageId);

    boolean categoryExists(long categoryId);

    boolean isCatalogueArticle(long articleId);

    // Link target: an online designable page other than the article-detail template.
    boolean isLinkablePage(String segment);
}
