package at.papo.partnerportal.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
        @NotBlank(message = "Artikelkategorie ist erforderlich.")
        @Size(max = 255, message = "Artikelkategorie darf höchstens 255 Zeichen lang sein.") String name
) {
}
