package at.papo.partnerportal.security;

import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.partner.Partner;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Locale;

// Resolves the authenticated partner's own Partner record from the Keycloak
// username claim — never trust a client-supplied partner id (docs/CONVENTIONS.md).
// The username claim is matched against the canonical (upper-case) Partner.partnerNumber;
// see partner-management spec requirement "Partner login linking via partner number".
@RequestScoped
public class CurrentPartner {

    @Inject
    JsonWebToken jwt;

    public Partner resolve() {
        Partner partner = Partner.find("partnerNumber", jwt.getName().toUpperCase(Locale.ROOT)).firstResult();
        if (partner == null) {
            throw new ApiException(403, "Kein Partnerkonto mit diesem Login verknüpft.");
        }
        if (!partner.active) {
            throw new ApiException(403, "Ihr Zugang wurde deaktiviert. Bitte wenden Sie sich an die Firma.");
        }
        return partner;
    }
}
