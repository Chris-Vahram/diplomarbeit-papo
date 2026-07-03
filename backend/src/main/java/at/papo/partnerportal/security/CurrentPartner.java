package at.papo.partnerportal.security;

import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.partner.Partner;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

// Resolves the authenticated partner's own Partner record from the Keycloak
// username claim — never trust a client-supplied partner id (docs/CONVENTIONS.md).
// See design.md "Keycloak user provisioning" and the auth-access-control spec
// requirement "Partner login linking".
@RequestScoped
public class CurrentPartner {

    @Inject
    JsonWebToken jwt;

    public Partner resolve() {
        String username = jwt.getName();
        Partner partner = Partner.find("keycloakUsername", username).firstResult();
        if (partner == null) {
            throw new ApiException(403, "Kein Partnerkonto mit diesem Login verknüpft.");
        }
        if (!partner.active) {
            throw new ApiException(403, "Ihr Zugang wurde deaktiviert. Bitte wenden Sie sich an die Firma.");
        }
        return partner;
    }
}
