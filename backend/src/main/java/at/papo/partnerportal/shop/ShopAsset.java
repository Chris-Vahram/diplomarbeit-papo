package at.papo.partnerportal.shop;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

// Image uploaded by a partner for its own shop (shop-assets spec). Belongs to exactly one shop;
// block image fields reference it by id. Stored as bytea like ProductImage (no @Lob, see there).
@Entity
@Table(name = "shop_asset")
public class ShopAsset extends PanacheEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    public PartnerShop shop;

    @Column(nullable = false)
    public byte[] data;

    // Verified from the file bytes at upload, never taken from the request.
    @Column(nullable = false)
    public String contentType;

    @Column(nullable = false)
    public String filename;

    @Column(nullable = false)
    public long sizeBytes;

    @Column(nullable = false)
    public Instant uploadedAt;

    // "Ich habe die Rechte an diesem Bild." - an upload without it is rejected.
    @Column(nullable = false)
    public boolean rightsConfirmed;
}
