package at.papo.partnerportal.product;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

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

    public String category;

    public String imageUrl;
}
