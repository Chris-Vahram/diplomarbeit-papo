package at.papo.partnerportal.partner;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

// Binds a verified OIDC subject (Keycloak user id) to a Partner (bind-partner-login-to-oidc-subject).
// Several logins per Partner are allowed so personal accounts can add rows later. Revocation is
// soft: a Partner that ever had a binding never qualifies for the username fallback again.
@Entity
@Table(name = "partner_login")
public class PartnerLogin extends PanacheEntity {

    @Column(nullable = false, unique = true)
    public String oidcSubject;

    @ManyToOne(optional = false)
    public Partner partner;

    @Column(nullable = false)
    public Instant createdAt = Instant.now();

    @Column(nullable = false)
    public String createdBy;

    public Instant revokedAt;

    public String revokedBy;
}
