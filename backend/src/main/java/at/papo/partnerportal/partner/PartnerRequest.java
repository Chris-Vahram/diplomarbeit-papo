package at.papo.partnerportal.partner;

import at.papo.partnerportal.common.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// Company-side create/edit — full record incl. commissionPercent and active
// (never exposed on the partner self-edit path, see PartnerContactRequest / FR-08).
public record PartnerRequest(
        @NotBlank(message = "Partnernummer ist erforderlich.") String partnerNumber,
        @NotBlank(message = "Name ist erforderlich.") String name,
        @NotNull(message = "Adresse ist erforderlich.") @Valid AddressRequest address,
        String email,
        String phone,
        @NotNull(message = "Provision ist erforderlich.")
        @DecimalMin(value = "0.0", message = "Provision darf nicht negativ sein.")
        @DecimalMax(value = "100.0", message = "Provision darf 100% nicht überschreiten.") BigDecimal commissionPercent,
        Boolean active,
        // Links a manually provisioned Keycloak login to this partner (see partner-management
        // spec "Company links Keycloak login"). Optional; import never sets this.
        String keycloakUsername
) {
}
