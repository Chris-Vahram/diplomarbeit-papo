package at.papo.partnerportal.customer;

import jakarta.validation.constraints.NotBlank;

// Mandatory: last name + address. First name/e-mail/phone optional (customer-management spec).
public record KundeRequest(
        @NotBlank(message = "Nachname ist erforderlich.") String lastName,
        @NotBlank(message = "Adresse ist erforderlich.") String address,
        String firstName,
        String email,
        String phone
) {
}
