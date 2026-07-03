package at.papo.partnerportal.enablement;

import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.Product;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// Existence of a row = the product is enabled for the partner; disabling removes the row.
@Entity
@Table(name = "product_enablement", uniqueConstraints = @UniqueConstraint(columnNames = {"partner_id", "product_id"}))
public class ProductEnablement extends PanacheEntity {

    @ManyToOne(optional = false)
    public Partner partner;

    @ManyToOne(optional = false)
    public Product product;
}
