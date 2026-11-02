package at.papo.partnerportal.product;

import at.papo.partnerportal.order.Order;
import at.papo.partnerportal.order.OrderItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

// add-article-tax-rate migration (design.md D5). Existing prices stay unchanged and count as gross
// with 20 % USt; order items get that rate as snapshot and orders their net/tax values. Commission
// records are never touched. Idempotent: every step only fills null columns.
@ApplicationScoped
public class ArticleTaxMigrationService {

    @Transactional
    public void migrate() {
        Product.update("taxRatePercent = ?1 where taxRatePercent is null", ArticleTax.normalized(ArticleTax.DEFAULT_RATE));
        OrderItem.update("taxRatePercentSnapshot = ?1 where taxRatePercentSnapshot is null",
                ArticleTax.normalized(ArticleTax.DEFAULT_RATE));
        List<Order> orders = Order.list("netValue is null or taxValue is null");
        for (Order order : orders) {
            Order.getEntityManager().refresh(order);
            order.applyTotalsFromItems();
        }
    }
}
