package at.papo.partnerportal.shop;

import at.papo.partnerportal.order.Order;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

// Idempotency of shop checkouts (add-shop-guest-checkout design D2): one order per shop and
// client key within 24 hours. The unique constraint also settles two concurrent submits.
@Entity
@Table(name = "shop_order_request", uniqueConstraints = @UniqueConstraint(columnNames = {"shop_id", "requestKey"}))
public class ShopOrderKey extends PanacheEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    public PartnerShop shop;

    @Column(nullable = false, length = 36)
    public String requestKey;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    public Order order;

    @Column(nullable = false)
    public Instant createdAt;
}
