package at.papo.partnerportal.partner;

import at.papo.partnerportal.common.Address;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
public class Partner extends PanacheEntity {

    @Column(nullable = false, unique = true)
    public String partnerNumber;

    @Column(nullable = false)
    public String name;

    @Embedded
    public Address address;

    public String email;

    public String phone;

    @Column(nullable = false, precision = 5, scale = 2)
    public BigDecimal commissionPercent;

    @Column(nullable = false)
    public boolean active = true;

    // Links a Keycloak login (username claim) to this Partner record.
    // Provisioned manually, never by import — see design.md.
    @Column(unique = true)
    public String keycloakUsername;
}
