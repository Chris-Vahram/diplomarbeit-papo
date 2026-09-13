package at.papo.partnerportal.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Name ist erforderlich.") String name,
        @NotNull(message = "Preis ist erforderlich.")
        @DecimalMin(value = "0.0", message = "Preis darf nicht negativ sein.") BigDecimal price,
        String description,
        Boolean available,
        String category,
        String imageUrl,
        Long categoryId
) {
}
