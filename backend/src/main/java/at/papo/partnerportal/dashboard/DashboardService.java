package at.papo.partnerportal.dashboard;

import at.papo.partnerportal.commission.CommissionRecord;
import at.papo.partnerportal.commission.CommissionService;
import at.papo.partnerportal.enablement.EnablementService;
import at.papo.partnerportal.order.Order;
import at.papo.partnerportal.order.OrderDto;
import at.papo.partnerportal.order.OrderStatus;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.Product;
import at.papo.partnerportal.security.CurrentCompany;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Dashboard totals (commission/revenue/order count) count only the partner's
// own fulfilled orders - decided 2026-07-03 (design.md, partner-dashboard spec).
// The latest-orders list shows recent orders of any status.
@ApplicationScoped
public class DashboardService {

    private static final int LATEST_ORDERS_LIMIT = 5;

    @Inject
    CurrentCompany currentCompany;

    @Inject
    CommissionService commissionService;

    @Inject
    EnablementService enablementService;

    public PartnerDashboardDto buildForPartner(Partner partner) {
        List<Order> fulfilledOrders = Order.list("partner = ?1 and status = ?2", partner, OrderStatus.FULFILLED);

        BigDecimal totalRevenue = fulfilledOrders.stream()
                .map(order -> order.orderValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Batch lookup instead of one findForOrder call per fulfilled order (same
        // pattern as buildForCompany below).
        Map<Long, CommissionRecord> fulfilledCommissionsByOrderId = commissionService.findForOrders(fulfilledOrders);
        BigDecimal totalCommission = fulfilledOrders.stream()
                .map(order -> fulfilledCommissionsByOrderId.get(order.id))
                .filter(java.util.Objects::nonNull)
                .map(commission -> commission.commissionValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // DB-side limit instead of loading every order and slicing in Java.
        List<Order> latestOrderEntities = Order.<Order>find("partner = ?1 order by id desc", partner)
                .page(0, LATEST_ORDERS_LIMIT)
                .list();
        Map<Long, CommissionRecord> latestCommissionsByOrderId = commissionService.findForOrders(latestOrderEntities);
        List<OrderDto> latestOrders = latestOrderEntities.stream()
                .map(order -> OrderDto.from(order, latestCommissionsByOrderId.get(order.id)))
                .toList();

        return new PartnerDashboardDto(totalCommission, totalRevenue, fulfilledOrders.size(),
                enablementService.countEnabledForPartner(partner), Product.count("company", partner.company), latestOrders);
    }

    // Two list queries + a batch commission lookup (same pattern as OrderService.listAll)
    // instead of per-order/per-partner queries - avoids N+1 across ~200 partners.
    public CompanyDashboardDto buildForCompany() {
        var company = currentCompany.resolve();
        List<Order> fulfilledOrders = Order.list("status = ?1 and partner.company = ?2", OrderStatus.FULFILLED, company);
        Map<Long, CommissionRecord> commissionsByOrderId = commissionService.findForOrders(fulfilledOrders);
        Map<Long, List<Order>> ordersByPartnerId = fulfilledOrders.stream()
                .collect(Collectors.groupingBy(order -> order.partner.id));

        List<CompanySettlementDto> settlement = Partner.<Partner>list("company", company).stream()
                .map(partner -> {
                    List<Order> partnerOrders = ordersByPartnerId.getOrDefault(partner.id, List.of());
                    BigDecimal revenue = partnerOrders.stream()
                            .map(order -> order.orderValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal commission = partnerOrders.stream()
                            .map(order -> commissionsByOrderId.get(order.id))
                            .filter(java.util.Objects::nonNull)
                            .map(record -> record.commissionValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new CompanySettlementDto(partner.id, partner.name, partnerOrders.size(), revenue, commission);
                })
                .toList();

        BigDecimal totalRevenue = settlement.stream()
                .map(CompanySettlementDto::revenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCommission = settlement.stream()
                .map(CompanySettlementDto::commission)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CompanyDashboardDto(totalRevenue, totalCommission, fulfilledOrders.size(),
                settlement.size(), settlement);
    }
}
