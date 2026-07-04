package at.papo.partnerportal.commission;

import at.papo.partnerportal.order.Order;
import at.papo.partnerportal.partner.Partner;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class CommissionService {

    // Basis/percentage snapshotted on the record itself (commission spec) - a
    // later change to Partner.commissionPercent must not alter past commissions.
    @Transactional
    public CommissionRecord createForOrder(Order order, Partner partner) {
        CommissionRecord record = new CommissionRecord();
        record.order = order;
        record.partner = partner;
        record.orderValue = order.orderValue;
        record.commissionPercentUsed = partner.commissionPercent;
        record.commissionValue = order.orderValue
                .multiply(partner.commissionPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        record.persist();
        return record;
    }

    public CommissionRecord findForOrder(Order order) {
        return CommissionRecord.find("order", order).firstResult();
    }

    // Batch variant to avoid an N+1 query when building an order list (order-management
    // spec) - one query for all the given orders' commission records, mapped by order id.
    public Map<Long, CommissionRecord> findForOrders(List<Order> orders) {
        if (orders.isEmpty()) {
            return Map.of();
        }
        List<CommissionRecord> records = CommissionRecord.list("order in ?1", orders);
        return records.stream().collect(Collectors.toMap(record -> record.order.id, Function.identity()));
    }
}
