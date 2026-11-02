package at.papo.partnerportal.product;

import at.papo.partnerportal.business.CompanyProfile;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.Instant;

@Entity
public class ProductImage extends PanacheEntity {

    // Owning company (add-company-tenancy). Nullable in DDL so Hibernate update can add it to
    // existing rows; CompanyTenancyMigrationService backfills it and services always set it.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    public CompanyProfile company;


    // Deliberately NOT @Lob: on PostgreSQL that maps to the legacy Large Object (oid) API,
    // which requires streaming the value inside the same transaction and throws
    // "Unable to access lob stream" once the session/response boundary is crossed. A plain
    // byte[] column maps to bytea directly (Hibernate 6+ default), which is what we want -
    // a normal column read/written like any other, no special transaction handling.
    @Column(nullable = false)
    public byte[] data;

    @Column(nullable = false)
    public String contentType;

    @Column(nullable = false)
    public String filename;

    @Column(nullable = false)
    public long sizeBytes;

    @Column(nullable = false)
    public Instant uploadedAt;
}
