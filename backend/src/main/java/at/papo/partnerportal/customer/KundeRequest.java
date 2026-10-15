package at.papo.partnerportal.customer;

import at.papo.partnerportal.common.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Mandatory: last name + address. First name/e-mail/phone optional (customer-management spec).
public record KundeRequest(
        @NotBlank(message = "Nachname ist erforderlich.") String lastName,
        @NotNull(message = "Adresse ist erforderlich.") @Valid AddressRequest address,
        String firstName,
        @Email(message = "E-Mail-Adresse ist ungültig.") @Size(max = 254, message = "E-Mail-Adresse ist zu lang.") String email,
        String phone
) {
}
