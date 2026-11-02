package at.papo.partnerportal.shop;

import at.papo.partnerportal.product.Product;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

// Visitor inquiry from the "Anfrageformular" block (shop-inquiries spec). Holds only the
// submitted fields, the shop and timestamps - no IP address, no automatic retention period.
@Entity
@Table(name = "shop_inquiry")
public class ShopInquiry extends PanacheEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    public PartnerShop shop;

    @Column(nullable = false, length = ShopInquiryService.NAME_MAX)
    public String name;

    @Column(length = ShopInquiryService.EMAIL_MAX)
    public String email;

    @Column(length = ShopInquiryService.PHONE_MAX)
    public String phone;

    @Column(nullable = false, length = ShopInquiryService.MESSAGE_MAX)
    public String message;

    // Optional article reference; articles are never deleted, only made unavailable.
    @ManyToOne
    @JoinColumn(name = "product_id")
    public Product article;

    @Column(nullable = false)
    public Instant createdAt;

    public Instant readAt;

    public Instant doneAt;
}
