package at.papo.partnerportal.customer;

import at.papo.partnerportal.common.Address;
import at.papo.partnerportal.partner.Partner;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(indexes = @Index(name = "idx_kunde_account_subject", columnList = "accountSubject"))
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

    // PORTAL or SHOP (customer-management "Customer origin"). Nullable in DDL for Hibernate update on
    // existing rows; OriginMigrationService backfills PORTAL.
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    public KundeOrigin origin = KundeOrigin.PORTAL;

    // OIDC subject of the papo-shop customer account that placed the shop order
    // (add-shop-customer-accounts); null for guests and portal customers. Cleared when the
    // account is deleted, the Kunde itself stays as the partner's business record.
    @Column(length = 64)
    public String accountSubject;
}
