package at.papo.partnerportal.commission;

import at.papo.partnerportal.order.Order;
import at.papo.partnerportal.partner.Partner;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
}
