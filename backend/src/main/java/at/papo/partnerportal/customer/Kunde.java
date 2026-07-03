package at.papo.partnerportal.customer;

import at.papo.partnerportal.partner.Partner;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class Kunde extends PanacheEntity {

    @Column(nullable = false)
    public String lastName;

    @Column(nullable = false)
    public String address;

    public String firstName;

    public String email;

    public String phone;

    // Creating/owning partner — Kunde records are scoped per partner, see design.md.
    @ManyToOne(optional = false)
    public Partner ownerPartner;
}
