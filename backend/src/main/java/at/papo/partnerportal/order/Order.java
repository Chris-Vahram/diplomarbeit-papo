package at.papo.partnerportal.order;

import at.papo.partnerportal.customer.Kunde;
import at.papo.partnerportal.partner.Partner;
import at.papo.partnerportal.product.ArticleTax;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// "ORDER" is a reserved SQL word in PostgreSQL — mapped to table "orders", see CONVENTIONS.md.
@Entity
@Table(name = "orders")
public class Order extends PanacheEntity {

    @Column(nullable = false)
    public LocalDate orderDate = LocalDate.now();

    @ManyToOne(optional = false)
    public Partner partner;

    @ManyToOne(optional = false)
    public Kunde kunde;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public OrderStatus status = OrderStatus.SUBMITTED;

    // Derived from order items at creation time, not recomputed later.
    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal orderValue;

    // Net value and USt amount derived at creation from the item snapshots (orderValue = gross,
    // netValue + taxValue = orderValue). Nullable in DDL for Hibernate update on existing rows;
    // ArticleTaxMigrationService backfills them.
    @Column(precision = 10, scale = 2)
    public BigDecimal netValue;

    @Column(precision = 10, scale = 2)
    public BigDecimal taxValue;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    public List<OrderItem> items = new ArrayList<>();

    // Company-side unseen/seen tracking (order-notifications spec). Only one
    // company role exists, so a single flag is enough — see design.md.
    @Column(nullable = false)
    public boolean seenByCompany = false;

    // INTERNAL or SHOP (order-management "Order origin"). Nullable in DDL for Hibernate update on
    // existing rows; OriginMigrationService backfills INTERNAL.
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    public OrderOrigin origin = OrderOrigin.INTERNAL;

    // Sets gross/net/tax from the item snapshots (unitPriceSnapshot + taxRatePercentSnapshot).
    public void applyTotalsFromItems() {
        ArticleTax.Totals totals = ArticleTax.totals(items.stream()
                .map(item -> new ArticleTax.Line(item.unitPriceSnapshot, item.quantity, item.taxRatePercentSnapshot))
                .toList());
        orderValue = totals.gross();
        netValue = totals.net();
        taxValue = totals.tax();
    }
}
