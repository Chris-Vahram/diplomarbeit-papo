package at.papo.partnerportal.shop;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

// One theme per shop (shop-theme spec), draft and published as JSON
// {"colors": {...}, "headingFont": "...", "bodyFont": "..."} validated by ShopThemeValidator.
@Entity
@Table(name = "shop_theme")
public class ShopTheme extends PanacheEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = "shop_id", nullable = false, unique = true)
    public PartnerShop shop;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    public String draftTheme;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    public String publishedTheme;

    @JdbcTypeCode(SqlTypes.JSON)
    public String previousPublishedTheme;

    @Column(nullable = false)
    public Instant updatedAt;

    // Optimistic lock, see ShopPage.version.
    @Version
    @Column(nullable = false, columnDefinition = "bigint default 0 not null")
    public long version;
}
