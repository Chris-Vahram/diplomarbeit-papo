package at.papo.partnerportal.order;

import at.papo.partnerportal.commission.CommissionRecord;
import at.papo.partnerportal.customer.KundeDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderDto(
        Long id,
        LocalDate orderDate,
        Long partnerId,
        String partnerName,
        KundeDto kunde,
        OrderStatus status,
        BigDecimal orderValue,
        List<OrderItemDto> items,
        BigDecimal commissionPercentUsed,
        BigDecimal commissionValue
) {
    public static OrderDto from(Order order, CommissionRecord commission) {
        return new OrderDto(order.id, order.orderDate, order.partner.id, order.partner.name,
                KundeDto.from(order.kunde), order.status, order.orderValue,
                order.items.stream().map(OrderItemDto::from).toList(),
                commission != null ? commission.commissionPercentUsed : null,
                commission != null ? commission.commissionValue : null);
    }
}
