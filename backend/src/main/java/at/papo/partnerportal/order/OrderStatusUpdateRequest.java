package at.papo.partnerportal.order;

import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(
        @NotNull(message = "Status ist erforderlich.") OrderStatus status
) {
}
