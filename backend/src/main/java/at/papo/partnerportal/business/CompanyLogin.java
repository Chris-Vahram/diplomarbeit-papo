package at.papo.partnerportal.business;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

// Binds a verified OIDC subject to one company (add-company-tenancy), same shape as PartnerLogin.
// Created only by the operator provisioning script; revocation is soft.
@Entity
@Table(name = "company_login")
public class CompanyLogin extends PanacheEntity {

    @Column(nullable = false, unique = true)
    public String oidcSubject;

    @ManyToOne(optional = false)
    public CompanyProfile company;

    @Column(nullable = false)
    public Instant createdAt = Instant.now();

    @Column(nullable = false)
    public String createdBy;

    public Instant revokedAt;

    public String revokedBy;
}
