package at.papo.partnerportal.order;

import java.math.BigDecimal;

public record OrderItemDto(
        Long productId,
        String productName,
        int quantity,
        BigDecimal unitPriceSnapshot,
        BigDecimal lineTotal
) {
    public static OrderItemDto from(OrderItem item) {
        return new OrderItemDto(item.product.id, item.product.name, item.quantity, item.unitPriceSnapshot,
                item.unitPriceSnapshot.multiply(BigDecimal.valueOf(item.quantity)));
    }
}
