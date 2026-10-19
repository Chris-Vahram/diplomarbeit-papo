package at.papo.partnerportal.product;

import at.papo.partnerportal.category.Category;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

@Entity
public class Product extends PanacheEntity {

    @Column(nullable = false)
    public String name;

    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal price;

    @Column(length = 2000)
    public String description;

    @Column(nullable = false)
    public boolean available = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_category_id")
    public Category articleCategory;

    public String imageUrl;

    // Set when imageUrl points at an uploaded/gallery-picked image (see product-images
    // capability); null when imageUrl is an external URL or unset.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    public ProductImage image;
}
