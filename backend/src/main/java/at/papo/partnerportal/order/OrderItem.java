package at.papo.partnerportal.order;

import at.papo.partnerportal.product.Product;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

@Entity
public class OrderItem extends PanacheEntity {

    @ManyToOne(optional = false)
    public Order order;

    @ManyToOne(optional = false)
    public Product product;

    @Column(nullable = false)
    public int quantity;

    // Product price at the moment of ordering — protects historical order value
    // and commission from later central price changes, see design.md.
    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal unitPriceSnapshot;
}
