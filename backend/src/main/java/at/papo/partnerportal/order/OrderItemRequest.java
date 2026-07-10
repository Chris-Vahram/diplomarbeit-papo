package at.papo.partnerportal.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull(message = "Produkt ist erforderlich.") Long productId,
        @NotNull(message = "Menge ist erforderlich.")
        @Min(value = 1, message = "Menge muss mindestens 1 sein.")
        @Max(value = 10000, message = "Menge darf 10000 nicht überschreiten.") Integer quantity
) {
}
