package at.papo.partnerportal.order;

import at.papo.partnerportal.product.ArticleTax;

import java.math.BigDecimal;
import java.util.List;

public record OrderItemDto(
        Long productId,
        String productName,
        int quantity,
        BigDecimal unitPriceSnapshot,
        BigDecimal lineTotal,
        BigDecimal taxRatePercentSnapshot,
        BigDecimal unitNetPrice,
        BigDecimal lineNetTotal
) {
    public static OrderItemDto from(OrderItem item) {
        return new OrderItemDto(item.product.id, item.product.name, item.quantity, item.unitPriceSnapshot,
                item.unitPriceSnapshot.multiply(BigDecimal.valueOf(item.quantity)), item.taxRatePercentSnapshot,
                ArticleTax.net(item.unitPriceSnapshot, item.taxRatePercentSnapshot),
                ArticleTax.totals(List.of(new ArticleTax.Line(item.unitPriceSnapshot, item.quantity,
                        item.taxRatePercentSnapshot))).net());
    }
}
