package at.papo.partnerportal.shop;

import at.papo.partnerportal.partner.Partner;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;

// One public shop per partner (partner-shop spec). The effective status is computed
// by PartnerShopService, never stored, so partner deactivation or article changes
// take effect without touching this row.
@Entity
@Table(name = "partner_shop")
public class PartnerShop extends PanacheEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = "partner_id", nullable = false, unique = true)
    public Partner partner;

    @Column(nullable = false, unique = true, length = ShopKennung.MAX_LENGTH)
    public String kennung;

    // null = never released by the company.
    public Instant releasedAt;

    @Column(nullable = false)
    public boolean offlineByCompany = false;

    @Column(length = 500)
    public String offlineReason;

    @Column(nullable = false)
    public Instant createdAt;
}
