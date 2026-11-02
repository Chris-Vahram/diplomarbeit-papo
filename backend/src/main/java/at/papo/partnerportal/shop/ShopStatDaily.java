package at.papo.partnerportal.shop;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

// Daily aggregate of anonymous shop events (shop-statistics spec, design decision 1). There is
// no raw event table: each event only increments one of these counters. dimension holds the
// page segment, article id, referrer domain or device class ("" when the metric has none).
// Identity id, so ShopStatCounter's native upsert can insert without knowing a sequence name.
@Entity
@Table(name = "shop_stat_daily", uniqueConstraints = @UniqueConstraint(name = ShopStatDaily.UNIQUE_KEY,
        columnNames = {"shop_id", "stat_day", "metric", "dimension"}))
public class ShopStatDaily extends PanacheEntityBase {

    static final String UNIQUE_KEY = "uk_shop_stat_daily";
    static final int DIMENSION_MAX = 253;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    public PartnerShop shop;

    @Column(name = "stat_day", nullable = false)
    public LocalDate day;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    public ShopStatMetric metric;

    @Column(nullable = false, length = DIMENSION_MAX)
    public String dimension = "";

    @Column(nullable = false)
    public long amount;
}
