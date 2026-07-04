package at.papo.partnerportal.dashboard;

import java.math.BigDecimal;

public record CompanySettlementDto(
        Long partnerId,
        String partnerName,
        long fulfilledOrderCount,
        BigDecimal revenue,
        BigDecimal commission
) {
}
