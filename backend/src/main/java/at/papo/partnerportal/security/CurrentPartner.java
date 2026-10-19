package at.papo.partnerportal.security;

import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.partner.PartnerLogin;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

// Resolves the authenticated partner's own Partner record from the verified OIDC subject
// (bind-partner-login-to-oidc-subject) - never trust a client-supplied partner id
// (docs/CONVENTIONS.md). Username, e-mail and partner number are not identity.
@RequestScoped
public class CurrentPartner {

    private static final String UNLINKED = "Kein Partnerkonto mit diesem Login verknüpft.";

    @Inject
    JsonWebToken jwt;

    public Partner resolve() {
        Partner partner = resolveLinked();
        if (!partner.active) {
            throw new ApiException(403, "Ihr Zugang wurde deaktiviert. Bitte wenden Sie sich an die Firma.");
        }
        return partner;
    }

    private Partner resolveLinked() {
        PartnerLogin login = PartnerLogin.find("oidcSubject", jwt.getSubject()).firstResult();
        if (login != null) {
            if (login.revokedAt != null) {
                throw new ApiException(403, UNLINKED);
            }
            return login.partner;
        }
        throw new ApiException(403, UNLINKED);
    }
}
