package at.papo.partnerportal.product;

import java.math.BigDecimal;

public record ProductDto(
        Long id,
        String name,
        BigDecimal price,
        String description,
        boolean available,
        String category,
        String imageUrl,
        Long categoryId,
        BigDecimal taxRatePercent,
        BigDecimal netPrice
) {
    public static ProductDto from(Product product) {
        return new ProductDto(product.id, product.name, product.price, product.description,
                product.available, product.articleCategory == null ? null : product.articleCategory.name,
                product.imageUrl, product.articleCategory == null ? null : product.articleCategory.id,
                product.taxRatePercent, ArticleTax.net(product.price, product.taxRatePercent));
    }
}
