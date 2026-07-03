package at.papo.partnerportal.dashboard;

import at.papo.partnerportal.commission.CommissionService;
import at.papo.partnerportal.enablement.EnablementService;
import at.papo.partnerportal.order.Order;
import at.papo.partnerportal.order.OrderDto;
import at.papo.partnerportal.order.OrderStatus;
import at.papo.partnerportal.partner.Partner;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.List;

// Dashboard totals (commission/revenue/order count) count only the partner's
// own fulfilled orders - decided 2026-07-03 (design.md, partner-dashboard spec).
// The latest-orders list shows recent orders of any status.
@ApplicationScoped
public class DashboardService {

    private static final int LATEST_ORDERS_LIMIT = 5;

    @Inject
    CommissionService commissionService;

    @Inject
    EnablementService enablementService;

    public PartnerDashboardDto buildForPartner(Partner partner) {
        List<Order> fulfilledOrders = Order.list("partner = ?1 and status = ?2", partner, OrderStatus.FULFILLED);

        BigDecimal totalRevenue = fulfilledOrders.stream()
                .map(order -> order.orderValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCommission = fulfilledOrders.stream()
                .map(commissionService::findForOrder)
                .filter(java.util.Objects::nonNull)
                .map(commission -> commission.commissionValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OrderDto> latestOrders = Order.<Order>list("partner = ?1 order by id desc", partner).stream()
                .limit(LATEST_ORDERS_LIMIT)
                .map(order -> OrderDto.from(order, commissionService.findForOrder(order)))
                .toList();

        return new PartnerDashboardDto(totalCommission, totalRevenue, fulfilledOrders.size(),
                enablementService.countEnabledForPartner(partner), latestOrders);
    }
}
