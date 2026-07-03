package at.papo.partnerportal.product;

import java.math.BigDecimal;

public record ProductDto(
        Long id,
        String name,
        BigDecimal price,
        String description,
        boolean available,
        String category,
        String imageUrl
) {
    public static ProductDto from(Product product) {
        return new ProductDto(product.id, product.name, product.price, product.description,
                product.available, product.category, product.imageUrl);
    }
}
