package at.papo.partnerportal.order;

import at.papo.partnerportal.commission.CommissionRecord;
import at.papo.partnerportal.customer.KundeDto;
import com.fasterxml.jackson.annotation.JsonInclude;

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
        BigDecimal commissionValue,
        boolean seenByCompany,
        // Only on create/status responses: what happened to the customer mail of this event
        // (order-emails spec); absent in lists.
        @JsonInclude(JsonInclude.Include.NON_NULL) String customerEmailNotice
) {
    public static OrderDto from(Order order, CommissionRecord commission) {
        return new OrderDto(order.id, order.orderDate, order.partner.id, order.partner.name,
                KundeDto.from(order.kunde), order.status, order.orderValue,
                order.items.stream().map(OrderItemDto::from).toList(),
                commission != null ? commission.commissionPercentUsed : null,
                commission != null ? commission.commissionValue : null,
                order.seenByCompany, null);
    }

    public OrderDto withNotice(String notice) {
        return new OrderDto(id, orderDate, partnerId, partnerName, kunde, status, orderValue, items,
                commissionPercentUsed, commissionValue, seenByCompany, notice);
    }
}
