package at.papo.partnerportal.enablement;

import at.papo.partnerportal.product.ArticleTax;
import at.papo.partnerportal.product.Product;

import java.math.BigDecimal;

public record EnablementDto(
        Long productId,
        String name,
        BigDecimal price,
        String category,
        boolean available,
        boolean enabled,
        BigDecimal taxRatePercent,
        BigDecimal netPrice
) {
    public static EnablementDto from(Product product, boolean enabled) {
        return new EnablementDto(product.id, product.name, product.price,
                product.articleCategory == null ? null : product.articleCategory.name,
                product.available, enabled, product.taxRatePercent,
                ArticleTax.net(product.price, product.taxRatePercent));
    }
}
