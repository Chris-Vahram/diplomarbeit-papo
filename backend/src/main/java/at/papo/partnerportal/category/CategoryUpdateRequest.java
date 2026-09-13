package at.papo.partnerportal.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryUpdateRequest(
        @NotBlank(message = "Artikelkategorie ist erforderlich.")
        @Size(max = 255, message = "Artikelkategorie darf höchstens 255 Zeichen lang sein.") String name,
        @NotNull(message = "Aktivstatus ist erforderlich.") Boolean active
) {
}
