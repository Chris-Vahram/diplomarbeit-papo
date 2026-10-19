package at.papo.partnerportal.partner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// The subject is the Keycloak user id; its format is the issuer's business, so only
// presence, length and absence of whitespace are checked.
public record PartnerLoginRequest(
        @NotBlank(message = "Keycloak-Benutzer-ID ist erforderlich.")
        @Size(max = 255, message = "Keycloak-Benutzer-ID darf höchstens 255 Zeichen haben.")
        @Pattern(regexp = "\\s*\\S+\\s*", message = "Keycloak-Benutzer-ID darf keine Leerzeichen enthalten.")
        String oidcSubject
) {
}
