package at.papo.partnerportal.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record CompanyDashboardDto(
        BigDecimal totalRevenue,
        BigDecimal totalCommission,
        long fulfilledOrderCount,
        long partnerCount,
        List<CompanySettlementDto> settlement
) {
}
