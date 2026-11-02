package at.papo.partnerportal.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull(message = "Artikel ist erforderlich.") Long productId,
        @NotNull(message = "Menge ist erforderlich.")
        @Min(value = 1, message = "Menge muss mindestens 1 sein.")
        @Max(value = OrderItemRequest.MAX_QUANTITY, message = "Menge darf 10000 nicht überschreiten.") Integer quantity
) {
    // Shared upper bound for portal and shop orders.
    public static final int MAX_QUANTITY = 10000;
}
