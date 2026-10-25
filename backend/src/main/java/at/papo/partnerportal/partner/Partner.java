package at.papo.partnerportal.partner;

import at.papo.partnerportal.business.BusinessTaxIdentifier;
import at.papo.partnerportal.business.CompanyProfile;
import at.papo.partnerportal.common.Address;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "company_id", "partnerNumber" }))
public class Partner extends PanacheEntity {

    // Owning company (add-company-tenancy). Nullable in DDL so Hibernate update can add it to
    // existing rows; CompanyTenancyMigrationService backfills it and services always set it.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    public CompanyProfile company;


    @Column(nullable = false)
    public String partnerNumber;

    @Column(nullable = false)
    public String name;

    @Embedded
    public Address address;

    @Embedded
    public BusinessTaxIdentifier taxIdentifier;

    public String email;

    public String phone;

    @Column(length = 4000)
    public String internalNote;

    @Column(nullable = false, precision = 5, scale = 2)
    public BigDecimal commissionPercent;

    @Column(nullable = false)
    public boolean active = true;
}
