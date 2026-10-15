package at.papo.partnerportal.partner;

import java.time.Instant;

public record PartnerLoginDto(Long id, String oidcSubject, Instant createdAt, Instant revokedAt) {
    static PartnerLoginDto from(PartnerLogin login) {
        return new PartnerLoginDto(login.id, login.oidcSubject, login.createdAt, login.revokedAt);
    }
}
