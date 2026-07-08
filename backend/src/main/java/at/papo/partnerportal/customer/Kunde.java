package at.papo.partnerportal.customer;

import at.papo.partnerportal.common.Address;
import at.papo.partnerportal.partner.Partner;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class Kunde extends PanacheEntity {

    @Column(nullable = false)
    public String lastName;

    @Embedded
    public Address address;

    public String firstName;

    public String email;

    public String phone;

    // Creating/owning partner — Kunde records are scoped per partner, see design.md.
    @ManyToOne(optional = false)
    public Partner ownerPartner;
}
