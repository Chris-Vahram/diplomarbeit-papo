package at.papo.partnerportal.dashboard;

import at.papo.partnerportal.order.OrderDto;

import java.math.BigDecimal;
import java.util.List;

public record PartnerDashboardDto(
        BigDecimal totalCommission,
        BigDecimal totalRevenue,
        long fulfilledOrderCount,
        long enabledProductCount,
        long totalProductCount,
        List<OrderDto> latestOrders
) {
}
