package at.papo.partnerportal.category;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "article_categories")
public class Category extends PanacheEntity {

    @Column(nullable = false, length = 255)
    public String name;

    @Column(name = "normalized_name", nullable = false, length = 510, unique = true)
    public String normalizedName;

    @Column(nullable = false)
    public boolean active = true;
}
