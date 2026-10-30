package at.papo.partnerportal.security;

import at.papo.partnerportal.business.CompanyLogin;
import at.papo.partnerportal.business.CompanyProfile;
import at.papo.partnerportal.common.ApiException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

// Resolves the calling company user's company from the verified OIDC subject only
// (add-company-tenancy). Headers, host, query and body never select the company.
@RequestScoped
public class CurrentCompany {

    private static final String UNLINKED = "Kein Firmenkonto mit diesem Login verknüpft.";

    @Inject
    JsonWebToken jwt;

    private CompanyProfile company;

    public CompanyProfile resolve() {
        if (company == null) {
            CompanyLogin login = CompanyLogin.find("oidcSubject", jwt.getSubject()).firstResult();
            if (login == null || login.revokedAt != null) {
                throw new ApiException(403, UNLINKED);
            }
            company = login.company;
        }
        return company;
    }
}
