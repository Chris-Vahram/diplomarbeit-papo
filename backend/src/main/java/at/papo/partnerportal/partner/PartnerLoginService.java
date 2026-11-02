package at.papo.partnerportal.partner;

import at.papo.partnerportal.business.CompanyLogin;
import at.papo.partnerportal.common.ApiException;
import at.papo.partnerportal.security.LoginSubjectLock;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Instant;
import java.util.List;

// Company-only management of partner login bindings (partner-management spec, "Partner form
// and API without Keycloak username field").
@ApplicationScoped
public class PartnerLoginService {

    static final String DUPLICATE = "Dieses Login ist bereits mit einem Partner verknüpft.";

    @Inject
    JsonWebToken jwt;

    @Inject
    PartnerService partnerService;

    public List<PartnerLoginDto> list(Long partnerId) {
        Partner partner = findPartner(partnerId);
        return PartnerLogin.<PartnerLogin>find("partner = ?1 order by id", partner).stream()
                .map(PartnerLoginDto::from)
                .toList();
    }

    @Transactional
    public PartnerLoginDto add(Long partnerId, PartnerLoginRequest request) {
        Partner partner = findPartner(partnerId);
        String subject = request.oidcSubject().trim();
        // Serialises concurrent bindings of the same subject (partner and company side share the
        // key) until commit, so the checks below are race-free and no unique constraint fires.
        LoginSubjectLock.lock(subject);
        if (CompanyLogin.count("oidcSubject", subject) > 0) {
            throw new ApiException(409, "Dieses Login ist bereits mit einem Firmenkonto verknüpft.");
        }
        PartnerLogin existing = PartnerLogin.find("oidcSubject", subject).firstResult();
        if (existing != null) {
            // Re-adding a revoked subject of the same partner reactivates it; anything else collides.
            if (existing.revokedAt != null && existing.partner.id.equals(partner.id)) {
                existing.revokedAt = null;
                existing.revokedBy = null;
                return PartnerLoginDto.from(existing);
            }
            throw new ApiException(409, DUPLICATE);
        }
        PartnerLogin login = new PartnerLogin();
        login.oidcSubject = subject;
        login.partner = partner;
        login.createdBy = jwt.getSubject();
        login.persist();
        return PartnerLoginDto.from(login);
    }

    @Transactional
    public void revoke(Long partnerId, Long loginId) {
        findPartner(partnerId);
        PartnerLogin login = PartnerLogin.findById(loginId);
        if (login == null || !login.partner.id.equals(partnerId)) {
            throw new ApiException(404, "Login nicht gefunden.");
        }
        if (login.revokedAt == null) {
            login.revokedAt = Instant.now();
            login.revokedBy = jwt.getSubject();
        }
    }

    private Partner findPartner(Long partnerId) {
        return partnerService.findOrThrow(partnerId);
    }
}
