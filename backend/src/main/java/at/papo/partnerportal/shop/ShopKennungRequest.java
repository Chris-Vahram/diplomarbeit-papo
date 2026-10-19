package at.papo.partnerportal.shop;

import jakarta.validation.constraints.NotBlank;

public record ShopKennungRequest(
        @NotBlank(message = "Kennung ist erforderlich.") String kennung
) {
}
