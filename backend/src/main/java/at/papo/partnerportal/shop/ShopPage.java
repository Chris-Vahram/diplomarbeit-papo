package at.papo.partnerportal.shop;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

// One page of a partner shop (shop-pages spec). Blocks are not rows: each version is one
// JSON snapshot {"blocks": [...]} validated by ShopBlockValidator (design.md Decision 2).
// System pages only reserve their slot and segment; they carry no content.
@Entity
@Table(name = "shop_page", uniqueConstraints = {
        @UniqueConstraint(name = "uk_shop_page_segment", columnNames = { "shop_id", "segment" }),
        @UniqueConstraint(name = "uk_shop_page_slot", columnNames = { "shop_id", "slot" })
})
public class ShopPage extends PanacheEntity {

    public static final int SEGMENT_MAX_LENGTH = 60;
    public static final int MENU_NAME_MAX_LENGTH = 40;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    public PartnerShop shop;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public ShopPageKind kind;

    // null for free pages; unique per shop otherwise (PostgreSQL allows several nulls).
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    public ShopPageSlot slot;

    @Column(nullable = false, length = SEGMENT_MAX_LENGTH)
    public String segment;

    @Column(nullable = false, length = MENU_NAME_MAX_LENGTH)
    public String menuName;

    // Navigation order among designable pages.
    @Column(nullable = false)
    public int position;

    // Online = publishedContent is visible to the public.
    @Column(nullable = false)
    public boolean online;

    @JdbcTypeCode(SqlTypes.JSON)
    public String draftContent;

    @JdbcTypeCode(SqlTypes.JSON)
    public String publishedContent;

    // One-step restore of the last published version.
    @JdbcTypeCode(SqlTypes.JSON)
    public String previousPublishedContent;

    public Instant publishedAt;

    @Column(nullable = false)
    public Instant updatedAt;
}
