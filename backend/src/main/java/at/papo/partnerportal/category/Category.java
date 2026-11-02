package at.papo.partnerportal.category;

import at.papo.partnerportal.business.CompanyProfile;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "article_categories",
        uniqueConstraints = @UniqueConstraint(columnNames = { "company_id", "normalized_name" }))
public class Category extends PanacheEntity {

    // Owning company (add-company-tenancy). Nullable in DDL so Hibernate update can add it to
    // existing rows; CompanyTenancyMigrationService backfills it and services always set it.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    public CompanyProfile company;


    @Column(nullable = false, length = 255)
    public String name;

    @Column(name = "normalized_name", nullable = false, length = 510)
    public String normalizedName;

    @Column(nullable = false)
    public boolean active = true;
}
